package com.xiaosu.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.xiaosu.DataSetAdapter;
import com.xiaosu.VerticalRollingTextView;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements VerticalRollingTextView.OnItemClickListener {

    @BindView(R.id.verticalRollingView)
    VerticalRollingTextView mVerticalRollingView;
    @BindView(R.id.button)
    Button button;
    private List<CharSequence> mDataSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        SpannableString span = new SpannableString("君不见，高堂明镜悲白发，朝如青丝暮成雪");
        span.setSpan(new ForegroundColorSpan(0xFF00FF00), 0, 8,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        SpannableString span1 = new SpannableString("人生得意须尽欢，莫使金樽空对月");
        span1.setSpan(new RelativeSizeSpan(0.8f), 0, 7,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        CharSequence[] mStrs = {
                "君不见，黄河之水天上来，奔流到海不复回",
                span,
                span1,
                "天生我材必有用，千金散尽还复来",
                "烹羊宰牛且为乐，会须一饮三百杯",
                "岑夫子，丹丘生，将进酒，杯莫停"
        };

        mDataSet = Arrays.asList(mStrs);
        mVerticalRollingView.setDataSetAdapter(new DataSetAdapter<CharSequence>(mDataSet) {

            @Override
            protected CharSequence text(CharSequence charSequence) {
                return charSequence;
            }
        });
        mVerticalRollingView.setOnItemClickListener(this);

        operate(mVerticalRollingView);
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

    @Override
    public void onItemClick(VerticalRollingTextView view, int index) {
        Toast.makeText(MainActivity.this, mDataSet.get(index), Toast.LENGTH_SHORT).show();
    }
}
