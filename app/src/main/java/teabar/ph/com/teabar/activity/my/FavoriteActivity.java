package teabar.ph.com.teabar.activity.my;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import teabar.ph.com.teabar.R;
import teabar.ph.com.teabar.adpter.FavoriteAdpter;
import teabar.ph.com.teabar.base.BaseActivity;
import teabar.ph.com.teabar.base.BaseWeakAsyncTask;
import teabar.ph.com.teabar.base.MyApplication;
import teabar.ph.com.teabar.pojo.Tea;
import teabar.ph.com.teabar.util.HttpUtils;
import teabar.ph.com.teabar.util.ToastUtil;

//我的最愛頁面
public class FavoriteActivity extends BaseActivity {


    @BindView(R.id.iv_power_fh)
    ImageView iv_power_fh;
    @BindView(R.id.rv_nearest)
    RecyclerView rv_nearest;
    MyApplication application;
    SharedPreferences preferences;
    QMUITipDialog tipDialog;
    List<Tea> list = new ArrayList<>();
    FavoriteAdpter favoriteAdpter ;
    String userId;
    @Override
    public void initParms(Bundle parms) {

    }

    @Override
    public int bindLayout() {
        setSteepStatusBar(true);
        return R.layout.activity_favorite;
    }

    @Override
    public void initView(View view) {


        if (application == null) {
            application = (MyApplication) getApplication();
        }
        application.addActivity(this);
        preferences = getSharedPreferences("my",MODE_PRIVATE);
        userId = preferences.getString("userId","");
        favoriteAdpter = new FavoriteAdpter(this,list,userId);
        rv_nearest.setLayoutManager(new LinearLayoutManager(this));
        rv_nearest.setAdapter(favoriteAdpter);
        new getFavoriteTeaAsynctask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==200){
            new getFavoriteTeaAsynctask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    String returnMsg1,returnMsg2;
    /*獲取喜爱茶的列表
    * */
    class getFavoriteTeaAsynctask extends BaseWeakAsyncTask<Void,Void,String,BaseActivity> {

        public getFavoriteTeaAsynctask(BaseActivity baseActivity) {
            super(baseActivity);
        }

        @Override
        protected String doInBackground(BaseActivity baseActivity, Void... voids) {
            String code = "";
            String result =   HttpUtils.getOkHpptRequest(HttpUtils.ipAddress+"/app/getCollectTea?userId="+userId );
            Log.e("back", "--->" + result);
            if (!ToastUtil.isEmpty(result)) {

                if (!"4000".equals(result)){
                    try {

                        JSONObject jsonObject = new JSONObject(result);
                        code = jsonObject.getString("state");
                        returnMsg1=jsonObject.getString("message2");
                        returnMsg2=jsonObject.getString("message3");
                        if ("200".equals(code)) {
                            list.clear();
                            JSONArray jsonArray = jsonObject.getJSONArray("data");
                            Gson gson = new Gson();
                            for (int i =0;i<jsonArray.length();i++){
                                Tea tea = gson.fromJson(jsonArray.get(i).toString(),Tea.class);
                                list.add(tea);
                            }
                        }else if ("400".equals(code)){
                            list.clear();
                            returnMsg1=jsonObject.getString("message2");
                            returnMsg2=jsonObject.getString("message3");
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
                favoriteAdpter.setmData(list);
                break;
                case "400":
                    favoriteAdpter.setmData(list);
                    if (application.IsEnglish()==0){
                        toast(returnMsg1);
                    }else {
                        toast(returnMsg2);
                    }
                    break;
                case "4000":
                    ToastUtil.showShort(FavoriteActivity.this, getText(R.string.toast_all_cs).toString());
                    break;
                default:
                    if (application.IsEnglish()==0){
                        toast(returnMsg1);
                    }else {
                        toast(returnMsg2);
                    }
                    break;

            }
        }
    }

    @Override
    public void doBusiness(Context mContext) {

    }
    //显示dialog
    public void showProgressDialog() {

        tipDialog = new QMUITipDialog.Builder(this)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord(getText(R.string.search_qsh).toString())
                .create();
        tipDialog.show();
    }

    @Override
    public void widgetClick(View v) {

    }

    @OnClick({ R.id.iv_power_fh})
    public void onClick(View view){
        switch (view.getId()){

            case R.id.iv_power_fh:
                finish();
                break;


        }
    }

}
