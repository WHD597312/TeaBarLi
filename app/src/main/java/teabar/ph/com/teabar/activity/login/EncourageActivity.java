package teabar.ph.com.teabar.activity.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.OnClick;
import teabar.ph.com.teabar.R;
import teabar.ph.com.teabar.base.BaseActivity;
import teabar.ph.com.teabar.base.BaseWeakAsyncTask;
import teabar.ph.com.teabar.base.MyApplication;
import teabar.ph.com.teabar.util.HttpUtils;
import teabar.ph.com.teabar.util.Utils;

public class EncourageActivity extends BaseActivity {
    @BindView(R.id.tv_encourage)
    TextView tv_encourage;
    @BindView(R.id.encourage_tv_day)
    TextView encourage_tv_day;
    @BindView(R.id.encourage_tv_year)
    TextView encourage_tv_year;
    @BindView(R.id.tv_encourage_zz)
    TextView tv_encourage_zz;
    @BindView(R.id.encourage_bt_in)
    Button encourage_bt_in;
    MyApplication application;
    SharedPreferences preferences;
    int language;
    @Override
    public void initParms(Bundle parms) {

    }

    @Override
    public int bindLayout() {
        setSteepStatusBar(true);
        return R.layout.activity_encourage;
    }

    @Override
    public void initView(View view) {
        if (application == null) {
            application = (MyApplication) getApplication();
        }
        application.addActivity(this);
        preferences = getSharedPreferences("my",MODE_PRIVATE);
        language = MyApplication.IsEnglish;
        new GetwebAsyncTask(this).execute();
        new GetEncourageAsyncTask(this).execute();
        Calendar c = Calendar.getInstance();
        int  year = c.get(Calendar.YEAR);
        String month = c.get(Calendar.MONTH)+1 +"" ;
        String day = c.get(Calendar.DATE)<10?"0"+c.get(Calendar.DATE):c.get(Calendar.DATE)+"";
        String Emonth="" ;

        switch (month){
            case "1":
                Emonth = "JAN";
                break;
            case "2":
                Emonth = "FEB";
                break;
            case "3":
                Emonth = "MAR";
                break;
            case "4":
                Emonth = "APR";
                break;
            case "5":
                Emonth = "MAY";
                break;

            case "6":
                Emonth = "JUN";
                break;
            case "7":
                Emonth = "JUL";
                break;
            case "8":
                Emonth = "OCT";
                break;
            case "9":
                Emonth = "SEP";
                break;
            case "10":
                Emonth = "AUG";
                break;
            case "11":
                Emonth = "NOV";
                break;
            case "12":
                Emonth = "DEC";
                break;
        }
        encourage_tv_year.setText(day);
        encourage_tv_day.setText(Emonth+" "+year);
        Utils.expandViewTouchDelegate(encourage_bt_in,70,70,70,70);
    }

    @Override
    public void doBusiness(Context mContext) {

    }

    @Override
    public void widgetClick(View v) {

    }
    @OnClick({R.id.encourage_bt_in})
            public void  onClick(View view){
        switch (view.getId()){
            case R.id.encourage_bt_in:
                startActivity(LoginActivity.class);
                break;

        }
    }

    String  encouraging ,message1,auth;
    class GetEncourageAsyncTask extends BaseWeakAsyncTask<Void ,Void ,String,BaseActivity> {

        public GetEncourageAsyncTask(BaseActivity baseActivity) {
            super(baseActivity);
        }

        @Override
        protected String doInBackground(BaseActivity baseActivity, Void... voids) {
            String code ="";
            String result = HttpUtils.getOkHpptRequest(HttpUtils.ipAddress+"/app/encouraging?type="+language);
            Log.i("language","-->"+language);

            if (!TextUtils.isEmpty(result)){
                try {
                    Log.i("language","-->"+result);
                    JSONObject jsonObject = new JSONObject(result);
                    code = jsonObject.getString("state");
                    message1 = jsonObject .getString("message1");
                    JSONObject data = jsonObject.getJSONObject("data");
                   encouraging  = data.getString("encouraging");
                   auth = data.getString("auth");
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
                    tv_encourage.setText(encouraging);
                    tv_encourage_zz.setText(auth);
                    break;
                    default:

                        break;
            }
        }
    }

    String shopUrl ;
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
                    message1 = jsonObject .getString("message1");
                    JSONObject data = jsonObject.getJSONObject("data");
                    shopUrl  = data.getString("shopUrl");
                    Log.i("ShopUrl","-->"+shopUrl);
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
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("shopUrl",shopUrl);
                    editor.commit();
                    break;
                default:

                    break;
            }
        }
    }
}
