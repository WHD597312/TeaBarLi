package teabar.ph.com.teabar.base;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.peihou.daemonservice.DaemonHolder;
import com.pgyersdk.crash.PgyCrashManager;
import com.ph.teabar.database.dao.DaoImp.FriendInforImpl;

import java.io.File;

import butterknife.ButterKnife;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.event.ContactNotifyEvent;
import cn.jpush.im.android.api.event.LoginStateChangeEvent;
import cn.jpush.im.android.api.model.UserInfo;
import teabar.ph.com.teabar.pojo.FriendInfor;
import teabar.ph.com.teabar.util.LogUtil;
import teabar.ph.com.teabar.util.SharePreferenceManager;
import teabar.ph.com.teabar.util.SharedPreferencesHelper;
import teabar.ph.com.teabar.util.ToastUtil;
import teabar.ph.com.teabar.util.language.LocalManageUtil;

public abstract class BaseActivity extends FragmentActivity implements
        View.OnClickListener {
    /** 是否沉浸状态栏 **/
    private boolean isSetStatusBar = false;
    /** 是否允许全屏 **/
    private boolean mAllowFullScreen = false;
    /** 是否禁止旋转屏幕 **/
    private boolean isAllowScreenRoate = true;
    /** 当前Activity渲染的视图View **/
    private View mContextView = null;
    /** 是否输出日志信息 **/
    private boolean isDebug;
    private String APP_NAME;
    FriendInforImpl friendInforDao;
    protected final String TAG = "BaseActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyApplication application = (MyApplication) getApplication();
        isDebug = application.isDebug;
        APP_NAME = application.APP_NAME;

//        CrashHandler crashHandler = CrashHandler.getInstance();
//        crashHandler.init(getApplicationContext());
        PgyCrashManager.register();
        $Log(TAG + "-->onCreate()");
        try {
            Bundle bundle = getIntent().getExtras();
            if(bundle!=null)
            initParms(bundle);
            mContextView = LayoutInflater.from(this)
                    .inflate(bindLayout(), null);
            if (mAllowFullScreen) {
                this.getWindow().setFlags(
                        WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
                requestWindowFeature(Window.FEATURE_NO_TITLE);
            }
            if (isSetStatusBar) {
                steepStatusBar();
            }
            setContentView(mContextView);
            ButterKnife.bind(this);
            JMessageClient.registerEventReceiver(this);
            if (!isAllowScreenRoate) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
            initView(mContextView);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            friendInforDao= new FriendInforImpl(getApplicationContext());
            doBusiness(this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (null != this.getCurrentFocus()) {
            InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            return mInputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        }
        return super.onTouchEvent(event);
    }

    public void onEventMainThread(LoginStateChangeEvent event) {
        final LoginStateChangeEvent.Reason reason = event.getReason();
        UserInfo myInfo = event.getMyInfo();
        if (myInfo != null) {
            String path;
            File avatar = myInfo.getAvatarFile();
            if (avatar != null && avatar.exists()) {
                path = avatar.getAbsolutePath();
            } else {
//                path = FileHelper.getUserAvatarPath(myInfo.getUserName());
            }
            SharePreferenceManager.setCachedUsername(myInfo.getUserName());
//            SharePreferenceManager.setCachedAvatarPath(path);
            JMessageClient.logout();
        }

    }


    @Override
    protected void onStart() {
        super.onStart();
        DaemonHolder.startService();
//        NotificationManagerCompat notification = NotificationManagerCompat.from(this);
//        boolean isEnabled = notification.areNotificationsEnabled();
//        Log.i("notification","通知权限-->"+isEnabled);
//        if (!isEnabled){
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BASE) {
//                // 进入设置系统应用权限界面
//                Intent intent = new Intent(Settings.ACTION_SETTINGS);
//                startActivity(intent);
//                return;
//            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {// 运行系统在5.x环境使用
//                // 进入设置系统应用权限界面
//                Intent intent = new Intent(Settings.ACTION_SETTINGS);
//                startActivity(intent);
//            }
//        }

    }
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocalManageUtil.setLocal(newBase));
    }
    /**
     * [沉浸状态栏]
     */
    private void steepStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            // 透明状态栏
//            getWindow().addFlags(
//                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // 透明导航栏
//            getWindow().addFlags(
//                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        }
    }


    String userNickname;
    //接收到好友事件
    public void onEvent(final ContactNotifyEvent event) {
        final String reason = event.getReason();
        final String username = event.getFromUsername();
        final String appKey = event.getfromUserAppKey();
        JMessageClient.getUserInfo(username, appKey, new GetUserInfoCallback() {
            @Override
            public void gotResult(int status, String desc, UserInfo userInfo) {
                if (status == 0) {
                    userNickname = userInfo.getNickname();
                    //对方接收了你的好友请求
                    if (event.getType() == ContactNotifyEvent.Type.invite_accepted) {
                        FriendInfor friend = new FriendInfor();
                        friend.setUseName(userNickname);
                        friend.setId(Long.valueOf(username));
                        friend.setAppKey(appKey);
                        friend.setAddNum(0);
//                        friend.setAddFriend(true);
                        friendInforDao.insert(friend);

                        //拒绝好友请求
                    } else if (event.getType() == ContactNotifyEvent.Type.invite_declined) {


                    } else if (event.getType() == ContactNotifyEvent.Type.invite_received) {
                        //如果同一个人申请多次,则只会出现一次;当点击进验证消息界面后,同一个人再次申请则可以收到
                        FriendInfor friendInfor = friendInforDao.findById(Long.valueOf(username));
                        if (friendInfor == null) {
                            FriendInfor friend = new FriendInfor();
                            friend.setUseName(userNickname);
                            friend.setAppKey(appKey);
                            friend.setId(Long.valueOf(username));
                            friend.setAddNum(0);
//                            friend.setAddFriend(true);
                            friendInforDao.insert(friend);
                        }else {
                            friendInfor.setAddNum(0);
//                            friendInfor.setAddFriend(true);
                            friendInforDao.update(friendInfor);
                        }

                    } else if (event.getType() == ContactNotifyEvent.Type.contact_deleted) {
                        FriendInfor friendInfor = friendInforDao.findById(Long.valueOf(userInfo.getUserName()));
                        if (friendInfor!=null){
                            friendInforDao.delete(friendInfor);
                        }
                        JMessageClient.deleteSingleConversation(userInfo.getUserName(), userInfo.getAppKey());

                    }
                }
            }
        });

    }



    /**
         * [初始化Bundle参数]
         *
         * @param parms
         */
    public abstract void initParms(Bundle parms);

    /**
     * [绑定布局]
     *
     * @return
     */
    public abstract int bindLayout();


    /**
     * [重写： 1.是否沉浸状态栏 2.是否全屏 3.是否禁止旋转屏幕]
     */
//     public abstract void setActivityPre();

    /**
     * [初始化控件]
     *
     * @param view
     */
    public abstract void initView(final View view);

    /**
     * [业务操作]
     *
     * @param mContext
     */
    public abstract void doBusiness(Context mContext);

    /** View点击 **/
    public abstract void widgetClick(View v);

    @Override
    public void onClick(View v) {
        if (fastClick())
            widgetClick(v);
    }

    /**
     * [页面跳转]
     *
     * @param clz
     */
    public void startActivity(Class<?> clz) {

        startActivity(clz, null);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void startActivityForResult(Intent intent, int requestCode, @Nullable Bundle options) {
        if (checkDoubleClick(intent)) {
            super.startActivityForResult(intent, requestCode, options);
        }


    }

    /**
     * [携带数据的页面跳转]
     *
     * @param clz
     * @param bundle
     */
    public void startActivity(Class<?> clz, Bundle bundle) {
        Intent intent = new Intent();
        intent.setClass(this, clz);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
    }

    @SuppressWarnings("unchecked")
    public <T extends View> T $(int resId) {
        return (T) super.findViewById(resId);
    }

    /**
     * [含有Bundle通过Class打开编辑界面]
     *
     * @param cls
     * @param bundle
     * @param requestCode
     */
    public void startActivityForResult(Class<?> cls, Bundle bundle,
                                       int requestCode) {
        Intent intent = new Intent();
        intent.setClass(this, cls);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.e(TAG + "--->onResume()");
    }

    @Override
    protected void onDestroy() {
        //注销消息接收
        JMessageClient.unRegisterEventReceiver(this);
        super.onDestroy();
        LogUtil.e(TAG + "--->onDestroy()");
    }

    /**
     * [是否允许全屏]
     *
     * @param allowFullScreen
     */
    public void setAllowFullScreen(boolean allowFullScreen) {
        this.mAllowFullScreen = allowFullScreen;
    }

    /**
     * [是否设置沉浸状态栏]
     *
     * @param isSetStatusBar
     */
    public void setSteepStatusBar(boolean isSetStatusBar) {
        this.isSetStatusBar = isSetStatusBar;
    }

    /**
     * [是否允许屏幕旋转]
     *
     * @param isAllowScreenRoate
     */
    public void setScreenRoate(boolean isAllowScreenRoate) {
        this.isAllowScreenRoate = isAllowScreenRoate;
    }

    /**
     * [日志输出]
     *
     * @param msg
     */
    protected void $Log(String msg) {
        if (isDebug) {
            Log.d(APP_NAME, msg);
        }
    }

    /**
     * [防止快速点击]
     *
     * @return
     */
    private boolean fastClick() {
        long lastClick = 0;
        if (System.currentTimeMillis() - lastClick <= 1000) {
            return false;
        }
        lastClick = System.currentTimeMillis();
        return true;
    }

    public int getUid() {
        SharedPreferencesHelper sharedPreferencesHelper=new SharedPreferencesHelper(this,"my");
        int userId= (int) sharedPreferencesHelper.getSharedPreference("userId",0);
        return userId ;
    }

    public void toast(String text) {
        ToastUtil.showShort(this,text);
    }

    public void toast(int resId) {
        ToastUtil.showShort(this,String.valueOf(resId));
    }
    private String mActivityJumpTag;        //activity跳转tag
    private long mClickTime;                //activity跳转时间

    /**
     * 检查是否重复跳转，不需要则重写方法并返回true
     */
    protected boolean checkDoubleClick(Intent intent) {

        // 默认检查通过
        boolean result = true;
        // 标记对象
        String tag;
        if (intent.getComponent() != null) { // 显式跳转
            tag = intent.getComponent().getClassName();
        }else if (intent.getAction() != null) { // 隐式跳转
            tag = intent.getAction();
        }else {
            return true;
        }

        if (tag.equals(mActivityJumpTag) && mClickTime >= SystemClock.uptimeMillis() - 500) {
            // 检查不通过
            result = false;
        }

        // 记录启动标记和时间
        mActivityJumpTag = tag;
        mClickTime = SystemClock.uptimeMillis();
        return result;
    }

}
