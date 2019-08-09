package teabar.ph.com.teabar.activity.login;



import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.ph.teabar.database.dao.DaoImp.EquipmentImpl;
import com.ph.teabar.database.dao.DaoImp.UserEntryImpl;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;
import me.jessyan.autosize.AutoSizeCompat;
import teabar.ph.com.teabar.R;
import teabar.ph.com.teabar.activity.MainActivity;
import teabar.ph.com.teabar.activity.question.BaseQuestionActivity;
import teabar.ph.com.teabar.base.BaseActivity;
import teabar.ph.com.teabar.base.MyApplication;
import teabar.ph.com.teabar.pojo.Equpment;
import teabar.ph.com.teabar.pojo.UserEntry;
import teabar.ph.com.teabar.util.FacebookHelper;
import teabar.ph.com.teabar.util.HttpUtils;
import teabar.ph.com.teabar.util.NetWorkUtil;
import teabar.ph.com.teabar.util.SharePreferenceManager;
import teabar.ph.com.teabar.util.ToastUtil;
import teabar.ph.com.teabar.util.Utils;

/**
 * 登录页面
 */
public class LoginActivity extends BaseActivity implements GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, View.OnClickListener  {
    MyApplication application;
    @BindView(R.id.et_login_user)
    EditText et_login_user;
    @BindView(R.id.et_login_pasw)
    EditText et_login_pasw;
    @BindView(R.id.tv_bj1)
    TextView tv_bj1;
    @BindView(R.id.iv_login_repass)
    ImageView iv_login_repass;
    @BindView(R.id.tv_login_tk)
    TextView tv_login_tk;
    SharedPreferences preferences;//用来保存个人登录信息
    boolean isHideFirst=true;
    String user;
    String password;
//    LoginButton loginButton;
    ImageView loginButton;
    CallbackManager callbackManager;
    private GoogleApiClient mGoogleApiClient;
    //    private SignInButton sign_in_button;
    private ImageView sign_in_button;
    private static int RC_SIGN_IN=10001;
    QMUITipDialog tipDialog;
    UserEntryImpl userEntryDao;
    EquipmentImpl equipmentDao;

    @Override
    public void initParms(Bundle parms) {

    }

    @Override
    public int bindLayout() {
        setSteepStatusBar(true);
        return R.layout.activity_login;
    }

    /**
     * 用autosize 来适配页面布局，根据页面高度来适配
     * @return
     */
    @Override
    public Resources getResources() {
        //需要升级到 v1.1.2 及以上版本才能使用 AutoSizeCompat
//        AutoSizeCompat.autoConvertDensityOfGlobal((super.getResources()));//如果没有自定义需求用这个方法
        AutoSizeCompat.autoConvertDensity((super.getResources()), 667, false);//如果有自定义需求就用这个方法
        return super.getResources();

    }

    @Override
    public void initView(View view) {

        if (application == null) {
            application = (MyApplication) getApplication();
        }
        application.addActivity(this);
        preferences = getSharedPreferences("my", MODE_PRIVATE);
        if (application.IsEnglish()==0){
            tv_login_tk.setText(Html.fromHtml("沒有帳號?<u>創建一個</u>"));
        }else {
            tv_login_tk.setText(Html.fromHtml("Don't have an account?<u>Create One</u>"));
        }
        userEntryDao = new UserEntryImpl(getApplicationContext());//初始化数据库用户表管理者
        equipmentDao = new EquipmentImpl(getApplicationContext());//初始化数据库设备表管理者
        et_login_user.setText(preferences.getString("user", ""));
        et_login_pasw.setText(preferences.getString("password", ""));
        callbackManager = CallbackManager.Factory.create();
        //自定义fb按钮，在你代码的正确地方
        loginButton = (ImageView) findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                showProgressDialog();
                LoginManager.getInstance()
                        .logInWithReadPermissions(LoginActivity.this,
                                Arrays.asList("public_profile", "user_friends","email"));
                CountDownTimer countDownTimer  = new CountDownTimer(6000,1000) {
                    @Override
                    public void onTick(long l) {

                    }

                    @Override
                    public void onFinish() {

                    }
                }.start();
            }
        });

        //用户自定义fb按钮的做法
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            //为了响应登录结果，您需要使用 LoginButton 注册回调.
            //如果登录成功，LoginResult 参数将拥有新的 AccessToken 及最新授予或拒绝的权限。
            @Override
            public void onSuccess(final LoginResult loginResult) {

                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                if (object != null) {
                                  String  email = object.optString("email");
                                    String  firstname = object.optString("first_name");
                                    String  lastname = object.optString("last_name");
                                    Log.e("log", "LoginActivity - email----" + email);
                                    Log.e("log", "LoginActivity - getLoginInfo::---" + object.toString());
                                    try {
                                        String id = object.getString("id");
                                        String name = object.getString("name");
                                        getFacebookUserImage(id,name);
                                    } catch (Exception e) {
                                        e.printStackTrace();
//

                                    }
//                                    AccessToken accessToken = loginResult.getAccessToken();
//                                    fbuserId = accessToken.getUserId();
//                                    String token = accessToken.getToken();
//                                    Log.e("log", "LoginActivity - accessToken：：：" + accessToken);
//                                    Log.e("log", "LoginActivity - userid:::" + fbuserId);

                               /*     if (accessToken != null) {
                                        //如果登录成功，跳转到登录成功界面，拿到facebook返回的email/userid等值，在我们后台进行操作

                                        // FbLogin();
                                    }*/
                                }
                            }
                        });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,link,gender,birthday,email,picture,locale," +
                        "updated_time,timezone,age_range,first_name,last_name");
                request.setParameters(parameters);
                request.executeAsync();
//                tv_login_tk.setText(Html.fromHtml(getString(R.string.login_tv_regist1)));
//                String str1 = "<font color='#101010'>"+getText(R.string.tea_kouwei_yc).toString()+" "+"</font>"+syno;

            }

            @Override
            public void onCancel() {
                  Toast.makeText(LoginActivity.this, "facebook_account_oauth_Cancel", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException e) {
                 Toast.makeText(LoginActivity.this, "facebook_account_oauth_Error", Toast.LENGTH_SHORT).show();
                    //判断超时异
                    LoginManager.getInstance().logOut();
                    Message message = new Message();
                    message.obj="timeout";
                    handler.sendMessage(message);

            }


        });
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestId()
                .build();

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .enableAutoManage(this, this)/* FragmentActivity *//* OnConnectionFailedListener */
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        sign_in_button = findViewById(R.id.sign_in_button);
        sign_in_button.setOnClickListener(this);

    }


    @SuppressLint("HandlerLeak")
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.obj=="timeout"){
                if (tipDialog!=null&&tipDialog.isShowing()){
                    tipDialog.dismiss();
                }
            }
        }
    };

    //facebook第三方登录
    private void getFacebookUserImage(final String facebookUserId, final String name) {
        FacebookHelper.getFacebookUserPictureAsync(facebookUserId, new FacebookHelper.FacebookUserImageCallback() {
            @Override
            public void onCompleted(String imageUrl) {
                //成功获取到了头像之后
                Log.i("Alex", "用户高清头像的下载url是" + imageUrl);
                Map<String,Object> params = new HashMap<>();
                params.put("userId", facebookUserId);
                params.put("photoUrl",imageUrl);
                params.put("userName",name);
                params.put("type1",2);//facebook 2 google 3
                new ThirdLoginAsynTask().execute(params);
                FacebookHelper.signOut();//如果获取失败了，别忘了将整个登录结果回滚
            }

            @Override
            public void onFailed(String reason) {
                FacebookHelper.signOut();//如果获取失败了，别忘了将整个登录结果回滚
                Log.i("AlexFB", reason);
            }
        });
    }





    @Override
    public void doBusiness(Context mContext) {

    }

    @Override
    public void widgetClick(View v) {


    }
    @OnClick({R.id.bt_login_ensure, R.id.sign_in_button , R.id.tv_login_regist,R.id.tv_login_forget,R.id.li_login_repass

    })
    public void onClick(View view){
        switch (view.getId()) {
            case R.id.tv_login_forget:
                startActivity(ForgetActivity.class );
                break;

            case R.id.bt_login_ensure:
                    if (!Utils.isFastClick()) {
                        user = et_login_user.getText().toString().trim();
                        password = et_login_pasw.getText().toString().trim();
                        if (TextUtils.isEmpty(user)) {
                            toast(getText(R.string.toast_forget_phone).toString());
                            break;
                        }
                        if (TextUtils.isEmpty(password)) {
                            toast(getText(R.string.toast_forget_pass).toString());
                            break;
                        }
                        boolean isConn = NetWorkUtil.isConnected(MyApplication.getContext());
                        if (isConn) {
                            showProgressDialog();
                            Map<String, Object> params = new HashMap<>();
                            if (user.contains("Lify")) {
                                params.put("hotelName", user);
                            } else {
                                if (user.contains("@")) {

                                    params.put("email", user);
                                } else {

                                    params.put("phone", user);
                                }
                            }
                             String sha256Password = Utils.shaEncrypt(password);
                            params.put("password", sha256Password);
                            new LoginAsynTask().execute(params);
                        } else {
                            ToastUtil.showShort(this, getText(R.string.toast_all_cs).toString());
                        }
                    }
                break;
            case R.id.sign_in_button:
                Log.i("robin","点击了登录按钮");
                signIn();
                break;


            case R.id.tv_login_regist:
                startActivity(RegisterActivity.class);
                break;

            case R.id.li_login_repass:
                if (RemeberPass){
                    RemeberPass=false;
                    iv_login_repass.setImageResource(R.mipmap.logo_xzno);
                }else {
                    RemeberPass=true;
                    iv_login_repass.setImageResource(R.mipmap.logo_xz);
                }

                break;


        }

    }
    boolean RemeberPass = true;
    /**
     * 自定义对话框
     */
//    private void ShareDialog() {
//        final Dialog dialog = new Dialog(this, R.style.MyDialog);
//        View view = View.inflate(this, R.layout.dialog_forgtpassword, null);
//        dialog.setContentView(view);
//        //使得点击对话框外部不消失对话框
//        dialog.setCanceledOnTouchOutside(true);
//        //设置对话框的大小
//        view.setMinimumHeight((int) (ScreenSizeUtils.getInstance(this).getScreenHeight() * 0.23f));
//        Window dialogWindow = dialog.getWindow();
//        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
//        lp.width = (int) (ScreenSizeUtils.getInstance(this).getScreenWidth() * 0.75f);
////        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
//        lp.height = (int) (ScreenSizeUtils.getInstance(this).getScreenHeight() * 0.40f);
//        lp.gravity = Gravity.CENTER;
//        dialogWindow.setAttributes(lp);
//        dialog.show();
//    }


    /*google 登录*/
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * 处理谷歌登录的流程
     * @param result
     */
    private void handleSignInResult(GoogleSignInResult result){
        Log.i("robin", "handleSignInResult:" + result.isSuccess());
        if(result.isSuccess()){
            Log.i("robin", "成功");
            GoogleSignInAccount acct = result.getSignInAccount();
            if(acct!=null){
                Log.i("robin", "用户名是:" + acct.getDisplayName());
                Log.i("robin", "用户email是:" + acct.getEmail());
                Log.i("robin", "用户头像是:" + acct.getPhotoUrl());
                Log.i("robin", "用户Id是:" + acct.getId());//之后就可以更新UI了
                Log.i("robin", "用户IdToken是:" + acct.getIdToken());
//                tv_bj1.setText("用户名是:" + acct.getDisplayName()+"\n用户email是:" + acct.getEmail()+"\n用户头像是:" + acct.getPhotoUrl()+ "\n用户Id是:" + acct.getId()+"\n用户IdToken是:" + acct.getIdToken());
            Map<String ,Object> params = new HashMap<>();
            params.put("userId",acct.getId());
            params.put("photoUrl",acct.getPhotoUrl());
            params.put("userName",acct.getDisplayName());
            params.put("type1",3);//facebook 2 google 3
//            showProgressDialog();
            new ThirdLoginAsynTask().execute(params);
            }
        }else{
//            tv_bj1.setText("登录失败");
            Log.i("robin", "没有成功"+result.getStatus());
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i("robin","google登录-->onConnected,bundle=="+bundle);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("robin","google登录-->onConnectionSuspended,i=="+i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i("robin","google登录-->onConnectionFailed,connectionResult=="+connectionResult);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        application.removeAllActivity();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient!=null&&mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    /* face he google 回调*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("robin", "requestCode==" + requestCode + ",resultCode==" + resultCode + ",data==" + data);
        if(requestCode==RC_SIGN_IN){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
        callbackManager.onActivityResult(requestCode, resultCode, data);
//        Log.e(TAG, "onActivityResult: -->"+requestCode+"......"+resultCode +data.toString() );
    }




    @Override
    protected void onStart() {
        super.onStart();
        if(mGoogleApiClient!=null) mGoogleApiClient.connect();
    /*    if (preferences.contains("phone") && !preferences.contains("password")) {
            String phone = preferences.getString("phone", "");
            et_login_user.setText(phone);
            et_login_pasw.setText("");
        }*/

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }


    //显示dialog
    public void showProgressDialog() {

        tipDialog = new QMUITipDialog.Builder(this)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord(getText(R.string.search_qsh).toString())

                .create();
        tipDialog.show();
    }



    String returnMsg1,returnMsg2;
    class LoginAsynTask extends AsyncTask<Map<String,Object>,Void,String> {

        @Override
        protected String doInBackground(Map<String, Object>... maps) {
            String code = "";
            Map<String, Object> prarms = maps[0];
            String result =   HttpUtils.postOkHpptRequest(HttpUtils.ipAddress+"/api/login",prarms);

            Log.e("back", "--->" + result);
            if (!ToastUtil.isEmpty(result)) {
                if (!"4000".equals(result)){
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    code = jsonObject.getString("state");
                    returnMsg1=jsonObject.getString("message2");
                    returnMsg2=jsonObject.getString("message3");
                    if ("200".equals(code)) {
                        JSONObject returnData = jsonObject.getJSONObject("data");
                       userId = returnData.getString("userId");
                        String userName = returnData.getString("userName");
                        String token = returnData.getString("token");
                        String photoUrl = returnData.getString("photoUrl");
                        type = returnData.getInt("type");
                        type1 = returnData.getInt("type1");
                        SharedPreferences.Editor editor = preferences.edit();
                        if (RemeberPass){
                            editor.putString("user",user);
                            editor.putString("password",password);
                        }else {
                            editor.remove("user");
                            editor.remove("password");
                            editor.commit();
                        }
                        editor.putString("userId", userId);
                        editor.putString("token",token);
                        editor.putString("userName",userName);
                        editor.putString("photoUrl",photoUrl);
                        editor.putString("photo","");
                        editor.putInt("type",type);
                        editor.putInt("type1",type1);
                        editor.commit();
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
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            switch (s) {

                case "200":

//                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    LoginJM();
//                    toast( "登录成功");
                    Map<String,Object> params = new HashMap<>();
                    params.put("userId",userId);
                    new FindDeviceAsynTask().execute(params);
                    break;
                case "4000":
                    if (tipDialog!=null&&tipDialog.isShowing()){
                        tipDialog.dismiss();
                    }
                    ToastUtil.showShort(LoginActivity.this, getText(R.string.toast_all_cs).toString());
                    break;
                default:
                    if (tipDialog!=null&&tipDialog.isShowing()){
                        tipDialog.dismiss();
                    }
                    if (application.IsEnglish()==0){
                        toast( returnMsg1);
                    }else {
                        toast( returnMsg2);
                    }

                    break;

            }
        }
    }
    String userId;
    int type ,type1;//type 用户是否第一次登录   ， 用户类型
    class ThirdLoginAsynTask extends AsyncTask<Map<String,Object>,Void,String> {

        @Override
        protected String doInBackground(Map<String, Object>... maps) {
            String code = "";
            Map<String, Object> prarms = maps[0];
            String result =   HttpUtils.postOkHpptRequest(HttpUtils.ipAddress+"/api/otherLogin",prarms);

            Log.e("back", "--->" + result);
            if (!ToastUtil.isEmpty(result)) {
                if (!"4000".equals(result)){
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        code = jsonObject.getString("state");
                        returnMsg1=jsonObject.getString("message2");
                        returnMsg2=jsonObject.getString("message3");
                        if ("200".equals(code)) {
                            JSONObject returnData = jsonObject.getJSONObject("data");
                             userId = returnData.getString("userId");
                            String userName = returnData.getString("userName");
                            String token = returnData.getString("token");
                             type = returnData.getInt("type");
                            String photoUrl = returnData.getString("photoUrl");
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("user",user);
                            editor.putString("password",password);
                            editor.putString("userId", userId);
                            editor.putString("token",token);
                            editor.putString("photoUrl",photoUrl);
                            editor.putString("userName",userName);
                            editor.putString("photo","");
                            editor.putInt("type",type);
                            editor.commit();
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
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            switch (s) {

                case "200":

                    if (type==0) {
//                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        LoginJM();
                    }
                    Map<String,Object> params = new HashMap<>();
                    params.put("userId",userId);
                    new FindDeviceAsynTask().execute(params);
                    break;
                case "4000":
                    if (tipDialog!=null&&tipDialog.isShowing()){
                        tipDialog.dismiss();
                    }
                    ToastUtil.showShort(LoginActivity.this, getText(R.string.toast_all_cs).toString());

                    break;
                default:
                    if (tipDialog!=null&&tipDialog.isShowing()){
                        tipDialog.dismiss();
                    }
                    if (application.IsEnglish()==0){
                        toast( returnMsg1);
                    }else {
                        toast( returnMsg2);
                    }
                    break;

            }
        }
    }


    public void LoginJM(){
        JMessageClient.login(userId+"", "123456", new BasicCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage) {

                if (responseCode == 0) {
                    SharePreferenceManager.setCachedPsw(password);
                    UserInfo myInfo = JMessageClient.getMyInfo();
                    File avatarFile = myInfo.getAvatarFile();
                    //登陆成功,如果用户有头像就把头像存起来,没有就设置null
                    if (avatarFile != null) {
                        SharePreferenceManager.setCachedAvatarPath(avatarFile.getAbsolutePath());
                    } else {
                        SharePreferenceManager.setCachedAvatarPath(null);
                    }
                    String username = myInfo.getUserName();
                    String appKey = myInfo.getAppKey();

                    UserEntry user = userEntryDao.findById(1);
                    if (null == user) {
                        user = new UserEntry(1,userId,username, appKey);
                        userEntryDao.insert(user);
                    }

                    Log.e(TAG, "gotResult: -->"+"JM登录成功" );

                } else {
//                    toast(  "登陆失败" + responseMessage);
                }
            }
        });
    }


    /**
     * 获取设备列表
     */
    class FindDeviceAsynTask extends AsyncTask<Map<String,Object>,Void,String> {

        @Override
        protected String doInBackground(Map<String, Object>... maps) {
            String code = "";
            Map<String, Object> prarms = maps[0];
            String result =   HttpUtils.postOkHpptRequest(HttpUtils.ipAddress+"/app/showUserDevice",prarms);

            Log.e("back", "--->" + result);
           if (!ToastUtil.isEmpty(result)) {
                if (!"4000".equals(result)){
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        code = jsonObject.getString("state");
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
                                    equpment.setOnLine(false);
                                    equpment.setMStage(-1);
                                    int flag = jsonObject1.getInt("flag");
                                    if (flag==1){
                                        equpment.setIsFirst(true);

                                    }else {
                                        equpment.setIsFirst(false);
                                    }
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
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            switch (s) {

                case "200":
                    if (tipDialog!=null&&tipDialog.isShowing()){
                        tipDialog.dismiss();
                    }
//                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    if (type1==0) {
                        if (type == 0) {//第一次登录跳转到问卷调查页面
//                            Intent intent = new Intent(LoginActivity.this, MQService.class);
//                            startService(intent);// 启动服务
                            startActivity(BaseQuestionActivity.class);
                        } else {//不是第一次登录跳转到主页面
//                            Intent intent = new Intent(LoginActivity.this, MQService.class);
//                            startService(intent);// 启动服务
                            startActivity(MainActivity.class);
                        }
                    }else {
//                        Intent intent = new Intent(LoginActivity.this, MQService.class);
//                        startService(intent);// 启动服务
                        startActivity(MainActivity.class);
                    }
//                        toast( returnMsg1);
                    break;
                case "4000":
                    if (tipDialog!=null&&tipDialog.isShowing()){
                        tipDialog.dismiss();
                    }
                    toast( getText(R.string.toast_all_cs).toString());
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


}
