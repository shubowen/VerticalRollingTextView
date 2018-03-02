package com.xiaosu.view.text.strategy;

import android.text.TextPaint;
import android.text.TextUtils;

import com.xiaosu.view.text.VerticalRollingTextView;

/**
 * 疏博文 新建于 2018/3/2.
 * 邮箱： shubw@icloud.com
 * 描述：文字布局策略
 */

public interface IStrategy {

    VerticalRollingTextView.LayoutWithTextSize getLayout(float autoSizeMinTextSizeInPx,
                                                         float autoSizeMaxTextSizeInPx,
                                                         float autoSizeStepGranularityInPx,
                                                         int wantTextSize,
                                                         int width,
                                                         int height,
                                                         TextPaint paint,
                                                         int maxLines,
                                                         CharSequence text,
                                                         TextUtils.TruncateAt mTruncateAt);

}
