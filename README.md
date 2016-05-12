# VerticalRollingTextView
竖直方向无限循环滚动显示文本的控件

**非常轻量级,只有一个类,不到200行代码,不依赖任何第三方!!!**

![image](https://github.com/shubowen/pullLayout/blob/master/app/image.gif)

**使用方法:**

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
    
            <com.xiaosu.demo.VerticalRollingTextView
                android:id="@+id/verticalRollingView"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:layout_marginLeft="10dp"/>
    
    </LinearLayout>
    
2.代码中设置数据集:

    mVerticalRollingView.setTexts(Arrays.asList(mStrs));
    
3.开始滚动:

    mVerticalRollingView.run();
    
4.暂停:

    mVerticalRollingView.stop();
    
