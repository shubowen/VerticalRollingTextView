package com.xiaosu;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.text.BoringLayout;
import android.text.Layout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * 作者：疏博文 创建于 2016-05-12 15:05
 * 邮箱：shubowen123@sina.cn
 * 描述：
 */
public class VerticalRollingTextView extends View {

    private static final String TAG = "VerticalRollingTextView";

    private final static int AUTO_SIZE = -2;
    private final static int FILL_PARENT = -1;

    private DataSetAdapter mDataSetAdapter;

    private TextPaint mPaint;

    private int mFirstVisibleIndex;

    private float mScrollY;

    private InternalAnimation mAnimation;

    /*防止动画结束的回调触发以后动画继续进行出现的错乱问题*/
    private boolean mAnimationEnded;

    private boolean isRunning;
    /*动画时间*/
    private int mDuration = 1000;
    /*动画间隔*/
    private int mAnimInterval = 2000;

    private SparseArray<Layout> mLayoutArr = new SparseArray<>();
    private int mTextColor;

    private int mItemHeight;
    private int mTextSize = AUTO_SIZE;

    private int mItemCount = 1;

    private OnItemClickListener listener;
    private float mDownY;

    public VerticalRollingTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        parseAttrs(context, attrs);
    }

    private void parseAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.VerticalRollingTextView);
        mTextColor = typedArray.getColor(R.styleable.VerticalRollingTextView_android_textColor, Color.BLACK);
        mTextSize = typedArray.getDimensionPixelSize(R.styleable.VerticalRollingTextView_android_textSize, AUTO_SIZE);
        mItemCount = typedArray.getInt(R.styleable.VerticalRollingTextView_itemCount, 1);
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

        int cursor = 0;
        while (true) {
            int index = mFirstVisibleIndex + cursor;
            index = index >= mDataSetAdapter.getItemCount() ?
                    (index % mDataSetAdapter.getItemCount()) : index;

            Layout layout = findLayoutByIndex(index);

            float top = mItemHeight * cursor;
            float bottom = mItemHeight * (cursor + 1);

            if (bottom - mScrollY < 0) {
//                Log.d(TAG, "第" + cursor + "条在屏幕上方不可见区域，不绘制");
                cursor++;
                continue;
            }

            float dy = top + (mItemHeight - layout.getHeight()) * 0.5f - mScrollY;

            if (dy > getHeight()) {
//                Log.d(TAG, "第" + cursor + "条超出可绘制区域，停止绘制");
                break;
            }

            canvas.save();
            canvas.translate(0, dy);
            layout.draw(canvas);
            canvas.restore();
//            Log.d(TAG, "绘制第" + cursor + "个条目");
            cursor++;
        }

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

        BoringLayout.Metrics metrics = BoringLayout.isBoring(cs, mPaint);

        layout = new BoringLayout(
                cs,
                mPaint,
                getWidth(),
                Layout.Alignment.ALIGN_NORMAL,
                1.0f,
                0.0f,
                metrics, false);

        mLayoutArr.put(index, layout);

        return layout;
    }

    public void setDataSetAdapter(DataSetAdapter adapter) {

        if (null == adapter) {
            throw new RuntimeException("adapter不能为空");
        }

        mDataSetAdapter = adapter;
        mFirstVisibleIndex = 0;
        invalidate();
    }

    /**
     * 开始转动,界面可见的时候调用
     */
    public void run() {
        if (isRunning) {
            return;
        }

        if (null == mAnimation) {
            mAnimation = new InternalAnimation();
            mAnimation.setDuration(mDuration);
        }

        isRunning = true;
        if (canGetBounds()) {
            mAnimation.updateValue(getHeight());
            post(mRollingTask);
        }
    }

    private boolean canGetBounds() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return isLaidOut();
        } else {
            return getWidth() > 0 && getHeight() > 0;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        mItemHeight = h / mItemCount;

        if (AUTO_SIZE == mTextSize) {
            mTextSize = Math.round(mItemHeight * 0.6f);
        }

        if (isRunning) {
            removeCallbacks(mRollingTask);

            mAnimation.updateValue(h);
            postDelayed(mRollingTask, mAnimInterval);
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
            startAnimation(mAnimation);
        }
    };

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(mRollingTask);
        if (isRunning()) {
            mAnimation.cancel();
        }
    }

    public void setItemCount(int itemCount) {
        mItemCount = itemCount;
    }

    public void setFirstVisibleIndex(int firstVisibleIndex) {
        mFirstVisibleIndex = firstVisibleIndex;
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

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.listener = onItemClickListener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        final int action = event.getAction();

        if (null != listener && isEnabled()) {

            if (action == MotionEvent.ACTION_DOWN) {
                mDownY = event.getY();
            }

            if (action == MotionEvent.ACTION_UP) {
                int cursor = 0;
                while (true) {
                    int index = mFirstVisibleIndex + cursor;
                    index = index >= mDataSetAdapter.getItemCount() ?
                            (index % mDataSetAdapter.getItemCount()) : index;

                    float top = mItemHeight * cursor - mScrollY;
                    float bottom = mItemHeight * (cursor + 1) - mScrollY;

                    if (top < mDownY && bottom > mDownY) {
                        listener.onItemClick(this, index);
                        break;
                    }

                    cursor++;
                }

                mDownY = 0;
            }

            return true;
        }

        return super.onTouchEvent(event);
    }

    public interface OnItemClickListener {
        void onItemClick(VerticalRollingTextView view, int index);
    }

    @Override
    public void startAnimation(Animation animation) {
        mAnimationEnded = false;
        super.startAnimation(animation);
    }

    private class InternalAnimation extends Animation {

        float endValue;

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {

            if (interpolatedTime == 0 || mAnimationEnded) {
                return;
            }

            mScrollY = evaluate(interpolatedTime, 0, endValue);

            invalidate();

            if (interpolatedTime == 1.0f) {
                animEnd();
            }
        }

        void updateValue(float endValue) {
            this.endValue = endValue;
        }

    }

    private void animEnd() {
        //1.角标+1
        mFirstVisibleIndex += mItemCount;
        //2.计算出正确的角标
        mFirstVisibleIndex = mFirstVisibleIndex < mDataSetAdapter.getItemCount() ?
                mFirstVisibleIndex : mFirstVisibleIndex % mDataSetAdapter.getItemCount();
        //3.位置复位
        mScrollY = 0;

        mAnimation.cancel();

        mAnimationEnded = true;

        if (isRunning) postDelayed(mRollingTask, mAnimInterval);
    }
}
