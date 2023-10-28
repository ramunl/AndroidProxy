package grgr.localproxy.util.fontAwesome

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.LruCache
import androidx.appcompat.widget.AppCompatButton

class ButtonAwesome : AppCompatButton {
    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init()
    }

    fun init() {
        var typeface = sTypefaceCache[NAME]
        if (typeface == null) {
            typeface = Typeface.createFromAsset(context.assets, "fontawesome-webfont.ttf")
            sTypefaceCache.put(NAME, typeface)
        }
        setTypeface(typeface)
    }

    companion object {
        private const val NAME = "FONTAWESOME"
        private val sTypefaceCache = LruCache<String, Typeface?>(12)
    }
}