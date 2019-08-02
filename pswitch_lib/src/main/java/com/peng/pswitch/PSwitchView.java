package com.peng.pswitch;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;


/**
 * Created by lipeng on 2019-07-31.
 */
public class PSwitchView extends View {

    private Paint mMainPaint;
    private Paint mDotPaint;
    private RectF mRectF;
    private RectF mRectFDot;
    private ValueAnimator mAnimator;
    private int mMeasuredWidth;
    private int mMeasuredHeight;
    private int mOutR;
    private float mInnerR;
    private int mDotMargin;
    private int mLimitLeftX;
    private int mLimitRightX;
    private int mOffColor;
    private int mOnColor;
    private float eventStartX;
    private int mDotColor;
    private float offsetX;
    private float colorRatio;
    private float mDotBeginX;
    private float mDotCenterX;
    private boolean isShapeCir = true;
    private boolean mSlidable = true; //false代表屏蔽滑动，不屏蔽点击。
    private boolean mChecked;
    private boolean mLastStatus;
    private SwitchCheckListener mCheckListener;
    private boolean isOnAttach;

    public PSwitchView(Context context) {
        this(context, null);
    }

    public PSwitchView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PSwitchView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs, defStyleAttr);
    }

    private void initView(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PSwitchView);
        mOnColor = a.getColor(R.styleable.PSwitchView_switch_open_color, ContextCompat.getColor(context, R.color.switch_open_color));
        mOffColor = a.getColor(R.styleable.PSwitchView_switch_close_color, ContextCompat.getColor(context, R.color.switch_close_color));
        mDotColor = a.getColor(R.styleable.PSwitchView_switch_dot_color, ContextCompat.getColor(context, R.color.switch_dot_color));
        mSlidable = a.getBoolean(R.styleable.PSwitchView_switch_slidable, true);
        mDotMargin = a.getDimensionPixelSize(R.styleable.PSwitchView_switch_dot_margin, DimensionUtil.dip2px(getContext(), 1.5f));
        mChecked = a.getBoolean(R.styleable.PSwitchView_switch_checked, false);
        int shapeValue = a.getInt(R.styleable.PSwitchView_switch_shape, 0);
        isShapeCir = shapeValue != 1;
        a.recycle();

        //初始化参数
        mMainPaint = new Paint();
        mMainPaint.setAntiAlias(true);
        mMainPaint.setStyle(Paint.Style.FILL);
        mMainPaint.setColor(mOffColor);
        mDotPaint = new Paint();
        mDotPaint.setAntiAlias(true);
        mDotPaint.setStyle(Paint.Style.FILL);
        mDotPaint.setColor(mDotColor);
        //开关外框区域
        mRectF = new RectF();
        //矩形开关内部块
        mRectFDot = new RectF();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = measureDimension(300, widthMeasureSpec);
        int height = measureDimension(80, heightMeasureSpec);
        if (width <= height) {
            width = height * 2;
        }
        setMeasuredDimension(width, height);
        initDraw();
    }

    private void initDraw() {
        mMeasuredWidth = getMeasuredWidth();
        mMeasuredHeight = getMeasuredHeight();
        mRectF.set(0, 0, mMeasuredWidth, mMeasuredHeight);
        mOutR = mMeasuredHeight / 2;
        mInnerR = mOutR - mDotMargin;
        mLimitLeftX = mOutR;
        mLimitRightX = mMeasuredWidth - mOutR;
        if (!mChecked) {
            mDotBeginX = mLimitLeftX;
            mDotCenterX = mLimitLeftX;
            colorRatio = 0;
        } else {
            mDotBeginX = mLimitRightX;
            mDotCenterX = mLimitRightX;
            colorRatio = 1;
        }
    }

    public int measureDimension(int defSize, int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = defSize;
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(defSize, specSize);
            }
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mMainPaint.setColor(getColor(colorRatio));
        if (isShapeCir) {
            canvas.drawRoundRect(mRectF, mOutR, mOutR, mMainPaint);
            canvas.drawCircle(mDotCenterX, mOutR, mInnerR, mDotPaint);
        } else {
            canvas.drawRect(mRectF, mMainPaint);
            mRectFDot.set(mDotCenterX - mInnerR, mMeasuredHeight / 2f - mInnerR, mDotCenterX + mInnerR, mMeasuredHeight / 2f + mInnerR);
            canvas.drawRect(mRectFDot, mDotPaint);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int actionMasked = event.getActionMasked();
        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN:
                mLastStatus = mChecked;
                eventStartX = event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                if (!mSlidable) {
                    return super.onTouchEvent(event);
                }
                offsetX = event.getX() - eventStartX;
                float tempX = offsetX + mDotBeginX;
                if (tempX < mLimitLeftX) {
                    tempX = mLimitLeftX;
                }
                if (tempX > mLimitRightX) {
                    tempX = mLimitRightX;
                }
                if (tempX >= mLimitLeftX && tempX <= mLimitRightX) {
                    mDotCenterX = tempX;
                    colorRatio = (mDotCenterX - mLimitLeftX) / (mLimitRightX - mLimitLeftX);
                    invalidateView();
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mDotBeginX = mDotCenterX;
                boolean isToRight = mDotBeginX > mMeasuredWidth / 2f;
                if (Math.abs(event.getX() - eventStartX) < 3) {
                    isToRight = !isToRight;
                }
                moveAnim(isToRight);
                break;
        }
        return true;
    }

    private void moveAnim(final boolean isToRight) {
        //滑块动画、背景色
        if (mAnimator != null && mAnimator.isRunning()) {
            //mAnimator.cancel();
            //mAnimator = null;
            return;
        }
        mAnimator = ValueAnimator.ofFloat(mDotCenterX, isToRight ? mLimitRightX : mLimitLeftX);
        mAnimator.setDuration(200);
        mAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mAnimator.start();
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mDotCenterX = (float) animation.getAnimatedValue();
                invalidateView();
                colorRatio = (mDotCenterX - mLimitLeftX) / (mLimitRightX - mLimitLeftX);
            }
        });
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mChecked = isToRight;
                mDotBeginX = isToRight ? mLimitRightX : mLimitLeftX;
                mAnimator.cancel();
                mAnimator = null;

                if (mCheckListener != null) {
                    if (mLastStatus != mChecked) {
                        mCheckListener.onCheckedChanged(mChecked);
                    }
                }
            }
        });
    }

    private int getColor(float radio) {
        int redStart = Color.red(mOffColor);
        int blueStart = Color.blue(mOffColor);
        int greenStart = Color.green(mOffColor);
        int redEnd = Color.red(mOnColor);
        int blueEnd = Color.blue(mOnColor);
        int greenEnd = Color.green(mOnColor);
        int red = (int) (redStart + ((redEnd - redStart) * radio + 0.5));
        int greed = (int) (greenStart + ((greenEnd - greenStart) * radio + 0.5));
        int blue = (int) (blueStart + ((blueEnd - blueStart) * radio + 0.5));
        return Color.argb(255, red, greed, blue);
    }

    private void invalidateView() {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            invalidate();
        } else {
            postInvalidate();
        }
    }

    public void toggle() {
        mLastStatus = isChecked();
        setChecked(!isChecked());
    }

    public void setChecked(boolean checked) {
        if (!isOnAttach) {
            mLastStatus = isChecked();
            mChecked = checked;
            initDraw();
            invalidateView();
            if (mCheckListener != null) {
                if (mLastStatus != mChecked) {
                    mCheckListener.onCheckedChanged(checked);
                }
            }
        } else {
            mLastStatus = isChecked();
            moveAnim(checked);
        }
    }

    public boolean isChecked() {
        return mChecked;
    }

    public interface SwitchCheckListener {
        void onCheckedChanged(boolean isChecked);
    }

    public void setOnSwitchCheckListener(SwitchCheckListener checkListener) {
        mCheckListener = checkListener;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isOnAttach = true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isOnAttach = false;
    }
}
