package com.xiaosu.view.text;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.text.Layout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.xiaosu.view.text.strategy.IStrategy;
import com.xiaosu.view.text.strategy.MultiLineStrategy;
import com.xiaosu.view.text.strategy.SingleLineStrategy;

/**
 * 作者：疏博文 创建于 2016-05-12 15:05
 * 邮箱：shubowen123@sina.cn
 * 描述：
 */
public class VerticalRollingTextView extends View {

    private IStrategy mStrategy;

    private static final int[] ATTRS = new int[]{
            android.R.attr.textSize,
            android.R.attr.textColor,
            android.R.attr.ellipsize,
            android.R.attr.maxLines,
            android.R.attr.duration
    };

    private static final String TAG = "VerticalRollingTextView";

    private final static int AUTO_SIZE = -2;

    private DataSetAdapter mDataSetAdapter;

    //平缓过度Adapter
    private DataSetAdapter mTempAdapter;

    private TextPaint mPaint;

    private int mFirstVisibleIndex;

    private float mScrollY;

    private boolean isRunning;
    /*动画时间*/
    private int mDuration = 1000;
    /*动画间隔*/
    private int mAnimInterval = 2000;

    private SparseArray<LayoutWithTextSize> mLayoutArr = new SparseArray<>();
    private int mTextColor;

    private int itemHeight;

    private int mTextSize = AUTO_SIZE;

    private int itemCount = 1;

    private OnItemClickListener listener;

    private float mDownY;

    private TextUtils.TruncateAt mTruncateAt;

    private int mMaxLines;
    private int mMinTextSize;
    private int mMaxTextSize;
    private ValueAnimator mAnim;

    public VerticalRollingTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerticalRollingTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        parseAttrs(context, attrs, defStyleAttr);
    }

    private void parseAttrs(Context context, AttributeSet attrs, int defStyleAttr) {

        final TypedArray a = context.obtainStyledAttributes(attrs, ATTRS, defStyleAttr, 0);
        mTextSize = a.getDimensionPixelSize(0, AUTO_SIZE);
        mTextColor = a.getColor(1, Color.BLACK);
        int ellipsize = a.getInt(2, -1);
        mMaxLines = a.getInt(3, -1);
        mDuration = a.getInt(4, mDuration);
        a.recycle();

        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.VerticalRollingTextView);
        itemCount = arr.getInt(R.styleable.VerticalRollingTextView_itemCount, 1);
        mMinTextSize = arr.getDimensionPixelSize(R.styleable.VerticalRollingTextView_minTextSize,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics()));
        mMaxTextSize = arr.getDimensionPixelSize(R.styleable.VerticalRollingTextView_maxTextSize, -1);
        mAnimInterval = arr.getInt(R.styleable.VerticalRollingTextView_animInterval, mAnimInterval);

        switch (ellipsize) {
            case 1:
                mTruncateAt = TextUtils.TruncateAt.START;
                break;
            case 2:
                mTruncateAt = TextUtils.TruncateAt.MIDDLE;
                break;
            case 3:
                mTruncateAt = TextUtils.TruncateAt.END;
                break;
            default:
                mTruncateAt = null;
                break;
        }

        arr.recycle();

        mStrategy = mMaxLines == 1 ? new SingleLineStrategy() : new MultiLineStrategy();
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

            LayoutWithTextSize lt = findLayoutByIndex(index);
            Layout layout = lt.layout;
            mPaint.setTextSize(lt.textSize);

            int height = layout.getHeight();
            float itemBottom = itemHeight * (cursor + 1);
            float textBottom = itemBottom - mScrollY + (height < itemHeight ? (itemHeight - height) * 0.5f : 0);

            if (textBottom < 0) {
//                Log.d(TAG, "第" + cursor + "条在屏幕上方不可见区域，不绘制");
                cursor++;
                continue;
            }

            float itemTop = itemHeight * cursor;
            float textTop = itemTop - mScrollY + (height < itemHeight ? (itemHeight - height) * 0.5f : 0);

            if (textTop > getHeight()) {
//                Log.d(TAG, "第" + cursor + "条超出可绘制区域，停止绘制");
                break;
            }


            canvas.save();
            canvas.translate(0, textTop);
            canvas.clipRect(0, 0, getWidth(), itemHeight);
            layout.draw(canvas);
            canvas.restore();
//            Log.d(TAG, "绘制第" + cursor + "个条目");
            cursor++;
        }

    }

    private LayoutWithTextSize findLayoutByIndex(int index) {

        LayoutWithTextSize lt = mLayoutArr.get(index);
        if (null != lt) {
            return lt;
        }

        CharSequence cs = mDataSetAdapter.getText(index);

        if (null == mPaint) {
            mPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setColor(mTextColor);
            mPaint.setTextSize(mTextSize);
        }

        final float autoSizeMaxTextSizeInPx = mMaxTextSize == -1 ? itemHeight * 0.6f : mMaxTextSize;
        final float autoSizeStepGranularityInPx = 2;//步进

        lt = mStrategy.getLayout(mMinTextSize, autoSizeMaxTextSizeInPx, autoSizeStepGranularityInPx,
                mTextSize, getWidth(), itemHeight, mPaint, mMaxLines, cs, mTruncateAt);

        mLayoutArr.put(index, lt);

        return lt;
    }

    public static class LayoutWithTextSize {
        public Layout layout;
        public int textSize;
    }

    /**
     * 会重置滚动距离胡数据，造成视觉突变，如果想要平滑过渡使用{@link VerticalRollingTextView#setDataSetAdapterQuiet(DataSetAdapter)}
     *
     * @param adapter 新的Adapter
     */
    public void setDataSetAdapter(DataSetAdapter adapter) {

        if (null == adapter) {
            throw new RuntimeException("adapter不能为空");
        }

        if (null == mDataSetAdapter || adapter != mDataSetAdapter) {
            boolean run = isRunning;
            if (run) stop();
            mDataSetAdapter = adapter;
            reset();
            if (run) run();
        }
    }

    /**
     * 平滑到新的Adapter(目前是在滚动停止的时候更新新的数据)
     *
     * @param adapter 新的Adapter
     */
    public void setDataSetAdapterQuiet(DataSetAdapter adapter) {
        // TODO: 2018/3/5 滚动的过程中更新，更加的平滑
        if (null == adapter) {
            throw new RuntimeException("adapter不能为空");
        }

        if (null == mDataSetAdapter || adapter != mDataSetAdapter) {
            if (isRunning) {
                mTempAdapter = adapter;
            } else {
                mDataSetAdapter = adapter;
                reset();
            }
        }
    }

    private void reset() {
        mScrollY = 0;
        mLayoutArr.clear();
        mStrategy.reset();
        mFirstVisibleIndex = 0;
    }

    /**
     * 开始转动,界面可见的时候调用
     */
    public void run() {
        if (isRunning) {
            return;
        }

        if (null == mAnim) {
            mAnim = ValueAnimator.ofInt(0, getHeight());
            mAnim.setDuration(mDuration);
            mAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mScrollY = (int) animation.getAnimatedValue();
                    invalidate();
                }
            });
            mAnim.addListener(new AnimListener());
        }

        isRunning = true;
        if (canGetBounds()) {
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

        itemHeight = h / itemCount;

        if (AUTO_SIZE == mTextSize) {
            mTextSize = Math.round(itemHeight * 0.6f);
        }

        if (isRunning) {
            removeCallbacks(mRollingTask);
            mAnim.setIntValues(0, h);
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
            mAnim.start();
        }
    };

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(mRollingTask);
        if (null != mAnim && mAnim.isRunning()) mAnim.cancel();
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
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
        if (null != mAnim) mAnim.setDuration(duration);
    }

    public void setAnimInterval(int animInterval) {
        mAnimInterval = animInterval;
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

                    float top = itemHeight * cursor - mScrollY;
                    float bottom = itemHeight * (cursor + 1) - mScrollY;

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

    private class AnimListener extends AnimatorListenerAdapter {
        @Override
        public void onAnimationEnd(Animator animation) {
            if (mTempAdapter == null) {
                //1.角标+1
                mFirstVisibleIndex += itemCount;
                //2.计算出正确的角标
                mFirstVisibleIndex = mFirstVisibleIndex < mDataSetAdapter.getItemCount() ?
                        mFirstVisibleIndex : mFirstVisibleIndex % mDataSetAdapter.getItemCount();
                //3.位置复位
                mScrollY = 0;
            } else {
                mDataSetAdapter = mTempAdapter;
                mTempAdapter = null;
                reset();
            }
            if (isRunning) postDelayed(mRollingTask, mAnimInterval);
        }
    }
}
