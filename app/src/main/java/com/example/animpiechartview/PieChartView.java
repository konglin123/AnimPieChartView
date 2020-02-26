package com.example.animpiechartview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PieChartView extends View {

    //外圆画笔
    private Paint outPaint;
    //内透明圆画笔
    private Paint alphaPaint;
    //内透明圆透明度
    private float alpha=Config.DEFAULT_CIRCLE_ALPHA;
    //内透明圆半径占外圆半径的百分比
    private float alphaRadiusPercent=Config.DEFAULT_ALPHA_RADIUS_PERCENT;

    //最内部白色圆画笔
    private Paint inHolePaint;
    //内白圆半径占外圆半径的百分比
    private float inHoleRadiusPercent=Config.DEFAULT_HOLE_RADIUS_PERCENT;

    /**各版块百分比及文字画笔*/
    private Paint blockTextPaint;
    /** 版块上的字号*/
    private int blockTextSize = Config.DEFAULT_BLOCK_TEXT_SIZE;
    /** 版块上文字的颜色*/
    private int blockTextColor = Config.DEFAULT_BLOCK_TEXT_COLOR;
    /** 是否展示百分比数*/
    private boolean disPlayPercent = true;

    /** 中心文字画笔*/
    private Paint centerTextPaint;
    /** 中心文字大小*/
    private int centerTextSize = Config.DEFAULT_CENTER_TEXT_SIZE;
    /** 中心文字颜色*/
    private int centerTextColor = Config.DEFAULT_CENTER_TEXT_COLOR;
    /** 中心文字*/
    private String centerText;
    /** 是否显示中心文字*/
    private boolean showCenterText=true;

    /** 动画数值*/
    private float mAnimatorValue;
    /** 动画*/
    private ValueAnimator mAnimator;
    /** 动画时长*/
    private int mAnimatorTime = Config.DEFAULT_ANIMATOR_TIME;


    //起始绘制角度
    private float startDegree=Config.DEFAULT_START_DEGREE;
    //当前绘制角度
    private float currentDegree;
    //外圆半径
    private float radius;
    //凸起板块之间的间距
    private int space=Config.DEFAULT_BLOCK_SPACE;
    //是否开启动画
    private boolean showAnimator = false;

    private OnPositionChangeListener onPositionChangeListener;


    private float total;
    private RectF radiusRectfF;
    private List<PieBean> pieBeanList=new ArrayList<>();
    private RectF alphaRectF;
    private float sumData;
    private Context mContext;

    public void setOnPositionChangeListener(OnPositionChangeListener onPositionChangeListener) {
        this.onPositionChangeListener = onPositionChangeListener;
    }

    public float getmAnimatorValue() {
        return mAnimatorValue;
    }

    public void setmAnimatorValue(float mAnimatorValue,boolean showAnimator) {
        this.mAnimatorValue = mAnimatorValue;
        this.showAnimator=showAnimator;
        invalidate();
    }

    public int getCenterTextSize() {
        return centerTextSize;
    }

    public void setCenterTextSize(int centerTextSize) {
        this.centerTextSize = centerTextSize;
    }

    public int getCenterTextColor() {
        return centerTextColor;
    }

    public void setCenterTextColor(int centerTextColor) {
        this.centerTextColor = centerTextColor;
    }

    public String getCenterText() {
        return centerText;
    }

    public void setCenterText(String centerText) {
        this.centerText = centerText;
    }

    public boolean isShowCenterText() {
        return showCenterText;
    }

    public void setShowCenterText(boolean showCenterText) {
        this.showCenterText = showCenterText;
    }

    public boolean isDisPlayPercent() {
        return disPlayPercent;
    }

    public void setDisPlayPercent(boolean disPlayPercent) {
        this.disPlayPercent = disPlayPercent;
    }

    public PieChartView setPieBeanList(List<PieBean> pieBeanList) {
        this.pieBeanList = pieBeanList;
        for (int i = 0; i < pieBeanList.size(); i++) {
            sumData+=pieBeanList.get(i).getData();
        }
        return this;
    }


    public boolean isShowAnimator() {
        return showAnimator;
    }

    public PieChartView(Context context) {
        this(context,null);
    }

    public PieChartView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public PieChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext=context;
        init();
    }

    private void init() {

        outPaint=new Paint();
        outPaint.setAntiAlias(true);
        outPaint.setStyle(Paint.Style.FILL);

        alphaPaint=new Paint();
        alphaPaint.setAntiAlias(true);
        alphaPaint.setStyle(Paint.Style.FILL);
        alphaPaint.setColor(Color.WHITE);
        alphaPaint.setAlpha((int) (alpha*255));

        inHolePaint=new Paint();
        inHolePaint.setAntiAlias(true);
        inHolePaint.setStyle(Paint.Style.FILL);
        inHolePaint.setColor(Color.WHITE);

        blockTextPaint=new Paint();
        blockTextPaint.setAntiAlias(true);
        blockTextPaint.setFakeBoldText(true);
        blockTextPaint.setTextSize(blockTextSize);
        blockTextPaint.setColor(blockTextColor);
        blockTextPaint.setTextAlign(Paint.Align.CENTER);

        centerTextPaint=new Paint();
        centerTextPaint.setAntiAlias(true);
        centerTextPaint.setTextSize(centerTextSize);
        centerTextPaint.setColor(centerTextColor);

        centerText="ChartView";

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.WHITE);


        //将画布移动到getwidth/2,getheight/2
        canvas.translate(getWidth()>>1,getHeight()>>1);
        calculate();

        if(isShowAnimator()){
            drawByAnim(canvas);
        }else{
            drawByNormal(canvas);
        }
    }

    /**动画绘制*/
    private void drawByAnim(Canvas canvas) {
//        for (int i = 0; i < pieBeanList.size(); i++) {
//            total+=pieBeanList.get(i).getData();
//        }

        for (int i = 0; i < pieBeanList.size(); i++) {
            if(sumData==0){
                return;
            }
            if(pieBeanList.get(i).getColor()==0){
                int color=getRandColor();
                outPaint.setColor(color);
                pieBeanList.get(i).setColor(color);
            }else {
                //设置每个板块的颜色
                outPaint.setColor(pieBeanList.get(i).getColor());
            }
            //计算每个板块扫过的角度
            float sweepDegree=pieBeanList.get(i).getData()*360f/sumData;
            canvas.save();

            float valueDegree=Math.min(sweepDegree,mAnimatorValue-(currentDegree-startDegree));
             //当前模块角平分线的sin和cos值
            if (pieBeanList.get(i).isRaised()){
                float mathCos = (float) (Math.cos((sweepDegree / 2 + currentDegree) / 180f * Math.PI));
                float mathSin = (float) (Math.sin((sweepDegree / 2 + currentDegree) / 180f * Math.PI));
                //若该板块设置凸起，平移画布，使其与周边各块产生间距，并突出
                canvas.translate(mathCos * space, mathSin * space);
            }
            //画外圆
            canvas.drawArc(radiusRectfF,currentDegree,valueDegree,true,outPaint);
            canvas.restore();

            alphaPaint.setAlpha((int) (alpha * 255));
            canvas.drawArc(alphaRectF, currentDegree, valueDegree, true, alphaPaint);
            canvas.drawCircle(0, 0,radius * inHoleRadiusPercent, inHolePaint);
            //若本次绘制角度大于该版块角度，则进入下一板块，否则结束循环，重新绘制
            if(sweepDegree<=valueDegree){
                currentDegree+=sweepDegree;
            }else {
                break;
            }

        }

    }


    /**
     * 这里有一个bug，就是invalidate和暗屏亮屏都会触发ondraw方法，我这里total设置成了全局变量，
     * 所以第一遍进ondraw是对的，以后再每一次进ondraw方法total都会翻倍，所以造成sweepDegree就会一直对半减小
     * 就会看到一个圆变成半圆，半圆变成四分之一圆的情况，以此类推
     * 这里total不能设置成全局的
     *
     */
    /** 普通绘制 */
    private void drawByNormal(Canvas canvas) {
        /**这个写法有问题*/
//        for (int i = 0; i < pieBeanList.size(); i++) {
//             total+=pieBeanList.get(i).getData();
//            }

        for (int i = 0; i < pieBeanList.size(); i++) {
            if(sumData==0){
                return;
            }
            if(pieBeanList.get(i).getColor()==0){
                int color=getRandColor();
                outPaint.setColor(color);
                pieBeanList.get(i).setColor(color);
            }else {
                //设置每个板块的颜色
            outPaint.setColor(pieBeanList.get(i).getColor());
            }
            //计算每个板块扫过的角度
            float sweepDegree=pieBeanList.get(i).getData()/sumData*360f;
            canvas.save();
            //当前模块角平分线的sin和cos值
            if (pieBeanList.get(i).isRaised()){
                float mathCos = (float) (Math.cos((sweepDegree / 2 + currentDegree) / 180f * Math.PI));
                float mathSin = (float) (Math.sin((sweepDegree / 2 + currentDegree) / 180f * Math.PI));
                //若该板块设置凸起，平移画布，使其与周边各块产生间距，并突出
                canvas.translate(mathCos * space, mathSin * space);
            }
            //画外圆
            canvas.drawArc(radiusRectfF,currentDegree,sweepDegree,true,outPaint);
            canvas.restore();
            //记录每个扇形起始和结束角度，待会点击需要判断在哪个扇形里需要用到
            pieBeanList.get(i).setStartDegree(currentDegree);
            pieBeanList.get(i).setEndDegree(currentDegree+sweepDegree);
            //上一个扇形的结束角度作为下一个扇形的开始角度
            currentDegree+=sweepDegree;
        }

        //画内透明圆
        canvas.drawCircle(0,0,radius*alphaRadiusPercent,alphaPaint);
        //画最内部白色圆
        canvas.drawCircle(0,0,radius*inHoleRadiusPercent,inHolePaint);

        //画外圆板块百分比或文字
        drawTextInner(canvas);

        //画中心文字
        if(isShowCenterText()&&!TextUtils.isEmpty(centerText)){
            Paint.FontMetrics fontMetrics = centerTextPaint.getFontMetrics();
            float textHeight=Math.abs(fontMetrics.descent-fontMetrics.ascent);
            Rect rect = new Rect();
            centerTextPaint.getTextBounds(centerText,0,centerText.length(),rect);
            int textWidth=rect.width();
            canvas.drawText(centerText,-textWidth/2,textHeight/2-fontMetrics.descent,centerTextPaint);
        }
    }

    private void calculate() {
        //外圆半径等于宽高较短的一半乘0.85
        radius=(Math.min(getWidth()-getPaddingLeft()-getPaddingRight(),getHeight()-getPaddingTop()-getPaddingBottom())>>1)*0.85f;
        currentDegree=startDegree;
        radiusRectfF = new RectF(-radius, -radius, radius, radius);
        alphaRectF = new RectF(-radius * alphaRadiusPercent, -radius * alphaRadiusPercent, radius * alphaRadiusPercent, radius * alphaRadiusPercent);
    }

    private int getRandColor(){
        Random random = new Random();
        return 0xff000000 | random.nextInt(0x00ffffff);
    }

    private void drawTextInner(Canvas canvas){
        for (int i = 0; i < pieBeanList.size(); i++) {
            if(sumData==0){
                return;
            }
            float sweepDgree = pieBeanList.get(i).getData()/ sumData * 360f ;
            canvas.save();
            //当前模块角平分线的sin和cos值
            float mathCos = (float) (Math.cos((sweepDgree / 2 + currentDegree) / 180f * Math.PI));
            float mathSin = (float) (Math.sin((sweepDgree / 2 + currentDegree) / 180f * Math.PI));

            //若该板块设置凸起，平移画布，使其与周边各块产生间距，并突出
            if (pieBeanList.get(i).isRaised()){
                canvas.translate(mathCos * space, mathSin * space);
            }

            String msg = pieBeanList.get(i).getMsg();
            Paint.FontMetrics metrics = blockTextPaint.getFontMetrics();
            float textRadius = (1 + alphaRadiusPercent) * radius / 2;

            if (isDisPlayPercent()) {
                //获取文字高度，因水平已居中
                //设置文字格式
                DecimalFormat format = new DecimalFormat("#0.00%");
                String text = format.format(sweepDgree / 360f);
                //绘制模块文字
                if (!TextUtils.isEmpty(msg)) {
                    canvas.drawText(msg, textRadius * mathCos,
                            textRadius * mathSin - Math.abs(metrics.descent), blockTextPaint);
                }
                //绘制模块百分比
                canvas.drawText(text, textRadius * mathCos,
                        textRadius * mathSin + Math.abs(metrics.ascent), blockTextPaint);
            } else {
                //获取文字高度，因水平已居中
                float textHeight = Math.abs(metrics.descent - metrics.ascent);
                //绘制模块文字
                if (!TextUtils.isEmpty(msg)) {
                    canvas.drawText(msg, textRadius * mathCos,
                            textRadius * mathSin + textHeight / 2 - metrics.descent, blockTextPaint);
                }
            }
            canvas.restore();
            currentDegree += sweepDgree;
        }
    }


    public PieChartView setShowAnimator(boolean showAnimator) {
        if (Looper.getMainLooper() != Looper.myLooper()){
            //非UI线程设置动画无效
            return this;
        }
        this.showAnimator = showAnimator;
        if (showAnimator){
            post(new Runnable() {
                @Override
                public void run() {
                    initValueAnimator();
                }
            });
        }else if (null != mAnimator && mAnimator.isRunning()){
            mAnimator.cancel();
        }
        return this;
    }

    private void initValueAnimator(){
        if(null == mAnimator) {
            mAnimator = ValueAnimator.ofFloat(0, 360f);
            mAnimator.setDuration(mAnimatorTime);
            mAnimator.setInterpolator(new LinearInterpolator());
            mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mAnimatorValue = (float) animation.getAnimatedValue();
                    refresh();
                }
            });
            mAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    showAnimator = false;
                    refresh();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }
        mAnimator.start();
    }

    public void refresh(){
        if (Looper.getMainLooper() == Looper.myLooper()) {
            invalidate();
        } else {
            postInvalidate();
        }
    }


    public interface OnPositionChangeListener{
        void onPositionChange(int position);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX;
        float touchY;
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                touchX=event.getX();
                touchY=event.getY();
                //判断是否在圆环上
                double distance=Math.pow(touchX-getWidth()/2,2)+Math.pow(touchY-getHeight()/2,2);
                if(distance>=Math.pow(radius*alphaRadiusPercent,2)&&distance<=Math.pow(radius,2)){
                    //获取touch点和圆心的连线 与 x轴正方向的夹角
                    float sweep = getSweep(touchX, touchY);
                    for (int i = 0; i < pieBeanList.size(); i++) {
                        if(sweep>=pieBeanList.get(i).getStartDegree()&&sweep<=pieBeanList.get(i).getEndDegree()){
                            onPositionChangeListener.onPositionChange(i);
                            pieBeanList.get(i).setRaised(true);
                        }else{
                            pieBeanList.get(i).setRaised(false);
                        }
                    }
                    invalidate();
                }else{
                    //圆环外不做处理
                }
                break;
        }
        return true;
    }

    private float getSweep(float touchX, float touchY) {
        float xZ = touchX - getWidth()/2;
        float yZ = touchY - getHeight()/2;
        float a = Math.abs(xZ);
        float b = Math.abs(yZ);
        double c = Math.toDegrees(Math.atan(b / a));
        if (xZ >= 0 && yZ >= 0) {//第一象限
            return (float) c;
        } else if (xZ <= 0 && yZ >= 0) {//第二象限
            return 180 - (float) c;
        } else if (xZ <= 0 && yZ <= 0) {//第三象限
            return (float) c + 180;
        } else {//第四象限
            return 360 - (float) c;
        }

    }
}

