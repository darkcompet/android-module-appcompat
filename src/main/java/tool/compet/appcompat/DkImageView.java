/*
 * Copyright (c) 2017-2021 DarkCompet. All rights reserved.
 */

package tool.compet.appcompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;

import tool.compet.core.DkConfig;
import tool.compet.core.DkConst;
import tool.compet.graphics.DkDrawable;
import tool.compet.graphics.DkRippleDrawable;
import tool.compet.view.DkViews;

/**
 * This is backward-compatibility version, provides some compatible features:
 * - Foreground: call `setDefaultForeground()` to set default ripple foreground.
 */
public class DkImageView extends AppCompatImageView {
	// By default, we try to create default foreground when size was changed
	private boolean createDefaultForegroundIfNotExist = true;

	// We only handle foreground when super does not support
	protected static final boolean isForegroundAvailableAtSuper = DkConst.SDK_VERSION >= Build.VERSION_CODES.M;
	protected Drawable foreground; // for api 23-

	// We only handle hotspot when super does not support
	protected static final boolean isHotspotAvailableAtSuper = DkConst.SDK_VERSION >= Build.VERSION_CODES.LOLLIPOP;
	protected boolean isPrePress;
	protected PressAction prePressAction;

	public DkImageView(Context context) {
		super(context);
		init(context);
	}

	public DkImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	@SuppressLint("CustomViewStyleable")
	public DkImageView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	private void init(Context context) {
		// Don't redraw while this time
		setWillNotDraw(false);

		//		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DKForegroundView);
		//		String animType = a.getString(R.styleable.DKForegroundView_dk_animation);
		//		int normalColor = a.getColor(R.styleable.DKForegroundView_dk_normal_color, Color.TRANSPARENT);
		//		int pressedColor = a.getColor(R.styleable.DKForegroundView_dk_pressed_color, Color.WHITE);
		//		a.recycle();

		//		int[][] states = {{-android.R.attr.state_pressed}, {android.R.attr.state_pressed}};
		//		int[] colors = {Color.TRANSPARENT, Color.WHITE};
		//		ColorStateList colorStates = new ColorStateList(states, colors);
	}

	// By default, update bounds of foreground to fit with layout
	@Override
	protected void onSizeChanged(int width, int height, int oldwidth, int oldheight) {
		// Create default foreground if not exist
		if (createDefaultForegroundIfNotExist && getForeground() == null) {
			setDefaultForeground();
		}

		if (! isForegroundAvailableAtSuper) {
			Drawable foreground = this.foreground;
			if (foreground != null) {
				foreground.setBounds(0, 0, width, height);
			}
		}

		super.onSizeChanged(width, height, oldwidth, oldheight);
	}

	// In `View.draw()` steps, this called to draw own content
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (! isForegroundAvailableAtSuper) {
			Drawable foreground = this.foreground;
			if (foreground != null) {
				foreground.draw(canvas);
			}
		}
	}

	// In `View.draw()` steps, this called to draw child views
	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);

		if (! isForegroundAvailableAtSuper) {
			Drawable foreground = this.foreground;
			if (foreground != null) {
				foreground.draw(canvas);
			}
		}
	}

	@Override
	protected boolean verifyDrawable(@NonNull Drawable drawable) {
		return super.verifyDrawable(drawable) || drawable == foreground;
	}

	// Override from api 16
	public void setBackground(Drawable background) {
		if (DkConst.SDK_VERSION >= Build.VERSION_CODES.JELLY_BEAN) {
			super.setBackground(background);
		}
		else {
			super.setBackgroundDrawable(background);
		}
	}

	// Override from api 23
	public Drawable getForeground() {
		if (isForegroundAvailableAtSuper) {
			return super.getForeground();
		}
		return foreground;
	}

	public void setDefaultForeground() {
		setDefaultForeground(DkConfig.colorAccent(getContext()));
	}

	public void setDefaultForeground(int pressedColor) {
		Drawable foreground;

		int[][] states = {
			{android.R.attr.state_pressed}, // pressed state
			//			{android.R.attr.state_focused}, // focused state
			//			{android.R.attr.state_activated}, // activated state
			{android.R.attr.state_empty}, // normal state
		};
		int[] colors = {
			pressedColor, // pressed color
			//			Color.BLUE, // focused color
			//			Color.YELLOW, // activated color
			Color.TRANSPARENT, // normal color
		};
		ColorStateList colorStateList = new ColorStateList(states, colors);

		if (isHotspotAvailableAtSuper) {
			foreground = new RippleDrawable(colorStateList, null, null);
		}
		else {
			foreground = new DkRippleDrawable(colorStateList);
		}

		setForeground(foreground);
	}

	// Override from api 23
	public void setForeground(Drawable drawable) {
		if (isForegroundAvailableAtSuper) {
			super.setForeground(drawable);
			return;
		}

		// If same drawable, just ignore
		Drawable foreground = this.foreground;
		if (foreground == drawable) {
			return;
		}

		// For new foreground, reset it first
		if (foreground != null) {
			foreground.setCallback(null);
			unscheduleDrawable(foreground);
		}

		// Perform set
		this.foreground = foreground = drawable;

		// Set callback to listen events from foreground
		if (foreground != null) {
			setWillNotDraw(false);

			foreground.setCallback(this);

			if (foreground.isStateful()) {
				foreground.setState(getDrawableState());
			}
		}
		else {
			setWillNotDraw(true);
		}

		// Invalidate
		requestLayout();
		invalidate();
	}

	// Override from api 14 (for compat view) or api 23 (new layout)
	protected void drawableStateChanged() {
		super.drawableStateChanged();

		if (! isForegroundAvailableAtSuper) {
			Drawable foreground = this.foreground;
			if (foreground != null && foreground.isStateful()) {
				foreground.setState(getDrawableState());
			}
		}
	}

	// Override from api 21
	public void drawableHotspotChanged(float x, float y) {
		if (isHotspotAvailableAtSuper) {
			super.drawableHotspotChanged(x, y);
		}

		// Handle hotspot changed for foreground at older version
		// Note: below is modified from super
		if (! isForegroundAvailableAtSuper) {
			// For older version, foreground should be instance of `DkDrawable`
			Drawable foreground = this.getForeground();
			if (foreground instanceof DkDrawable) {
				((DkDrawable) foreground).setHotspot(x, y);
			}

			dispatchDrawableHotspotChanged(x, y);
		}
	}

	// Override from api 22
	public void dispatchDrawableHotspotChanged(float x, float y) {
		if (DkConst.SDK_VERSION >= Build.VERSION_CODES.LOLLIPOP_MR1) {
			super.dispatchDrawableHotspotChanged(x, y);
		}
		else {
			//todo impl `hotspot changed` at old version
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (! isHotspotAvailableAtSuper) {
			handleTouchEventForHotspotChanged(event);
		}
		return super.onTouchEvent(event);
	}

	// Check touch event to update hotspot position for old api
	private void handleTouchEventForHotspotChanged(MotionEvent event) {
		final int actionMasked = event.getActionMasked();

		// Don't consume event in disabled state
		if (! isEnabled()) {
			if (isPrePress
				|| actionMasked == MotionEvent.ACTION_OUTSIDE
				|| actionMasked == MotionEvent.ACTION_CANCEL
				|| actionMasked == MotionEvent.ACTION_UP) {
				// Clear pre-press action
				isPrePress = false;
				removeCallbacks(prePressAction);
			}
			return;
		}

		final float x = event.getX();
		final float y = event.getY();
		final boolean clickable = isClickable();

		switch (actionMasked) {
			case MotionEvent.ACTION_DOWN: {
				// Only clickable view can perform press
				if (! clickable) {
					break;
				}
				// View in scrolling container should delay press action
				if (DkViews.isInScrollingContainer(this)) {
					isPrePress = true;

					PressAction prePressAction = this.prePressAction;
					if (prePressAction == null) {
						this.prePressAction = prePressAction = new PressAction();
					}
					prePressAction.x = x;
					prePressAction.y = y;
					postDelayed(prePressAction, ViewConfiguration.getTapTimeout());
				}
				// Otherwise, perform press immediately
				else {
					drawableHotspotChanged(x, y);
				}
				break;
			}
			case MotionEvent.ACTION_MOVE: {
				if (clickable) {
					 drawableHotspotChanged(x, y);
				}
				break;
			}
			case MotionEvent.ACTION_UP: {
				if (clickable && isPrePress) {
					isPrePress = false;
					removeCallbacks(prePressAction);

					 drawableHotspotChanged(x, y);
				}
				break;
			}
			case MotionEvent.ACTION_CANCEL: {
				if (isPrePress) {
					isPrePress = false;
					removeCallbacks(prePressAction);
				}
				break;
			}
		}
	}

	private class PressAction implements Runnable {
		float x;
		float y;

		@Override
		public void run() {
			isPrePress = false;
			drawableHotspotChanged(x, y);
		}
	}

	// region Get/Set

	public boolean isCreateDefaultForegroundIfNotExist() {
		return createDefaultForegroundIfNotExist;
	}

	public void setCreateDefaultForegroundIfNotExist(boolean createDefaultForegroundIfNotExist) {
		this.createDefaultForegroundIfNotExist = createDefaultForegroundIfNotExist;
	}

	// endregion Get/Set
}
