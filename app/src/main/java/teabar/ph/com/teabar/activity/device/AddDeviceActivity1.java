package teabar.ph.com.teabar.activity.device;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.gifdecoder.GifDecoder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.ph.teabar.database.dao.DaoImp.EquipmentImpl;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.Unbinder;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import teabar.ph.com.teabar.R;
import teabar.ph.com.teabar.activity.MainActivity;
import teabar.ph.com.teabar.base.BaseActivity;
import teabar.ph.com.teabar.base.BaseWeakAsyncTask;
import teabar.ph.com.teabar.base.MyApplication;
import teabar.ph.com.teabar.esptouch.EspWifiAdminSimple;
import teabar.ph.com.teabar.esptouch.EsptouchTask;
import teabar.ph.com.teabar.esptouch.IEsptouchListener;
import teabar.ph.com.teabar.esptouch.IEsptouchResult;
import teabar.ph.com.teabar.esptouch.IEsptouchTask;
import teabar.ph.com.teabar.pojo.Equpment;
import teabar.ph.com.teabar.service.MQService;
import teabar.ph.com.teabar.util.HttpUtils;
import teabar.ph.com.teabar.util.IsChinese;
import teabar.ph.com.teabar.util.ToastUtil;
import teabar.ph.com.teabar.util.view.ScreenSizeUtils;

//從推薦茶頁面 跳轉過來的添加設備頁面，供用戶展示配置設備的方式方法
public class AddDeviceActivity1 extends BaseActivity implements  EasyPermissions.PermissionCallbacks{
    MyApplication application;
    Unbinder unbinder;
    @BindView(R.id.et_add_name)
    EditText et_add_name;
    @BindView(R.id.et_add_pass)
    EditText et_add_pass;
    @BindView(R.id.li_device_ask)
    LinearLayout li_device_ask;
    @BindView(R.id.bt_device_add)
    Button bt_device_add;

    private EspWifiAdminSimple mWifiAdmin;
    String deviceMac,userId;
    QMUITipDialog tipDialog;
    SharedPreferences preferences;
    private  boolean clockisBound;
    EquipmentImpl equipmentDao;
    private static final int MESSAGE_SUCCESS1 = 1;
    private static final int MESSAGE_SUCCESS2 = 2;
    List<Equpment> list ;
    @Override
    public void initParms(Bundle parms) {

    }

    @Override
    public int bindLayout() {
        setSteepStatusBar(true);
        return R.layout.activity_adddevice1;
    }

    @Override
    public void initView(View view) {
        mWifiAdmin = new EspWifiAdminSimple(this);
        if (application == null) {
            application = (MyApplication) getApplication();
        }
        application.addActivity(this);
        equipmentDao = new EquipmentImpl(getApplicationContext());
        list = equipmentDao.findAll();
        preferences = getSharedPreferences("my", MODE_PRIVATE);
        userId = preferences.getString("userId","")+"";
        //绑定services
        clockintent = new Intent(AddDeviceActivity1.this, MQService.class);
        clockisBound = bindService(clockintent, clockconnection, Context.BIND_AUTO_CREATE);
        startService(clockintent);
        registerBroadcastReceiver();
        et_add_name.setEnabled(false);
        permissionGrantedSuccess();
    }
    Intent clockintent;
    MQService clcokservice;
    boolean boundclock;
    ServiceConnection clockconnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MQService.LocalBinder binder = (MQService.LocalBinder) service;
            clcokservice = binder.getService();
            boundclock = true;
            Log.e("QQQQQQQQQQQDDDDDDD", "onServiceConnected: ------->" );
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

    @Override
    protected void onStart() {
        super.onStart();
//        String ssid=mWifiAdmin.getWifiConnectedSsid();
//        et_add_name.setText(ssid);
//        et_add_name.setEnabled(false);
        permissionGrantedSuccess();
    }
    private boolean mReceiverRegistered = false;
    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 把执行结果的操作给EasyPermissions
        System.out.println(requestCode);
        if (isNeedCheck) {
            EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }
    private static final int RC_CAMERA_AND_LOCATION = 0;
    @AfterPermissionGranted(RC_CAMERA_AND_LOCATION)
    private void permissionGrantedSuccess() {
        String[] perms = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        if (EasyPermissions.hasPermissions(this, perms)) {

        } else {
//             没有申请过权限，现在去申请
            EasyPermissions.requestPermissions(this, getString(R.string.location),
                    RC_CAMERA_AND_LOCATION, perms);
        }
    }

    boolean isNeedCheck=true;
    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog
                    .Builder(this)
                    .setTitle(getText(R.string.dialog_add_qx).toString())
                    .setRationale( getText(R.string.dialog_add_qx1).toString())
                    .setPositiveButton(getText(R.string.dialog_add_qx2).toString())
                    .setNegativeButton(getText(R.string.dialog_add_qx3).toString())
                    .build()
                    .show();
            isNeedCheck = false;
        }
    }

    private boolean isSDKAtLeastP() {
        return Build.VERSION.SDK_INT >= 28;
    }
    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        if (isSDKAtLeastP()) {
            filter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
        }
        registerReceiver(mReceiver, filter);
        mReceiverRegistered = true;
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                return;
            }
            WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                    .getSystemService(WIFI_SERVICE);
            assert wifiManager != null;
            switch (action) {
                case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                    WifiInfo wifiInfo;
                    if (intent.hasExtra(WifiManager.EXTRA_WIFI_INFO)) {
                        wifiInfo = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
                    } else {
                        wifiInfo = wifiManager.getConnectionInfo();
                    }
                    onWifiChanged(wifiInfo);
                    break;
                case LocationManager.PROVIDERS_CHANGED_ACTION:
                    onWifiChanged(wifiManager.getConnectionInfo());
                    break;
            }
        }
    };
    String bSsid;

    private void onWifiChanged(WifiInfo info) {

        if (info == null) {
            et_add_name.setText("");
            et_add_pass.setText("");
            ToastUtil.showShort(AddDeviceActivity1.this, "WiFi已中断，请连接WiFi重新配置");
            if (mEsptouchTask != null) {
                mEsptouchTask.interrupt();
            }
            if (tipDialog != null && tipDialog.isShowing()) {

                et_add_name.setEnabled(true);
                et_add_pass.setEnabled(true);
                bt_device_add.setEnabled(true);
                tipDialog.dismiss();

            }
        } else {
            String apSsid = info.getSSID();
            bSsid = info.getBSSID();
            if (apSsid.startsWith("\"") && apSsid.endsWith("\"")) {
                apSsid = apSsid.substring(1, apSsid.length() - 1);
                if ("<unknown ssid>".equals(apSsid)) {
                    et_add_name.setText("");
                    et_add_pass.setText("");
                }
            }
            SharedPreferences wifi = getSharedPreferences("wifi", MODE_PRIVATE);
            if (wifi.contains(apSsid)) {
                et_add_name.setText(apSsid);
                String pswd = wifi.getString(apSsid, "");
                et_add_pass.setText(pswd);
            } else {
                et_add_name.setText(apSsid);
                et_add_pass.setText("");
                if ("<unknown ssid>".equals(apSsid)) {
                    et_add_name.setText("");
                    et_add_pass.setText("");
                }
            }
            if (!TextUtils.isEmpty(apSsid)) {
                if (apSsid.contains("+") || apSsid.contains("/") || apSsid.contains("#")) {
                    et_add_name.setText("");
                    ToastUtil.showShort(AddDeviceActivity1.this, "WiFi名称为不含有+/#特殊符号的英文");
                } else {
                    char[] chars = apSsid.toCharArray();
                    et_add_name.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ToastUtil.showShort(AddDeviceActivity1.this, "WiFi名称不可编辑");
                        }
                    });

                    for (char c : chars) {
                        if (IsChinese.isChinese(c)) {
                            ToastUtil.showShort(AddDeviceActivity1.this, "WiFi名称不能是中文");
                            et_add_name.setText("");
                            et_add_pass.setText("");
                            break;
                        }
                    }
                }
            } else {
                et_add_name.setText("");
                et_add_name.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ToastUtil.showShort(AddDeviceActivity1.this, "请连接英文名称的wifi");
                    }
                });
                et_add_pass.setText("");
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int frequence = info.getFrequency();
                if (frequence > 4900 && frequence < 5900) {
                    // Connected 5G wifi. Device does not support 5G
                    et_add_name.setText("");
                    et_add_name.setHint(getText(R.string.toast_wifi_5G).toString());
                    et_add_pass.setText("");

                }
            }
            if (isMatching && !TextUtils.isEmpty(wifiName) && !wifiName.equals(apSsid)) {
                isMatching = false;
                wifiName = "";
                ToastUtil.showShort(AddDeviceActivity1.this, "WiFi已切换,请重新配置");
                if (mEsptouchTask != null) {
                    mEsptouchTask.interrupt();
                }
                if (tipDialog != null && tipDialog.isShowing()) {
                    et_add_name.setEnabled(true);
                    et_add_pass.setEnabled(true);
                    bt_device_add.setEnabled(true);
                    tipDialog.dismiss();

                }
            }
        }
    }
    boolean isMatching =false;
    String wifiName ="";

    @OnClick({R.id.bt_device_add,R.id.li_device_ask  ,R.id.bt_add_main})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.bt_device_add:
                String ssid=et_add_name.getText().toString().trim();
                String apBssid=mWifiAdmin.getWifiConnectedBssid();
                String apPassword=et_add_pass.getText().toString();
                String taskResultCountStr = "1";
                if (TextUtils.isEmpty(ssid)){
                    toast(getText(R.string.toast_wifi_name).toString());
                    break;
                }
                if (TextUtils.isEmpty(apPassword)){
                    toast(getText(R.string.toast_forget_pass).toString());
                    break;
                }
                isMatching = true;
                wifiName = ssid;
                new EsptouchAsyncTask3().execute(ssid, apBssid, apPassword, taskResultCountStr);
            break;

            case R.id.li_device_ask:
                customDialog();
                break;

            case R.id.bt_add_main:
                application.removeActivity(this);
                startActivity(MainActivity.class);
                break;

        }
    }

    /**
     * 自定义对话框
     */
    Dialog dialog;
    int position ;
    ImageView imageView;
    private void customDialog() {
        dialog  = new Dialog(this, R.style.MyDialog);
        View view = View.inflate(this, R.layout.dialog_playgif, null);
        imageView = view.findViewById(R.id.iv_dia_gif);
        playGif1(0);
        dialog.setContentView(view);
        //使得点击对话框外部不消失对话框
        dialog.setCanceledOnTouchOutside(true);
        //设置对话框的大小
        view.setMinimumHeight((int) (ScreenSizeUtils.getInstance(this).getScreenHeight() * 0.43f));
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = (int) (ScreenSizeUtils.getInstance(this).getScreenWidth() * 0.75f);
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        dialogWindow.setAttributes(lp);
        dialog.show();

    }

    /**
     * 播放Gif动画
     * */
    int duration = 0;
    int gif[] = {R.mipmap.adddevice1,R.mipmap.adddevice2};
    int gif1[] = {R.mipmap.enadddevice1,R.mipmap.enadddevice2};
    public void playGif1(final int i){
        int mygif[] ;
        if (application.IsEnglish()==0){
            mygif =gif;
        }else {
            mygif =gif1;
        }
        duration=0;
        Glide.with(this)
                .load(mygif[i])
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .listener(new RequestListener<Integer, GlideDrawable>() {

                    @Override
                    public boolean onException(Exception arg0, Integer arg1,
                                               Target<GlideDrawable> arg2, boolean arg3) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource,
                                                   Integer model, Target<GlideDrawable> target,
                                                   boolean isFromMemoryCache, boolean isFirstResource) {
                        // 计算动画时长
                        GifDrawable drawable = (GifDrawable) resource;
                        GifDecoder decoder = drawable.getDecoder();
                        for (int i = 0; i < drawable.getFrameCount(); i++) {
                            duration += decoder.getDelay(i);
                        }
                        //发送延时消息，通知动画结束
                        if (i==0){
                            Log.e(TAG, "onResourceReady1: --》"+duration );
                            handler.sendEmptyMessageDelayed(MESSAGE_SUCCESS1,
                                    duration);
                        }else {
                            Log.e(TAG, "onResourceReady2: --》"+duration );
                            handler.sendEmptyMessageDelayed(MESSAGE_SUCCESS2,
                                    duration);
                        }

                        return false;
                    }
                }) //仅仅加载一次gif动画
                .into(new GlideDrawableImageViewTarget(imageView, 1));

    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what==MESSAGE_SUCCESS1){
                playGif1(1);
            }
            if (msg.what==MESSAGE_SUCCESS2){
                playGif1(0);
            }
        }
    };


    /**
     * 添加设备
     * */
    String returnMsg1,returnMsg2;

    class addDeviceAsyncTask extends BaseWeakAsyncTask<Map<String,Object>,Void,String,BaseActivity> {

        public addDeviceAsyncTask(BaseActivity baseActivity) {
            super(baseActivity);
        }

        @Override
        protected String doInBackground(BaseActivity baseActivity, Map<String, Object>... maps) {

            String code = "";
            Map<String ,Object> prarms = maps[0];
            String result = HttpUtils.postOkHpptRequest(HttpUtils.ipAddress+"/app/addDevice",prarms);
            Log.e("back", "--->"+result);
            if (!ToastUtil.isEmpty(result)){
                if (!"4000".equals(result)){
                try {
                    JSONObject jsonObject= new JSONObject(result);
                    code = jsonObject.getString("state");
                    returnMsg1=jsonObject.getString("message2");
                    returnMsg2=jsonObject.getString("message3");

//                    JSONObject returnData = jsonObject.getJSONObject("returnData");
                    if ("200".equals(code)){

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                }else {
                    code="4000";
                }
            }
            return code;
        }

        @Override
        protected void onPostExecute(BaseActivity baseActivity, String s) {
            switch (s) {
                case "4000":
                    toast(getText(R.string.toast_all_cs).toString());
                    if (tipDialog.isShowing()){
                        tipDialog.dismiss();
                    }
                break;

                case "200":
                    if (application.IsEnglish()==0){
                        toast(returnMsg1);
                    }else {
                        toast(returnMsg2);
                    }
                    Map<String,Object> params = new HashMap<>();
                    params.put("userId",userId);
                    new  FindDeviceAsynTask(AddDeviceActivity1.this).execute(params);

                    break;
                    default:
                        if (tipDialog.isShowing()){
                            tipDialog.dismiss();
                        }
                        toast(returnMsg1);
                        break;

            }
        }


    }


    class FindDeviceAsynTask extends BaseWeakAsyncTask<Map<String,Object>,Void,String,BaseActivity> {

        public FindDeviceAsynTask(BaseActivity baseActivity) {
            super(baseActivity);
        }

        @Override
        protected String doInBackground(BaseActivity baseActivity, Map<String, Object>... maps) {
            String code = "";
            Map<String, Object> prarms = maps[0];
            String result =   HttpUtils.postOkHpptRequest(HttpUtils.ipAddress+"/app/showUserDevice",prarms);

            Log.e("back", "--->" + result);
            if (!ToastUtil.isEmpty(result)) {
                if (!"4000".equals(result)){
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        code = jsonObject.getString("state");
                        returnMsg1=jsonObject.getString("message1");
                        if ("200".equals(code)) {
                            equipmentDao.deleteAll();
                            JSONArray returnData = jsonObject.getJSONArray("data");
                            if (returnData.length()>0){
                                for ( int i =0;i<returnData.length();i++){
                                    JSONObject jsonObject1 = returnData.getJSONObject(i);
                                    Equpment equpment = new Equpment();
                                    equpment.setMacAdress(jsonObject1.getString("mac"));
                                    equpment.setEqupmentId(jsonObject1.getLong("id"));
                                    equpment.setName(jsonObject1.getString("deviceName"));
                                    int flag = jsonObject1.getInt("flag");
                                    if (flag==1){
                                        equpment.setIsFirst(true);

                                    }else {
                                        equpment.setIsFirst(false);
                                    }
                                    equpment.setOnLine(false);
                                    equpment.setMStage(-1);
                                    equipmentDao.insert(equpment);
                                }
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else {
                    code="4000";
                }
            }

            return code;
        }

        @Override
        protected void onPostExecute(BaseActivity baseActivity, String s) {

            switch (s) {

                case "200":
                    if (tipDialog!=null&&tipDialog.isShowing()){
                        tipDialog.dismiss();
                    }
                    application.removeActivity(AddDeviceActivity1.this);
                    startActivity(MainActivity.class);
                    break;
                case "4000":
                    if (tipDialog!=null&&tipDialog.isShowing()){
                        tipDialog.dismiss();
                    }
                    toast(getText(R.string.toast_all_cs).toString());
                    break;
                default:
                    if (tipDialog!=null&&tipDialog.isShowing()){
                        tipDialog.dismiss();
                    }
//                    toast( returnMsg1);
                    break;

            }
        }
    }




    //显示dialog
    public void showProgressDialog(String message) {

        tipDialog = new QMUITipDialog.Builder(this)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord(message)
                .create();
        tipDialog.show();
        tipDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (mEsptouchTask!=null){
                    mEsptouchTask.interrupt();
                }

                toast(getText(R.string.toast_main_file).toString());
            }
        });

    }

    /**
     * WIFI模块配置
     * */

    private IEsptouchTask mEsptouchTask;

    private class EsptouchAsyncTask3 extends AsyncTask<String, Void, List<IEsptouchResult>> {


        // without the lock, if the user tap confirm and cancel quickly enough,
        // the bug will arise. the reason is follows:
        // 0. task is starting created, but not finished
        // 1. the task is cancel for the task hasn't been created, it do nothing
        // 2. task is created
        // 3. Oops, the task should be cancelled, but it is running
        private final Object mLock = new Object();

        @Override
        protected void onPreExecute() {

            showProgressDialog(getText(R.string.search_qsh).toString());


        }

        @Override
        protected List<IEsptouchResult> doInBackground(String... params) {
            int taskResultCount = -1;
            synchronized (mLock) {
                // !!!NOTICE
                String apSsid = params[0];
                String apBssid = params[1];
                String apPassword = params[2];
                String taskResultCountStr = params[3];
                taskResultCount = Integer.parseInt(taskResultCountStr);
                mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword, AddDeviceActivity1.this);
                mEsptouchTask.setEsptouchListener(myListener);
            }
            List<IEsptouchResult> resultList = mEsptouchTask.executeForResults(taskResultCount);
            return resultList;
        }

        @Override
        protected void onPostExecute(List<IEsptouchResult> result) {
            IEsptouchResult firstResult = result.get(0);
            // check whether the task is cancelled and no results received
            if (!firstResult.isCancelled()) {
                int count = 0;
                // max results to be displayed, if it is more than maxDisplayCount,
                // just show the count of redundant ones
                final int maxDisplayCount = 5;
                // the task received some results including cancelled while
                // executing before receiving enough results
                if (firstResult.isSuc()) {
                    StringBuilder sb = new StringBuilder();
                    try {
                        Thread.sleep(300);
                        Log.i("IEsptouchResult", "-->" + result.size());
                        for (IEsptouchResult resultInList : result) {
                            //                String ssid=et_add_name.getText().toString();
                            String ssid = resultInList.getBssid();
                            sb.append("配置成功" + ssid);

                            String onlineTopicName = "tea/" +wifiName+ ssid +"/status/transfer";
                            String offlineTopicName = "tea/" + wifiName+ssid + "/lwt";
                            String operate = "tea/"+wifiName+ssid+"/operate/transfer";
                            String extra = "tea/"+wifiName+ssid+"/extra/transfer";
                            String reset = "tea/"+wifiName+ssid+"/reset/transfer";
                            clcokservice.subscribe(onlineTopicName,1);
                            clcokservice.subscribe(offlineTopicName,1);
                            clcokservice.subscribe(operate,1);
                            clcokservice.subscribe(extra,1);
                            clcokservice.subscribe(reset,1);
//                            clcokservice.sendFindEqu(wifiName+ssid);
                            deviceMac = wifiName+ssid;
                            if (!TextUtils.isEmpty(ssid)) {
                                Map<String,Object> params = new HashMap<>();
                                 params.put("userId",userId);
                                 params.put("mac",deviceMac);
                                new addDeviceAsyncTask(AddDeviceActivity1.this).execute(params);
                                Log.e(TAG, "onPostExecute: -->ssid"+ deviceMac );
                                break;
                            }
                            count++;
                            if (count >= maxDisplayCount) {
                                break;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (count < result.size()) {
                        sb.append("\nthere's " + (result.size() - count)
                                + " more result(s) without showing\n");
                    }
                } else {
                    if (tipDialog!=null&&tipDialog.isShowing()){
                        tipDialog.dismiss();
                    }
                    Toast.makeText(AddDeviceActivity1.this,getText(R.string.toast_main_file).toString() , Toast.LENGTH_LONG).show();
                }
            }
        }
    }
    private void onEsptoucResultAddedPerform(final IEsptouchResult result) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                String text = result.getBssid() + " is connected to the wifi";
//                Toast.makeText(AddDeviceActivity.this, text,
//                        Toast.LENGTH_LONG).show();
            }

        });
    }

    private IEsptouchListener myListener = new IEsptouchListener() {

        @Override
        public void onEsptouchResultAdded(final IEsptouchResult result) {
            onEsptoucResultAddedPerform(result);
        }
    };
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(unbinder!=null)
            unbinder.unbind();//解绑注解
        if (handler!=null)
            handler.removeCallbacksAndMessages(null);
        if (clockisBound)
            unbindService(clockconnection);
        if (mReceiver!=null){
            unregisterReceiver(mReceiver);
        }
    }

}
