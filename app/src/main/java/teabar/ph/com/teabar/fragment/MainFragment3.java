package teabar.ph.com.teabar.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.ph.teabar.database.dao.DaoImp.EquipmentImpl;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.OnClick;
import teabar.ph.com.teabar.R;
import teabar.ph.com.teabar.activity.MainActivity;
import teabar.ph.com.teabar.activity.SearchActivity;
import teabar.ph.com.teabar.activity.device.MakeActivity;
import teabar.ph.com.teabar.activity.question.QusetionActivity;
import teabar.ph.com.teabar.adpter.MyplanAdapter;
import teabar.ph.com.teabar.adpter.TeaListAdapter;
import teabar.ph.com.teabar.base.BaseFragment;
import teabar.ph.com.teabar.base.BaseWeakAsyncTask;
import teabar.ph.com.teabar.base.MyApplication;
import teabar.ph.com.teabar.pojo.Equpment;
import teabar.ph.com.teabar.pojo.Plan;
import teabar.ph.com.teabar.pojo.Tea;
import teabar.ph.com.teabar.pojo.Weather;
import teabar.ph.com.teabar.service.MQService;
import teabar.ph.com.teabar.util.HttpUtils;
import teabar.ph.com.teabar.util.ToastUtil;
import teabar.ph.com.teabar.util.Utils;
import teabar.ph.com.teabar.util.zxing.android.CaptureActivity;

//首頁
public class MainFragment3 extends BaseFragment  {

    private RecyclerView rv_main_tealist;
    private ScrollView scrollView;
    RecyclerView rv_main_jh;
    TeaListAdapter teaListAdapter ;//茶的列表适配器
    MyplanAdapter myplanAdapter ;//健康計畫的適配器
    List<Plan> planList = new ArrayList<>();//健康計畫的列表
    List<Tea> list1 = new ArrayList<>();//草本口味的茶的列表
    List<Tea> list2 = new ArrayList<>();//水果口味的茶的列表
    List<Tea> list3 = new ArrayList<>();//功能茶的列表
    List<String> list = new ArrayList<>();//"草本口味"，"水果口味"，"功能茶"文字的列表
    private static final int REQUEST_CODE_SCAN = 0x0000;//二維碼掃描請求碼
    public static boolean isRunning = false;//該頁面是否可見
    private boolean MQBound;
    @BindView(R.id.li_main_title)
    RelativeLayout li_main_title ;
    @BindView(R.id.tv_main_online)
    TextView tv_main_online;
    @BindView(R.id.tv_main_hot)
    TextView tv_main_hot;
    @BindView(R.id.tv_main_name)
    TextView tv_main_name;
    @BindView(R.id.tv_main_know)
    TextView tv_main_know;
    @BindView(R.id.tv_main_bz1)
    TextView tv_main_bz1;
    @BindView(R.id.rl_main_question)
    RelativeLayout rl_main_question;
    @BindView(R.id.tv_weather_wd)
    TextView tv_weather_wd;
    @BindView(R.id.tv_weather_sd)
    TextView tv_weather_sd;
    @BindView(R.id.tv_weather_tq)
    TextView tv_weather_tq;
    @BindView(R.id.tv_weather_day)
    TextView tv_weather_day;
    @BindView(R.id.tv_weather_project)
    TextView tv_weather_project;
    @BindView(R.id.tv_weather_name)
    TextView tv_weather_name;
    @BindView(R.id.iv_main_online)
    ImageView iv_main_online;
    @BindView(R.id.iv_main_error)
    ImageView iv_main_error;
    @BindView(R.id.tv_main_error)
    TextView tv_main_error;
    @BindView(R.id.tv_main_num)
    TextView tv_main_num;
    @BindView(R.id.bt_main_dt)
    Button bt_main_dt;
    @BindView(R.id.tv_weather_place)
    TextView tv_weather_place;
    String firstMac;
    EquipmentImpl equipmentDao;
    SharedPreferences preferences;
    Equpment firstEqu;//用戶第一個設備
    public static String userId;
    public static String lastDay;
    public static String lastUserId;
    private static int lastLanguage=-1;

    int type1;
    Map<String,Object> map=new HashMap<>();
    Calendar mCalendar=Calendar.getInstance();
    SimpleDateFormat dateFormat;


    @Override
    public int bindLayout() {
        return R.layout.fragment_main3;

    }

    @Override
    public void initView(View view) {
        if (TextUtils.isEmpty(tips)){
            getTipsAsynTask = new getTipsAsynTask(this) ;//获取健康小知识
            getTipsAsynTask.execute();
        }
        if(list1.size()==0){
            getTeaListAsynTask = new  getTeaListAsynTask(this) ;//獲取茶列表
            getTeaListAsynTask .execute();
        }
        int language= ((MainActivity)getActivity()).getLanguage();
//        equipmentDao = new EquipmentImpl(getActivity().getApplicationContext());
         firstEqu = ((MainActivity)getActivity()).getFirstEqument();
        if (firstEqu!=null){
            firstMac = firstEqu.getMacAdress();
        }
        preferences = getActivity().getSharedPreferences("my",Context.MODE_PRIVATE);
        String name = preferences.getString("userName","");
        userId = preferences.getString("userId","");
        type1 = preferences.getInt("type1",0);
        tv_main_name.setText(name+"!");
        scrollView =view. findViewById(R.id.scrollView);
        rv_main_jh = view.findViewById(R.id.rv_main_jh);
        rv_main_tealist = view.findViewById(R.id.rv_main_tealist);
        if (type1==1 && rl_main_question!=null){
            rl_main_question.setVisibility(View.GONE);
        }
        dateFormat=new SimpleDateFormat("yyyy-MM-dd");
        String today=dateFormat.format(new Date());
        Log.e("todayeeeeee","lastDay="+lastDay);
        Log.e("todayeeeeee","today="+today);
        Log.e("todayeeeeee","userId="+userId);
        Log.e("todayeeeeee","lastUserId="+lastUserId);
        //当上一次的日期和今天的日期相同，并且上一次的用户,地址和现在的用户相同,上一次的语言和现在的相同，就不用调用接口
        if (!TextUtils.isEmpty(lastDay) && lastDay.equals(today) && (!TextUtils.isEmpty(lastUserId)) && lastUserId.equals(userId)&& !TextUtils.isEmpty(city) && language==lastLanguage){
            if (weathers.size()==3){
                Log.e("todayeeeeee","-->使用原始数据");
                Weather weather = weathers.get(0);
                tv_weather_wd.setText(weather.getTem()+"℃");
                tv_weather_sd.setText(weather.getHumidity()+"%");
                tv_weather_tq.setText(weather.getWea());
                tv_weather_day.setText(weather.getWeek());
                if (!TextUtils.isEmpty(city)){
                    Log.e("todayeeeeee","-->使用原始城市");
                    tv_weather_place.setText(city);
                }
            }
            if (tv_weather_project!=null&&tv_weather_name!=null) {
                Log.e("todayeeeeee","-->使用原始茶");
                tv_weather_project.setText(tea1.getProductNameEn());
                tv_weather_name.setText(tea1.getTeaNameEn());
            }
        }else {
            Log.e("todayeeeeee","-->调用接口");
            map.put("userId",userId);
            new GetWeaterAsynce(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,map);
        }
        //刷新默认设备
        RefrashFirstEqu1();

        //获取数据

        initView1();


        getAnswerNumAsynTask = new getAnswerNumAsynTask(this) ;
        getAnswerNumAsynTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        //绑定services
        MQintent = new Intent(getActivity(), MQService.class);
        MQBound = getActivity().bindService(MQintent, MQconnection, Context.BIND_AUTO_CREATE);
        long time=System.currentTimeMillis();
        mCalendar.setTimeInMillis(time);
        int  mHour=mCalendar.get(Calendar.HOUR_OF_DAY);



        /*判断时间*/
        if (mHour>3&&mHour<12){
            tv_main_bz1.setText(getText(R.string.main_home_zs).toString());
        }else if (mHour>12&&mHour<18){
            tv_main_bz1.setText(getText(R.string.main_home_zw).toString());
        }else if (mHour>18||mHour<3){
            tv_main_bz1.setText(getText(R.string.main_home_ws).toString());
        }

        Utils.expandViewTouchDelegate(bt_main_dt,50,0,50,50);

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

    @OnClick({R.id.bt_main_dt,R.id.iv_main_search,R.id.iv_main_sm,R.id.li_main_title,R.id.bt_make,R.id.iv_weather_left,R.id.iv_weather_right})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.bt_main_dt:
//                startActivity(new Intent(getActivity(),ChooseWhichActivity.class));
                startActivity(new Intent(getActivity(),QusetionActivity.class));
                break;
            case R.id.iv_main_search:
                startActivity(new Intent(getActivity(),SearchActivity.class));
                break;
            case R.id.iv_main_sm:
                //动态权限申请


                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, 1);
                } else {
                    goScan();
                }
                break;

            case R.id.li_main_title:
                if (((MainActivity) getActivity()).getFirstEqument()!=null) {
                    if (((MainActivity) getActivity()).getFirstEqument().getOnLine()) {
                        if (!Utils.isFastClick()) {
                            firstEqu = ((MainActivity) getActivity()).getFirstEqument();
                            if (firstEqu.getMStage()!=0xb6&&firstEqu.getMStage()!=0xb7) {
                                if (firstEqu.getMStage() != 0xb2) {
                                    li_main_title.setBackgroundColor(getActivity().getResources().getColor(R.color.main_title1));
                                    firstEquipmentCtrl.open(0Xc0, firstMac);
                                    firstEqu.setMStage(0xb2);
                                } else {
                                    li_main_title.setBackgroundResource(R.drawable.main_title);
                                    firstEquipmentCtrl.open(0Xc1, firstMac);
                                    firstEqu.setMStage(0xb0);
                                }
                            }else {
                                ToastUtil.showShort(getActivity(), getText(R.string.toast_updata_no).toString());
                            }
                        } else {
                            ToastUtil.showShort(getActivity(), getText(R.string.toast_equ_fast).toString());
                        }
                    }else {
                        ToastUtil.showShort(getActivity(), getText(R.string.toast_equ_online).toString());
                    }

                }else {
                    ToastUtil.showShort(getActivity(), getText(R.string.toast_equ_add).toString());

                }

                break;

            case R.id.bt_make:
                Intent intent = new Intent(getActivity(),MakeActivity.class);
                if (tea1!=null){
                    intent.putExtra("teaId",tea1.getId());
                }
                startActivity(intent);
                break;

            case R.id.iv_weather_left:
                if (chooseDay==1){
                    chooseDay=3;
                }else {
                    chooseDay=chooseDay-1;
                }
                if (weathers!=null&&weathers.size()>chooseDay-1) {
                    Weather weather = weathers.get(chooseDay - 1);
                    tv_weather_wd.setText(weather.getTem() + "℃");
                    tv_weather_sd.setText(weather.getHumidity() + "％");
                    tv_weather_tq.setText(weather.getWea());
                    tv_weather_day.setText(weather.getWeek());
                    switch (chooseDay) {
                        case 1:
                            if (tea1 != null) {
                                tv_weather_project.setText(tea1.getProductNameEn());
                                tv_weather_name.setText(tea1.getTeaNameEn());
                            }
                            break;
                        case 2:
                            if (tea2 != null) {
                                tv_weather_project.setText(tea2.getProductNameEn());
                                tv_weather_name.setText(tea2.getTeaNameEn());
                            }
                            break;
                        case 3:
                            if (tea3 != null) {
                                tv_weather_project.setText(tea3.getProductNameEn());
                                tv_weather_name.setText(tea3.getTeaNameEn());
                            }
                            break;
                    }
                }
                break;
            case R.id.iv_weather_right:
                if (chooseDay==3){
                    chooseDay=1;
                }else {
                    chooseDay=chooseDay+1;
                }
                if (weathers!=null&&weathers.size()>chooseDay-1) {
                    Weather weather1 = weathers.get(chooseDay - 1);
                    tv_weather_wd.setText(weather1.getTem() + "℃");
                    tv_weather_sd.setText(weather1.getHumidity() + "％");
                    tv_weather_tq.setText(weather1.getWea());
                    tv_weather_day.setText(weather1.getWeek());
                    switch (chooseDay) {
                        case 1:
                            if (tea1 != null) {
                                tv_weather_project.setText(tea1.getProductNameEn());
                                tv_weather_name.setText(tea1.getTeaNameEn());
                            }
                            break;
                        case 2:
                            if (tea2 != null) {
                                tv_weather_project.setText(tea2.getProductNameEn());
                                tv_weather_name.setText(tea2.getTeaNameEn());
                            }
                            break;
                        case 3:
                            if (tea3 != null) {
                                tv_weather_project.setText(tea3.getProductNameEn());
                                tv_weather_name.setText(tea3.getTeaNameEn());
                            }
                            break;
                    }
                }
                break;
        }
    }

    int chooseDay = 1;
    String errorMes;
    /*需要更改3個故障*/
    public void  RefrashFirstEqu1(){
        tv_main_error.setVisibility(View.GONE);
        iv_main_error.setVisibility(View.GONE);
        tv_main_hot.setVisibility(View.GONE);
        if ( ((MainActivity)Objects.requireNonNull(getActivity())).getFirstEqu()!=null) {
            Equpment equpment = ((MainActivity) getActivity()).getFirstEqu();
            if (equpment != null) {
                String error = equpment.getErrorCode();
                boolean hasError =false;
                errorMes="";
                if (!TextUtils.isEmpty(error)) {
                    String[] aa = error.split(",");
                    for (int i=0;i<aa.length;i++){
                        if ("1".equals(aa[i])){
                            hasError=true;
                        }
                    }
                    if (hasError) {
                        tv_main_error.setVisibility(View.VISIBLE);
                        iv_main_error.setVisibility(View.VISIBLE);
                        if ("1".equals(aa[6])) {
                            /*水位过低*/
                            if (TextUtils.isEmpty(errorMes)) {
                                errorMes = getText(R.string.main_home_error1).toString();
                            } else {
                                errorMes = errorMes + "," + getText(R.string.main_home_error1).toString();
                            }
                        }
                        if ("1".equals(aa[2])) {
                            /*垃圾盒*/
                            if (TextUtils.isEmpty(errorMes)) {
                                errorMes = getText(R.string.main_home_error2).toString();
                            } else {
                                errorMes = errorMes + "," + getText(R.string.main_home_error2).toString();
                            }
                        }
                        if ("1".equals(aa[3])) {
                            /*清洗周期*/
                            if (TextUtils.isEmpty(errorMes)) {
                                errorMes = getText(R.string.main_home_error3).toString();
                            } else {
                                errorMes = errorMes + "," + getText(R.string.main_home_error3).toString();
                            }
                        }
                        tv_main_error.setText(errorMes);
                    }
                }
                if (equpment.getOnLine()) {
                    tv_main_online.setText(R.string.equ_xq_online);
                    iv_main_online.setImageResource(R.mipmap.main_online3);
                    tv_main_hot.setVisibility(View.VISIBLE);
                }
                else {
                    iv_main_online.setImageResource(R.mipmap.main_outline3);
                    tv_main_online.setText(R.string.equ_xq_outline);
                }
                if (equpment.getMStage() == 0xb0) {
                    if (equpment.getHotFinish()==0)
                        tv_main_hot.setText(R.string.equ_xq_nohot);
                    else
                        tv_main_hot.setText(R.string.equ_xq_ishot);
                }else if (equpment.getMStage() == 0xb2) {
                    tv_main_hot.setText(R.string.equ_xq_dg);
                }else if (equpment.getMStage() == 0xb3) {
                    tv_main_hot.setText(R.string.equ_xq_jpz);
                }else if (equpment.getMStage() == 0xb4) {
                    tv_main_hot.setText(R.string.equ_xq_cpz);
                }else if (equpment.getMStage() == 0xb5) {
                    tv_main_hot.setText(R.string.equ_xq_cpwc);
                }else if (equpment.getMStage() == 0xc6) {
                    tv_main_hot.setText(R.string.equ_xq_qx);
                }
                else if (equpment.getMStage() == 0xb6||equpment.getMStage() == 0xb7) {
                    tv_main_hot.setText(R.string.equ_xq_sj);
                }
                else if (equpment.getMStage() == 0xc7) {
                    tv_main_hot.setText(R.string.equ_xq_jb);
                }
                if (equpment.getMStage() == 0xb2 || equpment.getMStage() == -1) {
                    li_main_title.setBackgroundColor(getActivity().getResources().getColor(R.color.main_title1));

                } else {
                    li_main_title.setBackgroundResource(R.drawable.main_title);

                }

            }
        }else {
            li_main_title.setBackgroundColor(getActivity().getResources().getColor(R.color.main_title1));
            iv_main_online.setImageResource(R.mipmap.main_outline3);
            firstEqu = null;
            firstMac = "";

        }

    }


    /*兩個默認設備同步*/
    public void  Synchronization(int type){
        //0是开机 1是关机 这里是显示 0 为绿色
        if (firstEqu==null){
            firstEqu = ((MainActivity)getActivity()).getFirstEqument();
        }
        if (firstEqu!=null) {
            if (type == 0xb2) {
                firstEqu.setMStage(0xb0);
                li_main_title.setBackgroundColor(getActivity().getResources().getColor(R.color.main_title1));

            } else {
                firstEqu.setMStage(0xb2);
                li_main_title.setBackgroundResource(R.drawable.main_title);

            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        firstEquipmentCtrl= ( FirstEquipmentCtrl) getActivity();
    }
    FirstEquipmentCtrl firstEquipmentCtrl;
    public  interface FirstEquipmentCtrl{
        void open(int type, String mac);
    }

    @Override
    public void onStart() {
        super.onStart();
        isRunning = true;
        if (!TextUtils.isEmpty(tips)){
            tv_main_know.setText(tips);//获取健康小知识
        }
    }

    private void initView1() {

        if (list.size()<3){
            for (int i= 0 ;i<3;i++){
                list.add(i+"");
            }
        }
        teaListAdapter = new TeaListAdapter(getActivity(),list,list1,list2,list3);
        teaListAdapter.update(list3,list2,list1);
        myplanAdapter = new MyplanAdapter(getActivity(),planList);
        if (planList.size()==0){
          getAllPlanAsynTask =  new getAllPlanAsynTask(this) ;
          getAllPlanAsynTask.execute();
        }
        Log.e(TAG, "initView1: -->"+planList.size()+"...."+list1.size()+"...."+tips );
        LinearLayoutManager linearLayoutManager  = new LinearLayoutManager(getActivity());
        rv_main_tealist.setLayoutManager(linearLayoutManager);
        rv_main_tealist.setAdapter(teaListAdapter);
        //解决滑动冲突、滑动不流畅
        rv_main_tealist.setHasFixedSize(true);
        rv_main_tealist.setNestedScrollingEnabled(false);
        rv_main_jh.setLayoutManager(new LinearLayoutManager(getActivity()));
        rv_main_jh.setAdapter(myplanAdapter);
        rv_main_jh.setHasFixedSize(true);
        rv_main_jh.setNestedScrollingEnabled(false);

    }

    @Override
    public void onStop() {
        super.onStop();
        isRunning =false;
        Log.e(TAG, "onStop: -->" );
        if (getAllPlanAsynTask!=null&&getAllPlanAsynTask.getStatus()==AsyncTask.Status.RUNNING){
            getAllPlanAsynTask.cancel(true);
        }
        if (getTipsAsynTask!=null&&getTipsAsynTask.getStatus()==AsyncTask.Status.RUNNING){
            getTipsAsynTask.cancel(true);
        }
        if (getTeaListAsynTask!=null&&getTeaListAsynTask.getStatus()==AsyncTask.Status.RUNNING){
            getTeaListAsynTask.cancel(true);
        }
        if (FindWeatherAsynTask!=null&&FindWeatherAsynTask.getStatus()==AsyncTask.Status.RUNNING){
            FindWeatherAsynTask.cancel(true);
        }
        if (getAnswerNumAsynTask!=null&&getAnswerNumAsynTask.getStatus()==AsyncTask.Status.RUNNING){
            getAnswerNumAsynTask.cancel(true);
        }

  }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (MQBound) {
            getActivity().unbindService(MQconnection);
        }
        if (getAllPlanAsynTask!=null&&getAllPlanAsynTask.getStatus()==AsyncTask.Status.RUNNING){
            getAllPlanAsynTask.cancel(true);
        }
        if (getTipsAsynTask!=null&&getTipsAsynTask.getStatus()==AsyncTask.Status.RUNNING){
            getTipsAsynTask.cancel(true);
        }
        if (getTeaListAsynTask!=null&&getTeaListAsynTask.getStatus()==AsyncTask.Status.RUNNING){
            getTeaListAsynTask.cancel(true);
        }
        if (FindWeatherAsynTask!=null&&FindWeatherAsynTask.getStatus()==AsyncTask.Status.RUNNING){
            FindWeatherAsynTask.cancel(true);
        }
        if (getAnswerNumAsynTask!=null&&getAnswerNumAsynTask.getStatus()==AsyncTask.Status.RUNNING){
            getAnswerNumAsynTask.cancel(true);
        }

    }
    public static String city;
    class GetWeaterAsynce extends BaseWeakAsyncTask<Map<String,Object>,Void,Integer,BaseFragment>{

        public GetWeaterAsynce(BaseFragment baseFragment) {
            super(baseFragment);
        }

        @Override
        protected Integer doInBackground(BaseFragment baseFragment, Map<String, Object>... maps) {
            Map<String,Object> params=maps[0];
            String url=HttpUtils.ipAddress+"/app/getWeather";
            int code=0;
            try {
                String result=HttpUtils.postOkHpptRequest(url,params);
                if (!TextUtils.isEmpty(result)){
                    JSONObject jsonObject=new JSONObject(result);
                    code=jsonObject.getInt("state");
                    if (code==200){
                        int year=mCalendar.get(Calendar.YEAR);
                        int month=mCalendar.get(Calendar.MONTH)+1;
                        int day=mCalendar.get(Calendar.DAY_OF_MONTH);
                        int week=Utils.getWeek2(mCalendar.get(Calendar.DAY_OF_WEEK));

                        Log.i("weekkkkkkk","-->"+week);
                        int nextWeek=week+1;
                        int ht=week+2;
                        if (nextWeek>7){
                            nextWeek=1;
                            ht=2;
                        }
                        if (ht>7){
                            ht=1;
                        }
                        lastLanguage=MyApplication.IsEnglish;
                        Calendar calendar= new GregorianCalendar();
                        calendar.setTime(new Date());
                        String today=dateFormat.format(calendar.getTime());
                        lastDay=today;
                        lastUserId=userId;
                        calendar.add(Calendar.DATE,1);
                        String tomorrow=dateFormat.format(calendar.getTime())+",";
                        calendar.add(Calendar.DATE,2);
                        String sht=dateFormat.format(calendar.getTime())+",";
                        String tod=today+","+getString(Utils.getWeek(week));
                        String tom=tomorrow+getString(Utils.getWeek(nextWeek));
                        String mHt=sht+getString(Utils.getWeek(ht));


                        JSONObject data=jsonObject.getJSONObject("data1");
                        String hum=data.getString("hum");
                        String weatheren=data.getString("weatheren");
                        String weather=data.getString("weather");
                        String temperature=data.getString("temperature");
                        JSONObject data2=jsonObject.getJSONObject("data2");
                        String hum2=data2.getString("hum");
                        String weatheren2=data2.getString("weatheren");
                        String weather2=data2.getString("weather");
                        String temperature2=data2.getString("temperature");
                        JSONObject data3=jsonObject.getJSONObject("data3");
                        String hum3=data3.getString("hum");
                        String weatheren3=data3.getString("weatheren");
                        String weather3=data3.getString("weather");
                        String temperature3=data3.getString("temperature");

                        weathers.clear();
                        if (MyApplication.IsEnglish==1){
                            weathers.add(new Weather(tod,weatheren,hum,temperature));
                            weathers.add(new Weather(tom,weatheren2,hum2,temperature2));
                            weathers.add(new Weather(mHt,weatheren3,hum3,temperature3));
                            city=jsonObject.getString("cityen");
                        }else {
                            weathers.add(new Weather(tod,weather,hum,temperature));
                            weathers.add(new Weather(tom,weather2,hum2,temperature2));
                            weathers.add(new Weather(mHt,weather3,hum3,temperature3));
                            city=jsonObject.getString("city");
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return code;
        }

        @Override
        protected void onPostExecute(BaseFragment baseFragment, Integer code) {
            if (code==200){
                if (weathers.size()==3){
                    Weather weather = weathers.get(0);
                    tv_weather_wd.setText(weather.getTem()+"℃");
                    tv_weather_sd.setText(weather.getHumidity()+"%");
                    tv_weather_tq.setText(weather.getWea());
                    tv_weather_day.setText(weather.getWeek());
                    if (!TextUtils.isEmpty(city))
                        tv_weather_place.setText(city);

                }
                FindWeatherAsynTask = new FindWeatherAsynTask(MainFragment3.this) ;
                        FindWeatherAsynTask .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    }
    //添加天气预报图

    public static List<Weather> weathers = new ArrayList<>();
    /*  获取天气预报*/
//    searchWeatherAsynTask  searchWeatherAsynTask ;
//    class searchWeatherAsynTask extends BaseWeakAsyncTask<Void,Void,String,BaseFragment> {
//
//        public searchWeatherAsynTask(BaseFragment baseFragment) {
//            super(baseFragment);
//        }
//
//        @Override
//        protected String doInBackground(BaseFragment baseFragment ,Void... voids) {
//            String type= "en";
//            if (language==0){
//                type= "ZH_TW";
//            }
//            String code = "";
//
//            String result =   HttpUtils.getOkHpptRequest("https://api.openweathermap.org/data/2.5/forecast?id=1819730&APPID=64f17a2291526f4669269007d621d3b6&lang="+type+"&units=metric&cnt=24" );
//            Log.e("back", "--->" + result);
//            if (!ToastUtil.isEmpty(result)) {
//                if (!"4000".equals(result)){
//                    try {
//                        JSONObject jsonObject = new JSONObject(result);
//                        code = jsonObject.getString("cod");
//                        JSONArray jsonArray = jsonObject.getJSONArray("list");
//                        for (int i =0;i<jsonArray.length();i++){
//                            if (i==0||i==9||i==17) {
//                                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
//                                Weather weather = new Weather();
//                                long time = jsonObject1.getLong("dt");
//                                String weak =  getWeek(time);
//                                JSONObject jsonObject11 = jsonObject1.getJSONObject("main");
//                                int temp = jsonObject11.getInt("temp");
//                                String humidity = jsonObject11.getString("humidity");
//                                JSONArray jsonObject12 = jsonObject1.getJSONArray("weather");
//                                JSONObject Jweathers = jsonObject12.getJSONObject( 0);
//                                String wea = Jweathers.getString("description");
//                                JSONObject jsonObject13 = jsonObject1.getJSONObject("wind");
//                                String air = jsonObject13.getString("speed");
//                                weather.setTem(temp+"");
//                                weather.setAir_level(air);
//                                weather.setWea(wea);
//                                weather.setHumidity(humidity);
//                                weather.setWeek(weak);
//                                weathers.add(weather);
//                            }
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }else {
//                    code="4000";
//                }
//            }
//            return code;
//        }
//
//
//
//        @Override
//        protected void onPostExecute(BaseFragment baseFragment ,String s) {
//
//            if(isCancelled()){
//                return;
//            }
//            switch (s) {
//
//                case "200":
//
//                    if (tv_weather_wd!=null&&tv_weather_sd!=null&&tv_weather_tq!=null&&tv_weather_day!=null) {
//
//                        Weather weather = weathers.get(0);
//                        tv_weather_wd.setText(weather.getTem() + "℃");
//                        tv_weather_sd.setText(weather.getHumidity() + "％");
//                        tv_weather_tq.setText(weather.getWea());
//                        tv_weather_day.setText(weather.getWeek());
//                    }
//                    break;
//                case "4000":
//
//                    ToastUtil.showShort(getActivity(), "连接超时，请重试");
//
//                    break;
//                default:
//
//
//                    break;
//
//            }
//        }
//    }
        /*
        * 獲取答題人數
        * */
        String dataNum = "1314";
    getAnswerNumAsynTask getAnswerNumAsynTask ;
    @SuppressLint("StaticFieldLeak")
    class getAnswerNumAsynTask extends BaseWeakAsyncTask<Void,Void,String,BaseFragment> {

        public getAnswerNumAsynTask(BaseFragment baseFragment) {
            super(baseFragment);
        }

        @Override
        protected String doInBackground(BaseFragment baseFragment ,Void... voids) {

            String code = "";
            String result =   HttpUtils.getOkHpptRequest("http://47.98.131.11:8081/app/getUserExamCount" );
            Log.e("back", "--->" + result);
            if (!ToastUtil.isEmpty(result)) {
                if (!"4000".equals(result)){
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        code = jsonObject.getString("state");
                        dataNum = jsonObject.getString("data");

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
        protected void onPostExecute(BaseFragment baseFragment ,String s) {

            if(isCancelled()){
                return;
            }
            switch (s) {

                case "200":
                    if (tv_main_num!=null)
                    tv_main_num.setText(dataNum+" "+getText(R.string.main_home_wj3).toString());
                    break;

                default:
                    if (tv_main_num!=null)
                    tv_main_num.setText(dataNum+" "+getText(R.string.main_home_wj3).toString());
                    break;

            }
        }
    }

    //根据天气选择不同的来区分的三种茶 具体看FindWeatherAsynTask这个任务的功能来获取这三种茶
    public static Tea tea1 = new Tea();
    public static Tea tea2 = new Tea();
    public static Tea tea3 = new Tea();

    /**
     * 獲取星期幾
     * @param time
     * @return
     */
    public  String getWeek(long time) {

        Calendar cd = Calendar.getInstance();
        cd.setTimeInMillis(time*1000);

        int year  = cd.get(Calendar.YEAR); //获取年份
        int month = cd.get(Calendar.MONTH)+1; //获取月份
        int day   = cd.get(Calendar.DAY_OF_MONTH); //获取日期
        int week  = cd.get(Calendar.DAY_OF_WEEK); //获取星期

        String weekString;
        switch (week) {
            case Calendar.SUNDAY:
                weekString = getText(R.string.weather_week_7).toString();
                break;
            case Calendar.MONDAY:
                weekString = getText(R.string.weather_week_1).toString();
                break;
            case Calendar.TUESDAY:
                weekString = getText(R.string.weather_week_2).toString();
                break;
            case Calendar.WEDNESDAY:
                weekString = getText(R.string.weather_week_3).toString();
                break;
            case Calendar.THURSDAY:
                weekString = getText(R.string.weather_week_4).toString();
                break;
            case Calendar.FRIDAY:
                weekString = getText(R.string.weather_week_5).toString();
                break;
            default:
                weekString = getText(R.string.weather_week_6).toString();
                break;

        }

        return year+"-"+month+"-"+day+" ， "+weekString;
    }

    List<Tea> below = new ArrayList<>();
    List<Tea> above = new ArrayList<>();
    List<Tea> Humidity = new ArrayList<>();
    /* 查询天气茶*/
    FindWeatherAsynTask FindWeatherAsynTask;
    class  FindWeatherAsynTask extends BaseWeakAsyncTask<Void,Void,String,BaseFragment> {

        public FindWeatherAsynTask(BaseFragment baseFragment) {
            super(baseFragment);
        }

        @Override
        protected String doInBackground(BaseFragment baseFragment ,Void... voids) {

            String code = "";
            String result =   HttpUtils.getOkHpptRequest(HttpUtils.ipAddress+"/app/getWeatherTea" );
            Log.e("back", "--->" + result);
            if (!ToastUtil.isEmpty(result)) {
                if (!"4000".equals(result)){
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        code = jsonObject.getString("state");
                        JSONObject jsonObject1 = jsonObject.getJSONObject("data");
                        JSONArray jsonArray = jsonObject1.getJSONArray("below");
                        for (int i =0;i<jsonArray.length();i++){
                            JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                            Gson gson = new Gson();
                            Tea tea = gson.fromJson(jsonObject2.toString(),Tea.class);
                            below.add(tea);
                        }
                        JSONArray jsonArray1 = jsonObject1.getJSONArray("Humidity");
                        for (int i =0;i<jsonArray1.length();i++){
                            JSONObject jsonObject2 = jsonArray1.getJSONObject(i);
                            Gson gson = new Gson();
                            Tea tea = gson.fromJson(jsonObject2.toString(),Tea.class);
                            Humidity.add(tea);
                        }
                        JSONArray jsonArray2 = jsonObject1.getJSONArray("above");
                        for (int i =0;i<jsonArray2.length();i++){
                            JSONObject jsonObject2 = jsonArray2.getJSONObject(i);
                            Gson gson = new Gson();
                            Tea tea = gson.fromJson(jsonObject2.toString(),Tea.class);
                            above.add(tea);
                        }
                        if("200".equals(code)){
                            for (int i =0;i<weathers.size();i++){
                                Tea teas;
                                if (i==0){
                                    if (Humidity.size()>0&&above.size()>0&&below.size()>0) {
                                        if (Integer.valueOf(weathers.get(0).getHumidity()) > 70) {
                                            int num2 = (int) (Math.random() * Humidity.size());
                                            teas = Humidity.get(num2);
                                            tea1 = teas;
                                        } else {
                                            if (Integer.valueOf(weathers.get(0).getTem()) > 21) {
                                                int num2 = (int) (Math.random() * above.size());

                                                teas = above.get(num2);
                                                tea1 = teas;
                                            } else {
                                                int num2 = (int) (Math.random() * below.size());
                                                teas = below.get(num2);
                                                tea1 = teas;
                                            }
                                        }

                                    }
                                }
                                if (i==1){
                                    if (Humidity.size()>0&&above.size()>0&&below.size()>0) {
                                        if (Integer.valueOf(weathers.get(1).getHumidity()) > 70) {
                                            int num2 = (int) (Math.random() * Humidity.size());
                                            teas = Humidity.get(num2);
                                            tea2 = teas;
                                        } else {
                                            if (Integer.valueOf(weathers.get(1).getTem()) > 21) {
                                                int num2 = (int) (Math.random() * above.size());
                                                teas = above.get(num2);
                                                tea2 = teas;
                                            } else {
                                                int num2 = (int) (Math.random() * below.size());
                                                teas = below.get(num2);
                                                tea2 = teas;
                                            }
                                        }

                                    }
                                }

                                if (i==2){
                                    if (Humidity.size()>0&&above.size()>0&&below.size()>0) {
                                        if (Integer.valueOf(weathers.get(2).getHumidity()) > 70) {
                                            int num2 = (int) (Math.random() * Humidity.size());
                                            teas = Humidity.get(num2);
                                            tea3 = teas;
                                        } else {
                                            if (Integer.valueOf(weathers.get(2).getTem()) > 21) {
                                                int num2 = (int) (Math.random() * above.size());
                                                teas = above.get(num2);
                                                tea3 = teas;
                                            } else {
                                                int num2 = (int) (Math.random() * below.size());
                                                teas = below.get(num2);
                                                tea3 = teas;
                                            }
                                        }

                                    }
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
        protected void onPostExecute(BaseFragment baseFragment ,String s) {

            if(isCancelled()){
                return;
            }
            switch (s) {

                case "200":

                    if (tv_weather_project!=null&&tv_weather_name!=null) {
                        tv_weather_project.setText(tea1.getProductNameEn());
                        tv_weather_name.setText(tea1.getTeaNameEn());
                    }
                    break;
                case "4000":

                    ToastUtil.showShort(getActivity(), R.string.toast_all_cs);

                    break;
                default:


                    break;

            }
        }
    }



    String returnMsg1,returnMsg2;
    getTeaListAsynTask getTeaListAsynTask;
    /*  获取茶列表*/
    class getTeaListAsynTask extends BaseWeakAsyncTask<Void,Void,String,BaseFragment> {

        public getTeaListAsynTask(BaseFragment baseFragment) {
            super(baseFragment);
        }

        @Override
        protected String doInBackground(BaseFragment baseFragment ,Void... voids) {
            String code = "";
            String result =   HttpUtils.getOkHpptRequest(HttpUtils.ipAddress+"/app/getTea?userId="+userId );
            Log.e("back", "--->" + result);
            if (!ToastUtil.isEmpty(result)) {
                if (!"4000".equals(result)){
                    try {
                        list1.clear();
                        list2.clear();
                        list3.clear();
                        JSONObject jsonObject = new JSONObject(result);
//                        JsonObject content = new JsonParser().parse(result).getAsJsonObject();
                        code = jsonObject.getString("state");
                        returnMsg1=jsonObject.getString("message1");
                        JSONObject jsonObject1 =  jsonObject.getJSONObject("data");
//                        JSONObject jsonObject1 = jsonObject.getJSONObject("data");
                        if ("200".equals(code)) {
                            JSONArray healingTea = jsonObject1.getJSONArray("healingTea");
                            Gson gson = new Gson();
                            for (int i = 0; i < healingTea.length(); i++) {
                                //功能茶
                                Tea teaList = gson.fromJson(healingTea.get(i).toString(),Tea.class);
                                list1.add(teaList);
                            }
                            JSONArray fruitTea = jsonObject1.getJSONArray("fruitTea");
                            for (int i = 0; i < fruitTea.length(); i++) {
                                //水果
                                Tea teaList = gson.fromJson(fruitTea.get(i).toString() ,Tea.class);
                                list2.add(teaList);
                            }
                            JSONArray herbalWellness = jsonObject1.getJSONArray("herbalWellness");
                            for (int i = 0; i < herbalWellness.length(); i++) {
                                // 草本
                                Tea teaList = gson.fromJson(herbalWellness.get(i).toString(),Tea.class);
                                list3.add(teaList);
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
        protected void onPostExecute(BaseFragment baseFragment ,String s) {

            if(isCancelled()){
                return;
            }
            switch (s) {

                case "200":
                    if (teaListAdapter!=null)
                    teaListAdapter.update(list3,list2,list1);
                    break;
                case "4000":

                    ToastUtil.showShort(getActivity(), "连接超时，请重试");

                    break;
                default:

                    ToastUtil.showShort(getActivity(), returnMsg1);
                    break;

            }
        }
    }

    /*  获取小知识*/
    String tips ;
    getTipsAsynTask getTipsAsynTask ;
    class getTipsAsynTask extends BaseWeakAsyncTask<Void,Void,String,BaseFragment> {

        public getTipsAsynTask(BaseFragment baseFragment) {
            super(baseFragment);
        }

        @Override
        protected String doInBackground(BaseFragment baseFragment ,Void... voids) {
            String code = "";
            String result =   HttpUtils.getOkHpptRequest(HttpUtils.ipAddress+"/app/getTips?type=1" );
            Log.e("back", "--->" + result);
            if (!ToastUtil.isEmpty(result)) {
                if (!"4000".equals(result)){
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        code = jsonObject.getString("state");
                        returnMsg1=jsonObject.getString("message1");
                        JSONObject jsonObject1 =  jsonObject.getJSONObject("data");
                      if ("200".equals(code)) {
                           tips = jsonObject1.getString("tips");

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
        protected void onPostExecute(BaseFragment baseFragment ,String s) {

            if(isCancelled()){
                return;
            }
            switch (s) {

                case "200":
                    if (!TextUtils.isEmpty(tips)&&tv_main_know!=null){
                        tv_main_know.setText(tips);
                    }
                    break;
                case "4000":

                    ToastUtil.showShort(getActivity(), "连接超时，请重试");

                    break;
                default:
                    ToastUtil.showShort(getActivity(), returnMsg1);
                    break;

            }
        }
    }
/*  获取全部计划*/
    getAllPlanAsynTask getAllPlanAsynTask;
    class getAllPlanAsynTask extends BaseWeakAsyncTask<Void,Void,String,BaseFragment> {

        public getAllPlanAsynTask(BaseFragment baseFragment) {
            super(baseFragment);
        }

        @Override
        protected String doInBackground(BaseFragment baseFragment ,Void... voids) {
            String code = "";
            String result =   HttpUtils.getOkHpptRequest(HttpUtils.ipAddress+"/app/getPlan" );
            Log.e("getAllPlanAsynTask", "--->" + result);
            if (!ToastUtil.isEmpty(result)) {
                if (!"4000".equals(result)){
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        code = jsonObject.getString("state");
                        returnMsg1=jsonObject.getString("message1");
                        JSONArray jsonArray =  jsonObject.getJSONArray("data");
                        if ("200".equals(code)) {
                            planList.clear();
                            Gson gson = new Gson();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                //功能茶
                                Plan plan = gson.fromJson(jsonArray.get(i).toString(),Plan.class);
                                planList.add(plan);
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
        protected void onPostExecute(BaseFragment baseFragment ,String s) {

            if(isCancelled()){
                return;
            }
            switch (s) {

                case "200":
                    if (myplanAdapter!=null)
                    myplanAdapter.update(planList);
                    break;
                case "4000":

                    ToastUtil.showShort(getActivity(), "连接超时，请重试");

                    break;
                default:
                    ToastUtil.showShort(getActivity(), returnMsg1);
                    break;

            }
        }
    }
    @Override
    public void doBusiness(Context mContext) {

    }

    @Override
    public void widgetClick(View v) {

    }










//    @OnClick({R.id.iv_main_search})
//    public void onClick(View view){
//        switch (view.getId()){
//            case R.id.iv_main_search:
//
//                //动态权限申请
//                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, 1);
//                } else {
//                    goScan();
//                }
//                break;
//        }
//    }

    /**
     * 跳转到扫码界面扫码
     */
    private void goScan(){
        Intent intent = new Intent(getActivity(), CaptureActivity.class);
        startActivityForResult(intent, REQUEST_CODE_SCAN);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    goScan();
                } else {
                    Toast.makeText(getActivity(), getText(R.string.toast_main_xj).toString(), Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 扫描二维码/条码回传
        if (requestCode == REQUEST_CODE_SCAN && resultCode == Activity.RESULT_OK) {
            if (data != null) {
//                返回的文本内容
                String content = data.getStringExtra("codedContent");
//                返回的BitMap图像

                if (content.contains("teaId:")){
                    String teaId= content.substring(content.indexOf(":")+1);
                    long num = 0;
                    try {
                        num = Long.valueOf(teaId);
                        Intent intent = new Intent(getActivity(),MakeActivity.class);
                        intent.putExtra("teaId",num);
                        startActivity(intent);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        ToastUtil.showShort(getActivity(),getText(R.string.toast_main_code).toString());
                    }

//                ToastUtil.showShort(this,content);
                }else {
                    ToastUtil.showShort(getActivity(),getText(R.string.toast_main_code).toString());
                }


            }
        }
    }

}
