#version 150

uniform sampler2D DepthSampler;
uniform mat4 InverseViewProjection;
uniform vec3 CameraRelative;
uniform vec3 Axis;
uniform float BeamHeight;
uniform float WaveRadius;
uniform float Time;
uniform float Strength;

in vec2 texCoord;
out vec4 fragColor;

// Distance-field emission and outward shock fronts, inspired by orbital_railgun's strike shader.
// Integrate only the bounded beam volume; use analytic shell intersections for the thin shockwaves.
vec3 local(vec3 p) {
    vec3 tangent = abs(Axis.y) > 0.5 ? vec3(1.0, 0.0, 0.0) : vec3(0.0, 1.0, 0.0);
    return vec3(dot(p, tangent), dot(p, Axis), dot(p, cross(tangent, Axis)));
}

vec3 unproject(float depth) {
    vec4 p = InverseViewProjection * vec4(texCoord * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    return p.xyz / p.w;
}

vec2 boxInterval(vec3 origin, vec3 ray, vec3 lo, vec3 hi) {
    vec3 direction = mix(vec3(-1.0), vec3(1.0), step(vec3(0.0), ray)) * max(abs(ray), vec3(0.00001));
    vec3 a = (lo - origin) / direction;
    vec3 b = (hi - origin) / direction;
    vec3 nearHit = min(a, b), farHit = max(a, b);
    return vec2(max(max(nearHit.x, nearHit.y), nearHit.z), min(min(farHit.x, farHit.y), farHit.z));
}

float cylinderLength(vec3 origin, vec3 ray, float radius, float halfHeight, float sceneDistance) {
    vec2 interval = boxInterval(origin, ray, vec3(-radius, -halfHeight, -radius), vec3(radius, halfHeight, radius));
    float a = dot(ray.xz, ray.xz);
    float b = dot(origin.xz, ray.xz);
    float c = dot(origin.xz, origin.xz) - radius * radius;
    if (a > 0.000001) {
        float discriminant = b * b - a * c;
        if (discriminant < 0.0) return 0.0;
        float root = sqrt(discriminant);
        interval.x = max(interval.x, (-b - root) / a);
        interval.y = min(interval.y, (-b + root) / a);
    } else if (c > 0.0) {
        return 0.0;
    }
    return max(0.0, min(interval.y, sceneDistance) - max(interval.x, 0.0));
}

float shell(vec3 origin, vec3 ray, float radius, float width, float height, float sceneDistance) {
    float outer = cylinderLength(origin, ray, radius + width, height, sceneDistance);
    float inner = cylinderLength(origin, ray, max(radius - width, 0.0), height, sceneDistance);
    return max(outer - inner, 0.0);
}

void main() {
    vec3 nearPoint = unproject(0.0);
    vec3 scenePoint = unproject(texture(DepthSampler, texCoord).r);
    vec3 ray = local(normalize(scenePoint - nearPoint));
    vec3 origin = local(CameraRelative + nearPoint);
    float sceneDistance = length(scenePoint - nearPoint);
    vec2 interval = boxInterval(origin, ray, vec3(-12.0, 0.0, -12.0), vec3(12.0, BeamHeight, 12.0));
    float start = max(interval.x, 0.0);
    float end = min(interval.y, sceneDistance);
    vec2 waveInterval = boxInterval(origin, ray, vec3(-WaveRadius - 1.4, -1.8, -WaveRadius - 1.4),
                                   vec3(WaveRadius + 1.4, 1.8, WaveRadius + 1.4));
    bool wavesVisible = min(waveInterval.y, sceneDistance) > max(waveInterval.x, 0.0);
    if (end <= start && !wavesVisible) discard;
    vec3 energy = vec3(0.0);
    if (end > start) {
        float closest = clamp(-dot(origin.xz, ray.xz) / max(dot(ray.xz, ray.xz), 0.00001), start, end);
        vec3 flowPoint = origin + ray * closest;
        float flow = 0.82 + 0.12 * sin(flowPoint.y * 0.75 - Time * 10.0)
                     + 0.06 * sin(flowPoint.y * 2.1 - Time * 19.0 + flowPoint.x * 1.7 + flowPoint.z);
        float stride = (end - start) / 24.0;
        for (int i = 0; i < 24; i++) {
            vec3 p = origin + ray * (start + (float(i) + 0.5) * stride);
            float radiusSquared = dot(p.xz, p.xz);
            // A ~6.5 block core, broad orange corona, and much softer outer bloom.
            float coreDistance = radiusSquared / (3.25 * 3.25);
            float core = exp(-coreDistance * coreDistance * coreDistance);
            float corona = exp(-radiusSquared / 21.0);
            float bloom = exp(-radiusSquared / 65.0);
            float ends = smoothstep(0.0, 1.5, p.y) * (1.0 - smoothstep(BeamHeight - 28.0, BeamHeight, p.y));
            energy += (vec3(1.0, 0.77, 0.30) * core * 1.2
                       + vec3(1.0, 0.27, 0.015) * (corona * 0.18 + bloom * 0.035)) * flow * ends * stride;
        }
    }
    vec3 color = vec3(1.0) - exp(-energy * 0.48);
    for (int wave = 0; wave < 4 && wavesVisible; wave++) {
        float phase = fract(Time * 0.19 + float(wave) * 0.25);
        float radius = phase * WaveRadius;
        float fade = smoothstep(0.0, 0.07, phase) * (1.0 - smoothstep(0.55, 1.0, phase));
        float crest = shell(origin, ray, radius, 0.24, 0.65, sceneDistance);
        float glow = shell(origin, ray, radius, 1.4, 1.8, sceneDistance);
        float light = (1.0 - exp(-crest * 1.1)) + (1.0 - exp(-glow * 0.22)) * 0.38;
        color += vec3(1.0, 0.31, 0.018) * light * fade * 0.85;
    }
    fragColor = vec4(color * Strength, 1.0);
}
