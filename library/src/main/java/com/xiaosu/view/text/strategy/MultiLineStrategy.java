package com.xiaosu.view.text.strategy;

import android.annotation.TargetApi;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextDirectionHeuristic;
import android.text.TextDirectionHeuristics;
import android.text.TextPaint;
import android.text.TextUtils;

import com.xiaosu.view.text.VerticalRollingTextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 疏博文 新建于 2018/3/2.
 * 邮箱： shubw@icloud.com
 * 描述：多行显示自适应显示，根据条目的高度选择最大的文字的高度(从6.0的AutoSize抄来的)
 */

public class MultiLineStrategy implements IStrategy {

    private int[] mAutoSizeTextSizesInPx = new int[0];
    private int mMaxLines;
    private int mHeight;
    private TextPaint mPaint;

    public VerticalRollingTextView.LayoutWithTextSize getLayout(float autoSizeMinTextSizeInPx,
                                                                float autoSizeMaxTextSizeInPx,
                                                                float autoSizeStepGranularityInPx,
                                                                int wantTextSize,
                                                                int width,
                                                                int height,
                                                                TextPaint paint,
                                                                int maxLines,
                                                                CharSequence text,
                                                                TextUtils.TruncateAt mTruncateAt) {

        this.mMaxLines = maxLines;
        this.mPaint = paint;
        this.mHeight = height;

        setupAutoSizeText(autoSizeMinTextSizeInPx, autoSizeMaxTextSizeInPx, autoSizeStepGranularityInPx);

        Layout target = null;

        int bestSizeIndex = 0;
        int lowIndex = bestSizeIndex + 1;
        int highIndex = mAutoSizeTextSizesInPx.length - 1;
        int sizeToTryIndex;
        //中间查找，找到合适的最大的文字大小
        while (lowIndex <= highIndex) {
            sizeToTryIndex = (lowIndex + highIndex) / 2;
            Layout layout = suggestedSizeFitsInSpace(mAutoSizeTextSizesInPx[sizeToTryIndex], width, -1, text, Layout.Alignment.ALIGN_NORMAL);
            if (null != layout) {
                target = layout;
                bestSizeIndex = lowIndex;
                lowIndex = sizeToTryIndex + 1;
//                Log.d(TAG, "SizeFitsInSpace: " + mAutoSizeTextSizesInPx[sizeToTryIndex]);
            } else {
                highIndex = sizeToTryIndex - 1;
                bestSizeIndex = highIndex;
            }
        }

        VerticalRollingTextView.LayoutWithTextSize lt = new VerticalRollingTextView.LayoutWithTextSize();
        lt.layout = target;
        lt.textSize = mAutoSizeTextSizesInPx[bestSizeIndex];

        return lt;
    }

    @Override
    public void reset() {
        mAutoSizeTextSizesInPx = new int[0];
    }

    private void setupAutoSizeText(float autoSizeMinTextSizeInPx,
                                   float autoSizeMaxTextSizeInPx,
                                   float autoSizeStepGranularityInPx) {
        // Calculate the sizes set based on minimum size, maximum size and step size if we do
        // not have a predefined set of sizes or if the current sizes array is empty.
        if (mAutoSizeTextSizesInPx.length == 0) {
            // Calculate sizes to choose from based on the current auto-size configuration.
            int autoSizeValuesLength = (int) Math.ceil(
                    (autoSizeMaxTextSizeInPx - autoSizeMinTextSizeInPx)
                            / autoSizeStepGranularityInPx);
            // Also reserve a slot for the max size if it fits.
            if ((autoSizeMaxTextSizeInPx - autoSizeMinTextSizeInPx)
                    % autoSizeStepGranularityInPx == 0) {
                autoSizeValuesLength++;
            }
            int[] autoSizeTextSizesInPx = new int[autoSizeValuesLength];
            float sizeToAdd = autoSizeMinTextSizeInPx;
            for (int i = 0; i < autoSizeValuesLength; i++) {
                autoSizeTextSizesInPx[i] = Math.round(sizeToAdd);
                sizeToAdd += autoSizeStepGranularityInPx;
            }
            mAutoSizeTextSizesInPx = cleanupAutoSizePresetSizes(autoSizeTextSizesInPx);
        }
    }

    private int[] cleanupAutoSizePresetSizes(int[] presetValues) {
        final int presetValuesLength = presetValues.length;
        if (presetValuesLength == 0) {
            return presetValues;
        }
        Arrays.sort(presetValues);

        final List<Integer> uniqueValidSizes = new ArrayList<>();
        for (final int currentPresetValue : presetValues) {
            if (currentPresetValue > 0
                    && Collections.binarySearch(uniqueValidSizes, currentPresetValue) < 0) {
                uniqueValidSizes.add(currentPresetValue);
            }
        }

        if (presetValuesLength == uniqueValidSizes.size()) {
            return presetValues;
        } else {
            final int uniqueValidSizesLength = uniqueValidSizes.size();
            final int[] cleanedUpSizes = new int[uniqueValidSizesLength];
            for (int i = 0; i < uniqueValidSizesLength; i++) {
                cleanedUpSizes[i] = uniqueValidSizes.get(i);
            }
            return cleanedUpSizes;
        }
    }

    private StaticLayout suggestedSizeFitsInSpace(int suggestedSizeInPx,
                                                  int availableWidth,
                                                  int maxLines,
                                                  CharSequence text,
                                                  Layout.Alignment alignment) {

        mPaint.setTextSize(suggestedSizeInPx);

        final StaticLayout layout = Build.VERSION.SDK_INT >= 23
                ? createStaticLayoutForMeasuring(text, alignment, availableWidth)
                : createStaticLayoutForMeasuringPre23(text, alignment, availableWidth);

        // Lines overflow.
        if (maxLines != -1 && layout.getLineCount() > maxLines) {
            return null;
        }

        // Height overflow.
        if (layout.getHeight() > mHeight) {
            return null;
        }

        return layout;
    }

    @TargetApi(23)
    private StaticLayout createStaticLayoutForMeasuring(CharSequence text,
                                                        Layout.Alignment alignment,
                                                        int availableWidth) {
        final TextDirectionHeuristic textDirectionHeuristic = TextDirectionHeuristics.FIRSTSTRONG_LTR;

        final StaticLayout.Builder layoutBuilder = StaticLayout.Builder.obtain(
                text, 0, text.length(), mPaint, availableWidth);

        return layoutBuilder.setAlignment(alignment)
                .setLineSpacing(
                        0.0f,
                        1.0f)
                .setIncludePad(false)
//                .setBreakStrategy(mTextView.getBreakStrategy())
//                .setHyphenationFrequency(mTextView.getHyphenationFrequency())
                .setMaxLines(mMaxLines == -1 ? Integer.MAX_VALUE : mMaxLines)
                .setTextDirection(textDirectionHeuristic)
                .build();
    }

    @TargetApi(14)
    private StaticLayout createStaticLayoutForMeasuringPre23(CharSequence text,
                                                             Layout.Alignment alignment, int availableWidth) {

        return new StaticLayout(text,
                mPaint,
                availableWidth,
                alignment,
                1.0f,
                0.0f,
                false);
    }

}
