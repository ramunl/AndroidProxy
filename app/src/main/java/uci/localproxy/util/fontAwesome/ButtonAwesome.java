package uci.localproxy.util.fontAwesome;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.LruCache;

import androidx.appcompat.widget.AppCompatButton;


public class ButtonAwesome extends AppCompatButton {

	private final static String NAME = "FONTAWESOME";
	private static LruCache<String, Typeface> sTypefaceCache = new LruCache<String, Typeface>(12);

	
	public ButtonAwesome(Context context) {
		super(context);
		init();
			
	}
	public ButtonAwesome(Context context,AttributeSet attrs) {
		super(context,attrs);
		init();	
	}
	public ButtonAwesome(Context context, AttributeSet attrs, int defStyle){
		super(context,attrs,defStyle);
		init();	
	}
	
	public void init(){
		Typeface typeface = sTypefaceCache.get(NAME);

		if (typeface == null) {

			typeface = Typeface.createFromAsset(getContext().getAssets(), "fontawesome-webfont.ttf");
			sTypefaceCache.put(NAME, typeface);

		}

		setTypeface(typeface);
	}
}


