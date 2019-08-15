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
import teabar.ph.com.teabar.activity.device.MakeActivity;
import teabar.ph.com.teabar.pojo.Tea;
import teabar.ph.com.teabar.util.GlideCircleTransform;

public class TeaAdapter extends RecyclerView.Adapter<TeaAdapter.MyViewHolder> {

    private List<Tea> mDatas = new ArrayList<>();
    private Context mContext;
    public TeaAdapter(Context context, List<Tea> list) {
        this.mContext = context;
        this.mDatas = list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        MyViewHolder holder = new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_mailpic,viewGroup,false));
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder,  int i) {
        String headImg = mDatas.get(i).getTeaPicture();

        myViewHolder.tv_mail_name .setText(mDatas.get(i).getProductNameEn());


        Glide.with(mContext).load(headImg).placeholder(R.mipmap.moren)// 正在加载中的图片  
                .error(R.mipmap.moren)// 加载失败的图片  
                .diskCacheStrategy(DiskCacheStrategy.ALL).transform(new GlideCircleTransform(mContext)).into(myViewHolder.iv_mail_pic);
        myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long id = mDatas.get(i).getId();
                Intent intent = new Intent(mContext,MakeActivity.class);
                intent.putExtra("teaId",mDatas.get(i).getId());
                mContext.startActivity(intent);
            }
        });
    }
    public void update(List<Tea> tea1 ){
        this.mDatas  = tea1;
        notifyDataSetChanged();
    }
    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv_mail_name;
        ImageView iv_mail_pic;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_mail_pic = itemView.findViewById(R.id.iv_mail_pic);
            tv_mail_name = itemView.findViewById(R.id.tv_mail_name);

        }
    }


}
