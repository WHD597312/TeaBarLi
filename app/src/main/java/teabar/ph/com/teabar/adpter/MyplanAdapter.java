package teabar.ph.com.teabar.adpter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

import teabar.ph.com.teabar.R;
import teabar.ph.com.teabar.activity.BuyPlanActivity;
import teabar.ph.com.teabar.base.MyApplication;
import teabar.ph.com.teabar.pojo.Plan;
import teabar.ph.com.teabar.view.PlanProgressBar;

//計畫適配器
public class MyplanAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_TOP = 0;//健康知識標識
    private static final int TYPE_BUTTON = 1;//個人計畫表示
    private List<Plan> mDatas = new ArrayList<>();
    private Context mContext;
    public MyplanAdapter(Context context, List<Plan> list) {
        this.mContext = context;
        this.mDatas = list;
    }

    /**
     * 返回佈局類型
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
//        return super.getItemViewType(position);
//        if (position==2){
            return 1;
//        }else {
//            return 0;
//        }

    }


    /**
     *
     * @param viewGroup
     * @param position 佈局類型 這裡佈局類型只有一種，統一為1
     * @return
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {

        // 个人计划卡片
        if (  position== TYPE_TOP ) {
          MyViewHolder holder = new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_myplan,viewGroup,false));
            return holder;
        }
        // 健康计划卡片
        if ( position== TYPE_BUTTON ) {
            MyViewHolder2 holder = new MyViewHolder2(LayoutInflater.from(mContext).inflate(R.layout.item_myhealthp,viewGroup,false));
            return holder;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder  holder, final int position) {
        if (holder instanceof MyViewHolder) {

        }

        if (holder instanceof MyViewHolder2) {
            String headImg = mDatas.get(position).getPlanPhoto();
            String s = mContext.getText(R.string.weather_week).toString() ;
             String title;
             if (MyApplication.IsEnglish==1){
                 title=mDatas.get(position).getPlanNameEn();
             }else {
                 title=mDatas.get(position).getPlanNameCn();
             }
             String week =  mDatas.get(position).getPlanTime()+" "+s;
            ((MyViewHolder2) holder).tv_health_name .setText(title);
            ((MyViewHolder2) holder).tv_health_mes .setText(mDatas.get(position).getDescribeEn());
            Glide.with(mContext).load(headImg).diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.color.white).into(((MyViewHolder2) holder).iv_plan_pic);
            ((MyViewHolder2) holder).itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext,BuyPlanActivity.class);
                    intent.putExtra("plan",mDatas.get(position));
                    intent.putExtra("title",title);
                    intent.putExtra("week",week);
                    mContext.startActivity(intent);
                }
            });

        }
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }
    public void update(List<Plan> plans){
        this.mDatas = plans;
        notifyDataSetChanged();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView iv_plan_pic;
            TextView tv_plan_name, tv_tea_name, tv_plan_day;
            PlanProgressBar pl_progress;

            public MyViewHolder(View itemView) {
                super(itemView);
                iv_plan_pic = itemView.findViewById(R.id.iv_plan_pic);
                tv_plan_name = itemView.findViewById(R.id.tv_plan_name);
                tv_tea_name = itemView.findViewById(R.id.tv_tea_name);
                tv_plan_day = itemView.findViewById(R.id.tv_plan_day);
                pl_progress = itemView.findViewById(R.id.pl_progress);
            }
        }
    class MyViewHolder2 extends RecyclerView.ViewHolder {
        ImageView iv_plan_pic;
        TextView tv_health_name, tv_health_mes, tv_plan_day;
        PlanProgressBar pl_progress;

        public MyViewHolder2(View itemView) {
            super(itemView);
            iv_plan_pic = itemView.findViewById(R.id.iv_plan_pic);
            tv_health_name = itemView.findViewById(R.id.tv_health_name);
            tv_health_mes = itemView.findViewById(R.id.tv_health_mes);
            tv_plan_day = itemView.findViewById(R.id.tv_plan_day);

        }
    }

}
