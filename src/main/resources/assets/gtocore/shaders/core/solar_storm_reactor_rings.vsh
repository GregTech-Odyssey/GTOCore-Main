#version 150

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec3 Axis;
uniform float TopHeight;
uniform float ConeHeight;
uniform float ConeRadius;
uniform float ParticleSize;
uniform float ParticleSizeVariation;
uniform float RingRadialThickness;
uniform float RingVerticalThickness;
uniform float RiseDuration;
uniform float ParticleStride;
uniform float Time;
uniform float Strength;

in vec3 Position;
in vec2 UV0;
out vec2 spriteCoord;
out vec4 particleColor;

const float TAU = 6.28318530718;

float random(float seed) {
    return fract(sin(seed * 127.1 + 311.7) * 43758.5453);
}

void main() {
    float seed = Position.z;
    // Each cohort remains a ring during its rise. Radius grows linearly from the cone apex.
    float progress = fract(Time / RiseDuration + UV0.x);
    float fade = smoothstep(0.0, 0.06, progress) * (1.0 - smoothstep(0.78, 1.0, progress));
    float theta = UV0.y * TAU + Time * (0.045 + UV0.x * 0.025);
    float drift = sin(Time * (0.55 + random(seed) * 0.5) + seed) * RingRadialThickness * progress;
    float radius = progress * ConeRadius + drift;
    float lift = TopHeight + progress * ConeHeight + sin(Time * 0.8 + seed * 2.3) * RingVerticalThickness * progress;
    vec3 tangent = abs(Axis.y) > 0.5 ? vec3(1.0, 0.0, 0.0) : vec3(0.0, 1.0, 0.0);
    vec3 point = tangent * (cos(theta) * radius) + Axis * lift + cross(tangent, Axis) * (sin(theta) * radius);
    vec4 view = ModelViewMat * vec4(point, 1.0);
    float size = (ParticleSize + random(seed + 4.0) * ParticleSizeVariation) * (0.6 + 0.4 * progress);
    view.xy += Position.xy * size;
    gl_Position = ProjMat * view;
    spriteCoord = Position.xy;
    // Muted ochre/gold, without the white-hot tones used by the beam.
    vec3 color = mix(vec3(0.33, 0.23, 0.018), vec3(0.70, 0.51, 0.055), random(seed + 1.0));
    float visible = mod(seed, ParticleStride) < 0.5 ? 1.0 : 0.0;
    particleColor = vec4(color, fade * Strength * visible);
}
