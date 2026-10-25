#version 150

uniform vec4 ColorModulator;
uniform float Completion;
uniform float GridScale;
uniform float StarCoreRatio;
uniform vec2 Rotation;
uniform float Time;
uniform float Hemisphere;
uniform float Opacity;

in vec2 texCoord;
out vec4 fragColor;

const float PI = 3.141592653589793;
const float SQRT_THREE = 1.7320508075688772;

uint cellSeed(ivec2 cell) {
    uint seed = uint(cell.x) * 73428767u ^ uint(cell.y) * 912931u ^ 0x0d1505feu;
    seed = (seed ^ (seed >> 16u)) * 0x7feb352du;
    seed = (seed ^ (seed >> 15u)) * 0x846ca68bu;
    return seed ^ (seed >> 16u);
}

void main() {
    if (Completion <= 0.0) discard;
    vec2 p = texCoord * 2.0 - 1.0;
    if (max(abs(p.x), abs(p.y)) > 1.0) discard;
    float depth = sqrt(max(0.0, 1.0 - max(p.x * p.x, p.y * p.y)));
    float latitudeCos = sqrt(max(0.0, 1.0 - p.y * p.y));
    float horizontal = max(0.000001, sqrt(p.x * p.x + depth * depth));
    vec3 ray = vec3(p.x * latitudeCos / horizontal, p.y, Hemisphere * depth * latitudeCos / horizontal);
    vec3 axis = vec3(sin(Rotation.y), cos(Rotation.y), 0.0);
    float c = cos(Rotation.x), s = sin(Rotation.x);
    ray = ray * c + cross(axis, ray) * s + axis * dot(axis, ray) * (1.0 - c);

    float columns = max(2.0, round(12.0 * GridScale) * 2.0);
    vec2 uv = vec2(atan(ray.x, ray.z) * (SQRT_THREE * columns / (2.0 * PI)),
                   asin(clamp(ray.y, -1.0, 1.0)) * (18.0 * GridScale / PI));
    float r = uv.y / 1.5, q = uv.x / SQRT_THREE - r * 0.5;
    vec3 rounded = round(vec3(q, r, -q - r));
    vec3 error = abs(rounded - vec3(q, r, -q - r));
    if (error.x > error.y && error.x > error.z) rounded.x = -rounded.y - rounded.z;
    else if (error.y > error.z) rounded.y = -rounded.x - rounded.z;
    vec2 center = vec2(SQRT_THREE * (rounded.x + rounded.y * 0.5), 1.5 * rounded.y);
    ivec2 cell = ivec2(int(mod(rounded.x, columns)), int(rounded.y));
    uint seed = cellSeed(cell);
    float rank = (float(seed & 0x00ffffffu) + 0.5) / 16777216.0;
    float built = Completion >= 1.0 || rank < Completion ? 1.0 : 0.0;

    // 小于屏幕像素的格子按覆盖率混合，避免闪烁或退化为粗大色块。
    float footprint = max(length(dFdx(uv)), length(dFdy(uv)));
    float resolved = 1.0 - smoothstep(0.45, 1.2, footprint);
    vec2 local = abs(uv - center);
    float edgeDistance = SQRT_THREE * 0.5 - max(local.x, dot(local, vec2(0.5, SQRT_THREE * 0.5)));
    float aa = max(fwidth(edgeDistance), 0.001);
    // 恒星本体遮挡后半球，发光强度降低不代表恒星变透明。
    if (Hemisphere < 0.0 && max(abs(p.x), abs(p.y)) <= StarCoreRatio) discard;
    if (built == 0.0 && resolved >= 1.0) discard;
    float edge = (1.0 - smoothstep(0.045 - aa, 0.045 + aa, edgeDistance)) * resolved;
    float period = 4.0 + float((seed >> 16u) % 5000u) * 0.001;
    float pulse = (seed & 3u) == 0u ? 0.5 + 0.5 * sin(Time * (2.0 * PI / period) + float(seed & 65535u) * (2.0 * PI / 65536.0)) : 0.0;
    vec3 color = mix(vec3(0.149, 0.545, 0.847), vec3(0.412, 0.812, 1.0), edge);
    float opacity = mix(0.863 + pulse * 0.11, 0.878 + pulse * 0.04, edge);
    float coverage = mix(Completion, built, resolved);
    fragColor = vec4(color, opacity * coverage * Opacity) * ColorModulator;
}
