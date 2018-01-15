package com.xiaosu;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.view.ViewCompat;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * 作者：疏博文 创建于 2016-05-12 15:05
 * 邮箱：shubowen123@sina.cn
 * 描述：
 */
public class VerticalRollingTextView extends View {

    private DataSetAdapter mDataSetAdapter;

    private TextPaint mPaint;

    private int mCurrentIndex;
    private int mNextIndex;

    private float mScrollY;

    private InternalAnimation mAnimation = new InternalAnimation();

    /*防止动画结束的回调触发以后动画继续进行出现的错乱问题*/
    private boolean mAnimationEnded;

    private boolean isRunning;
    /*动画时间*/
    private int mDuration = 1000;
    /*动画间隔*/
    private int mAnimInterval = 2000;

    private SparseArray<Layout> mLayoutArr = new SparseArray<>();
    private int mTextColor;
    private int mTextSize;

    public VerticalRollingTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        parseAttrs(context, attrs);
    }

    private void parseAttrs(Context context, AttributeSet attrs) {
        float density = getResources().getDisplayMetrics().density;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.VerticalRollingTextView);
        mTextColor = typedArray.getColor(R.styleable.VerticalRollingTextView_android_textColor, Color.BLACK);
        mTextSize = typedArray.getDimensionPixelSize(R.styleable.VerticalRollingTextView_android_textSize, (int) (density * 14));
        mDuration = typedArray.getInt(R.styleable.VerticalRollingTextView_android_duration, mDuration);
        mAnimInterval = typedArray.getInt(R.styleable.VerticalRollingTextView_animInterval, mAnimInterval);

        typedArray.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 绘制文本
        if (mDataSetAdapter == null || mDataSetAdapter.isEmpty()) {
            return;
        }

        Layout cLayout = findLayoutByIndex(mCurrentIndex);
        Layout nLayout = findLayoutByIndex(mNextIndex);

        float anchor = (getHeight() - cLayout.getHeight()) * 0.5f - mScrollY;

        canvas.save();
        canvas.translate(0, anchor);
        cLayout.draw(canvas);
        canvas.restore();

        canvas.save();
        canvas.translate(0, anchor + getHeight());
        nLayout.draw(canvas);
        canvas.restore();
    }

    private Layout findLayoutByIndex(int index) {

        Layout layout = mLayoutArr.get(index);
        if (null != layout) {
            return layout;
        }

        CharSequence cs = mDataSetAdapter.getText(index);

        if (null == mPaint) {
            mPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setColor(mTextColor);
            mPaint.setTextSize(mTextSize);
        }

        layout = new StaticLayout(
                cs,
                mPaint,
                getWidth(),
                Layout.Alignment.ALIGN_NORMAL,
                1.0f,
                0.0f,
                false);

        mLayoutArr.put(index, layout);

        return layout;
    }

    public void setDataSetAdapter(DataSetAdapter dataSetAdapter) {
        mDataSetAdapter = dataSetAdapter;
        mCurrentIndex = 0;
        confirmNextIndex();
        invalidate();
    }

    /**
     * 开始转动,界面可见的时候调用
     */
    public void run() {
        if (isRunning) {
            return;
        }

        isRunning = true;
        mAnimation.setDuration(mDuration);
        if (ViewCompat.isLaidOut(this)) {
            mAnimation.updateValue(0, getHeight());
            postDelayed(mRollingTask, mDuration);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (isRunning) {
            removeCallbacks(mRollingTask);

            mAnimation.updateValue(0, h);
            postDelayed(mRollingTask, mDuration);
        }
    }

    /**
     * @return true代表正在转动
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * 停止转动,界面不可见的时候自动调用
     */
    public void stop() {
        isRunning = false;
        removeCallbacks(mRollingTask);
    }

    Runnable mRollingTask = new Runnable() {
        @Override
        public void run() {
            mAnimationEnded = false;
            startAnimation(mAnimation);
            postDelayed(this, mAnimInterval);
        }
    };


    private void animationEnd() {
        //1.角标+1
        mCurrentIndex++;
        //2.计算出正确的角标
        mCurrentIndex = mCurrentIndex < mDataSetAdapter.getItemCount() ? mCurrentIndex : mCurrentIndex % mDataSetAdapter.getItemCount();
        //3.计算下一个待显示文字角标
        confirmNextIndex();
        //3.位置复位
        mScrollY = 0;
        mAnimationEnded = true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(mRollingTask);
        if (isRunning()) {
            mAnimation.cancel();
        }
    }

    public void setCurrentIndex(int currentIndex) {
        mCurrentIndex = currentIndex;
    }

    public void setTextColor(int textColor) {
        mTextColor = textColor;
    }

    public void setTextSize(int textSize) {
        mTextSize = textSize;
    }

    public void setDuration(int duration) {
        mDuration = duration;
        if (isRunning()) {
            mAnimation.setDuration(duration);
        }
    }

    public void setAnimInterval(int animInterval) {
        mAnimInterval = animInterval;
    }

    /**
     * 计算第二个角标
     */
    private void confirmNextIndex() {
        //3.计算第二个角标
        mNextIndex = mCurrentIndex + 1;
        //4.计算出正确的第二个角标
        mNextIndex = mNextIndex < mDataSetAdapter.getItemCount() ? mNextIndex : 0;
    }

    /**
     * float估值器
     *
     * @param fraction
     * @param startValue
     * @param endValue
     * @return
     */
    private float evaluate(float fraction, float startValue, float endValue) {
        return startValue + fraction * (endValue - startValue);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        throw new UnsupportedOperationException();
    }

    public void setOnItemClickListener(final OnItemClickListener onItemClickListener) {
        super.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(VerticalRollingTextView.this, mCurrentIndex);
            }
        });
    }

    public interface OnItemClickListener {
        void onItemClick(VerticalRollingTextView view, int index);
    }

    private class InternalAnimation extends Animation {

        float startValue;
        float endValue;

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            if (mAnimationEnded) return;

            mScrollY = evaluate(interpolatedTime, startValue, endValue);
            if (mScrollY == endValue) {
                animationEnd();
            }
            postInvalidate();
        }

        void updateValue(float startValue, float endValue) {
            this.startValue = startValue;
            this.endValue = endValue;
        }

    }

}
