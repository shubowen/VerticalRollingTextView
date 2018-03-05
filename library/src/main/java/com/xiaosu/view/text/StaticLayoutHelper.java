package com.xiaosu.view.text;

import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextDirectionHeuristic;
import android.text.TextDirectionHeuristics;
import android.text.TextPaint;
import android.text.TextUtils;

/**
 * 疏博文 新建于 2018/3/5.
 * 邮箱： shubw@icloud.com
 * 描述：请添加此文件的描述
 */

public class StaticLayoutHelper {

    public static StaticLayout createStaticLayout(CharSequence source,
                                                  TextPaint paint,
                                                  int width,
                                                  Layout.Alignment align,
                                                  float spacingmult,
                                                  float spacingadd,
                                                  boolean includepad,
                                                  TextUtils.TruncateAt ellipsize,
                                                  int ellipsizedWidth,
                                                  int maxLines) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder layoutBuilder = StaticLayout.Builder.obtain(
                    source, 0, source.length(), paint, width);

            return layoutBuilder.setAlignment(align)
                    .setLineSpacing(spacingadd, spacingmult)
                    .setIncludePad(includepad)
                    .setMaxLines(maxLines)
                    .setEllipsize(ellipsize)
                    .setEllipsizedWidth(ellipsizedWidth)
                    .setTextDirection(TextDirectionHeuristics.FIRSTSTRONG_LTR)
                    .build();
        } else {
            /*5.1.1 StaticLayout(CharSequence source, int bufstart, int bufend,
                                    TextPaint paint, int outerwidth,
                                    Alignment align, TextDirectionHeuristic textDir,
                                            float spacingmult, float spacingadd,
                                    boolean includepad,
                                    TextUtils.TruncateAt ellipsize, int ellipsizedWidth, int maxLines)*/
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    return StaticLayout.class.getConstructor(CharSequence.class,
                            Integer.class,
                            Integer.class,
                            TextPaint.class,
                            Integer.class,
                            Layout.Alignment.class, TextDirectionHeuristic.class,
                            Float.class,
                            Float.class, Boolean.class, TextUtils.TruncateAt.class,
                            Integer.class,
                            Integer.class)
                            .newInstance(
                                    source,
                                    0,
                                    source.length(),
                                    paint,
                                    width,
                                    align,
                                    TextDirectionHeuristics.FIRSTSTRONG_LTR,
                                    spacingmult,
                                    spacingadd,
                                    includepad,
                                    ellipsize,
                                    ellipsizedWidth,
                                    maxLines);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
