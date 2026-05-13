package uikit.drawable

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import com.tonapps.uikit.color.separatorCommonColor
import uikit.base.BaseDrawable
import uikit.extensions.dp

class FooterDrawable(context: Context) : BaseDrawable() {

    private val radius = 28f.dp
    private val path = Path()
    private val rectF = RectF()

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.TRANSPARENT
    }

    // Border paint for glass edge effect
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1f.dp
        color = Color.argb(40, 255, 255, 255)
    }

    // Inner highlight gradient (top edge glow)
    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.5f.dp
    }

    // Shadow paint for depth
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(30, 0, 0, 0)
        style = Paint.Style.FILL
    }

    private var dividerVisible = false

    fun setColor(color: Int) {
        if (backgroundPaint.color != color) {
            backgroundPaint.color = color
            invalidateSelf()
        }
    }

    fun setDivider(value: Boolean) {
        dividerVisible = value
        invalidateSelf()
    }

    override fun draw(canvas: Canvas) {
        rectF.set(bounds)
        path.reset()
        path.addRoundRect(rectF, radius, radius, Path.Direction.CW)

        // Draw shadow underneath
        val shadowRect = RectF(rectF)
        shadowRect.offset(0f, 2f.dp)
        val shadowPath = Path()
        shadowPath.addRoundRect(shadowRect, radius, radius, Path.Direction.CW)
        canvas.drawPath(shadowPath, shadowPaint)

        // Draw main glass background
        canvas.save()
        canvas.clipPath(path)
        if (backgroundPaint.color != Color.TRANSPARENT) {
            canvas.drawPaint(backgroundPaint)
        }
        canvas.restore()

        // Draw border
        val borderRect = RectF(rectF)
        borderRect.inset(borderPaint.strokeWidth / 2f, borderPaint.strokeWidth / 2f)
        canvas.drawRoundRect(borderRect, radius, radius, borderPaint)

        // Draw top highlight gradient
        if (rectF.height() > 0 && rectF.width() > 0) {
            highlightPaint.shader = LinearGradient(
                rectF.left, rectF.top,
                rectF.left, rectF.top + rectF.height() * 0.5f,
                Color.argb(25, 255, 255, 255),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            )
            val highlightRect = RectF(borderRect)
            highlightRect.inset(highlightPaint.strokeWidth / 2f, highlightPaint.strokeWidth / 2f)
            canvas.drawRoundRect(highlightRect, radius - 1f.dp, radius - 1f.dp, highlightPaint)
        }
    }
}
