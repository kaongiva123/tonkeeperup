package com.tonapps.tonkeeper.ui.component.wallet

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Outline
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import android.view.WindowInsets
import android.view.animation.OvershootInterpolator
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.WindowInsetsCompat
import com.tonapps.emoji.ui.EmojiView
import com.tonapps.tonkeeper.extensions.fixW5Title
import com.tonapps.tonkeeper.extensions.isLightTheme
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.icon.UIKitIcon
import com.tonapps.blockchain.model.legacy.Wallet
import com.tonapps.core.flags.WalletFeature
import uikit.drawable.BarDrawable
import uikit.drawable.DotDrawable
import uikit.drawable.FooterDrawable
import uikit.extensions.getDimensionPixelSize
import uikit.extensions.dp
import uikit.extensions.setPaddingTop
import uikit.extensions.statusBarHeight
import uikit.widget.RowLayout
import kotlin.math.abs

class WalletHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : RowLayout(context, attrs, defStyle),
    BarDrawable.BarDrawableOwner {

    var doWalletSwipe: ((right: Boolean) -> Unit)? = null

    private val barHeight = context.getDimensionPixelSize(uikit.R.dimen.barHeight)
    private val cornerRadius = 28f.dp

    private var topOffset: Int = statusBarHeight
        set(value) {
            if (field != value) {
                field = value
                setPaddingTop(value)
                requestLayout()
            }
        }

    private val swipeGestureListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onFling(
            ev1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            val threshold = 100
            val velocityThreshold = 100
            val e1 = ev1 ?: return false
            val diffY = e2.y - e1.y
            val diffX = e2.x - e1.x
            if (abs(diffX) > abs(diffY) && abs(diffX) > threshold && abs(velocityX) > velocityThreshold) {
                doWalletSwipe?.invoke(diffX > 0)
                return true
            }
            return false
        }
    }

    private val swipeDetector = GestureDetector(context, swipeGestureListener, handler)
    private val supportView: AppCompatImageView
    private val historyView: AppCompatImageView
    private val settingsView: View
    private val walletView: View
    private val emojiView: EmojiView
    private val nameView: AppCompatTextView
    private val arrowView: AppCompatImageView
    private val settingsDot: View

    private val glassDrawable = FooterDrawable(context).apply {
        if (context.isLightTheme) {
            setColor(0xCCFFFFFF.toInt())
        } else {
            setColor(0xB31C1C1E.toInt())
        }
    }

    var onSupportClick: (() -> Unit)? = null
        set(value) {
            field = value
            supportView.setOnClickListener { value?.invoke() }
        }

    var onHistoryClick: (() -> Unit)? = null
        set(value) {
            field = value
            historyView.setOnClickListener { value?.invoke() }
        }

    var onSettingsClick: (() -> Unit)? = null
        set(value) {
            field = value
            settingsView.setOnClickListener { value?.invoke() }
        }

    var onWalletClick: (() -> Unit)? = null
        set(value) {
            field = value
            walletView.setOnClickListener { value?.invoke() }
        }

    init {
        setPadding(
            context.getDimensionPixelSize(uikit.R.dimen.offsetMedium),
            topOffset,
            0,
            0
        )
        super.setBackground(glassDrawable)
        elevation = 8f.dp

        clipToOutline = true
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, cornerRadius)
            }
        }

        // Entrance animation
        alpha = 0f
        translationY = -40f.dp
        post {
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setInterpolator(OvershootInterpolator(0.8f))
                .start()
        }

        inflate(context, R.layout.view_wallet_header, this)
        supportView = findViewById(R.id.support)
        supportView.setImageResource(
            when {
                WalletFeature.NewRampFlow.isEnabled -> UIKitIcon.ic_qr_viewfinder_thin_28
                else -> UIKitIcon.ic_question_message_outline_28
            }
        )
        historyView = findViewById(R.id.history)
        settingsView = findViewById(R.id.settings)
        walletView = findViewById(R.id.wallet)
        walletView.setOnTouchListener { v, event -> swipeDetector.onTouchEvent(event) }
        emojiView = findViewById(R.id.wallet_emoji)
        nameView = findViewById(R.id.wallet_name)
        arrowView = findViewById(R.id.wallet_arrow)

        settingsDot = findViewById(R.id.settings_dot)
        settingsDot.background = DotDrawable(context)
    }

    fun setHistoryVisible(visible: Boolean) {
        historyView.visibility = if (visible) View.VISIBLE else View.GONE
    }

    fun setDot(dot: Boolean) {
        settingsDot.visibility = if (dot) View.VISIBLE else View.GONE
    }

    override fun setDivider(value: Boolean) {
        // No divider on floating island
    }

    fun setWallet(walletLabel: Wallet.Label) {
        if (walletLabel.isEmpty) {
            walletView.visibility = View.GONE
            return
        }
        walletView.visibility = View.VISIBLE
        nameView.text = walletLabel.name.fixW5Title()
        emojiView.setEmoji(walletLabel.emoji, Color.TRANSPARENT)
        walletView.backgroundTintList = ColorStateList.valueOf(walletLabel.color)
    }

    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        val compatInsets = WindowInsetsCompat.toWindowInsetsCompat(insets)
        val statusInsets = compatInsets.getInsets(WindowInsetsCompat.Type.statusBars())
        topOffset = statusInsets.top
        return super.onApplyWindowInsets(insets)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(barHeight + topOffset, MeasureSpec.EXACTLY))
    }
}
