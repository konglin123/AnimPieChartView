package com.example.animpiechartview;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private PieChartView pieChartView;
    private List<PieBean> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pieChartView=findViewById(R.id.pie_chart);
        initData();
    }

    private void initData() {
        list = new ArrayList<>();
        for (int i = 1; i < 7; i ++) {
            list.add(new PieBean(i * 20, String.format("第%s区", i)));
        }
//        pieChartView.setPieBeanList(list);
//        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 360f);
//        valueAnimator.setDuration(2000);
//        valueAnimator.setInterpolator(new LinearInterpolator());
//        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator animation) {
//                float value= (float) animation.getAnimatedValue();
//                pieChartView.setmAnimatorValue(value,true);
//            }
//        });
//        valueAnimator.addListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animation) {
//               pieChartView.setmAnimatorValue(0,false);
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animation) {
//
//            }
//        });
//        valueAnimator.start();
        pieChartView.setPieBeanList(list).setShowAnimator(true);
        pieChartView.setOnPositionChangeListener(new PieChartView.OnPositionChangeListener() {
            @Override
            public void onPositionChange(int position) {
                String msg = list.get(position).getMsg();
                Toast.makeText(MainActivity.this,msg,Toast.LENGTH_SHORT).show();
            }
        });

    }
}
