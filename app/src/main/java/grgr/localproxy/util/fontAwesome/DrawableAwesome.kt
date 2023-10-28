package grgr.localproxy.util.fontAwesome

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics

class DrawableAwesome(
    private val icon: Int, sizeDpi: Int, color: Int,
    antiAliased: Boolean, fakeBold: Boolean, shadowRadius: Float,
    shadowDx: Float, shadowDy: Float, shadowColor: Int, private val context: Context
) : Drawable() {
    private val paint: Paint
    private val width: Int
    private val height: Int
    private val size: Float
    private val color: Int
    private val antiAliased: Boolean
    private val fakeBold: Boolean
    private val shadowRadius: Float
    private val shadowDx: Float
    private val shadowDy: Float
    private val shadowColor: Int

    init {
        size = dpToPx(sizeDpi) * PADDING_RATIO
        height = dpToPx(sizeDpi)
        width = dpToPx(sizeDpi)
        this.color = color
        this.antiAliased = antiAliased
        this.fakeBold = fakeBold
        this.shadowRadius = shadowRadius
        this.shadowDx = shadowDx
        this.shadowDy = shadowDy
        this.shadowColor = shadowColor
        paint = Paint()
        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.CENTER
        paint.color = this.color
        paint.textSize = size
        val font = Typeface.createFromAsset(context.assets, "fontawesome-webfont.ttf")
        paint.typeface = font
        paint.isAntiAlias = this.antiAliased
        paint.isFakeBoldText = this.fakeBold
        paint.setShadowLayer(this.shadowRadius, this.shadowDx, this.shadowDy, this.shadowColor)
    }

    override fun getIntrinsicHeight(): Int {
        return height
    }

    override fun getIntrinsicWidth(): Int {
        return width
    }

    override fun draw(canvas: Canvas) {
        val xDiff = width / 2.0f
        val stringIcon = context.resources.getString(icon)
        canvas.drawText(stringIcon, xDiff, size, paint)
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(cf: ColorFilter?) {
        paint.colorFilter = cf
    }

    private fun dpToPx(dp: Int): Int {
        val displayMetrics = context.resources.displayMetrics
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
    }

    class DrawableAwesomeBuilder(private val context: Context, private val icon: Int) {
        private var sizeDpi = 32
        private var color = Color.GRAY
        private var antiAliased = true
        private var fakeBold = true
        private var shadowRadius = 0f
        private var shadowDx = 0f
        private var shadowDy = 0f
        private var shadowColor = Color.WHITE
        fun setSize(size: Int) {
            sizeDpi = size
        }

        fun setColor(color: Int) {
            this.color = color
        }

        fun setAntiAliased(antiAliased: Boolean) {
            this.antiAliased = antiAliased
        }

        fun setFakeBold(fakeBold: Boolean) {
            this.fakeBold = fakeBold
        }

        fun setShadow(radius: Float, dx: Float, dy: Float, color: Int) {
            shadowRadius = radius
            shadowDx = dx
            shadowDy = dy
            shadowColor = color
        }

        fun build(): DrawableAwesome {
            return DrawableAwesome(
                icon, sizeDpi, color, antiAliased, fakeBold,
                shadowRadius, shadowDx, shadowDy, shadowColor, context
            )
        }
    }

    companion object {
        private const val PADDING_RATIO = 0.88f
    }
}