package teabar.ph.com.teabar.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.CustomListener;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.jaygoo.widget.OnRangeChangedListener;
import com.jaygoo.widget.RangeSeekBar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.OnClick;
import me.jessyan.autosize.AutoSizeCompat;
import teabar.ph.com.teabar.R;
import teabar.ph.com.teabar.base.BaseActivity;
import teabar.ph.com.teabar.base.MyApplication;
import teabar.ph.com.teabar.util.ToastUtil;

//設置個人喝茶時間，和沖泡茶次數
public class SetDrinkActivity extends BaseActivity {


    @BindView(R.id.iv_power_fh)
    ImageView iv_power_fh;
    @BindView(R.id.tv_drink_num1)
    TextView tv_drink_num1;
    @BindView(R.id.iv_drink_open)
    ImageView iv_drink_open;
    @BindView(R.id.tv_drink_time)
    TextView tv_drink_time;
    MyApplication application;
    boolean isOpen = true;
    private RangeSeekBar seekbar1;
    private TimePickerView pvCustomTime;
    SharedPreferences alermPreferences;

    @Override
    public void initParms(Bundle parms) {

    }

    @Override
    public int bindLayout() {
        setSteepStatusBar(true);
        return R.layout.activity_setdrink;
    }

    @Override
    public void initView(View view) {


        if (application == null) {
            application = (MyApplication) getApplication();
        }
     alermPreferences=getSharedPreferences("alerm",MODE_PRIVATE);
        int num = alermPreferences.getInt("setdrink",4);
        if (alermPreferences.contains("time")){
            String time = alermPreferences.getString("time", "");
            tv_drink_time.setText(time);
            isOpen=alermPreferences.getBoolean("open", false);
            if (isOpen){
                iv_drink_open.setImageResource(R.mipmap.equ_open);
            }else {
                iv_drink_open.setImageResource(R.mipmap.equ_close);
            }
        }
        application.addActivity(this);
        initCustomTimePicker();
        seekbar1 = findViewById(R.id.seekbar2);
        seekbar1.setValue(num);
        tv_drink_num1.setText(num+"");
        seekbar1.setOnRangeChangedListener(new OnRangeChangedListener() {
            @Override
            public void onRangeChanged(RangeSeekBar view, float leftValue, float rightValue, boolean isFromUser) {
                tv_drink_num1.setText((int) leftValue+"");

            }

            @Override
            public void onStartTrackingTouch(RangeSeekBar view,  boolean isLeft) {
                //do what you want!!
            }

            @Override
            public void onStopTrackingTouch(RangeSeekBar view,  boolean isLeft) {
                //do what you want!!


            }
        });



    }

    @Override
    public Resources getResources() {
        //需要升级到 v1.1.2 及以上版本才能使用 AutoSizeCompat
//        AutoSizeCompat.autoConvertDensityOfGlobal((super.getResources()));//如果没有自定义需求用这个方法
        AutoSizeCompat.autoConvertDensity((super.getResources()), 667, false);//如果有自定义需求就用这个方法
        return super.getResources();

    }

    @Override
    public void doBusiness(Context mContext) {

    }

    @Override
    public void widgetClick(View v) {

    }

    @OnClick({ R.id.iv_power_fh ,R.id.rl_drin_remind, R.id.tv_drink_time,R.id.btn_submit})
    public void onClick(View view){
        switch (view.getId()){

            case R.id.iv_power_fh:
                finish();
                break;

            case R.id.rl_drin_remind:

                if (isOpen){
                    iv_drink_open.setImageResource(R.mipmap.equ_close);
                    isOpen=false;
                }else {
                    iv_drink_open.setImageResource(R.mipmap.equ_open);
                    isOpen=true;
                }
                if (alermPreferences.contains("open")){
                    SharedPreferences.Editor editor=alermPreferences.edit();
                    editor.putBoolean("open",isOpen);
                    editor.commit();
                }
                break;

            case R.id.tv_drink_time:
                pvCustomTime.show();
                break;
            case R.id.btn_submit:
                String time=tv_drink_time.getText().toString();
                SharedPreferences.Editor editor=alermPreferences.edit();
                editor.clear();
                if (!TextUtils.isEmpty(time)){
                    editor.putString("time",time);
                    editor.putBoolean("open",isOpen);
                    if (editor.commit()){
                        ToastUtil.showShort(this,getText(R.string.toast_add_cg).toString());
                    }
                }
                editor.putInt("setdrink",Integer.valueOf(tv_drink_num1.getText().toString()));
                editor.commit();
                Intent intent = new Intent();
                intent.putExtra("num",tv_drink_num1.getText());
                setResult(100,intent);
                finish();
                break;

        }
    }



    private void initCustomTimePicker() {

        /**
         * @description
         *
         * 注意事项：
         * 1.自定义布局中，id为 optionspicker 或者 timepicker 的布局以及其子控件必须要有，否则会报空指针.
         * 具体可参考demo 里面的两个自定义layout布局。
         * 2.因为系统Calendar的月份是从0-11的,所以如果是调用Calendar的set方法来设置时间,月份的范围也要是从0-11
         * setRangDate方法控制起始终止时间(如果不设置范围，则使用默认时间1900-2100年，此段代码可注释)
         */
        Calendar selectedDate = Calendar.getInstance();//系统当前时间
        Calendar startDate = Calendar.getInstance();
//        startDate.set(2014, 1, 23);
        Calendar endDate = Calendar.getInstance();
//        endDate.set(selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH));
        //时间选择器 ，自定义布局
        pvCustomTime = new TimePickerBuilder(this, new OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {//选中事件回调
                String time = getTime(date);
                tv_drink_time.setText(time);
            }
        })
                /*.setType(TimePickerView.Type.ALL)//default is all
                .setCancelText("Cancel")
                .setSubmitText("Sure")
                .setContentTextSize(18)
                .setTitleSize(20)
                .setTitleText("Title")
                .setTitleColor(Color.BLACK)
               /*.setDividerColor(Color.WHITE)//设置分割线的颜色

                .setLineSpacingMultiplier(1.6f)//设置两横线之间的间隔倍数
                .setTitleBgColor(Color.DKGRAY)//标题背景颜色 Night mode
                .setBgColor(Color.BLACK)//滚轮背景颜色 Night mode
                .setSubmitColor(Color.WHITE)
                .setCancelColor(Color.WHITE)*/
                /*.animGravity(Gravity.RIGHT)// default is center*/
                .setTextColorCenter(Color.parseColor("#00dfad"))//设置选中项的颜色
                .setTitleBgColor(Color.WHITE)
                .setOutSideCancelable(false)//点击屏幕，点在控件外部范围时，是否取消显示
                .setDate(selectedDate)
//                .setRangDate(startDate, endDate)
                .setLayoutRes(R.layout.pickerview_custom_time, new CustomListener() {

                    @Override
                    public void customLayout(View v) {
                        final TextView tvSubmit = (TextView) v.findViewById(R.id.tv_finish);
                        TextView ivCancel = (TextView) v.findViewById(R.id.iv_cancel);
                        tvSubmit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pvCustomTime.returnData();
                                pvCustomTime.dismiss();
                            }
                        });
                        ivCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pvCustomTime.dismiss();
                            }
                        });
                    }
                })
                .setContentTextSize(18)
                .setDividerColor(Color.TRANSPARENT)//设置分割线的颜色
                .setType(new boolean[]{ false, false, false,true, true, false})
                .setLabel("","","","", "","")
                .setLineSpacingMultiplier(1.2f)
                .setTextXOffset(0, 0,0, 0,0,0)
                .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
                .build();

    }
    private String getTime(Date date) {//可根据需要自行截取数据显示
        Log.d("getTime()", "choice date millis: " + date.getTime());
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        return format.format(date);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
