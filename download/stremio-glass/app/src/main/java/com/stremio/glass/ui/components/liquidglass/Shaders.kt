package com.stremio.glass.ui.components.liquidglass

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush

/**
 * AGSL shader strings for the liquid glass refraction effect.
 * These replicate the Kyant0 AndroidLiquidGlass shader approach.
 */

// Rounded rectangle SDF with per-corner radii
const val SDF_ROUNDED_RECT = """
float sdRoundedRect(float2 coord, float2 halfSize, float4 radii) {
    float2 q = abs(coord) - halfSize + radii.xy;
    float2 r = (coord.x > 0.0) ?
        ((coord.y > 0.0) ? radii.xy : radii.xw) :
        ((coord.y > 0.0) ? radii.zy : radii.zw);
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - length(r);
}
"""

// Analytic gradient of the SDF
const val SDF_GRADIENT = """
float2 gradSdRoundedRect(float2 coord, float2 halfSize, float4 radii) {
    float2 q = abs(coord) - halfSize + radii.xy;
    float eps = 0.5;
    float d = sdRoundedRect(coord, halfSize, radii);
    float dx = sdRoundedRect(coord + float2(eps, 0), halfSize, radii) - d;
    float dy = sdRoundedRect(coord + float2(0, eps), halfSize, radii) - d;
    return float2(dx, dy) / eps;
}
"""

// Circular arc height profile: 1 - sqrt(1 - x^2)
const val CIRCLE_MAP = """
float circleMap(float x) {
    return 1.0 - sqrt(1.0 - x * x);
}
"""

// Main refraction shader
val RefractionShaderString = """
uniform shader content;
uniform float2 size;
uniform float2 offset;
uniform float4 cornerRadii;
uniform float refractionHeight;
uniform float refractionAmount;
uniform float depthEffect;

$SDF_ROUNDED_RECT
$SDF_GRADIENT
$CIRCLE_MAP

half4 main(float2 coord) {
    float2 center = size * 0.5;
    float2 halfSize = size * 0.5 - cornerRadii.x;
    float2 localCoord = coord - offset - center;
    
    float d = sdRoundedRect(localCoord, halfSize, cornerRadii);
    float2 grad = gradSdRoundedRect(localCoord, halfSize, cornerRadii);
    float gradLen = length(grad);
    float2 gradNorm = gradLen > 0.001 ? grad / gradLen : float2(0.0, 1.0);
    
    // Height profile
    float maxDist = min(halfSize.x, halfSize.y);
    float normalizedDist = clamp(-d / maxDist, 0.0, 1.0);
    float height = circleMap(normalizedDist) * refractionHeight;
    
    // Refraction displacement
    float2 displacement = gradNorm * height * refractionAmount / size;
    float2 refractedCoord = coord + displacement * depthEffect;
    
    half4 color = content.eval(refractedCoord);
    
    // Edge darkening
    float edgeFactor = smoothstep(-2.0, 2.0, d);
    color.rgb *= mix(0.85, 1.0, edgeFactor);
    
    return color;
}
"""

// Refraction with chromatic aberration
val RefractionWithDispersionShaderString = """
uniform shader content;
uniform float2 size;
uniform float2 offset;
uniform float4 cornerRadii;
uniform float refractionHeight;
uniform float refractionAmount;
uniform float chromaticAberration;
uniform float depthEffect;

$SDF_ROUNDED_RECT
$SDF_GRADIENT
$CIRCLE_MAP

half4 main(float2 coord) {
    float2 center = size * 0.5;
    float2 halfSize = size * 0.5 - cornerRadii.x;
    float2 localCoord = coord - offset - center;
    
    float d = sdRoundedRect(localCoord, halfSize, cornerRadii);
    float2 grad = gradSdRoundedRect(localCoord, halfSize, cornerRadii);
    float gradLen = length(grad);
    float2 gradNorm = gradLen > 0.001 ? grad / gradLen : float2(0.0, 1.0);
    
    float maxDist = min(halfSize.x, halfSize.y);
    float normalizedDist = clamp(-d / maxDist, 0.0, 1.0);
    float height = circleMap(normalizedDist) * refractionHeight;
    float2 displacement = gradNorm * height * refractionAmount / size;
    
    // Chromatic aberration - offset R, G, B differently
    float dispersion = chromaticAberration * normalizedDist * normalizedDist;
    float2 rOffset = displacement * (1.0 + dispersion * 0.02) * depthEffect;
    float2 gOffset = displacement * depthEffect;
    float2 bOffset = displacement * (1.0 - dispersion * 0.02) * depthEffect;
    
    float r = content.eval(coord + rOffset).r;
    float g = content.eval(coord + gOffset).g;
    float b = content.eval(coord + bOffset).b;
    float a = content.eval(coord + gOffset).a;
    
    half4 color = half4(r, g, b, a);
    
    float edgeFactor = smoothstep(-2.0, 2.0, d);
    color.rgb *= mix(0.85, 1.0, edgeFactor);
    
    return color;
}
"""

// Highlight shader - directional light
val DefaultHighlightShaderString = """
uniform shader content;
uniform float2 size;
uniform float2 offset;
uniform float4 cornerRadii;
uniform float lightX;
uniform float lightY;
uniform float falloff;
uniform float intensity;

$SDF_ROUNDED_RECT
$SDF_GRADIENT
$CIRCLE_MAP

half4 main(float2 coord) {
    float2 center = size * 0.5;
    float2 halfSize = size * 0.5 - cornerRadii.x;
    float2 localCoord = coord - offset - center;
    
    float d = sdRoundedRect(localCoord, halfSize, cornerRadii);
    float2 grad = gradSdRoundedRect(localCoord, halfSize, cornerRadii);
    float gradLen = length(grad);
    float2 gradNorm = gradLen > 0.001 ? grad / gradLen : float2(0.0, 1.0);
    
    float2 lightDir = normalize(float2(lightX, lightY));
    float NdotL = dot(gradNorm, lightDir);
    
    float maxDist = min(halfSize.x, halfSize.y);
    float normalizedDist = clamp(-d / maxDist, 0.0, 1.0);
    float height = circleMap(normalizedDist);
    
    float highlight = pow(abs(NdotL) * height, falloff) * intensity;
    highlight *= smoothstep(0.0, 1.0, normalizedDist);
    
    half4 base = content.eval(coord);
    return half4(base.rgb + highlight * 0.5, base.a);
}
"""

// Ambient highlight shader - split light/dark
val AmbientHighlightShaderString = """
uniform shader content;
uniform float2 size;
uniform float2 offset;
uniform float4 cornerRadii;
uniform float intensity;

$SDF_ROUNDED_RECT
$CIRCLE_MAP

half4 main(float2 coord) {
    float2 center = size * 0.5;
    float2 halfSize = size * 0.5 - cornerRadii.x;
    float2 localCoord = coord - offset - center;
    
    float d = sdRoundedRect(localCoord, halfSize, cornerRadii);
    float maxDist = min(halfSize.x, halfSize.y);
    float normalizedDist = clamp(-d / maxDist, 0.0, 1.0);
    
    float light = step(0.0, d) * 0.3 + (1.0 - step(0.0, d)) * smoothstep(0.0, 0.5, normalizedDist);
    light *= intensity;
    
    half4 base = content.eval(coord);
    return half4(base.rgb + light * 0.3, base.a);
}
"""

/**
 * Create a RuntimeShader brush from AGSL shader code.
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun createRefractionBrush(
    shaderCode: String,
    size: Size,
    cornerRadius: Float,
    refractionHeight: Float,
    refractionAmount: Float,
    chromaticAberration: Float = 0f,
    depthEffect: Float = 1f
): ShaderBrush {
    val shader = RuntimeShader(shaderCode)
    shader.setFloatUniform("size", size.width, size.height)
    shader.setFloatUniform("offset", 0f, 0f)
    shader.setFloatUniform("cornerRadii", cornerRadius, cornerRadius, cornerRadius, cornerRadius)
    shader.setFloatUniform("refractionHeight", refractionHeight)
    shader.setFloatUniform("refractionAmount", refractionAmount)
    shader.setFloatUniform("depthEffect", depthEffect)
    if (chromaticAberration > 0f) {
        shader.setFloatUniform("chromaticAberration", chromaticAberration)
    }
    return ShaderBrush(shader)
}
