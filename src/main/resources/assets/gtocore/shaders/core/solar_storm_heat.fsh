#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;
uniform mat4 InverseViewProjection;
uniform vec2 ScreenSize;
uniform float CameraHeight;
uniform float GroundHeight;
uniform float ViewDistance;
uniform float Time;
uniform float Strength;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    float depth = texture(DepthSampler, texCoord).r;
    vec4 position = InverseViewProjection * vec4(texCoord * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    vec3 relative = position.xyz / position.w;
    vec4 farPosition = InverseViewProjection * vec4(texCoord * 2.0 - 1.0, 1.0, 1.0);
    vec3 ray = normalize(farPosition.xyz / farPosition.w);
    float planeDistance = abs(ray.y) > 0.00001 ? (GroundHeight - CameraHeight) / ray.y : -1.0;
    // Translucent plasma does not write depth. Its known surface supplies the missing depth.
    bool plasma = ray.y < -0.00001 && planeDistance > 0.0 && planeDistance < ViewDistance
        && (depth >= 0.999999 || planeDistance < length(relative));
    if (plasma) relative = ray * planeDistance;
    else if (depth >= 0.999999) discard;
    float height = relative.y + CameraHeight - GroundHeight;
    float groundBand = 1.0 - smoothstep(3.0, 18.0, abs(height));
    float distanceFade = smoothstep(2.0, 12.0, length(relative));
    float farFade = 1.0 - smoothstep(ViewDistance * 0.75, ViewDistance, length(relative));
    float envelope = groundBand * distanceFade * farFade * Strength;
    if (envelope <= 0.001) discard;

    vec2 wave = vec2(
        sin(texCoord.y * 190.0 - Time * 7.0 + sin(texCoord.x * 75.0 + Time * 2.0)),
        sin(texCoord.x * 155.0 + Time * 4.0 + sin(texCoord.y * 95.0 - Time * 3.0))
    );
    vec2 halfTexel = 0.5 / ScreenSize;
    vec2 uv = clamp(texCoord + wave * vec2(3.5, 1.8) * envelope / ScreenSize, halfTexel, 1.0 - halfTexel);
    // Do not drag the sky into terrain along the silhouette.
    if (!plasma && texture(DepthSampler, uv).r >= 0.999999) uv = texCoord;
    if (plasma) {
        vec4 sampleFar = InverseViewProjection * vec4(uv * 2.0 - 1.0, 1.0, 1.0);
        // Keep samples on the same side of the horizon, without introducing any color tint.
        if (sampleFar.y / sampleFar.w * ray.y <= 0.0) uv = texCoord;
    }
    fragColor = vec4(texture(DiffuseSampler, uv).rgb, 1.0);
}
