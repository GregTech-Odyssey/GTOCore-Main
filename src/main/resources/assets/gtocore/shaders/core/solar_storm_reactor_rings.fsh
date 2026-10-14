#version 150

in vec2 spriteCoord;
in vec4 particleColor;
out vec4 fragColor;

void main() {
    float radius = length(spriteCoord);
    if (radius > 1.0 || particleColor.a < 0.001) discard;
    float grain = 1.0 - smoothstep(0.13, 0.35, max(abs(spriteCoord.x), abs(spriteCoord.y)));
    float glow = exp(-radius * radius * 5.5) * (1.0 - smoothstep(0.7, 1.0, radius));
    fragColor = vec4(particleColor.rgb * (grain * 0.8 + glow * 0.28), particleColor.a);
}
