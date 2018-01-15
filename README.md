# VerticalRollingTextView
竖直方向无限循环滚动文本的控件

**非常轻量级,直接继承View实现**

![image](https://github.com/shubowen/VerticalRollingTextView/blob/master/app/image.gif)

**使用方法:**

先在项目build.gradle中添加依赖:

    compile 'com.xiaosu:VerticalRollingTextView:1.3.0'

1.现在布局文件中声明

    <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="50dp"
            android:background="@android:color/white"
            android:gravity="center_vertical">
    
            <ImageView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginLeft="15dp"
                android:src="@mipmap/gd_xiaoxi"/>
    
            <com.xiaosu.VerticalRollingTextView
                android:id="@+id/verticalRollingView"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:layout_marginLeft="10dp"/>
    
    </LinearLayout>
    
2.代码中设置数据集:

    mVerticalRollingView.setDataSetAdapter(new DataSetAdapter<CharSequence>(mDataSet) {
    
                @Override
                protected CharSequence text(CharSequence charSequence) {
                    return charSequence;
                }
            });
    
3.开始滚动:

    mVerticalRollingView.run();
    
4.暂停:

    mVerticalRollingView.stop();

5.设置点击监听:

    mVerticalRollingView.setOnItemClickListener(this);

6.点击回调
    
    public void onItemClick(VerticalRollingTextView view, int index) {
        //index是当前条目的角标
    }

7.可以在布局中设置的属性:

    <declare-styleable name="VerticalRollingTextView">
        <!--文字颜色-->
        <attr name="android:textColor"/>
        <!--文字大小-->
        <attr name="android:textSize"/>
        <!--滚动动画时长-->
        <attr name="android:duration"/>
        <!--两次动画之间的间隔-->
        <attr name="animInterval" format="integer"/>
    </declare-styleable>

    
