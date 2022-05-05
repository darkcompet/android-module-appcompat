/*
 * Copyright (c) 2017-2020 DarkCompet. All rights reserved.
 */

package tool.compet.appcompat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

import tool.compet.graphics.DkShadowDrawable;

/**
 * This use clip path, xfer...
 *
 * Note: Resource references will not work correctly in images
 * generated for this vector icon for API < 21.
 *
 * Ref: https://github.com/hdodenhof/CircleImageView
 */
public class DkCircleImageView extends AppCompatImageView {
	private Path circlePath;
	private DkShadowDrawable shadowDrawable;

	public DkCircleImageView(Context context) {
		super(context);
		init(context);
	}

	public DkCircleImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public DkCircleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	private void init(Context context) {
		setScaleType(ScaleType.CENTER_CROP);
		circlePath = new Path();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		int centerX = w >> 1; // inside this view
		int centerY = h >> 1; // inside this view
		int radius = Math.min(centerX, centerY);

		circlePath.reset(); // it is important to avoid multiple circle paths drawn
		circlePath.addCircle(centerX, centerY, radius, Path.Direction.CCW);

		super.onSizeChanged(w, h, oldw, oldh);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.clipPath(circlePath);

		super.onDraw(canvas);
	}
}
