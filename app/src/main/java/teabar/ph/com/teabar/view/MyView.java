package teabar.ph.com.teabar.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.jaygoo.widget.Utils;

import me.jessyan.autosize.utils.ScreenUtils;
import teabar.ph.com.teabar.R;

public class MyView extends View
{

    private Paint mPaint;
    private Paint mPaint2;
    private int mStrokeWith;
    private boolean mIsRound;
//    private int mColor01;
//    private int mColor02;
//    private int mColor03;
//    private int mColor04;
//    private int mColor05;
//    private int mColor06;
//    private int mColor07;
    private int mViewAangle;
    private int mStartAangle;
    private int mViewPadding;
    private Paint mPaint1;
    private int mPointColor;
    private int mPointColor1;
    private int mPointRaido;
    private int mPointRaido1;
    private float mView_x0;
    private float mView_y0;
    private int mPointAngle=0;
    private  OnProgressListener mOnProgressListener;
    private Paint mTextPaint;
    private Paint mTextPaint1;
    public void setOnProgressListener(OnProgressListener onProgressListener) {
        mOnProgressListener = onProgressListener;
    }

    public MyView(Context context) {
        super(context);
    }

    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        int color_01 = getResources().getColor(R.color.nomal_green);
        int color_02 = getResources().getColor(R.color.nomal_green);
        int color_03 = getResources().getColor(R.color.nomal_green);
        int color_04 = getResources().getColor(R.color.nomal_green);
        int color_05 = getResources().getColor(R.color.nomal_green);
        int color_06 = getResources().getColor(R.color.nomal_green);
        int color_07 = getResources().getColor(R.color.nomal_green);




        /*获取属性集合*/
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.ColorCircleProgressView, 0, 0);

        /*渐变颜色值*/
//        mColor01 = typedArray.getColor(R.styleable.ColorCircleProgressView_Color01, color_01);
//        mColor02 = typedArray.getColor(R.styleable.ColorCircleProgressView_Color02, color_02);
//        mColor03 = typedArray.getColor(R.styleable.ColorCircleProgressView_Color03, color_03);
//        mColor04 = typedArray.getColor(R.styleable.ColorCircleProgressView_Color04, color_04);
//        mColor05 = typedArray.getColor(R.styleable.ColorCircleProgressView_Color05, color_05);
//        mColor06 = typedArray.getColor(R.styleable.ColorCircleProgressView_Color06, color_06);
//        mColor07 = typedArray.getColor(R.styleable.ColorCircleProgressView_Color07, color_07);

        /*圆环角度，开始的角度，到边框的距离*/
        mViewAangle = typedArray.getInteger(R.styleable.ColorCircleProgressView_ViewAngle, 180);
        mStartAangle = typedArray.getInteger(R.styleable.ColorCircleProgressView_StartAngle, 180);
        mViewPadding = typedArray.getInteger(R.styleable.ColorCircleProgressView_ViewPadding, 40);

        /*圆环的大小及是否圆角*/
        mStrokeWith = typedArray.getInteger(R.styleable.ColorCircleProgressView_StrokeWith, 40);
        mIsRound = typedArray.getBoolean(R.styleable.ColorCircleProgressView_IsRound, true);

        /*Point的颜色和大小*/
        mPointColor = typedArray.getColor(R.styleable.ColorCircleProgressView_PointColor, Color.WHITE);
        mPointColor1 = typedArray.getColor(R.styleable.ColorCircleProgressView_PointColor1, Color.WHITE);
        mPointRaido = typedArray.getInteger(R.styleable.ColorCircleProgressView_PointRadio, 10);
        mPointRaido1 = typedArray.getInteger(R.styleable.ColorCircleProgressView_PointRadio1, 1);



        /*设置圆环画笔*/
        SetPaint();

        /*设置移动点的画笔*/
        SetPaint01();

        typedArray.recycle();
    }

    private void SetPaint01() {
        mPaint1 = new Paint();
        mPaint1.setColor(mPointColor);

    }

    private void SetPaint() {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);  /*画笔为线条线条*/
        mPaint.setStrokeWidth(mStrokeWith);     /*线条的宽*/
        mPaint.setAntiAlias(true);               /*抗锯齿*/
        mPaint.setColor(Color.WHITE);
        if(mIsRound) {mPaint.setStrokeCap(Paint.Cap.ROUND);}  /*是否圆角*/
        mPaint2 = new Paint();
        mPaint2.setStyle(Paint.Style.STROKE);  /*画笔为线条线条*/
        mPaint2.setStrokeWidth(mStrokeWith);     /*线条的宽*/
        mPaint2.setAntiAlias(true);               /*抗锯齿*/
        mPaint2.setColor(Color.parseColor("#FC4D80"));
        if(mIsRound) {mPaint2.setStrokeCap(Paint.Cap.ROUND);}  /*是否圆角*/

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(Utils.dp2px(getContext(), 18));
        mTextPaint.setColor(Color.parseColor("#bbbbbb"));

        mTextPaint1 = new Paint();
        mTextPaint1.setAntiAlias(true);
        mTextPaint1.setTextSize(Utils.dp2px(getContext(), 25));
        mTextPaint1.setColor(Color.parseColor("#333333"));

    }
int width1 = 0;
//    String temp = "90℃";
    @Override
    protected void onDraw(Canvas canvas) {

        /*得到view的宽高*/
        int width = getWidth();
        int height = getHeight();
        width1 = getWidth()/2;
        /*把宽高赋值给全局变量,得到圆心的坐标*/
        mView_x0=width/2;
        mView_y0=height/2;
        String str = "65";
        canvas.drawText(str, mViewPadding+50, mView_y0+15, mTextPaint);
         String s = getResources().getText(R.string.equ_xq_temp).toString();
        canvas.drawText("95", width-mViewPadding-50- getTextWidth(str,mTextPaint), mView_y0+15, mTextPaint);
        canvas.drawText(s ,mView_x0-mViewPadding/2- getTextWidth(str,mTextPaint)/2,mView_y0-20,mTextPaint);
//        canvas.drawText(temp,mView_x0-mViewPadding/2- getTextWidth(temp,mTextPaint1)/2,mView_y0-50,mTextPaint1);
//        Log.i(TAG, (float) (mArcRadius + bDistance) - 2 * (mTextPaint.descent() + mTextPaint.ascent()) + "");

        /*设置线性渐变*/
//        SweepGradient sweepGradient = new SweepGradient(width/ 2, height/ 2, new int[]{mColor01, mColor02, mColor03, mColor04, mColor05, mColor06, mColor07}, null);
//        mPaint.setShader(sweepGradient);






        /*定义圆环的所占的矩形区域:注意view一定为正方形*/

        RectF   rectF = new RectF(0 + mViewPadding, 0 + mViewPadding, width - mViewPadding, width - mViewPadding);

        /*根据矩形区域画扇形:因为sweep的起点在右边中心处，所以先旋转90度画布*/
        canvas.rotate(90,width/2,height/2);
        canvas.drawArc(rectF, mStartAangle - 90, mViewAangle, false, mPaint);


        /*动态获取圆上起始点的坐标*/
        //圆点坐标：width/2,height/2
        //半径：（width-mViewPadding-mViewPadding）/2
        //角度：a0


        if(mPointAngle<=90){mPointAngle=90;}
        else if(mPointAngle>270&mPointAngle<=360){mPointAngle=270;}

        /*将45-315范围的角度转为0-100*/
//        if(mOnProgressListener!=null) {
//            int progress = (int)((mPointAngle - 90) / 2.7);
//            mOnProgressListener.onScrollingListener(progress);
//        }

        float x0=width/2;
        float y0=height/2;
        float R = (float) ((width - mViewPadding - mViewPadding) / 2);
        float Point_x= (float) (x0+R*Math.cos(mPointAngle*3.14/180));
        float Point_y= (float) (y0+R*Math.sin(mPointAngle * 3.14 / 180));
        canvas.drawArc(rectF, mStartAangle-90 , mPointAngle-90, false, mPaint2);
        Log.e("GGGGGGGGGGGGGG", "onDraw: -->"+mPointAngle );
        canvas.drawCircle(Point_x,Point_y,mPointRaido1,mPaint2);
        canvas.drawCircle(Point_x,Point_y,mPointRaido,mPaint1);


    }

    public int getTextWidth(String str,Paint paint){
        Rect rect = new Rect();
        paint.getTextBounds(str, 0, str.length(), rect);
        int w = rect.width();
//        int h = rect.height();
        return w;
    }

    public void setCurProgress(int curProgress) {
        this.mPointAngle = (int)(((float)curProgress-65)/25f*180)+90;

        Log.e("DDDDDD", "setCurProgress: -->"+mPointAngle +"...."+curProgress+";;;;"+(((float)curProgress-65)/25f*180));
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {


            /*获取点击位置的坐标*/
            float Action_x = event.getX();
            float Action_y = event.getY();

            /*根据坐标转换成对应的角度*/
            float get_x0 = Action_x - mView_x0;
            float get_y0 = Action_y - mView_y0;
            if (event.getAction()==MotionEvent.ACTION_MOVE&&isInCiecle(Action_x,Action_y)&&isTouch(Action_x,Action_y)) {


                /*01：左下角区域*/
                if (get_x0 <= 0 & get_y0 >= 0) {
                    float tan_x = get_x0 * (-1);
                    float tan_y = get_y0;
                    double atan = Math.atan(tan_x / tan_y);
                    mPointAngle = (int) Math.toDegrees(atan);
                }

                /*02：左上角区域*/
                if (get_x0 <= 0 & get_y0 <= 0) {
                    float tan_x = get_x0 * (-1);
                    float tan_y = get_y0 * (-1);
                    double atan = Math.atan(tan_y / tan_x);
                    mPointAngle = (int) Math.toDegrees(atan) + 90;
                }

                /*03：右上角区域*/
                if (get_x0 >= 0 & get_y0 <= 0) {
                    float tan_x = get_x0;
                    float tan_y = get_y0 * (-1);
                    double atan = Math.atan(tan_x / tan_y);
                    mPointAngle = (int) Math.toDegrees(atan) + 180;
                }

                /*04：右下角区域*/
                if (get_x0 >= 0 & get_y0 >= 0) {
                    float tan_x = get_x0;
                    float tan_y = get_y0;
                    double atan = Math.atan(tan_y / tan_x);
                    mPointAngle = (int) Math.toDegrees(atan) + 270;
                }
                if(mOnProgressListener!=null) {

                    mOnProgressListener.onScrollingListener(65+(mPointAngle-90)*30/180);
                }

                /*得到点的角度后进行重绘*/
                invalidate();
            }

        return true;
    }
    /**判断落点是否在圆环上*/
    public boolean isInCiecle(float x,float y){
        Log.i("x","-->"+x);
        Log.i("y","-->"+y);
        float distance = (float) Math.sqrt((x-width1)*(x-width1)+(y-width1)*(y-width1));
        Log.i("distance","-->"+distance);
        int smallCircleRadus=width1-100;
        Log.i("smallCircleRadus","-->"+smallCircleRadus);
        float get_x0 = x - mView_x0;
        float get_y0 = y - mView_y0;

        if (distance>=smallCircleRadus && distance<=width1 &&((get_x0 <= 0 & get_y0 <= 0)||(get_x0 >= -10 & get_y0 <= 10)))
            return true;
        else
            return false;
    }

    private boolean isTouch(float x, float y) {
        double radius = (getWidth() - getPaddingLeft() - getPaddingRight() + getCircleWidth()) / 2;
        double centerX = getWidth() / 2;
        double centerY = getHeight() / 2;
        return Math.pow(centerX - x, 2) + Math.pow(centerY - y, 2) < radius * radius;
    }
private float getCircleWidth() {
    return Math.max(50, Math.max(50, 25));
}

    public interface OnProgressListener{

        public void onScrollingListener( Integer progress);

    }



}
