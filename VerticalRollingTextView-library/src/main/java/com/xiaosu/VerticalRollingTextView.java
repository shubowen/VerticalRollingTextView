package com.xiaosu;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * 作者：疏博文 创建于 2016-05-12 15:05
 * 邮箱：shubowen123@sina.cn
 * 描述：
 */
public class VerticalRollingTextView extends View {

    DataSetAdapter mDataSetAdapter;

    private final Paint mPaint;

    private int mCurrentIndex;
    private int mNextIndex;

    Rect bounds = new Rect();

    private float mCurrentOffsetY;

    private float mOrgOffsetY = -1;

    private final float mTextTopToAscentOffset;
    private float mOffset;

    private Animation mAnimation;

    /*防止动画结束的回调触发以后动画继续进行出现的错乱问题*/
    private boolean mAnimationEnded;

    public VerticalRollingTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLACK);
        mPaint.setTypeface(Typeface.DEFAULT);

        float density = getResources().getDisplayMetrics().density;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.VerticalRollingTextView);
        mPaint.setColor(typedArray.getColor(R.styleable.VerticalRollingTextView_text_color, Color.BLACK));
        mPaint.setTextSize(typedArray.getDimensionPixelOffset(R.styleable.VerticalRollingTextView_android_textSize, (int) (density * 14)));
        typedArray.recycle();

        Paint.FontMetricsInt metricsInt = mPaint.getFontMetricsInt();
        mTextTopToAscentOffset = metricsInt.ascent - metricsInt.top;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 绘制文本
        if (mDataSetAdapter == null || mDataSetAdapter.isEmpty()) {
            return;
        }

        String text1 = mDataSetAdapter.getText(mCurrentIndex);
        String text2 = mDataSetAdapter.getText(mNextIndex);

        //只需要进行一次测量
        if (mOrgOffsetY == -1) {
            mPaint.getTextBounds(text1, 0, text1.length(), bounds);
            mOffset = (getHeight() + bounds.height()) * 0.5f;
            mOrgOffsetY = mCurrentOffsetY = mOffset - mTextTopToAscentOffset;
        }

        canvas.drawText(text1, 0, mCurrentOffsetY, mPaint);
        canvas.drawText(text2, 0, mCurrentOffsetY + mOffset + mTextTopToAscentOffset, mPaint);
    }

    public void setDataSetAdapter(DataSetAdapter dataSetAdapter) {
        mDataSetAdapter = dataSetAdapter;
        confirmNextIndex();
    }

    /**
     * 开始转动,界面可见的时候调用
     */
    public void run() {

        if (null != mAnimation && !mAnimation.hasEnded()) {
            return;
        }

        final float start = mCurrentOffsetY;
        final float end = -2 * mTextTopToAscentOffset;
        mAnimation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (mAnimationEnded) return;

                mCurrentOffsetY = evaluate(interpolatedTime, start, end);
                if (mCurrentOffsetY == end) {
                    animationEnd();
                }
                postInvalidate();
            }
        };

        mAnimation.setDuration(1000);
        post(mRollingTask);
    }

    /**
     * @return true代表正在转动
     */
    public boolean isRunning() {
        return null != mAnimation && !mAnimation.hasEnded();
    }

    /**
     * 停止转动,界面不可见的时候调用
     */
    public void stop() {
        removeCallbacks(mRollingTask);
    }

    Runnable mRollingTask = new Runnable() {
        @Override
        public void run() {
            mAnimationEnded = false;
            startAnimation(mAnimation);
            postDelayed(this, 2000);
        }
    };


    public void animationEnd() {
        //1.角标+1
        mCurrentIndex++;
        //2.计算出正确的角标
        mCurrentIndex = mCurrentIndex < mDataSetAdapter.getItemCount() ? mCurrentIndex : mCurrentIndex % mDataSetAdapter.getItemCount();
        //3.计算下一个待显示文字角标
        confirmNextIndex();
        //3.位置复位
        mCurrentOffsetY = mOrgOffsetY;
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
    float evaluate(float fraction, float startValue, float endValue) {
        return startValue + fraction * (endValue - startValue);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {

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

}
