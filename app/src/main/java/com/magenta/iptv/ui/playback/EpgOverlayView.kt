package com.magenta.iptv.ui.playback

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import com.magenta.iptv.data.model.Programme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EpgOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val handler = Handler(Looper.getMainLooper())
    private val hideRunnable = Runnable { hide() }

    private val textView: TextView

    init {
        val dp8 = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics
        ).toInt()
        val dp16 = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 16f, resources.displayMetrics
        ).toInt()

        textView = TextView(context).apply {
            setTextColor(android.graphics.Color.WHITE)
            textSize = 16f
            setPadding(dp16, dp16, dp16, dp16)
            setBackgroundColor(0xCC000000.toInt())
            setLineSpacing(0f, 1.2f)
        }

        val params = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.END
            setMargins(dp8, dp8, dp16, dp16)
        }

        addView(textView, params)
        visibility = GONE
    }

    fun show(channelId: String, epg: Map<String, Programme>) {
        val programme = epg[channelId] ?: run {
            hide()
            return
        }

        val startStr = timeFormat.format(Date(programme.startTime))
        val endStr = timeFormat.format(Date(programme.endTime))

        textView.text = buildString {
            append("Now Playing: ")
            append(programme.title)
            append("\n")
            append(startStr)
            append(" - ")
            append(endStr)
        }

        visibility = VISIBLE

        handler.removeCallbacks(hideRunnable)
        handler.postDelayed(hideRunnable, 5000L)
    }

    fun hide() {
        handler.removeCallbacks(hideRunnable)
        visibility = GONE
    }

    override fun onDetachedFromWindow() {
        handler.removeCallbacks(hideRunnable)
        super.onDetachedFromWindow()
    }
}
