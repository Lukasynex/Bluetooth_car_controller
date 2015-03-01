package com.binacja.luk.main;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class UniversalLayout extends RelativeLayout {
	ImageView btn_up;
	ImageView btn_down;
	ImageView btn_left;
	ImageView btn_right;

	ImageView btn_horn;
	ImageView btn_light;
	ImageView btn_plus;
	ImageView btn_minus;
	private final Context context;
	private int screenWidth, screenHeight;

	public UniversalLayout(Context context, AttributeSet set) {
		super(context, set);
		this.context = context;
		init();
	}

	public UniversalLayout(Context context, int screenW, int screenH) {
		super(context);
		this.context = context;
		screenWidth = screenW;
		screenHeight = screenH;

		init();
	}

	private void init() {

		btn_down = new ImageView(context);
		btn_down.setBackgroundResource(R.drawable.downbutton100);

		btn_up = new ImageView(context);
		btn_up.setBackgroundResource(R.drawable.upbutton100);

		btn_left = new ImageView(context);
		btn_left.setBackgroundResource(R.drawable.leftbutton100);

		btn_right = new ImageView(context);
		btn_right.setBackgroundResource(R.drawable.rightbutton100);

		btn_light = new ImageView(context);
		btn_light.setBackgroundResource(R.drawable.light_down);

		btn_plus = new ImageView(context);
		btn_plus.setBackgroundResource(R.drawable.a_plus);

		btn_minus = new ImageView(context);
		btn_minus.setBackgroundResource(R.drawable.a_minus);

		btn_horn = new ImageView(context);
		btn_horn.setBackgroundResource(R.drawable.a_sound_down);

		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				screenWidth / 4, screenHeight / 2);
		btn_down.setLayoutParams(layoutParams);
		btn_up.setLayoutParams(layoutParams);
		btn_right.setLayoutParams(layoutParams);
		btn_left.setLayoutParams(layoutParams);

		btn_horn.setLayoutParams(layoutParams);
		btn_light.setLayoutParams(layoutParams);
		btn_plus.setLayoutParams(layoutParams);
		btn_minus.setLayoutParams(layoutParams);
		
		this.addView(btn_down, 0, screenHeight/2);
		this.addView(btn_up, 0, screenHeight/2 - btn_up.getHeight());
		this.addView(btn_left, screenWidth - btn_left.getWidth(), screenHeight/2 - btn_left.getHeight());
		this.addView(btn_right, screenWidth - btn_right.getWidth(), screenHeight/2);
		
		this.addView(btn_horn, screenWidth/2 - btn_horn.getWidth(), screenHeight/2 - btn_horn.getHeight());
		this.addView(btn_light, screenWidth/2 - btn_light.getWidth(), screenHeight/2);
		
		this.addView(btn_plus, screenWidth/2, screenHeight/2 - btn_plus.getHeight());
		this.addView(btn_minus, screenWidth/2, screenHeight/2);
	}

}
