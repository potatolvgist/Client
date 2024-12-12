package dev.blend.util.render

import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import dev.blend.util.IAccessor
import dev.blend.util.misc.MiscUtil
import kotlinx.io.IOException
import org.lwjgl.nanovg.NVGColor
import org.lwjgl.nanovg.NVGPaint
import org.lwjgl.nanovg.NanoVG.*
import org.lwjgl.nanovg.NanoVGGL3
import org.lwjgl.opengl.GL11
import org.lwjgl.system.MemoryStack
import java.awt.Color
import java.lang.IllegalArgumentException
import java.nio.ByteBuffer

object DrawUtil: IAccessor {

    var context = -1L
    private lateinit var regularFont: ByteBuffer

    @JvmStatic
    fun initialize() {
        context = NanoVGGL3.nvgCreate(NanoVGGL3.NVG_ANTIALIAS or NanoVGGL3.NVG_STENCIL_STROKES)
        if (context == -1L) {
            throw IllegalStateException("NanoVG Context could not be created.")
        }
        ResourceManager.init()
    }

    @JvmStatic
    fun begin() {
        preRender()
        nvgBeginFrame(context, mc.window.width.toFloat(), mc.window.height.toFloat(), 1.0f)
        save()
        scale()
    }
    @JvmStatic
    fun end() {
        restore()
        nvgEndFrame(context)
        postRender()
    }

    @JvmStatic
    fun save() = nvgSave(context)
    @JvmStatic
    fun restore() = nvgRestore(context)

    @JvmStatic
    fun scale() = scale(mc.window.scaleFactor)
    @JvmStatic
    fun scale(scaleFactor: Number) = scale(scaleFactor, scaleFactor)
    @JvmStatic
    fun scale(xScaleFactor: Number, yScaleFactor: Number) = nvgScale(context, xScaleFactor.toFloat(), yScaleFactor.toFloat())

    @JvmStatic
    fun translate(xTranslate: Number, yTranslate: Number) = nvgTranslate(context, xTranslate.toFloat(), yTranslate.toFloat())
    @JvmStatic
    fun resetTranslate() = nvgResetTransform(context)

    @JvmStatic
    fun scissor(x: Number, y: Number, width: Number, height: Number) = nvgScissor(context, x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat())
    @JvmStatic
    fun resetScissor() = nvgResetScissor(context)
    @JvmStatic
    fun intersectScissor(x: Number, y:Number, width:Number, height: Number) = nvgIntersectScissor(context, x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat())
    @JvmStatic
    fun beginPath() = nvgBeginPath(context)
    @JvmStatic
    fun pathWindingCCW() = nvgPathWinding(context, NVG_CCW)
    @JvmStatic
    fun fill() = nvgFill(context)
    @JvmStatic
    fun nvgRoundedRect(x: Number, y: Number, width: Number, height: Number, radius: Number) = nvgRoundedRect(context, x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), radius.toFloat())

    // Shapes.
    @JvmStatic
    fun rect(x: Number, y: Number, width: Number, height: Number, color: Color, alignment: Alignment = Alignment.TOP_LEFT) {
        nvgBeginPath(context)
        NVGColor.calloc().use { nvgColor ->
            nvgRect(context, alignX(x, width, alignment), alignY(y, height, alignment), width.toFloat(), height.toFloat())
            nvgRGBAf(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f, nvgColor)
            nvgFillColor(context, nvgColor)
            nvgFill(context)
        }
        nvgClosePath(context)
    }
    @JvmStatic
    fun rect(x: Number, y: Number, width: Number, height: Number, stroke: Number, color: Color, alignment: Alignment = Alignment.TOP_LEFT) {
        nvgBeginPath(context)
        NVGColor.calloc().use { nvgColor ->
            nvgRect(context, alignX(x, width, alignment), alignY(y, height, alignment), width.toFloat(), height.toFloat())
            nvgRGBAf(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f, nvgColor)
            nvgStrokeWidth(context, stroke.toFloat())
            nvgStrokeColor(context, nvgColor)
            nvgStroke(context)
        }
        nvgClosePath(context)
    }
    @JvmStatic
    fun rect(x: Number, y: Number, width: Number, height: Number, gradient: Gradient, alignment: Alignment = Alignment.TOP_LEFT) {
        nvgBeginPath(context)
        NVGPaint.calloc().use { nvgPaint ->
            nvgRect(context, alignX(x, width, alignment), alignY(y, height, alignment), width.toFloat(), height.toFloat())
            NVGColor.calloc().use { primary ->
                NVGColor.calloc().use { secondary ->
                    nvgRGBAf(gradient.primary.red / 255f, gradient.primary.green / 255f, gradient.primary.blue / 255f, gradient.primary.alpha / 255f, primary)
                    nvgRGBAf(gradient.secondary.red / 255f, gradient.secondary.green / 255f, gradient.secondary.blue / 255f, gradient.secondary.alpha / 255f, secondary)
                    nvgLinearGradient(context, gradient.origin.x.toFloat(), gradient.origin.y.toFloat(), gradient.end.x.toFloat(), gradient.end.y.toFloat(), primary, secondary, nvgPaint)
                }
            }
            nvgFillPaint(context, nvgPaint)
            nvgFill(context)
        }
        nvgClosePath(context)
    }
    @JvmStatic
    fun rect(x: Number, y: Number, width: Number, height: Number, stroke: Number, gradient: Gradient, alignment: Alignment = Alignment.TOP_LEFT) {
        nvgBeginPath(context)
        NVGPaint.calloc().use { nvgPaint ->
            nvgRect(context, alignX(x, width, alignment), alignY(y, height, alignment), width.toFloat(), height.toFloat())
            NVGColor.calloc().use { primary ->
                NVGColor.calloc().use { secondary ->
                    nvgRGBAf(gradient.primary.red / 255f, gradient.primary.green / 255f, gradient.primary.blue / 255f, gradient.primary.alpha / 255f, primary)
                    nvgRGBAf(gradient.secondary.red / 255f, gradient.secondary.green / 255f, gradient.secondary.blue / 255f, gradient.secondary.alpha / 255f, secondary)
                    nvgLinearGradient(context, gradient.origin.x.toFloat(), gradient.origin.y.toFloat(), gradient.end.x.toFloat(), gradient.end.y.toFloat(), primary, secondary, nvgPaint)
                }
            }
            nvgStrokeWidth(context, stroke.toFloat())
            nvgStrokePaint(context, nvgPaint)
            nvgStroke(context)
        }
        nvgClosePath(context)
    }

    @JvmStatic
    fun roundedRect(x: Number, y: Number, width: Number, height: Number, radius: Number, color: Color, alignment: Alignment = Alignment.TOP_LEFT) {
        nvgBeginPath(context)
        NVGColor.calloc().use { nvgColor ->
            nvgRoundedRect(context, alignX(x, width, alignment), alignY(y, height, alignment), width.toFloat(), height.toFloat(), radius.toFloat())
            nvgRGBAf(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f, nvgColor)
            nvgFillColor(context, nvgColor)
            nvgFill(context)
        }
        nvgClosePath(context)
    }
    @JvmStatic
    fun roundedRect(x: Number, y: Number, width: Number, height: Number, radius: Number, gradient: Gradient, alignment: Alignment = Alignment.TOP_LEFT) {
        nvgBeginPath(context)
        NVGPaint.calloc().use { nvgPaint ->
            nvgRoundedRect(context, alignX(x, width, alignment), alignY(y, height, alignment), width.toFloat(), height.toFloat(), radius.toFloat())
            NVGColor.calloc().use { primary ->
                NVGColor.calloc().use { secondary ->
                    nvgRGBAf(gradient.primary.red / 255f, gradient.primary.green / 255f, gradient.primary.blue / 255f, gradient.primary.alpha / 255f, primary)
                    nvgRGBAf(gradient.secondary.red / 255f, gradient.secondary.green / 255f, gradient.secondary.blue / 255f, gradient.secondary.alpha / 255f, secondary)
                    nvgLinearGradient(context, gradient.origin.x.toFloat(), gradient.origin.y.toFloat(), gradient.end.x.toFloat(), gradient.end.y.toFloat(), primary, secondary, nvgPaint)
                }
            }
            nvgFillPaint(context, nvgPaint)
            nvgFill(context)
        }
        nvgClosePath(context)
    }
    @JvmStatic
    fun roundedRect(x: Number, y: Number, width: Number, height: Number, radius: Number, stroke: Number, color: Color, alignment: Alignment = Alignment.TOP_LEFT) {
        nvgBeginPath(context)
        NVGColor.calloc().use { nvgColor ->
            nvgRoundedRect(context, alignX(x, width, alignment), alignY(y, height, alignment), width.toFloat(), height.toFloat(), radius.toFloat())
            nvgRGBAf(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f, nvgColor)
            nvgStrokeWidth(context, stroke.toFloat())
            nvgStrokeColor(context, nvgColor)
            nvgStroke(context)
        }
        nvgClosePath(context)
    }
    @JvmStatic
    fun roundedRect(x: Number, y: Number, width: Number, height: Number, radius: Number, stroke: Number, gradient: Gradient, alignment: Alignment = Alignment.TOP_LEFT) {
        nvgBeginPath(context)
        NVGPaint.calloc().use { nvgPaint ->
            nvgRoundedRect(context, alignX(x, width, alignment), alignY(y, height, alignment), width.toFloat(), height.toFloat(), radius.toFloat())
            NVGColor.calloc().use { primary ->
                NVGColor.calloc().use { secondary ->
                    nvgRGBAf(gradient.primary.red / 255f, gradient.primary.green / 255f, gradient.primary.blue / 255f, gradient.primary.alpha / 255f, primary)
                    nvgRGBAf(gradient.secondary.red / 255f, gradient.secondary.green / 255f, gradient.secondary.blue / 255f, gradient.secondary.alpha / 255f, secondary)
                    nvgLinearGradient(context, gradient.origin.x.toFloat(), gradient.origin.y.toFloat(), gradient.end.x.toFloat(), gradient.end.y.toFloat(), primary, secondary, nvgPaint)
                }
            }
            nvgStrokeWidth(context, stroke.toFloat())
            nvgStrokePaint(context, nvgPaint)
            nvgStroke(context)
        }
        nvgClosePath(context)
    }
    @JvmStatic
    fun roundedRect(x: Number, y: Number, width: Number, height: Number, radius: DoubleArray, color: Color, alignment: Alignment = Alignment.TOP_LEFT) {
        if (radius.size != 4) {
            throw IllegalArgumentException("DoubleArray of size 4 required. only ${radius.size} found.")
        }
        nvgBeginPath(context)
        NVGColor.calloc().use { nvgColor ->
            nvgRoundedRectVarying(context, alignX(x, width, alignment), alignY(y, height, alignment), width.toFloat(), height.toFloat(), radius[0].toFloat(), radius[1].toFloat(), radius[2].toFloat(), radius[3].toFloat())
            nvgRGBAf(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f, nvgColor)
            nvgFillColor(context, nvgColor)
            nvgFill(context)
        }
        nvgClosePath(context)
    }
    @JvmStatic
    fun roundedRect(x: Number, y: Number, width: Number, height: Number, radius: DoubleArray, gradient: Gradient, alignment: Alignment = Alignment.TOP_LEFT) {
        if (radius.size != 4) {
            throw IllegalArgumentException("DoubleArray of size 4 required. only ${radius.size} found.")
        }
        nvgBeginPath(context)
        NVGPaint.calloc().use { nvgPaint ->
            nvgRoundedRectVarying(context, alignX(x, width, alignment), alignY(y, height, alignment), width.toFloat(), height.toFloat(), radius[0].toFloat(), radius[1].toFloat(), radius[2].toFloat(), radius[3].toFloat())
            NVGColor.calloc().use { primary ->
                NVGColor.calloc().use { secondary ->
                    nvgRGBAf(gradient.primary.red / 255f, gradient.primary.green / 255f, gradient.primary.blue / 255f, gradient.primary.alpha / 255f, primary)
                    nvgRGBAf(gradient.secondary.red / 255f, gradient.secondary.green / 255f, gradient.secondary.blue / 255f, gradient.secondary.alpha / 255f, secondary)
                    nvgLinearGradient(context, gradient.origin.x.toFloat(), gradient.origin.y.toFloat(), gradient.end.x.toFloat(), gradient.end.y.toFloat(), primary, secondary, nvgPaint)
                }
            }
            nvgFillPaint(context, nvgPaint)
            nvgFill(context)
        }
        nvgClosePath(context)
    }

    // Font Rendering
    @JvmStatic
    fun drawString(text: String, x: Number, y: Number, size: Number, color: Color, alignment: Alignment = Alignment.TOP_LEFT, font: FontResource = ResourceManager.FontResources.regular) {
        nvgBeginPath(context)
        NVGColor.calloc().use { nvgColor ->
            nvgRGBAf(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f, nvgColor)
            nvgFillColor(context, nvgColor)
            nvgFontFace(context, font.identifier)
            nvgFontSize(context, size.toFloat())
            nvgTextAlign(context, getAlignFlags(alignment))
            nvgText(context, x.toFloat(), y.toFloat(), text)
        }
        nvgClosePath(context)
    }
    @JvmStatic
    fun drawString(text: String, x: Number, y: Number, size: Number, gradient: Gradient, alignment: Alignment = Alignment.TOP_LEFT, font: FontResource = ResourceManager.FontResources.regular) {
        nvgBeginPath(context)
        NVGPaint.calloc().use { nvgPaint ->
            NVGColor.calloc().use { primary ->
                NVGColor.calloc().use { secondary ->
                    nvgRGBAf(gradient.primary.red / 255f, gradient.primary.green / 255f, gradient.primary.blue / 255f, gradient.primary.alpha / 255f, primary)
                    nvgRGBAf(gradient.secondary.red / 255f, gradient.secondary.green / 255f, gradient.secondary.blue / 255f, gradient.secondary.alpha / 255f, secondary)
                    nvgLinearGradient(context, gradient.origin.x.toFloat(), gradient.origin.y.toFloat(), gradient.end.x.toFloat(), gradient.end.y.toFloat(), primary, secondary, nvgPaint)
                }
            }
            nvgFillPaint(context, nvgPaint)
            nvgFontFace(context, font.identifier)
            nvgFontSize(context, size.toFloat())
            nvgTextAlign(context, getAlignFlags(alignment))
            nvgText(context, x.toFloat(), y.toFloat(), text)
        }
        nvgClosePath(context)
    }
    @JvmStatic
    fun getStringWidth(text: String, size: Number, font: FontResource = ResourceManager.FontResources.regular): Double {
        var width: Float
        nvgFontFace(context, font.identifier)
        nvgFontSize(context, size.toFloat())
        MemoryStack.stackPush().use {
            val bounds = floatArrayOf(0.0f, 0.0f, 0.0f, 0.0f)
            nvgTextBounds(context, 0.0f, 0.0f, text, bounds)
            width = bounds[2] - bounds[0]
        }
        return width.toDouble()
    }

    // custom shit

    /**
     * Q: How bad is this code?
     * A: yes.
     */
    @JvmStatic
    fun rainbowBar(x: Number, y: Number, width: Number, height: Number, radius: Number, alignment: Alignment = Alignment.TOP_LEFT) {
        val yay = width.toDouble() / 10.0
        var x = x.toDouble()
        for (hue in 0..9) {
            roundedRect(
                x, y, yay, height,
                doubleArrayOf(
                    if (hue == 0) {
                        radius.toDouble()
                    } else {
                        0.0
                    },
                    if (hue == 9) {
                        radius.toDouble()
                    } else {
                        0.0
                    },
                    if (hue == 9) {
                        radius.toDouble()
                    } else {
                        0.0
                    },
                    if (hue == 0) {
                        radius.toDouble()
                    } else {
                        0.0
                    }
                ),
                Gradient(
                    Color.getHSBColor(hue / 10.0f, 1.0f, 1.0f),
                    Color.getHSBColor((hue / 10.0f) + 0.1f, 1.0f, 1.0f),
                    Point(x, y),
                    Point(x + yay, y)
                ),
                alignment
            )
            x += yay
        }
    }

    private fun preRender() {
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
//        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE)
//        RenderSystem.disableDepthTest()
//        RenderSystem.depthFunc(GL11.GL_LESS)
//        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT)
    }
    private fun postRender() {
        RenderSystem.disableCull()
        RenderSystem.disableDepthTest()
        RenderSystem.enableBlend()
//        RenderSystem.defaultBlendFunc()
//        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA)
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE)
    }
    private fun alignX(x: Number, width: Number, alignment: Alignment): Float {
        return when (alignment) {
            Alignment.TOP_RIGHT, Alignment.CENTER_RIGHT, Alignment.BOTTOM_RIGHT -> x.toDouble() - width.toDouble()
            Alignment.TOP_CENTER, Alignment.CENTER, Alignment.BOTTOM_CENTER -> x.toDouble() - (width.toDouble() / 2.0)
            else -> x
        }.toFloat()
    }
    private fun alignY(y: Number, height: Number, alignment: Alignment): Float {
        return when (alignment) {
            Alignment.BOTTOM_LEFT, Alignment.BOTTOM_CENTER, Alignment.BOTTOM_RIGHT -> y.toDouble() - height.toDouble()
            Alignment.CENTER_LEFT, Alignment.CENTER, Alignment.CENTER_RIGHT -> y.toDouble() - (height.toDouble() / 2.0)
            else -> y
        }.toFloat()
    }
    private fun getAlignFlags(alignment: Alignment): Int {
        val x = when (alignment) {
            Alignment.TOP_CENTER, Alignment.CENTER, Alignment.BOTTOM_CENTER -> NVG_ALIGN_CENTER
            Alignment.TOP_RIGHT, Alignment.CENTER_RIGHT, Alignment.BOTTOM_RIGHT -> NVG_ALIGN_RIGHT
            else -> NVG_ALIGN_LEFT
        }
        val y = when (alignment) {
            Alignment.CENTER_LEFT, Alignment.CENTER, Alignment.CENTER_RIGHT -> NVG_ALIGN_MIDDLE
            Alignment.BOTTOM_LEFT, Alignment.BOTTOM_CENTER, Alignment.BOTTOM_RIGHT -> NVG_ALIGN_BOTTOM
            else -> NVG_ALIGN_TOP
        }
        return x or y
    }

}