package teabar.ph.com.teabar.activity.device;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.CallbackManager;
import com.facebook.share.model.ShareHashtag;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.gson.Gson;
import com.ph.teabar.database.dao.DaoImp.EquipmentImpl;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import me.jessyan.autosize.AutoSizeCompat;
import teabar.ph.com.teabar.R;
import teabar.ph.com.teabar.activity.BuyNowActivity;
import teabar.ph.com.teabar.base.BaseActivity;
import teabar.ph.com.teabar.base.BaseWeakAsyncTask;
import teabar.ph.com.teabar.base.MyApplication;
import teabar.ph.com.teabar.pojo.Equpment;
import teabar.ph.com.teabar.pojo.Tea;
import teabar.ph.com.teabar.service.MQService;
import teabar.ph.com.teabar.util.HttpUtils;
import teabar.ph.com.teabar.util.ToastUtil;
import teabar.ph.com.teabar.util.view.ScreenSizeUtils;
import teabar.ph.com.teabar.view.WaveProgress;

//沖泡頁面
public class MakeActivity extends BaseActivity {

    @BindView(R.id.tv_make_name)
    TextView tv_make_name;
    @BindView(R.id.tv_make_style)
    TextView tv_make_style;
    @BindView(R.id.tv_make_mes)
    TextView tv_make_mes;
    @BindView(R.id.tv_make_plmes)
    TextView tv_make_plmes;
    @BindView(R.id.tv_make_temp)
    TextView tv_make_temp;
    @BindView(R.id.tv_make_sl)
    TextView tv_make_sl;
    @BindView(R.id.tv_make_time)
    TextView tv_make_time;

    @BindView(R.id.iv_make_pic)
    ImageView iv_make_pic;
    @BindView(R.id.bt_make_make)
    Button bt_make_make;
    @BindView(R.id.iv_make_choose)
    ImageView iv_make_choose;
    @BindView(R.id.iv_make_love)
    ImageView iv_make_love;
    @BindView(R.id.tv_toast)
    TextView tv_toast;
    @BindView(R.id.tv_toast_mes)
    TextView tv_toast_mes;

    @BindView(R.id.tv_mes_title)
    TextView tv_mes_title;
    @BindView(R.id.tv_plmes_title)
    TextView tv_plmes_title;
    @BindView(R.id.tv_tea_desc)
    TextView tv_tea_desc;
    private boolean MQBound;
    MyApplication application;
    EquipmentImpl equipmentDao;//數據庫設備表管理者
    List<Equpment> equpments;//用戶所有的設備
    String firstMac;//第一個設備的mac地址
    Equpment Firstequpment;//第一個設備
    public static boolean isRunning = false;//該頁面是否可見
    Tea tea;
    long teaId= -1;//茶id
    String userId;//用戶id
    SharedPreferences preferences;//用戶個人信息
    QMUITipDialog tipDialog;//加載數據對話框
    String id ;
    long FirstId;
    MessageReceiver receiver;
    CallbackManager callbackManager;
    String shopUrl;
    ShareDialog shareDialog;
    @Override
    public void initParms(Bundle parms) {
        teaId = parms.getLong("teaId");
    }

    @Override
    public int bindLayout() {
        setSteepStatusBar(true);
        return R.layout.activity_make;
    }
    @Override
    public Resources getResources() {
        //需要升级到 v1.1.2 及以上版本才能使用yi以宽度适配 AutoSizeCompat
//        AutoSizeCompat.autoConvertDensityOfGlobal((super.getResources()));//如果没有自定义需求用这个方法
      AutoSizeCompat.autoConvertDensity((super.getResources()), 360, true);//如果有自定义需求就用这个方法
        return super.getResources();
    }

    @Override
    public void initView(View view) {
        if (application == null) {
            application = (MyApplication) getApplication();
        }
        application.addActivity(this);

        preferences = getSharedPreferences("my",MODE_PRIVATE);
        userId = preferences.getString("userId","");
        shopUrl = preferences.getString("shopUrl","");
        if (TextUtils.isEmpty(shopUrl)){
            new  GetwebAsyncTask(this).execute();
        }
        equipmentDao = new EquipmentImpl(getApplicationContext());
        equpments= equipmentDao.findAll();
        bt_make_make.setVisibility(View.VISIBLE);
        iv_make_choose.setVisibility(View.VISIBLE);
        if (equpments.size()==0){
//            bt_make_make.setVisibility(View.INVISIBLE);
//            iv_make_choose.setVisibility(View.INVISIBLE);
        }else {
            if ( teaId==-1){
//                bt_make_make.setVisibility(View.INVISIBLE);
//                iv_make_choose.setVisibility(View.INVISIBLE);
            }
            for (int i = 0;i<equpments.size();i++){
                if (equpments.get(i).getIsFirst()){
                        Firstequpment = equpments.get(i);
                    firstMac = equpments.get(i).getMacAdress();
                    FirstId = equpments.get(i).getEqupmentId();
                }

            }
        }
            if (teaId!=-1){
                new getTeaAsyncTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                id = teaId+"";
            }
        IntentFilter intentFilter = new IntentFilter("MakeActivity");//在該頁面中，動態註冊廣播，以便隨時監聽設備最新狀態
        receiver = new MessageReceiver();
        registerReceiver(receiver, intentFilter);
        //绑定services
        MQintent = new Intent(this, MQService.class);
        MQBound =  bindService(MQintent, MQconnection, Context.BIND_AUTO_CREATE);
        shareDialog = new ShareDialog(this);
        // this part is optional
//        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
//
//
//            @Override
//            public void onSuccess(Sharer.Result result) {
//                ToastUtil.show(MakeActivity.this,"分享成功",Toast.LENGTH_SHORT) ;
//            }
//
//            @Override
//            public void onCancel() {
//                toast("取消分享");
//            }
//
//            @Override
//            public void onError(FacebookException error) {
//                toast("分享失败");
//            }
//        });

    }
    //显示dialog
    public void showProgressDialog() {
        tipDialog = new QMUITipDialog.Builder(this)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord(getText(R.string.search_qsh).toString())
                .create();
        tipDialog.show();
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

    /*获取web网址*/
    class GetwebAsyncTask extends BaseWeakAsyncTask<Void ,Void ,String,BaseActivity> {

        public GetwebAsyncTask(BaseActivity baseActivity) {
            super(baseActivity);
        }

        @Override
        protected String doInBackground(BaseActivity baseActivity, Void... voids) {
            String code ="";
            String result = HttpUtils.getOkHpptRequest(HttpUtils.ipAddress+"/tea/getTeaUrl?urlId=1");
            if (!TextUtils.isEmpty(result)){
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    code = jsonObject.getString("state");
                    JSONObject data = jsonObject.getJSONObject("data");
                    shopUrl  = data.getString("shopUrl");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return code;
        }

        @Override
        protected void onPostExecute(BaseActivity baseActivity, String s) {

            switch (s){
                case "200":
                    if (tipDialog!=null&&tipDialog.isShowing()){
                        tipDialog.dismiss();
                    }

                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("shopUrl",shopUrl);
                    editor.commit();
                    break;
                default:

                    break;
            }
        }
    }

    class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("qqqqqZZZZ???", "11111");
            /*以弃用在选择设备中进行*/
            int nowStage = intent.getIntExtra("nowStage",-1);//状态
            int height = intent.getIntExtra("height",0);//高水位
            int low = intent.getIntExtra("low",0);//低水位
            int size = height*256 +low;//计算后水位
            String errorCode = intent.getStringExtra("errorCode");
            Log.e(TAG, "onReceive: -->"+nowStage );
            if (IsMakeing==1){
                if (!TextUtils.isEmpty(errorCode)) {
//                    有错误直接停止
                    String[] aa = errorCode.split(",");
                    for (String anAa : aa) {
                        if ("1".equals(anAa)) {
                            if (dialog1 != null && dialog1.isShowing()) {
                                dialog1.dismiss();
                            }
                        }
                    }
                }
                Message message = new Message();
                message.arg1 = size;
                message.arg2 = nowStage;
                handler.sendMessage(message);
            }
            Equpment msg1 =(Equpment)intent.getSerializableExtra("msg1");
            if (msg1!=null){
                Firstequpment = msg1;
            }

        }
    }
    /*
    * 接收到冲泡信息更新流量ui 0xB5冲泡完成 以弃用在选择设备中进行
    * */
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int size = msg.arg1;
            int nowStage = msg.arg2;
            if (waterView!=null){
                float value;
                 value = 100* size/tea.getWaterYield();
                if ((int) value!=0){
                    waterView.setValue(value);
                    tv_number.setText( (int) value+"");
                }
                if (nowStage ==0xb5){
                    searchThread.stopThread();
                    waterView.setValue(110f );
                    tv_make_title.setText(R.string.equ_xq_cpwc);
                    tv_number.setText(100+"");
                    li_make_finish.setVisibility(View.VISIBLE);
                    bt_view_stop.setVisibility(View.GONE);
                }
            }

        }
    };

    WaveProgress waterView;
    Dialog dialog;
    Dialog dialog1;
    CountDownTimer countDownTimer;
    int IsMakeing = 0;
    TextView tv_make_title,tv_full;
    TextView tv_number;
    LinearLayout li_make_finish;
     Button bt_view_stop,bt_view_sc,bt_view_fx;
/*
* 弃用 现在统一在选择设备中
* */
    private void customDialog( ) {
        IsMakeing =0;
        dialog1  = new Dialog(this, R.style.MyDialog);
        View view = View.inflate(this, R.layout.view_make, null);
         bt_view_stop = view.findViewById(R.id.bt_view_stop);
        tv_number = view.findViewById(R.id.tv_number);
        final TextView tv_units = view.findViewById(R.id.tv_units);
        tv_make_title = view.findViewById(R.id.tv_make_title);
         li_make_finish = view.findViewById(R.id.li_make_finish);
        bt_view_sc = view.findViewById(R.id.bt_view_sc);
        bt_view_fx = view.findViewById(R.id.bt_view_fx);
        tv_full = view .findViewById(R.id.tv_full);
        TextView tv_dialog_bj = view.findViewById(R.id.tv_dialog_bj);
        ImageView iv_dialog_move = view.findViewById(R.id.iv_dialog_move);
        ImageView bt_dialog_bj = view.findViewById(R.id.bt_dialog_bj);
        tv_dialog_bj.setVisibility(View.INVISIBLE);
        bt_dialog_bj.setVisibility(View.INVISIBLE);
        waterView = (WaveProgress) view.findViewById(R.id.waterView);
        waterView.setWaveColor(Color.parseColor("#37dbc2"), Color.parseColor("#81fbe6"));
        waterView.setMaxValue(100f);
        waterView.setVisibility(View.INVISIBLE);
        Animation operatingAnim = AnimationUtils.loadAnimation(this, R.anim.dialog_rotate);
        LinearInterpolator lin = new LinearInterpolator();
        operatingAnim.setInterpolator(lin);
        if (operatingAnim != null) {
            iv_dialog_move.startAnimation(operatingAnim);
        }  else {
            iv_dialog_move.setAnimation(operatingAnim);
            iv_dialog_move.startAnimation(operatingAnim);
        }
        tv_make_title.setText(R.string.equ_xq_jpz);
        int r =0;
        int g = 0;
        int b =0;
        if (tea!=null){
            String rgb = tea.getRgb();
            String[] aa = rgb.split(",");
            if (aa.length>2){
                r = Integer.valueOf(aa[0]);
                g = Integer.valueOf(aa[1]);
                b = Integer.valueOf(aa[2]);
            }
        }

//        waterView.setValue(100f );

            if (tea!=null) {
                MQservice.sendMakeMess(tea.getWaterYield(), tea.getSeconds(), tea.getTemperature(), equpments.get(0).getMacAdress(), r, g, b);
            }


//        waterView.setValue(100f );
//        MQservice.sendMakeMess(tea.getWaterYield(),tea.getSeconds(),tea.getTemperature(),firstMac);
//        waterView.setValue(50f);
        long time =tea.getSeconds();
         countDownTimer = new CountDownTimer(time*1000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                    tv_number.setText(millisUntilFinished/1000+"");
                    Log.e("DDDDDD", "onTick: -->"+ (millisUntilFinished /1000));

            }

            @Override
            public void onFinish() {
                    waterView.setVisibility(View.VISIBLE);
                    tv_dialog_bj.setVisibility(View.VISIBLE);
                    bt_dialog_bj.setVisibility(View.VISIBLE);
                    iv_dialog_move.clearAnimation();
                    iv_dialog_move.setVisibility(View.INVISIBLE);
                    tv_number.setTextColor(getResources().getColor(R.color.white));
                    waterView.setValue(-10f );
                    tv_units.setText("％");
                    tv_make_title.setText(R.string.equ_xq_cpz);
                    IsMakeing =1;
                    if (searchThread==null){
                        searchThread = new SearchThread();
                        searchThread.start();
                    }else {
                        searchThread.starThread();
                    }


            }
        } ;
        countDownTimer.start();
        dialog1.setContentView(view);
        //使得点击对话框外部不消失对话框
        dialog1.setCanceledOnTouchOutside(false);
        //设置对话框的大小
        view.setMinimumHeight((int) (ScreenSizeUtils.getInstance(this).getScreenHeight() * 0.23f));
        Window dialogWindow = dialog1.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = (int) (ScreenSizeUtils.getInstance(this).getScreenWidth() * 0.75f);
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        dialogWindow.setAttributes(lp);
        bt_view_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MQservice.sendStop(firstMac);
                if (searchThread!=null&&Running){
                    searchThread.stopThread();
                }
                dialog1.dismiss();
                if (countDownTimer!=null){
                    countDownTimer.cancel();
                }
            }
        });
        bt_view_fx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ShareDialog.canShow(ShareLinkContent.class)) {
                    ShareLinkContent linkContent = new ShareLinkContent.Builder()
//                    "https://lify-wellness.myshopify.com/collections/all"
                            .setContentUrl(Uri.parse(tea.getTeaPhoto()))
                            .setShareHashtag(new ShareHashtag.Builder().setHashtag("#Lify").build())
                            .setQuote(tea.getTeaNameEn())
                            .build();

                    shareDialog.show(linkContent);
                }
            }
        });
        bt_view_sc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog1!=null&&dialog1.isShowing()){
                    dialog1.dismiss();
                }
            }
        });
        dialog1.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (searchThread!=null){
                    searchThread.stopThread();
                }

            }
        });
        dialog1.show();

    }
    /*以弃用在选择设备中进行 每隔3s查询流量*/
    SearchThread searchThread;
    boolean  Running = true;
    class SearchThread extends  Thread{
        @Override
        public void run() {
            super.run();
            while (true) {
            try {
                    if (Running) {
                        sleep(3000);
                        Log.e(TAG, "run: -->+++++++++++");
                        MQservice.sendSearchML(Firstequpment.getMacAdress());
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
        public void stopThread(){
             Running=false;
        }
        public void starThread(){
            Running=true;
        }
    }

    @Override
    public void onBackPressed() {
        if (operate==1){
            setResult(200);
        }
        finish();
    }

    @OnClick({R.id.iv_equ_fh,R.id.iv_make_choose,R.id.bt_make_make,R.id.iv_make_love ,R.id.iv_make_share ,R.id.bt_make_buy})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.iv_equ_fh://返回鍵
                if (operate==1){
                    setResult(200);
                }
                finish();
                break;

            case R.id.iv_make_choose:
                Intent intent = new Intent(this,MethodActivity.class);
                intent.putExtra("tea",tea);
                startActivityForResult(intent,200);
                break;
            case R.id.bt_make_make:
                if (equpments.size()>0) {
//                    if (equpments.size()>1) {
                        Intent intent1 = new Intent(this, ChooseDeviceActivity.class);
                        intent1.putExtra("teaId", teaId);
                        startActivity(intent1);
//                    }else {
//                        customDialog();
//                    }
                } else {
                    toast(getText(R.string.toast_equ_add).toString());
                }
                break;
            case R.id.iv_make_love://添加喜愛
                if (tea!=null){
                    if (tea.getIsCollection()==1){
                         customDialog1(false);
                         tea.setIsCollection(0);
                    }else {
                        customDialog1(true);
                        tea.setIsCollection(1);
                    }
                }

                break;

            case R.id.iv_make_share://分享

                popupmenuWindow();
                break;

            case R.id.bt_make_buy:
                if (TextUtils.isEmpty(shopUrl)){
                    showProgressDialog();
                    new GetwebAsyncTask(this).execute();
                }else {
                    Intent intent1 = new Intent(this,BuyNowActivity.class);
                    intent1.putExtra("myUrl","https://lify-wellness.myshopify.com/cart/"+tea.getShopId()+":1");
                    startActivity(intent1);
                }
                break;
        }
    }
    /*分享*/

    private PopupWindow popupWindow1;
    public void popupmenuWindow() {
        if (popupWindow1 != null && popupWindow1.isShowing()) {
            return;
        }

        View view = View.inflate(this, R.layout.popview_myshare, null);
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;

        RelativeLayout rl_mode_1 = (RelativeLayout) view.findViewById(R.id.rl_mode_1);
        RelativeLayout rl_mode_2 = (RelativeLayout) view.findViewById(R.id.rl_mode_2);
        RelativeLayout rl_mode_3 = (RelativeLayout) view.findViewById(R.id.rl_mode_3);
        RelativeLayout rl_mode_4 = (RelativeLayout) view.findViewById(R.id.rl_mode_4);


        if (popupWindow1==null)
            popupWindow1 = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        //点击空白处时，隐藏掉pop窗口
        popupWindow1.setFocusable(true);
        popupWindow1.setOutsideTouchable(true);
        popupWindow1.update();
        //添加弹出、弹入的动画
        popupWindow1.setAnimationStyle(R.style.Popupwindow);
        popupWindow1.showAtLocation(view, Gravity.BOTTOM, 0,0);
//        WindowManager.LayoutParams lp=getWindow().getAttributes();
//            lp.alpha=0.3f;
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
//        getWindow().setAttributes(lp);
        backgroundAlpha(0.5f);
        //添加按键事件监听

        View.OnClickListener listener = new View.OnClickListener() {
            public void onClick(View v) {
                String teaName;
                String shareContent;
                if (MyApplication.IsEnglish==1){
                    teaName=tea.getTeaNameEn();
                    shareContent=tea.getSynopsisEn();
                }else {
                    teaName=tea.getTeaNameCn();
                    shareContent=tea.getSynopsisCn();
                }

                shareContent=shareContent+"\n"+"https://play.google.com/store/apps/details?id=teabar.ph.com.teabar";
                switch (v.getId()) {

                    case R.id.rl_mode_1:
                        /*copyLink分享*/
                        //获取剪贴板管理器：
                        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        // 创建普通字符型ClipData

                        ClipData mClipData = ClipData.newRawUri(teaName,Uri.parse(shareContent));
                        // 将ClipData内容放到系统剪贴板里。
                        cm.setPrimaryClip(mClipData);
                        toast(getText(R.string.equ_xq_cg).toString());
                        popupWindow1.dismiss();
                        break;
                    case R.id.rl_mode_2:
                        /*email分享*/
                        //                                Intent shareIntent = new Intent();
//                                shareIntent.setAction(Intent.ACTION_SEND);
//                                shareIntent.putExtra(Intent.EXTRA_TITLE, getString(R.string.share));
//                                //设置邮件默认地址
//                                String []emailReciver = new String[]{"pop1030123@163.com", "fulon@163.com"};
//                                shareIntent.putExtra(android.content.Intent.EXTRA_EMAIL, emailReciver);
////设置邮件默认标题
//                                shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.share));
////设置要默认发送的内容
//                                shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, "sss");
//                                shareIntent.putExtra(Intent.EXTRA_TEXT, tea.getTeaPhoto());
//                                shareIntent.setType("text/plain");
//
//                                //设置分享列表的标题，并且每次都显示分享列表
//                                startActivity(Intent.createChooser(shareIntent, getString(R.string.share)));
                        Intent email = new Intent(Intent.ACTION_SEND);
                        email.setType("message/rfc822");
                        email.putExtra(Intent.EXTRA_EMAIL, new String[] {""});
                        email.putExtra(Intent.EXTRA_SUBJECT,    teaName);
                        email.putExtra(Intent.EXTRA_TEXT   , shareContent);
                        startActivity(Intent.createChooser(email, getString(R.string.share)));
                        popupWindow1.dismiss();

                        break;
                    case R.id.rl_mode_3:

                        /*facebook分享*/
                        if (ShareDialog.canShow(ShareLinkContent.class)) {
                            ShareLinkContent linkContent = new ShareLinkContent.Builder()
//                    "https://lify-wellness.myshopify.com/collections/all"
                                    .setContentUrl(Uri.parse(tea.getTeaPhoto()))
                                    .setShareHashtag(new ShareHashtag.Builder().setHashtag("#"+teaName).build())
                                    .setQuote(shareContent)
                                    .build();

                            shareDialog.show(linkContent);
                        }
                        popupWindow1.dismiss();
                        break;
                    case R.id.rl_mode_4:
                        if (isContainPackName(MakeActivity.this,"com.whatsapp")){
                            shareAudioRecord(MakeActivity.this,new Intent(),teaName+"\n"+shareContent);
                        }else {
                            ToastUtil.showShort(MakeActivity.this,MakeActivity.this.getString(R.string.no_find_whatsapp));
                        }
                        popupWindow1.dismiss();
                        break;



                }
            }
        };

        rl_mode_1.setOnClickListener(listener);
        rl_mode_2.setOnClickListener(listener);
        rl_mode_3.setOnClickListener(listener);
        rl_mode_4.setOnClickListener(listener);

        popupWindow1.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp=getWindow().getAttributes();
                lp.alpha=1.0f;
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                getWindow().setAttributes(lp);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==200){

        }
    }

    public  boolean isContainPackName(final Context mContext , String packName) {
        boolean isContainPack = false;
        try {
            PackageManager packageManager = mContext.getPackageManager();
            PackageInfo info = packageManager.getPackageInfo(packName , PackageManager.GET_ACTIVITIES);
            if(info != null){
                isContainPack = true;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return isContainPack;
    }
    //直接调用系统的分享，
    public void shareAudioRecord(Context mContext , Intent sendIntent,String shareText) {
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT,shareText);//shareText 为需要分享的内容
        sendIntent.setType("text/plain");
        sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sendIntent.setPackage("com.whatsapp");//packageName 为需要分享到的包名
        startActivity(sendIntent);


    }
    private void backgroundAlpha(float f) {
        WindowManager.LayoutParams lp =getWindow().getAttributes();
        lp.alpha = f;
        getWindow().setAttributes(lp);
    }
    /**
     * 添加喜愛
     */

    boolean which;
    private void customDialog1( final boolean add) {
        dialog  = new Dialog(this, R.style.MyDialog);
        View view = View.inflate(this, R.layout.dialog_del, null);
        TextView tv_dialog_qx = (TextView) view.findViewById(R.id.tv_dia_qx);
        TextView tv_dialog_qd = (TextView) view.findViewById(R.id.tv_dia_qd);
        TextView tv_dia_title = view.findViewById(R.id.tv_dia_title);
        TextView et_dia_name = view.findViewById(R.id.et_dia_name);
        if (add){
            tv_dia_title.setText( R.string.dialog_love_title);
            et_dia_name.setText(R.string.dialog_love_title1);
        }else {
            tv_dia_title.setText(R.string.dialog_love_deltitle );
            et_dia_name.setText(R.string.dialog_love_deltitle1 );
        }

        dialog.setContentView(view);
        //使得点击对话框外部不消失对话框
        dialog.setCanceledOnTouchOutside(false);
        //设置对话框的大小
        view.setMinimumHeight((int) (ScreenSizeUtils.getInstance(this).getScreenHeight() * 0.23f));
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = (int) (ScreenSizeUtils.getInstance(this).getScreenWidth() * 0.75f);
        lp.height = (int) (ScreenSizeUtils.getInstance(this).getScreenWidth() * 0.45f);

        lp.gravity = Gravity.CENTER;
        dialogWindow.setAttributes(lp);
        tv_dialog_qx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }

        });
        tv_dialog_qd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (add){
                    which = true;
                    Map<String,Object> params = new HashMap<>();
                    params.put("userId",userId );
                    params.put("teaId",id );
                    new CollectTeaAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,params);
                    iv_make_love.setImageResource(R.mipmap.make_yes1);
                }else {
                    which = false;
                    Map<String,Object> params = new HashMap<>();
                    params.put("userId",userId );
                    params.put("teaId", id);
                    new CollectTeaAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,params);
                    iv_make_love.setImageResource(R.mipmap.make_no1);

                }

                dialog.dismiss();

            }
        });
        dialog.show();

    }

    int operate;//0表示没有操作过喜爱 1表示有
    /**
     *  添加喜愛
     *
     */
    String returnMsg1="",returnMsg2="";
    class CollectTeaAsyncTask extends AsyncTask<Map<String,Object>,Void,String> {

        @Override
        protected String doInBackground(Map<String, Object>... maps) {
            String code = "";
            String ip;
            Map<String, Object> prarms = maps[0];
            if (which){
                ip ="/app/collectTea";
            }else {
                ip ="/app/cancelCollectTea";
            }
            String result =   HttpUtils.postOkHpptRequest(HttpUtils.ipAddress+ip,prarms);
            Log.e("back", "--->" + result);
            if (!ToastUtil.isEmpty(result)) {
                if (!"4000".equals(result)){
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        code = jsonObject.getString("state");
                        returnMsg1=jsonObject.getString("message2");
                        returnMsg2 = jsonObject.getString("message3");

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
        protected void onPostExecute(String s) {
            switch (s) {
                case "200":
                    operate=1;
                    if (application.IsEnglish()==0){
                        toast(returnMsg1);
                    }else {
                        toast(returnMsg2);
                    }

                    break;

                case "4000":
                    toast(  getText(R.string.toast_all_cs).toString());
                    break;
                default:

                    break;

            }
        }
    }

    /**
     *  獲取詳情信息
     *
     */

    class getTeaAsyncTask extends BaseWeakAsyncTask<Void,Void,String,BaseActivity> {

        public getTeaAsyncTask(BaseActivity baseActivity) {
            super(baseActivity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog();
        }

        @Override
        protected String doInBackground(BaseActivity baseActivity, Void... voids) {
            String code = "";
            String ip;
            ip ="/app/getTeaById?teaId="+teaId+"&userId="+userId;
            String result =   HttpUtils.getOkHpptRequest(HttpUtils.ipAddress+ip );
            Log.e("back", "--->" + result);
            if (!ToastUtil.isEmpty(result)) {
                if (!"4000".equals(result)){
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        code = jsonObject.getString("state");
                        returnMsg1=jsonObject.getString("message1");
                        if ("200".equals(code)){
                            JSONObject jsonObject1  = jsonObject.getJSONObject("data");
                            Gson gson = new Gson();
                            tea = gson.fromJson(jsonObject1.toString(),Tea.class);
                        }
                        returnMsg2 = jsonObject.getString("message2");
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
                    if (tipDialog!=null&&tipDialog.isShowing()) {
                        tipDialog.dismiss();
                    }
                    String syno = tea.getSynopsisEn() ;
//                    String str1 = "<font color='#101010'>"+getText(R.string.tea_kouwei_yc).toString()+" "+"</font>"+syno;
//                    String str2 = "<font color='#101010'>"+getText(R.string.tea_kouwei_kw)+" "+"</font>"+tea.getTasteEn();
//                    String str3 = "<font color='#101010'>"+getText(R.string.equ_xq_pl)+" "+"</font>"+tea.getIngredientEn();
                    if (TextUtils.isEmpty(syno)){
                        tv_make_mes.setText(tea.getTasteEn());
                    }else {
                        tv_make_mes.setText(syno);
                    }
                    if (application.IsEnglish()==1){
                        tv_make_name.setText(tea.getTeaNameEn());
                        tv_tea_desc.setText(tea.getAccountEn());
                        tv_make_mes.setText(tea.getTasteEn());
                        tv_toast_mes.setText(tea.getSynopsisEn());
                    }else {
                        tv_make_name.setText(tea.getTeaNameCn());
                        tv_tea_desc.setText(tea.getAccountCn());
                        tv_make_mes.setText(tea.getTasteCn());
                        tv_toast_mes.setText(tea.getSynopsisCn());
                    }
                    tv_make_style.setText(tea.getProductNameEn());
                    tv_make_plmes.setText (tea.getIngredientEn());
                    tv_make_temp.setText(tea.getTemperature()+"℃");
                    tv_make_sl.setText(tea.getWaterYield()+"ml");
                    tv_make_time.setText(tea.getSeconds()+"s");
                    if (tea.getIsCollection()==1){
                        iv_make_love.setImageResource(R.mipmap.make_yes1);
                    }
                    Glide.with(MakeActivity.this).load(tea.getTeaPhoto()).diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.color.white).into(iv_make_pic);
                    break;

                case "4000":
                   toast(  getText(R.string.toast_all_cs).toString());
                    break;
                default:
                    if (tipDialog!=null&&tipDialog.isShowing()) {
                        tipDialog.dismiss();
                    }
                    toast(getString(R.string.no_find_tea));
                    break;

            }
        }
    }


    /**
     *  添加到喝茶記錄
     *
     */

    class AddTeaAsyncTask extends BaseWeakAsyncTask<Map<String,Object>,Void,String,BaseActivity> {

        public AddTeaAsyncTask(BaseActivity baseActivity) {
            super(baseActivity);
        }

        @Override
        protected String doInBackground(BaseActivity baseActivity, Map<String, Object>... maps) {
            String code = "";
            String ip;
            Map<String, Object> prarms = maps[0];
            String result =   HttpUtils.postOkHpptRequest(HttpUtils.ipAddress+"/app/addTeaDrink",prarms);
            Log.e("back", "--->" + result);
            if (!ToastUtil.isEmpty(result)) {
                if (!"4000".equals(result)){
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        code = jsonObject.getString("state");
                        returnMsg1=jsonObject.getString("message1");

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

                    break;

                case "4000":
//                    toast(  "连接超时，请重试");
                    break;
                default:
//                   toast( returnMsg1);
                    break;

            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (MQBound) {
             unbindService(MQconnection);
        }
        if(countDownTimer!=null){
            countDownTimer.cancel();
        }
        if (receiver!=null)
            unregisterReceiver(receiver);
        isRunning = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isRunning = true;
    }

//    @Override
//    public boolean onKeyUp(int keyCode, KeyEvent event) {
//        switch (keyCode){
//            case KeyEvent.KEYCODE_BACK:
//                startActivity(MainActivity.class);
//                break;
//        }
//        return super.onKeyUp(keyCode, event);
//    }
}
