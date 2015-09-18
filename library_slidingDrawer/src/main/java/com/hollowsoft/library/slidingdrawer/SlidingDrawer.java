/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.hollowsoft.library.slidingdrawer;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

/**
 * SlidingDrawer hides content out of the screen and allows the user to drag a
 * handle to bring the content on screen. SlidingDrawer can be used vertically
 * or horizontally.
 *
 * A special widget composed of two children views: the handle, that the users
 * drags, and the content, attached to the handle and dragged with it.
 *
 * SlidingDrawer should be used as an overlay inside layouts. This means
 * SlidingDrawer should only be used inside of a FrameLayout or a RelativeLayout
 * for instance. The size of the SlidingDrawer defines how much space the
 * content will occupy once slid out so SlidingDrawer should usually use
 * match_parent for both its dimensions.
 *
 * Inside an XML layout, SlidingDrawer must define the id of the handle and of
 * the content:
 *
 * <pre class="prettyprint">
 * &lt;com.hollowsoft.library.slidingdrawer.SlidingDrawer
 *     android:id="@+id/drawer"
 *     android:layout_width="match_parent"
 *     android:layout_height="match_parent"
 *
 *     android:handle="@+id/handle"
 *     android:content="@+id/content"&gt;
 *
 *     &lt;ImageView
 *         android:id="@id/handle"
 *         android:layout_width="88dip"
 *         android:layout_height="44dip" /&gt;
 *
 *     &lt;GridView
 *         android:id="@id/content"
 *         android:layout_width="match_parent"
 *         android:layout_height="match_parent" /&gt;
 *
 * &lt;/com.hollowsoft.library.slidingdrawer.SlidingDrawer&gt;
 * </pre>
 *
 * @attr ref R.styleable#SlidingDrawer_animateOnClick
 * @attr ref R.styleable#SlidingDrawer_allowSingleTap
 * @attr ref R.styleable#SlidingDrawer_topOffset
 * @attr ref R.styleable#SlidingDrawer_bottomOffset
 * @attr ref R.styleable#SlidingDrawer_orientation
 * @attr ref R.styleable#SlidingDrawer_handle
 * @attr ref R.styleable#SlidingDrawer_content
 *
 * <p> This class has ported and improved from the Android Open Source Project.
 *
 * @see <a href="http://http://developer.android.com/reference/android/widget/SlidingDrawer.html">
 *      SlidingDrawer</a>
 *
 * @author Igor Morais
 * @author mor41s.1gor@gmail.com
 */
public class SlidingDrawer extends ViewGroup {

	/**
	 *
	 */
	private static final int TAP_THRESHOLD = 6;

	/**
	 *
	 */
	private static final int VELOCITY_UNITS = 1000;

	/**
	 *
	 */
	private static final int ANIMATION_FRAME_DURATION = 1000 / 60;

	/**
	 *
	 */
	private static final float MAXIMUM_ACCELERATION = 2000.0f;

	/**
	 *
	 */
	private static final float MAXIMUM_MINOR_VELOCITY = 150.0f;
	private static final float MAXIMUM_MAJOR_VELOCITY = 200.0f;
	private static final float MAXIMUM_TAP_VELOCITY = 100.0f;

	/**
	 *
	 */
	private static final int MESSAGE_ANIMATE = 1000;

	/**
	 *
	 */
	private static final int DRAWER_EXPANDED = -10001;
	private static final int DRAWER_COLLAPSED = -10002;

	/**
	 *
	 */
	public static final int ORIENTATION_HORIZONTAL = 0;
	public static final int ORIENTATION_VERTICAL = 1;

	/**
	 *
	 */
	private final int tapThreshold;
	private final int velocityUnits;

	/**
	 *
	 */
	private final int maximumAcceleration;

	/**
	 *
	 */
	private final int maximumMinorVelocity;
	private final int maximumMajorVelocity;
	private final int maximumTapVelocity;

	/**
	 *
	 */
	private VelocityTracker velocityTracker;

	/**
	 *
	 */
	private float animatedAcceleration;
	private float animatedVelocity;
	private float animationPosition;

	/**
	 *
	 */
	private int touchDelta;
	private long animationLastTime;
	private long currentAnimationTime;

	/**
	 *
	 */
	private final Rect invalidateRect = new Rect();
	private final Rect frameRect = new Rect();

	/**
	 *
	 */
	private final Handler drawerHandler = new DrawerHandler();

	/**
	 * Styleables.
	 */
	private final boolean animateOnClick;
	private final boolean allowSingleTap;
	private final int topOffset;
	private final int bottomOffset;
	private final boolean isVertical;
	private final int handleResId;
	private final int contentResId;

	/**
	 *
	 */
	private int handleWidth;
	private int handleHeight;

	/**
	 *
	 */
	private boolean isExpanded;
	private boolean isTracking;
	private boolean isAnimating;
	private boolean isLocked;

	/**
	 *
	 */
	private View handleView;
	private View contentView;

	/**
	 * Listeners.
	 */
	private OnDrawerOpenListener onDrawerOpenListener;
	private OnDrawerCloseListener onDrawerCloseListener;
	private OnDrawerScrollListener onDrawerScrollListener;

	/**
	 * Creates a new SlidingDrawer from a specified set of attributes defined in
	 * XML.
	 *
	 * @param context
	 *            The application's environment.
	 * @param attributeSet
	 *            The attributes defined in XML.
	 */
	public SlidingDrawer(final Context context, final AttributeSet attributeSet) {
		this(context, attributeSet, 0);
	}

	/**
	 * Creates a new SlidingDrawer from a specified set of attributes defined in
	 * XML.
	 *
	 * @param context
	 *            The application's environment.
	 * @param attributeSet
	 *            The attributes defined in XML.
	 * @param defStyle
	 *            The style to apply to this widget.
	 */
	public SlidingDrawer(final Context context, final AttributeSet attributeSet, final int defStyle) {
		super(context, attributeSet, defStyle);

		final TypedArray typedArray = context.obtainStyledAttributes(attributeSet,
				R.styleable.SlidingDrawer, defStyle, 0);

		animateOnClick = typedArray.getBoolean(R.styleable.SlidingDrawer_animateOnClick, true);
		allowSingleTap = typedArray.getBoolean(R.styleable.SlidingDrawer_allowSingleTap, true);
		topOffset = (int) typedArray.getDimension(R.styleable.SlidingDrawer_topOffset, 0.0f);
		bottomOffset = (int) typedArray.getDimension(R.styleable.SlidingDrawer_bottomOffset, 0.0f);

		final int orientation = typedArray.getInt(R.styleable.SlidingDrawer_orientation,
				ORIENTATION_VERTICAL);

		isVertical = orientation == ORIENTATION_VERTICAL;

		handleResId = typedArray.getResourceId(R.styleable.SlidingDrawer_handle, Integer.MIN_VALUE);
		if (handleResId == Integer.MIN_VALUE) {
			throw new IllegalArgumentException(
					"The handle attribute is required and must refer to a valid child.");
		}

		contentResId = typedArray.getResourceId(R.styleable.SlidingDrawer_content, Integer.MIN_VALUE);
		if (contentResId == Integer.MIN_VALUE) {
			throw new IllegalArgumentException(
					"The content attribute is required and must refer to a valid child.");
		}

		if (handleResId == contentResId) {
			throw new IllegalArgumentException(
					"The content and handle attributes must refer to different children.");
		}

		typedArray.recycle();

		final float density = getResources().getDisplayMetrics().density;
		tapThreshold = (int) (TAP_THRESHOLD * density + 0.5f);
		velocityUnits = (int) (VELOCITY_UNITS * density + 0.5f);

		maximumAcceleration = (int) (MAXIMUM_ACCELERATION * density + 0.5f);

		maximumMinorVelocity = (int) (MAXIMUM_MINOR_VELOCITY * density + 0.5f);
		maximumMajorVelocity = (int) (MAXIMUM_MAJOR_VELOCITY * density + 0.5f);
		maximumTapVelocity = (int) (MAXIMUM_TAP_VELOCITY * density + 0.5f);

		setAlwaysDrawnWithCacheEnabled(false);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see android.view.View#onFinishInflate()
	 */
	@Override
	protected void onFinishInflate() {

		handleView = findViewById(handleResId);
		if (handleView == null) {
			throw new IllegalArgumentException(
					"The handle attribute is must refer to an existing child.");
		}

		contentView = findViewById(contentResId);
		if (contentView == null) {
			throw new IllegalArgumentException(
					"The content attribute is must refer to an existing child.");
		}

		handleView.setOnClickListener(new DrawerToggler());

		contentView.setVisibility(View.GONE);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see android.view.View#onMeasure(int, int)
	 */
	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
		final int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
		final int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);

		final int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
		final int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

		if (widthSpecMode == MeasureSpec.UNSPECIFIED || heightSpecMode == MeasureSpec.UNSPECIFIED) {
			throw new IllegalStateException("The SlidingDrawer cannot have unspecified dimensions.");
		}

		measureChild(handleView, widthMeasureSpec, heightMeasureSpec);

		if (isVertical) {

			final int height = heightSpecSize - handleView.getMeasuredHeight() - topOffset;
			contentView.measure(MeasureSpec.makeMeasureSpec(widthSpecSize, MeasureSpec.EXACTLY),
					MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));

		} else {

			final int width = widthSpecSize - handleView.getMeasuredWidth() - topOffset;
			contentView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
					MeasureSpec.makeMeasureSpec(heightSpecSize, MeasureSpec.EXACTLY));
		}

		setMeasuredDimension(widthSpecSize, heightSpecSize);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see android.view.ViewGroup#onLayout(boolean, int, int, int, int)
	 */
	@Override
	protected void onLayout(final boolean changed, final int left, final int top, final int right,
			final int bottom) {

		if (!isTracking) {

			final int width = right - left;
			final int height = bottom - top;

			final int handleMeasuredWidth = handleView.getMeasuredWidth();
			final int handleMeasuredHeight = handleView.getMeasuredHeight();

			final int handleLeft;
			final int handleTop;

			if (isVertical) {

				handleLeft = (width - handleMeasuredWidth) - ((width - handleMeasuredWidth) / 3); // GUDNAM
				handleTop = isExpanded ? topOffset : height - handleMeasuredHeight + bottomOffset;

				try {
					contentView.layout(0, topOffset + handleMeasuredHeight, contentView.getMeasuredWidth(),
							topOffset + handleMeasuredHeight + contentView.getMeasuredHeight());
				} catch (IndexOutOfBoundsException ioobe){
					ioobe.printStackTrace();
				}

			} else {

				handleLeft = isExpanded ? topOffset : width - handleMeasuredWidth + bottomOffset;
				handleTop = (height - handleMeasuredHeight) / 2;

				contentView.layout(topOffset + handleMeasuredWidth, 0, topOffset + handleMeasuredWidth
						+ contentView.getMeasuredWidth(), contentView.getMeasuredHeight());
			}

			handleView.layout(handleLeft, handleTop, handleLeft + handleMeasuredWidth, handleTop
					+ handleMeasuredHeight);

			handleWidth = handleView.getWidth();
			handleHeight = handleView.getHeight();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see android.view.ViewGroup#dispatchDraw(android.graphics.Canvas)
	 */
	@Override
	protected void dispatchDraw(final Canvas canvas) {
		final long drawingTime = getDrawingTime();

		drawChild(canvas, handleView, drawingTime);

		if (isTracking || isAnimating) {
			final Bitmap bitmap = contentView.getDrawingCache();

			if (bitmap == null) {
				canvas.save();

				canvas.translate(isVertical ? 0 : handleView.getLeft() - topOffset,
						isVertical ? handleView.getTop() - topOffset : 0);

				drawChild(canvas, contentView, drawingTime);

				canvas.restore();

			} else {

				if (isVertical) {
					canvas.drawBitmap(bitmap, 0, handleView.getBottom(), null);

				} else {
					canvas.drawBitmap(bitmap, handleView.getRight(), 0, null);
				}
			}

		} else if (isExpanded) {
			drawChild(canvas, contentView, drawingTime);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * android.view.ViewGroup#onInterceptTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onInterceptTouchEvent(final MotionEvent event) {
		if (isLocked) {
			return false;
		}

		final float x = event.getX();
		final float y = event.getY();

		handleView.getHitRect(frameRect);
		if (!isTracking && !frameRect.contains((int) x, (int) y)) {
			return false;
		}

		final int action = event.getAction();
		if (action == MotionEvent.ACTION_DOWN) {
			isTracking = true;

			handleView.setPressed(true);

			prepareContent();

			if (onDrawerScrollListener != null) {
				onDrawerScrollListener.onScrollStarted();
			}

			if (isVertical) {
				final int top = handleView.getTop();
				touchDelta = (int) y - top;

				prepareTracking(top);

			} else {
				final int left = handleView.getLeft();
				touchDelta = (int) x - left;

				prepareTracking(left);
			}

			velocityTracker.addMovement(event);
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
	 */
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		if (isLocked) {
			return true;
		}

		if (isTracking) {
			velocityTracker.addMovement(event);

			final int action = event.getAction();
			switch (action) {

			case MotionEvent.ACTION_MOVE:
				moveHandle((int) (isVertical ? event.getY() : event.getX()) - touchDelta);

				break;

			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				velocityTracker.computeCurrentVelocity(velocityUnits);

				boolean isNegative;

				float xVelocity = velocityTracker.getXVelocity();
				float yVelocity = velocityTracker.getYVelocity();

				if (isVertical) {
					isNegative = yVelocity < 0;

					if (xVelocity < 0) {
						xVelocity = -xVelocity;
					}

					if (xVelocity > maximumMinorVelocity) {
						xVelocity = maximumMinorVelocity;
					}

				} else {
					isNegative = xVelocity < 0;

					if (yVelocity < 0) {
						yVelocity = -yVelocity;
					}

					if (yVelocity > maximumMinorVelocity) {
						yVelocity = maximumMinorVelocity;
					}
				}

				float velocity = (float) Math.hypot(xVelocity, yVelocity);
				if (isNegative) {
					velocity = -velocity;
				}

				final int left = handleView.getLeft();
				final int top = handleView.getTop();

				if (Math.abs(velocity) < maximumTapVelocity) {

					if (isVertical ? (isExpanded && top < tapThreshold + topOffset)
							|| (!isExpanded && top > bottomOffset + getBottom() - getTop() - handleHeight - tapThreshold)
							: (isExpanded && left < tapThreshold + topOffset)
							|| (!isExpanded && left > bottomOffset + getRight() - getLeft() - handleWidth - tapThreshold)) {

						if (allowSingleTap) {
							playSoundEffect(SoundEffectConstants.CLICK);

							if (isExpanded) {
								animateClose(isVertical ? top : left);

							} else {
								animateOpen(isVertical ? top : left);
							}

						} else {
							performFling(isVertical ? top : left, velocity, false);
						}

					} else {
						performFling(isVertical ? top : left, velocity, false);
					}

				} else {
					performFling(isVertical ? top : left, velocity, false);
				}

				break;

			default:
				// Don't need to be implemented.
				break;
			}
		}

		return isTracking || isAnimating || super.onTouchEvent(event);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see android.view.View#onInitializeAccessibilityNodeInfo(android.view.
	 * accessibility.AccessibilityNodeInfo)
	 */
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	public void onInitializeAccessibilityNodeInfo(final AccessibilityNodeInfo nodeInfo) {
		super.onInitializeAccessibilityNodeInfo(nodeInfo);

		nodeInfo.setClassName(SlidingDrawer.class.getName());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * android.view.View#onInitializeAccessibilityEvent(android.view.accessibility
	 * .AccessibilityEvent)
	 */
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	public void onInitializeAccessibilityEvent(final AccessibilityEvent event) {
		super.onInitializeAccessibilityEvent(event);

		event.setClassName(SlidingDrawer.class.getName());
	}

	/**
	 *
	 */
	private void openDrawer() {
		moveHandle(DRAWER_EXPANDED);

		contentView.setVisibility(View.VISIBLE);

		if (!isExpanded) {
			isExpanded = true;

			if (onDrawerOpenListener != null) {
				onDrawerOpenListener.onDrawerOpened();
			}
		}
	}

	/**
	 *
	 */
	private void closeDrawer() {
		moveHandle(DRAWER_COLLAPSED);

		contentView.setVisibility(View.GONE);
		contentView.destroyDrawingCache();

		if (isExpanded) {
			isExpanded = false;

			if (onDrawerCloseListener != null) {
				onDrawerCloseListener.onDrawerClosed();
			}
		}
	}

	/**
	 *
	 * @param position
	 */
	private void animateOpen(final int position) {
		prepareTracking(position);
		performFling(position, -maximumAcceleration, true);
	}

	/**
	 *
	 * @param position
	 */
	private void animateClose(final int position) {
		prepareTracking(position);
		performFling(position, maximumAcceleration, true);
	}

	/**
	 *
	 */
	private void prepareContent() {
		if (!isAnimating) {

			if (contentView.isLayoutRequested()) {

				if (isVertical) {
					final int height = getBottom() - getTop() - handleHeight - topOffset;

					contentView.measure(
							MeasureSpec.makeMeasureSpec(getRight() - getLeft(), MeasureSpec.EXACTLY),
							MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));

					contentView.layout(0, topOffset + handleHeight, contentView.getMeasuredWidth(),
							topOffset + handleHeight + contentView.getMeasuredHeight());

				} else {

					final int handleWidth = handleView.getWidth();
					final int width = getRight() - getLeft() - handleWidth - topOffset;

					contentView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
							MeasureSpec.makeMeasureSpec(getBottom() - getTop(), MeasureSpec.EXACTLY));

					contentView.layout(handleWidth + topOffset, 0,
							topOffset + handleWidth + contentView.getMeasuredWidth(),
							contentView.getMeasuredHeight());
				}
			}

			contentView.getViewTreeObserver().dispatchOnPreDraw();

			if (!contentView.isHardwareAccelerated()) {
				contentView.buildDrawingCache();
			}

			contentView.setVisibility(View.GONE);
		}
	}

	/**
	 *
	 * @param position
	 */
	private void prepareTracking(final int position) {
		isTracking = true;
		velocityTracker = VelocityTracker.obtain();

		if (isExpanded) {

			if (isAnimating) {
				isAnimating = false;
				drawerHandler.removeMessages(MESSAGE_ANIMATE);
			}

			moveHandle(position);

		} else {
			isAnimating = true;

			animatedAcceleration = maximumAcceleration;
			animatedVelocity = maximumMajorVelocity;

			animationPosition = bottomOffset
					+ (isVertical ? getHeight()
							- handleHeight : getWidth() - handleWidth);

			moveHandle((int) animationPosition);

			drawerHandler.removeMessages(MESSAGE_ANIMATE);

			animationLastTime = SystemClock.uptimeMillis();
			currentAnimationTime = animationLastTime + ANIMATION_FRAME_DURATION;
		}
	}

	/**
	 *
	 */
	private void stopTracking() {
		isTracking = false;

		handleView.setPressed(false);

		if (onDrawerScrollListener != null) {
			onDrawerScrollListener.onScrollEnded();
		}

		if (velocityTracker != null) {
			velocityTracker.recycle();
		}
	}

	/**
	 *
	 */
	private void incrementAnimation() {
		final long currentTime = SystemClock.uptimeMillis();
		final float time = (currentTime - animationLastTime) / 1000.0f;

		animatedVelocity = animatedVelocity + (animatedAcceleration * time);

		animationPosition = animationPosition + (animatedVelocity * time)
				+ (0.5f * animatedAcceleration * time * time);

		animationLastTime = currentTime;
	}

	/**
	 *
	 */
	private void doAnimation() {

		if (isAnimating) {
			incrementAnimation();

			if (animationPosition >= bottomOffset + (isVertical ? getHeight() : getWidth()) - 1) {
				isAnimating = false;
				closeDrawer();

			} else if (animationPosition < topOffset) {
				isAnimating = false;
				openDrawer();

			} else {
				moveHandle((int) animationPosition);

				currentAnimationTime += ANIMATION_FRAME_DURATION;

				drawerHandler.sendMessageAtTime(drawerHandler.obtainMessage(MESSAGE_ANIMATE),
						currentAnimationTime);
			}
		}
	}

	/**
	 *
	 * @param position
	 * @param velocity
	 * @param always
	 */
	private void performFling(final int position, final float velocity, final boolean always) {
		animatedVelocity = velocity;
		animationPosition = position;

		if (isExpanded) {

			if (always || (velocity > maximumMajorVelocity
					|| (position > topOffset + (isVertical ? handleHeight : handleWidth)
							&& velocity > -maximumMajorVelocity))) {

				animatedAcceleration = maximumAcceleration;
				if (velocity < 0) {
					animatedVelocity = 0;
				}

			} else {

				animatedAcceleration = -maximumAcceleration;
				if (velocity > 0) {
					animatedVelocity = 0;
				}
			}

		} else {

			if (!always && (velocity > maximumMajorVelocity
					|| (position > (isVertical ? getHeight()
							: getWidth()) / 2 && velocity > -maximumMajorVelocity))) {

				animatedAcceleration = maximumAcceleration;
				if (velocity < 0) {
					animatedVelocity = 0;
				}

			} else {

				animatedAcceleration = -maximumAcceleration;
				if (velocity > 0) {
					animatedVelocity = 0;
				}
			}
		}

		isAnimating = true;

		animationLastTime = SystemClock.uptimeMillis();
		currentAnimationTime = animationLastTime + ANIMATION_FRAME_DURATION;

		drawerHandler.removeMessages(MESSAGE_ANIMATE);
		drawerHandler.sendMessageAtTime(drawerHandler.obtainMessage(MESSAGE_ANIMATE),
				currentAnimationTime);

		stopTracking();
	}

	/**
	 *
	 * @param position
	 */
	private void moveHandle(final int position) {

		if (isVertical) {

			if (position == DRAWER_EXPANDED) {
				handleView.offsetTopAndBottom(topOffset - handleView.getTop());
				invalidate();

			} else if (position == DRAWER_COLLAPSED) {
				handleView.offsetTopAndBottom(bottomOffset + getBottom() - getTop() - handleHeight
						- handleView.getTop());

				invalidate();

			} else {

				final int top = handleView.getTop();
				int deltaY = position - top;

				if (position < topOffset) {
					deltaY = topOffset - top;

				} else if (deltaY > bottomOffset + getBottom() - getTop() - handleHeight - top) {
					deltaY = bottomOffset + getBottom() - getTop() - handleHeight - top;
				}

				handleView.offsetTopAndBottom(deltaY);
				handleView.getHitRect(frameRect);

				invalidateRect.set(frameRect);

				invalidateRect.union(frameRect.left, frameRect.top - deltaY, frameRect.right,
						frameRect.bottom - deltaY);

				invalidateRect.union(0, frameRect.bottom - deltaY, getWidth(), frameRect.bottom - deltaY
						+ contentView.getHeight());

				invalidate(invalidateRect);
			}

		} else {

			if (position == DRAWER_EXPANDED) {
				handleView.offsetLeftAndRight(topOffset - handleView.getLeft());
				invalidate();

			} else if (position == DRAWER_COLLAPSED) {
				handleView.offsetLeftAndRight(bottomOffset + getRight() - getLeft() - handleWidth
						- handleView.getLeft());

				invalidate();

			} else {

				final int left = handleView.getLeft();
				int deltaX = position - left;

				if (position < topOffset) {
					deltaX = topOffset - left;

				} else if (deltaX > bottomOffset + getRight() - getLeft() - handleWidth - left) {
					deltaX = bottomOffset + getRight() - getLeft() - handleWidth - left;
				}

				handleView.offsetLeftAndRight(deltaX);
				handleView.getHitRect(frameRect);

				invalidateRect.set(frameRect);

				invalidateRect.union(frameRect.left - deltaX, frameRect.top, frameRect.right - deltaX,
						frameRect.bottom);

				invalidateRect.union(frameRect.right - deltaX, 0,
						frameRect.right - deltaX + contentView.getWidth(), getHeight());

				invalidate(invalidateRect);
			}
		}
	}

	/**
	 * @author Igor Morais
	 * @author Mor41s.1gor@gmail.com
	 */
	private class DrawerToggler implements OnClickListener {

		/*
		 * (non-Javadoc)
		 *
		 * @see android.view.View.OnClickListener#onClick(android.view.View)
		 */
		@Override
		public void onClick(final View view) {
			if (!isLocked) {

				if (animateOnClick) {
					animateToggle();

				} else {
					toggle();
				}
			}
		}
	}

	/**
	 * @author Igor Morais
	 * @author Mor41s.1gor@gmail.com
	 */
	@SuppressLint("HandlerLeak")
	private class DrawerHandler extends Handler {

		/*
		 * (non-Javadoc)
		 *
		 * @see android.os.Handler#handleMessage(android.os.Message)
		 */
		@Override
		public void handleMessage(final Message message) {

			if (message.what == MESSAGE_ANIMATE) {
				doAnimation();
			}
		}
	}

	/**
	 * Sets the listener that receives a notification when the drawer becomes
	 * open.
	 *
	 * @param onDrawerOpenListener
	 *            The listener to be notified when the drawer is opened.
	 */
	public final void setOnDrawerOpenListener(final OnDrawerOpenListener onDrawerOpenListener) {
		this.onDrawerOpenListener = onDrawerOpenListener;
	}

	/**
	 * Sets the listener that receives a notification when the drawer becomes
	 * close.
	 *
	 * @param onDrawerCloseListener
	 *            The listener to be notified when the drawer is closed.
	 */
	public final void setOnDrawerCloseListener(final OnDrawerCloseListener onDrawerCloseListener) {
		this.onDrawerCloseListener = onDrawerCloseListener;
	}

	/**
	 * Sets the listener that receives a notification when the drawer starts or
	 * ends a scroll. A fling is considered as a scroll. A fling will also
	 * trigger a drawer opened or drawer closed event.
	 *
	 * @param onDrawerScrollListener
	 *            The listener to be notified when scrolling starts or stops.
	 */
	public final void setOnDrawerScrollListener(final OnDrawerScrollListener onDrawerScrollListener) {
		this.onDrawerScrollListener = onDrawerScrollListener;
	}

	/**
	 * Returns the handle of the drawer.
	 *
	 * @return The View reprenseting the handle of the drawer, identified by the
	 *         "handle" id in XML.
	 */
	public final View getHandle() {
		return handleView;
	}

	/**
	 * Returns the content of the drawer.
	 *
	 * @return The View reprenseting the content of the drawer, identified by
	 *         the "content" id in XML.
	 */
	public final View getContent() {
		return contentView;
	}

	/**
	 * Indicates whether the drawer is currently fully opened.
	 *
	 * @return True if the drawer is opened, false otherwise.
	 */
	public final boolean isOpened() {
		return isExpanded;
	}

	/**
	 * Indicates whether the drawer is scrolling or flinging.
	 *
	 * @return True if the drawer is scroller or flinging, false otherwise.
	 */
	public final boolean isMoving() {
		return isTracking || isAnimating;
	}

	/**
	 * Indicates whether the drawer is currently locked.
	 *
	 * @return True if the drawer is locked, false otherwise.
	 */
	public final boolean isLocked() {
		return isLocked;
	}

	/**
	 * Locks the SlidingDrawer so that touch events are ignores.
	 *
	 * @see #unlock()
	 */
	public final void lock() {
		isLocked = true;
	}

	/**
	 * Unlocks the SlidingDrawer so that touch events are processed.
	 *
	 * @see #lock()
	 */
	public final void unlock() {
		isLocked = false;
	}

	/**
	 * Opens the drawer immediately.
	 *
	 * @see #close()
	 * @see #toggle()
	 * @see #animateOpen()
	 */
	public final void open() {
		openDrawer();

		invalidate();
		requestLayout();

		sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
	}

	/**
	 * Closes the drawer immediately.
	 *
	 * @see #open()
	 * @see #toggle()
	 * @see #animateClose()
	 */
	public final void close() {
		closeDrawer();

		invalidate();
		requestLayout();
	}

	/**
	 * Toggles the drawer open and close. Takes effect immediately.
	 *
	 * @see #open()
	 * @see #close()
	 * @see #animateOpen()
	 * @see #animateClose()
	 * @see #animateToggle()
	 */
	public final void toggle() {

		if (isExpanded) {
			closeDrawer();

		} else {
			openDrawer();
		}

		invalidate();
		requestLayout();
	}

	/**
	 * Opens the drawer with an animation.
	 *
	 * @see #open()
	 * @see #close()
	 * @see #toggle()
	 * @see #animateClose()
	 * @see #animateToggle()
	 */
	public final void animateOpen() {
		prepareContent();

		if (onDrawerScrollListener != null) {
			onDrawerScrollListener.onScrollStarted();
		}

		animateOpen(isVertical ? handleView.getTop() : handleView.getLeft());

		sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);

		if (onDrawerScrollListener != null) {
			onDrawerScrollListener.onScrollEnded();
		}
	}

	/**
	 * Closes the drawer with an animation.
	 *
	 * @see #open()
	 * @see #close()
	 * @see #toggle()
	 * @see #animateOpen()
	 * @see #animateToggle()
	 */
	public final void animateClose() {
		prepareContent();

		if (onDrawerScrollListener != null) {
			onDrawerScrollListener.onScrollStarted();
		}

		animateClose(isVertical ? handleView.getTop() : handleView.getLeft());

		if (onDrawerScrollListener != null) {
			onDrawerScrollListener.onScrollEnded();
		}
	}

	/**
	 * Toggles the drawer open and close with an animation.
	 *
	 * @see #open()
	 * @see #close()
	 * @see #toggle()
	 * @see #animateOpen()
	 * @see #animateClose()
	 */
	public final void animateToggle() {

		if (isExpanded) {
			animateClose();

		} else {
			animateOpen();
		}
	}
}
