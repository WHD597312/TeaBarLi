package teabar.ph.com.teabar.activity.social;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import teabar.ph.com.teabar.R;
import teabar.ph.com.teabar.adpter.AllCommentAdapter;
import teabar.ph.com.teabar.base.BaseActivity;
import teabar.ph.com.teabar.base.BaseWeakAsyncTask;
import teabar.ph.com.teabar.base.MyApplication;
import teabar.ph.com.teabar.pojo.Comment;
import teabar.ph.com.teabar.util.HttpUtils;
import teabar.ph.com.teabar.util.NetWorkUtil;
import teabar.ph.com.teabar.util.ToastUtil;
//所有設備評論
public class AllCommentActivity extends BaseActivity {
    String id;
    @BindView(R.id.rv_allcomment)
    RecyclerView rv_allcomment;
    @BindView(R.id.refreshLayout_xq)
    RefreshLayout refreshLayou;
    QMUITipDialog tipDialog;
    List<Comment> mLists;
    int pageNum=1;
    AllCommentAdapter allCommentAdapter;
    MyApplication application;
    @Override
    public void initParms(Bundle parms) {
         id = parms.getString("contentId");
    }

    @Override
    public int bindLayout() {
        setSteepStatusBar(true);
        return R.layout.activity_allcomment;
    }

    @Override
    public void initView(View view) {
        if (application == null) {
            application = (MyApplication) getApplication();
        }
        showProgressDialog();
        mLists = new ArrayList<>();
        Map<String ,Object> params = new HashMap<>();
        params.put("contentId",id);
        params.put("currentPage",1);
        params.put("pageSize",10);
        new FindMethordAsynTask(this).execute(params);
        refreshLayou.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {

                boolean isConn = NetWorkUtil.isConnected(MyApplication.getContext());
                if (isConn){
                    mLists.clear();
                    pageNum=1;
                    Map<String ,Object> params = new HashMap<>();
                    params.put("contentId",id);
                    params.put("currentPage",1);
                    params.put("pageSize",10);
                    new FindMethordAsynTask(AllCommentActivity.this).execute(params);
                }else {
//                    toast(   "无网络可用，请检查网络");
                }

            }

        });

        refreshLayou.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                boolean isConn = NetWorkUtil.isConnected(MyApplication.getContext());
                if (isConn){
//                    refreshLayout.finishLoadMore(5000,false,false);
                    pageNum++;
                    Map<String ,Object> params = new HashMap<>();
                    params.put("contentId",id);
                    params.put("currentPage",pageNum);
                    params.put("pageSize",10);
                    new FindMethordAsynTask(AllCommentActivity.this).execute(params);
                }else {
//                    toast(   "无网络可用，请检查网络");
                }
            }
        });
        allCommentAdapter = new AllCommentAdapter(this,mLists);
        rv_allcomment.setLayoutManager(new LinearLayoutManager(this));
        rv_allcomment.setAdapter(allCommentAdapter);

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
    class FindMethordAsynTask extends BaseWeakAsyncTask<Map<String,Object>,Void,String,BaseActivity> {

        public FindMethordAsynTask(BaseActivity baseActivity) {
            super(baseActivity);
        }

        @Override
        protected String doInBackground(BaseActivity baseActivity, Map<String, Object>... maps) {
            String code = "";
            Map<String, Object> prarms = maps[0];
            String result =   HttpUtils.postOkHpptRequest(HttpUtils.ipAddress+"/content/showComment",prarms);
            Log.e("back", "--->" + result);
            if (!ToastUtil.isEmpty(result)) {
                if (!"4000".equals(result)){
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        code = jsonObject.getString("state");
                        returnMsg1=jsonObject.getString("message2");
                        returnMsg2=jsonObject.getString("message3");
                        JSONObject jsonObject2  = jsonObject.getJSONObject("data");
                        JSONArray  jsonArray = jsonObject2.getJSONArray("items");
                        for (int i = 0;i<jsonArray.length();i++){
                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                            Comment comment = new Comment();
                            String comment1 = jsonObject1.getString("comment");
                            long id = jsonObject1.getLong("id");
                            String commentUserName = jsonObject1.getString("commentUserName");
                            String photoUrl = jsonObject1.getString("photoUrl");
                            comment.setComment(comment1);
                            comment.setId(id);
                            comment.setCommentUserName(commentUserName);
                            comment.setPhotoUrl(photoUrl);
                            mLists.add(comment);
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
                    allCommentAdapter.setmDatas(mLists);
                    refreshLayou.finishLoadMore(true);
                    refreshLayou.finishRefresh(true);

//                    toast( returnMsg1);

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

    @Override
    public void widgetClick(View v) {

    }
    @OnClick({R.id.iv_power_fh})
    public  void onClick(View view){
        switch (view.getId()){
            case R.id.iv_power_fh :
                finish();
                break;
        }
    }

}
