package teabar.ph.com.teabar.adpter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import teabar.ph.com.teabar.R;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.MyviewHolder> {

    private List<String> mData;
    private Context context;
    private EqupmentInformAdapter.OnItemClickListener onItemClickListener;

    public FriendAdapter(Context context , List<String> list ) {
        this.context = context;
        this.mData = list;

    }

    @NonNull
    @Override
    public MyviewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
       MyviewHolder holder = new MyviewHolder(LayoutInflater.from(context).inflate(R.layout.item_friend,viewGroup,false));
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MyviewHolder myviewHolder, int position) {

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    class MyviewHolder extends RecyclerView.ViewHolder{
        public MyviewHolder(View itemView){
            super(itemView);
        }
    }
}
