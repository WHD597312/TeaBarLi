package teabar.ph.com.teabar.activity.my;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import teabar.ph.com.teabar.R;
import teabar.ph.com.teabar.base.BaseActivity;
import teabar.ph.com.teabar.base.BaseWeakAsyncTask;
import teabar.ph.com.teabar.pojo.Plan;
import teabar.ph.com.teabar.util.HttpUtils;
import teabar.ph.com.teabar.util.ToastUtil;

//添加購買計畫
public class AddPurchaseActivity extends BaseActivity {


    @BindView(R.id.rl_fits)
    RecyclerView rl_fits;

    Map<String,Object> params=new HashMap<>();
    String userId;
    PlanAdapter planAdapter;
    List<Plan> plans=new ArrayList<>();
    @Override
    public void initParms(Bundle parms) {
        userId=parms.getString("userId");
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_add_purchase;
    }

    @Override
    public void initView(View view) {
        params.put("userId",userId);
        planAdapter=new PlanAdapter(this,plans);
        LinearLayoutManager manager=new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        rl_fits.setLayoutManager(manager);
        rl_fits.setAdapter(planAdapter);
        new GetAllPlanAsync(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,params);
    }

    @Override
    public void onBackPressed() {
        if (selected==1){
            setResult(200);
        }
        finish();
    }

    @OnClick({R.id.iv_plan_fh})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.iv_plan_fh:
                if (selected==1){
                    setResult(200);
                }
                finish();
                break;
        }
    }
    @Override
    public void doBusiness(Context mContext) {

    }

    List<Long> plansId=new ArrayList<>();
    @Override
    public void widgetClick(View v) {

    }
    int selectPosition=-1;
    int selected;
    class PlanAdapter extends RecyclerView.Adapter<PlanHolder>{

        private Context context;
        private List<Plan> plans;

        public PlanAdapter(Context context, List<Plan> plans) {
            this.context = context;
            this.plans = plans;
        }

        @NonNull
        @Override
        public PlanHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view=View.inflate(context,R.layout.item_user_plan,null);

            return new PlanHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PlanHolder planHolder, int i) {
            Plan plan=plans.get(i);

            String planName="";
            String describe="";
            planName=plan.getPlanNameEn();
            describe=plan.getDescribeEn();
            planHolder.tv_title.setText(planName);
            planHolder.tv_desc.setText(describe);
            int flag=plan.getFlag();
            if (flag==1){
                planHolder.tv_add.setBackground(getResources().getDrawable(R.drawable.answer_button2));
                planHolder.tv_add.setTextColor(getResources().getColor(R.color.color_gray));
                planHolder.tv_add.setText(getString(R.string.added));
            }else {
                planHolder.tv_add.setBackground(getResources().getDrawable(R.drawable.answer_buttonlv));
                planHolder.tv_add.setTextColor(getResources().getColor(R.color.nomal_green));
                planHolder.tv_add.setText(getString(R.string.add));
            }

            planHolder.tv_add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (plan.getFlag()==0){
                        selectPosition=i;
                        params.clear();
                        plansId.clear();
                        plansId.add(plan.getId());
                        params.put("userId",userId);
                        params.put("planId",plansId);
                        new AddUserPlanAsync(AddPurchaseActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,params);
                    }else {
                        ToastUtil.showShort(AddPurchaseActivity.this,getResources().getString(R.string.plan_added));
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return plans.size();
        }
    }
    class PlanHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.tv_title)
        TextView tv_title;
        @BindView(R.id.tv_desc)
        TextView tv_desc;
        @BindView(R.id.tv_add)
        TextView tv_add;
        public PlanHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
    class GetAllPlanAsync extends BaseWeakAsyncTask<Map<String,Object>,Void,Integer,AddPurchaseActivity>{

        public GetAllPlanAsync(AddPurchaseActivity addPurchaseActivity) {
            super(addPurchaseActivity);
        }

        @Override
        protected Integer doInBackground(AddPurchaseActivity addPurchaseActivity, Map<String, Object>... maps) {
            String url=HttpUtils.ipAddress+"/app/getAllPlan";
            Map<String,Object> map=maps[0];
            int code=0;
            try {
                String result=HttpUtils.postOkHpptRequest(url,map);
                if (!TextUtils.isEmpty(result)){
                    if (!TextUtils.isEmpty(result)){
                        JSONObject jsonObject=new JSONObject(result);
                        code=jsonObject.getInt("state");
                        if (code==200){
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

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return code;
        }

        @Override
        protected void onPostExecute(AddPurchaseActivity addPurchaseActivity, Integer integer) {
            if (integer==200 && !plans.isEmpty()){
                planAdapter.notifyDataSetChanged();
            }
        }
    }
    class AddUserPlanAsync extends BaseWeakAsyncTask<Map<String,Object>,Void,Integer,AddPurchaseActivity>{

        public AddUserPlanAsync(AddPurchaseActivity addPurchaseActivity) {
            super(addPurchaseActivity);
        }

        @Override
        protected Integer doInBackground(AddPurchaseActivity addPurchaseActivity, Map<String, Object>... maps) {
            String url=HttpUtils.ipAddress+"/app/addPlans";
            Map<String,Object> map=maps[0];

            int code=0;
            try {
                String result=HttpUtils.postOkHpptRequest(url,map);
                if (!TextUtils.isEmpty(result)){
                    JSONObject jsonObject=new JSONObject(result);
                    code=jsonObject.getInt("state");
                    String message3=jsonObject.getString("message3");
                    if ("Add a success".equals(message3)){
                        code=200;
                    }else if ("The plan has been added".equals(message3)){
                        code=-200;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return code;
        }

        @Override
        protected void onPostExecute(AddPurchaseActivity addPurchaseActivity, Integer integer) {
            if (integer==200){
                selected=1;
                ToastUtil.showShort(AddPurchaseActivity.this,getResources().getString(R.string.plan_add));
                Plan plan=plans.get(selectPosition);
                plan.setFlag(1);
                plans.set(selectPosition,plan);
                planAdapter.notifyDataSetChanged();
            }else if (integer==-200){
                ToastUtil.showShort(AddPurchaseActivity.this,getResources().getString(R.string.plan_added));
            }else {
                ToastUtil.showShort(AddPurchaseActivity.this,getResources().getString(R.string.toast_all_cs));
            }
        }
    }
}
