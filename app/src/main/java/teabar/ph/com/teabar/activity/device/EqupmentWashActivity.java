package teabar.ph.com.teabar.activity.device;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jaygoo.widget.OnRangeChangedListener;
import com.jaygoo.widget.RangeSeekBar;

import butterknife.BindView;
import butterknife.OnClick;
import me.jessyan.autosize.utils.ScreenUtils;
import teabar.ph.com.teabar.R;
import teabar.ph.com.teabar.base.BaseActivity;
import teabar.ph.com.teabar.base.MyApplication;
import teabar.ph.com.teabar.pojo.Equpment;
import teabar.ph.com.teabar.service.MQService;

public class EqupmentWashActivity extends BaseActivity {
    MyApplication application;
    @BindView(R.id.tv_main_1)
    TextView tv_main_1;
    @BindView(R.id.iv_equ_fh)
    ImageView iv_equ_fh;
    @BindView(R.id.tv_wash_number)
    TextView tv_wash_number;
    private RangeSeekBar seekbar1;
    private boolean MQBound;
    int number;
    Equpment equpment;
    @Override
    public void initParms(Bundle parms) {
        equpment = (Equpment) parms.getSerializable("equpment");
    }

    @Override
    public int bindLayout() {
        setSteepStatusBar(true);
        return R.layout.activity_wash;
    }

    @Override
    public void initView(View view) {
        if (application == null) {
            application = (MyApplication) getApplication();
        }
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                ScreenUtils.getStatusBarHeight());
        tv_main_1.setLayoutParams(params);
        application.addActivity(this);
        seekbar1 = findViewById(R.id.seekbar1);
        seekbar1.setValue(18);
        seekbar1.setOnRangeChangedListener(new OnRangeChangedListener() {
            @Override
            public void onRangeChanged(RangeSeekBar view, float leftValue, float rightValue, boolean isFromUser) {
                tv_wash_number.setText((int) leftValue+"");
                number = (int) leftValue;
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
        MQintent = new Intent(this, MQService.class);
        MQBound = bindService(MQintent, MQconnection, Context.BIND_AUTO_CREATE);

    }

    Intent MQintent;
    MQService MQservice;
    boolean boundservice;
    ServiceConnection MQconnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MQService.LocalBinder binder = (MQService.LocalBinder) service;
            MQservice = binder.getService();
            boundservice = true;
            Log.e("QQQQQQQQQQQDDDDDDD", "onServiceConnected: ------->");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Override
    public void doBusiness(Context mContext) {

    }

    @Override
    public void widgetClick(View v) {

    }
    @OnClick({R.id.iv_equ_fh,R.id.bt_wash_esure })
    public void onClick(View view){
        switch (view.getId()){
            case R.id.iv_equ_fh:
                finish();
                break;

            case R.id.bt_wash_esure:
                MQservice.sendWashNum(equpment.getMacAdress(),number);
                toast(getText(R.string.toast_equ_wash).toString());
                finish();
                break;

        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (MQBound) {
            unbindService(MQconnection);
        }

    }
}
