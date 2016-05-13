package com.xiaosu.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.xiaosu.DataSetAdapter;
import com.xiaosu.VerticalRollingTextView;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    String[] mStrs = {
            "君不见，黄河之水天上来，奔流到海不复回",
            "君不见，高堂明镜悲白发，朝如青丝暮成雪",
            "人生得意须尽欢，莫使金樽空对月",
            "天生我材必有用，千金散尽还复来",
            "烹羊宰牛且为乐，会须一饮三百杯",
            "岑夫子，丹丘生，将进酒，杯莫停"
    };

    @BindView(R.id.verticalRollingView)
    VerticalRollingTextView mVerticalRollingView;
    @BindView(R.id.button)
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mVerticalRollingView.setDataSetAdapter(new DataSetAdapter<String>(Arrays.asList(mStrs)) {

            @Override
            protected String text(String s) {
                return s;
            }
        });
    }

    @OnClick(R.id.button)
    void operate(View view) {
        if (mVerticalRollingView.isRunning()) {
            mVerticalRollingView.stop();
            button.setText("滚动");
        } else {
            mVerticalRollingView.run();
            button.setText("停止");
        }
    }

}
