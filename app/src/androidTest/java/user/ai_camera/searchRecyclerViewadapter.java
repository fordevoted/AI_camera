package user.ai_camera;

import android.content.Context;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class searchRecyclerViewadapter extends Adapter<ViewHolder> {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FOOTER = 1;
    private static final int TYPE_FIRST = 2 ;
    private Context context;
    protected List data;
    public int count;
    private boolean isFirst;
    private View view_copy, view_copy_first;

    public searchRecyclerViewadapter(Context context, List data) {
        this.context = context;
        this.data = data;
        count = 0;
        isFirst = true;
    }

    public interface OnItemClickListener {
        void onItemClick(View view,Button tb, int position);

        void onItemLongClick(View view, int position);
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public int getItemCount() {
        return data.size() == 0 ? 0 : data.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position + 1 == getItemCount()) {
            return TYPE_FOOTER;
        }else if (position==0){
            isFirst = false;
            return TYPE_FIRST;
        }
        else {
            return TYPE_ITEM;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            //Log.d("test", "in Create count is "+count);
            View view = LayoutInflater.from(context).inflate(R.layout.search_item_base, parent,
                    false);
            view_copy = view;
            return new ItemViewHolder(view);
        } else if (viewType == TYPE_FOOTER) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_foot, parent,
                    false);
            return new FootViewHolder(view);
        }else if(viewType==TYPE_FIRST) {
            View  view = LayoutInflater.from(context).inflate(R.layout.search_item_base, parent,
                    false);
            view_copy_first = view;
            return new ItemViewHolder(view);
        }
        return null;
    }


    @Override
    public void onBindViewHolder( ViewHolder holder, int position) {
        //holder.setIsRecyclable(false);
        if (holder instanceof ItemViewHolder) {
            //holder.tv.setText(data.get(position));
            if (onItemClickListener != null) {
                if(holder.getItemViewType()==TYPE_FIRST){
                    if(position > 0) {
                        Log.d("TYPE_ITEM","before "+holder.getItemViewType());
                        holder = new ItemViewHolder(view_copy);
                        Log.d("TYPE_ITEM","after "+holder.getItemViewType());
                    }
                }
                else if(holder.getItemViewType()==TYPE_ITEM){
                    if(position == 0){
                        Log.d("TYPE_ITEM","Ibefore "+holder.getItemViewType());
                        holder = new ItemViewHolder(view_copy_first);

                        Log.d("TYPE_ITEM","Iafter "+holder.getItemViewType());

                    }
                }
                TextView text = holder.itemView.findViewById(R.id.search_place);
                String str = text.getText().toString();
                text.setText(str.substring(0,str.length()-3)+"  "+position);

                final ViewHolder finalHolder = holder;
                ((ItemViewHolder) holder).tb.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = finalHolder.getLayoutPosition();
                        Log.d("type", String.valueOf(finalHolder.getItemViewType())+"position "+position);
                        onItemClickListener.onItemClick(((ItemViewHolder) finalHolder).tb, ((ItemViewHolder) finalHolder).tb, position);
                    }
                });

                holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        int position = finalHolder.getLayoutPosition();
                        onItemClickListener.onItemLongClick(finalHolder.itemView, position);
                        return false;
                    }
                });
            }
        }
    }


    static class ItemViewHolder extends ViewHolder {

        Button tb;

        public ItemViewHolder(View view) {
            super(view);
            view.setVisibility(View.VISIBLE);
            tb = view.findViewById(R.id.search_info);
        }
    }

    static class FootViewHolder extends ViewHolder {

        public FootViewHolder(View view) {
            super(view);
            view.setVisibility(View.VISIBLE);
        }
    }
}