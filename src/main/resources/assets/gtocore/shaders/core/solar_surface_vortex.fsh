#version 150

uniform sampler2D DiffuseSampler;
uniform vec2 ScreenSize;
uniform float Time;
uniform float Strength;
uniform vec4 ColorModulator;

in vec2 localUv;
out vec4 fragColor;

float hash21(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

float valueNoise(vec2 p) {
    vec2 cell = floor(p);
    vec2 blend = fract(p);
    blend = blend * blend * (3.0 - 2.0 * blend);

    return mix(
        mix(hash21(cell), hash21(cell + vec2(1.0, 0.0)), blend.x),
        mix(hash21(cell + vec2(0.0, 1.0)), hash21(cell + vec2(1.0)), blend.x),
        blend.y
    );
}

void main() {
    float radius = length(localUv);
    if (radius >= 1.0 || Strength <= 0.001) discard;

    float edge = 1.0 - smoothstep(0.65, 1.0, radius);
    float core = smoothstep(0.02, 0.18, radius);
    float angle = atan(localUv.y, localUv.x + 0.00001);
    // Two rotating spiral arms bend the scene, with a softer heat shimmer between them.
    float perturbation = valueNoise(localUv * 11.0 + vec2(Time * 0.17, -Time * 0.13)) - 0.5;
    float phase = angle * 2.0 - radius * 19.0 + Time * 3.5 + perturbation * 0.24;
    float arm = pow(0.5 + 0.5 * sin(phase), 5.0);
    vec2 radial = localUv / max(radius, 0.001);
    vec2 tangent = vec2(-radial.y, radial.x);
    float envelope = edge * core * Strength;
    vec2 offset = (tangent * (3.0 + 12.0 * arm) + radial * (sin(phase) * 3.0 + perturbation * 0.7)) * envelope;
    vec2 uv = gl_FragCoord.xy / ScreenSize;
    vec2 halfTexel = 0.5 / ScreenSize;
    vec3 scene = texture(DiffuseSampler, clamp(uv + offset / ScreenSize, halfTexel, 1.0 - halfTexel)).rgb;
    vec3 glow = vec3(1.0, 0.28, 0.035) * arm * envelope * 0.12;
    fragColor = vec4(scene + glow, edge * core * Strength * 0.85) * ColorModulator;
}
