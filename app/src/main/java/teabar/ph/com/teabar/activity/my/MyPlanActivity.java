package teabar.ph.com.teabar.activity.my;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import teabar.ph.com.teabar.R;
import teabar.ph.com.teabar.adpter.PlanAdapter;
import teabar.ph.com.teabar.base.BaseActivity;
import teabar.ph.com.teabar.base.BaseWeakAsyncTask;
import teabar.ph.com.teabar.base.MyApplication;
import teabar.ph.com.teabar.pojo.Plan;
import teabar.ph.com.teabar.util.HttpUtils;
import teabar.ph.com.teabar.util.ToastUtil;

//我的個人計畫頁面
public class MyPlanActivity extends BaseActivity {
    @BindView(R.id.rv_myplan)
    RecyclerView rv_myplan;

    PlanAdapter mPlanAdapter;//個人計畫列表適配器
    List<Plan> plans = new ArrayList<>();
    MyApplication application;
    String userId;//用户id
    Map<String,Object> params=new HashMap<>();
    Map<String,Object> planParams=new HashMap<>();
    @Override
    public void initParms(Bundle parms) {
        userId=parms.getString("userId");
    }

    @Override
    public int bindLayout() {
        setSteepStatusBar(true);
        return R.layout.activity_myplan;
    }

    @Override
    public void initView(View view) {
        if (application == null) {
            application = (MyApplication) getApplication();
        }
        application.addActivity(this);
        params.put("userId",userId);
        Log.i("userId","-->"+userId);
        new GetPersonPlanAsync(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,params);//获取个人计划
        mPlanAdapter = new PlanAdapter(this,plans);
        rv_myplan.setLayoutManager(new LinearLayoutManager(this));
        rv_myplan.setAdapter(mPlanAdapter);
        mPlanAdapter.setItemClicklistener(new PlanAdapter.OnItemClicklistener() {//点击mPlanAdapter中的item,弹出计划时间对话框
            @Override
            public void onItemClickListener(Plan plan,int position) {
                setPlanTimeDialog(plan,position);
            }
        });

    }

    int amFlag;
    int pmFlag;
    int operatePosition;
    int amOrPm=0;//操作am,pm標誌 1為am 2為pm
    PlanDialog planDialog;
    private void setPlanTimeDialog(Plan plan,int position){
        if (planDialog!=null && planDialog.isShowing()){
            return;
        }
        operatePosition=position;
        planDialog=new PlanDialog(this);
        planDialog.setPlan(plan);

        planParams.put("userId",userId);
        planParams.put("planId",plan.getId());
        planParams.put("amFlag",plan.getAmFlag());
        planParams.put("pmFlag",plan.getPmFlag());
        planDialog.setListener(new PlanDialog.AmOrPmClickListener() {
            @Override
            public void OnAmClickListener() {
                if (plan.getAmFlag()==1){
                    amFlag=0;
                }else {
                    amFlag=1;
                }
                planParams.put("amFlag",amFlag);
                amOrPm=1;
                new UpdatePlanTimeAsync(MyPlanActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,planParams);
            }

            @Override
            public void OnPmClickListener() {
                if (plan.getPmFlag()==1){
                    pmFlag=0;
                }else {
                    pmFlag=1;
                }
                amOrPm=2;
                planParams.put("pmFlag",pmFlag);
                new UpdatePlanTimeAsync(MyPlanActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,planParams);
            }
        });
        planDialog.setCanceledOnTouchOutside(true);
        backgroundAlpha(0.8f);

        planDialog.show();
        planDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                backgroundAlpha(1.0f);
            }
        });
    }
    //设置蒙版
    private void backgroundAlpha(float f) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = f;
        getWindow().setAttributes(lp);
    }
    @Override
    public void doBusiness(Context mContext) {

    }

    @Override
    public void widgetClick(View v) {

    }
    @OnClick({R.id.iv_plan_fh,R.id.img_add})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.iv_plan_fh:
                finish();
                break;

            case R.id.img_add:
                Intent intent=new Intent(this,AddPurchaseActivity.class);
                intent.putExtra("userId",userId);
                startActivityForResult(intent,200);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==200){
            new GetPersonPlanAsync(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,params);
        }
    }

    Plan plan;
    class UpdatePlanTimeAsync extends BaseWeakAsyncTask<Map<String,Object>,Void,Integer,MyPlanActivity>{

        public UpdatePlanTimeAsync(MyPlanActivity myPlanActivity) {
            super(myPlanActivity);
        }

        @Override
        protected Integer doInBackground(MyPlanActivity myPlanActivity, Map<String, Object>... maps) {
            String url=HttpUtils.ipAddress+"/app/updateFlag";
            int code=0;
            Map<String,Object> map=maps[0];
            try {
                String result=HttpUtils.postOkHpptRequest(url,map);
                if (!TextUtils.isEmpty(result)){
                    JSONObject jsonObject=new JSONObject(result);
                    code=jsonObject.getInt("state");
                    plan=plans.get(operatePosition);
                    if (amOrPm==1){
                        plan.setAmFlag(amFlag);
                    }else if (amOrPm==2){
                        plan.setPmFlag(pmFlag);
                    }
                    plans.set(operatePosition,plan);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return code;
        }

        @Override
        protected void onPostExecute(MyPlanActivity myPlanActivity, Integer integer) {
            if (integer==200){
                ToastUtil.showShort(MyPlanActivity.this,R.string.modify_success);
                if (planDialog!=null && planDialog.isShowing() && plan!=null){
                    planDialog.updatePlan(plan);
                    planDialog.dismiss();
                }
            }else {
                ToastUtil.showShort(MyPlanActivity.this,R.string.modify_fail);
            }
        }
    }
    //获取个人计划
    class GetPersonPlanAsync extends BaseWeakAsyncTask<Map<String,Object>,Void,Integer,MyPlanActivity>{

        public GetPersonPlanAsync(MyPlanActivity myPlanActivity) {
            super(myPlanActivity);
        }

        @Override
        protected Integer doInBackground(MyPlanActivity myPlanActivity, Map<String, Object>... maps) {
            String url=HttpUtils.ipAddress+"/app/getUserPlan";
            int code=0;
            Map<String,Object> map=maps[0];
            try {
                String result=HttpUtils.postOkHpptRequest(url,map);
                Log.i("GetPersonPlanAsync","-->"+result);
                if (!TextUtils.isEmpty(result)){
                    JSONObject jsonObject=new JSONObject(result);
                    code=jsonObject.getInt("state");
                    if (code==200){
                        plans.clear();
                        JSONArray data=jsonObject.getJSONArray("data");
                        int size=data.length();
                        for (int i = 0; i <size ; i++) {
                            JSONObject object=data.getJSONObject(i);
                            Gson gson=new Gson();
                            Plan plan=gson.fromJson(object.toString(), Plan.class);
                            plans.add(plan);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return code;
        }

        @Override
        protected void onPostExecute(MyPlanActivity myPlanActivity, Integer integer) {
            if (integer==200 && !plans.isEmpty()){
                mPlanAdapter.notifyDataSetChanged();
            }
        }
    }
}
