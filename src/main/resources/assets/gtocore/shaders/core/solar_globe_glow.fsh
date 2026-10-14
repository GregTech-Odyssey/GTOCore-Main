#version 150

#moj_import <fog.glsl>

uniform vec4 ColorModulator;
uniform float GameTime;
uniform float FogStart;
uniform float FogEnd;

in float vertexDistance;
in vec2 glowCoord;

out vec4 fragColor;

void main() {
    float radius = length(glowCoord);
    float falloff = exp(-5.0 * radius * radius) * (1.0 - smoothstep(0.72, 1.0, radius));
    float pulse = 0.94 + 0.06 * sin(GameTime * 1200.0 * 1.7);
    float strength = 0.48 * falloff * pulse * linear_fog_fade(vertexDistance, FogStart, FogEnd);
    strength *= ColorModulator.a;
    if (strength <= 0.001) {
        discard;
    }
    vec3 color = mix(vec3(1.0, 0.72, 0.22), vec3(1.0, 0.30, 0.025), smoothstep(0.15, 0.85, radius));
    // ONE + ONE blending adds light without changing the scene's alpha channel.
    fragColor = vec4(color * ColorModulator.rgb * strength, 0.0);
}
