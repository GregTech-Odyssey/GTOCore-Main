#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float GameTime;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in float vertexAlpha;
in vec2 texCoord0;

out vec4 fragColor;

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

float noise(vec2 p) {
    vec2 cell = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash(cell), hash(cell + vec2(1.0, 0.0)), f.x),
               mix(hash(cell + vec2(0.0, 1.0)), hash(cell + vec2(1.0, 1.0)), f.x), f.y);
}

void main() {
    vec4 surface = texture(Sampler0, texCoord0);
    float alpha = surface.a * vertexAlpha * ColorModulator.a;
    if (alpha < 0.1) {
        discard;
    }

    // Use atlas texels so the granulation keeps its scale across texture packs.
    vec2 p = texCoord0 * vec2(textureSize(Sampler0, 0)) * 0.24;
    float time = GameTime * 1200.0;
    float flow = noise(p + vec2(time * 0.22, -time * 0.13));
    float granulation = noise(p * 2.1 + vec2(flow * 1.8, time * 0.12));
    float flare = smoothstep(0.55, 0.88, flow * 0.65 + granulation * 0.35);
    float luminance = dot(surface.rgb, vec3(0.2126, 0.7152, 0.0722));
    vec3 hot = mix(vec3(1.0, 0.24, 0.025), vec3(1.0, 0.87, 0.38), luminance);
    vec3 color = mix(surface.rgb, hot, 0.28) * (1.08 + granulation * 0.18);
    color += vec3(1.0, 0.56, 0.12) * flare * 0.32;
    fragColor = linear_fog(vec4(color * ColorModulator.rgb, alpha), vertexDistance, FogStart, FogEnd, FogColor);
}
