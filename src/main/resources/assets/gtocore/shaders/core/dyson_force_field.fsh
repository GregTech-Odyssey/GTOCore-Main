#version 150

uniform vec4 ColorModulator;
uniform mat4 ViewProjection;
uniform mat4 InverseViewProjection;
uniform float CameraHeight;
uniform vec2 GridOrigin;
uniform float ViewDistance;
uniform float Completion;

in vec2 texCoord;
out vec4 fragColor;

const float SQRT_THREE = 1.7320508075688772;
const float APOTHEM = SQRT_THREE * 0.5;
// Each prism has side length 4.0; extra lattice spacing leaves narrow assembly joints.
const float SIDE_LENGTH = 4.0;
const float INRADIUS = APOTHEM * SIDE_LENGTH;
const float CELL_PITCH = SIDE_LENGTH * 1.0125;
const float THICKNESS = 1.0;
const float BEVEL = 0.035 * SIDE_LENGTH;
const vec3 NORMALS[6] = vec3[6](
    vec3(0.0, 0.0, 1.0), vec3(APOTHEM, 0.0, 0.5), vec3(APOTHEM, 0.0, -0.5),
    vec3(0.0, 0.0, -1.0), vec3(-APOTHEM, 0.0, -0.5), vec3(-APOTHEM, 0.0, 0.5)
);

float cellRank(vec2 id) {
    uvec2 key = uvec2(ivec2(id) & ivec2(255));
    uint seed = key.x * 73428767u ^ key.y * 912931u;
    seed = (seed ^ (seed >> 16u)) * 0x7feb352du;
    seed = (seed ^ (seed >> 15u)) * 0x846ca68bu;
    seed ^= seed >> 16u;
    return (float(seed & 0x00ffffffu) + 0.5) / 16777216.0;
}

void cellAt(vec2 p, out vec2 center, out vec2 id) {
    vec2 spacing = vec2(3.0, SQRT_THREE) * CELL_PITCH;
    vec2 a = floor(p / spacing + 0.5);
    vec2 b = floor((p - spacing * 0.5) / spacing + 0.5);
    vec2 da = p - a * spacing;
    vec2 db = p - (b + 0.5) * spacing;
    bool first = dot(da, da) < dot(db, db);
    center = first ? a * spacing : (b + 0.5) * spacing;
    id = first ? a * 2.0 : b * 2.0 + 1.0;
}

void clipFace(vec3 n, float bound, vec3 origin, vec3 ray,
              inout float entry, inout float exit, inout vec3 entryNormal, inout vec3 exitNormal) {
    float distance = bound - dot(n, origin);
    float direction = dot(n, ray);
    if (abs(direction) < 0.000001) {
        if (distance < 0.0) exit = -1.0;
        return;
    }
    float t = distance / direction;
    if (direction < 0.0) {
        if (t > entry) { entry = t; entryNormal = n; }
    } else if (t < exit) {
        exit = t; exitNormal = n;
    }
}

bool intersectPrism(vec3 origin, vec3 ray, float start, float end, out float hit, out vec3 normal) {
    float entry = -1.0e20, exit = 1.0e20;
    vec3 entryNormal = vec3(0.0), exitNormal = vec3(0.0);
    clipFace(vec3(0.0, 1.0, 0.0), 0.0, origin, ray, entry, exit, entryNormal, exitNormal);
    clipFace(vec3(0.0, -1.0, 0.0), THICKNESS, origin, ray, entry, exit, entryNormal, exitNormal);
    for (int i = 0; i < 6; i++) {
        vec3 n = NORMALS[i];
        clipFace(n, INRADIUS, origin, ray, entry, exit, entryNormal, exitNormal);
        clipFace(n + vec3(0.0, 1.0, 0.0), INRADIUS - BEVEL, origin, ray, entry, exit, entryNormal, exitNormal);
        clipFace(n - vec3(0.0, 1.0, 0.0), INRADIUS + THICKNESS - BEVEL, origin, ray, entry, exit, entryNormal, exitNormal);
    }
    bool inside = entry < 0.0;
    hit = inside ? exit : entry;
    normal = normalize(inside ? exitNormal : entryNormal);
    return entry <= exit && hit >= start - 0.0001 && hit <= end;
}

void main() {
    vec4 farPoint = InverseViewProjection * vec4(texCoord * 2.0 - 1.0, 1.0, 1.0);
    vec3 ray = normalize(farPoint.xyz / farPoint.w);
    float start, end;
    if (abs(ray.y) < 0.000001) {
        if (CameraHeight > 0.0 || CameraHeight < -THICKNESS) discard;
        start = 0.0;
        end = ViewDistance;
    } else {
        float top = -CameraHeight / ray.y;
        float bottom = (-THICKNESS - CameraHeight) / ray.y;
        start = max(0.0, min(top, bottom));
        end = max(top, bottom);
    }
    end = min(end, ViewDistance / max(length(ray.xz), 0.000001));
    if (Completion <= 0.0 || start >= end) discard;

    vec2 surface = ray.xz * start;
    float footprint = max(length(dFdx(surface)), length(dFdy(surface))) / SIDE_LENGTH;
    float resolved = 1.0 - smoothstep(0.12, 0.5, footprint);
    float hit = start;
    vec3 normal = vec3(0.0, ray.y < 0.0 ? 1.0 : -1.0, 0.0);
    vec2 hitCenter = vec2(0.0);
    bool found = false;
    float cursor = start;
    // Traverse cell boundaries, never tiny ray-march steps or a world-sized CPU mesh.
    for (int step = 0; step < 32; step++) {
        if (resolved <= 0.0 || cursor > end) break;
        vec2 center, id;
        cellAt(GridOrigin + ray.xz * (cursor + 0.0001), center, id);
        if (Completion >= 1.0 || cellRank(id) < Completion) {
            vec3 origin = vec3(GridOrigin.x - center.x, CameraHeight, GridOrigin.y - center.y);
            if (intersectPrism(origin, ray, start, end, hit, normal)) {
                found = true;
                hitCenter = center;
                break;
            }
        }
        vec2 local = GridOrigin + ray.xz * cursor - center;
        float next = end + 0.001;
        for (int i = 0; i < 6; i++) {
            float direction = dot(NORMALS[i].xz, ray.xz);
            if (direction > 0.000001) {
                next = min(next, cursor + (APOTHEM * CELL_PITCH - dot(NORMALS[i].xz, local)) / direction);
            }
        }
        cursor = max(cursor + 0.0002, next + 0.0001);
    }
    if (!found && resolved >= 1.0) discard;
    if (!found) hit = start;

    vec3 position = vec3(GridOrigin.x, CameraHeight, GridOrigin.y) + ray * hit;
    vec2 local = abs(position.xz - hitCenter);
    float edgeDistance = INRADIUS - max(dot(local, vec2(APOTHEM, 0.5)), local.y);
    float edge = 1.0 - smoothstep(BEVEL, BEVEL + 0.045 * SIDE_LENGTH, edgeDistance);
    float rim = 1.0 - smoothstep(0.012 * SIDE_LENGTH, 0.045 * SIDE_LENGTH, min(abs(position.y), abs(position.y + THICKNESS)));
    float side = 1.0 - abs(normal.y);
    vec3 color = mix(vec3(0.035, 0.30, 0.80), vec3(0.02, 0.20, 0.48), side);
    float highlight = max(edge * abs(normal.y), rim * side);
    color = mix(color, vec3(0.13, 0.85, 1.0), highlight * 0.9);
    float alpha = 0.16 + side * 0.24 + highlight * 0.40;
    float faceLight = 0.78 + 0.22 * max(dot(normal, normalize(vec3(-0.3, 0.8, 0.5))), 0.0);
    color *= faceLight;
    alpha = mix(0.18 * Completion, found ? alpha : 0.0, resolved);
    color = mix(vec3(0.04, 0.38, 0.82), color, resolved);
    float fade = 1.0 - smoothstep(ViewDistance * 0.75, ViewDistance, length(ray.xz * hit));
    if (alpha * fade <= 0.001) discard;
    fragColor = vec4(color, alpha * fade) * ColorModulator;
    // Test against real terrain at the ray hit, including prism sides and the underside.
    vec4 clip = ViewProjection * vec4(ray * hit, 1.0);
    if (clip.w <= 0.0 || clip.z < -clip.w || clip.z > clip.w) discard;
    gl_FragDepth = clip.z / clip.w * 0.5 + 0.5;
}
