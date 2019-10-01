package user.ai_camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RecyclerViewAdapter extends Adapter<ViewHolder> {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FOOTER = 1;
    private static final int TYPE_FIRST = 2 ;
    private Context context;

    protected List data;
    protected  List<Map<String,String>> imgUrl;
    public int count;
    public static int index = 0 , maxindex = 0;
    private View view_copy, view_copy_first;
    private Point size;
	private Handler handler = new Handler();
	public Map<String, Bitmap> dictionary = new HashMap<>();

    public RecyclerViewAdapter(Context context,Point size, List data,List<Map<String,String>> imgUrl) {
        this.context = context;
        this.data = data;
        this.imgUrl = imgUrl;
        count = 0;
        index = 0;
        maxindex = 0;
        this.size = size;
    }

    public interface OnItemClickListener {
        void onItemClick(ItemViewHolder holder,ImageButton ib, int position);
		void onCameraClick(ItemViewHolder holder,ImageButton ib, int position);
        void onItemLongClick(View view, int position);
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(getItemCount() <= 1){
           // Log.d("init test","only one item");
            return TYPE_FOOTER;
        }else {
           // Log.d("init test","item's number"+getItemCount());
            if (position + 1 == getItemCount()) {
                if(position +1 >= imgUrl.size()-1){
                	return TYPE_ITEM;
				}else {
					return TYPE_FOOTER;
				}
            } else if (position == 0) {
                return TYPE_FIRST;
            } else {
                return TYPE_ITEM;
            }
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            //Log.d("test", "in Create count is "+count);
            View view = LayoutInflater.from(context).inflate(R.layout.item_base, parent,
                    false);
            view_copy = view;
            return new ItemViewHolder(view,false);
        } else if (viewType == TYPE_FOOTER) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_foot, parent,
                    false);
            return new FootViewHolder(view);
        }else if(viewType==TYPE_FIRST) {
             View  view = LayoutInflater.from(context).inflate(R.layout.item_base_first, parent,
                        false);
             view_copy_first = view;
             return new ItemViewHolder(view,true);
            }
            return null;
    }


    @Override
    public void onBindViewHolder( ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            //holder.tv.setText(data.get(position));
            holder.setIsRecyclable(false);
            if (onItemClickListener != null) {

				RetrieveImageTask retrieveImageTask;
                if(position ==0){
					retrieveImageTask = new RetrieveImageTask(((ItemViewHolder) holder).ib,size,true);
				}
				else{
					retrieveImageTask =  new RetrieveImageTask(((ItemViewHolder) holder).ib,size,false);
				}
				((ItemViewHolder) holder).retrieveImageTask = retrieveImageTask;
				index = position + 1;
				if(index >= maxindex){
					maxindex = index;
				}
				if(position+1 < imgUrl.size()){
				    //Log.d("RecyclerView Adapter Keyword Receive Test",imgUrl.get(0).get("name"));
					if(position+1 % 2 == 1 &&  position +1 != 1) {
						((ItemViewHolder) holder).root.setCardBackgroundColor(Color.parseColor("#0F000000"));
					}else if ((position + 1) % 2 == 0 ){
						((ItemViewHolder) holder).root.setCardBackgroundColor(Color.parseColor("#00000000"));
					}
					if(dictionary.get(imgUrl.get(position+1).get("name")) == null) {
						retrieveImageTask.execute("http://140.115.51.177:8000/media/" + imgUrl.get(0).get("name") + "/" + imgUrl.get(position + 1).get("name"));
						double score = Double.parseDouble(Objects.requireNonNull(imgUrl.get(position + 1).get("score")));

						((ItemViewHolder) holder).score = score;
						/*if(score <= 4){
							((ItemViewHolder) holder).fire.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.redlight32));
						}else if (score <= 6 && score > 4){
							((ItemViewHolder) holder).fire.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.yellowlight32));
						}else{
							((ItemViewHolder) holder).fire.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.greenlight32));
						}*/

						((ItemViewHolder) holder).tvInfo.setText((String.format("%.2f",((ItemViewHolder) holder).score) + " ・ " + imgUrl.get(0).get("name")));
						Runnable runnable = new Runnable() {
							@Override
							public void run() {
								if(retrieveImageTask.resultBack() != null){
									dictionary.put(Objects.requireNonNull(imgUrl.get(position + 1).get("name")), retrieveImageTask.resultBack());
								}else{
									handler.postDelayed(this,100);
								}
							}
						};
						handler.postDelayed(runnable,100);

					}else{
						Bitmap img = dictionary.get(imgUrl.get(position + 1).get("name"));
						if (position == 0) {
							((ItemViewHolder) holder).ib.setImageBitmap(Bitmap.createScaledBitmap(Objects.requireNonNull(img), (int) (size.x * 0.85d), (int) (size.y * 0.7d), true));
						} else {
							((ItemViewHolder) holder).ib.setImageBitmap(Bitmap.createScaledBitmap(Objects.requireNonNull(img), (int) (size.x), (int) (size.x *0.6d), true));
						}
						((ItemViewHolder) holder).score = Double.parseDouble(Objects.requireNonNull(imgUrl.get(position + 1).get("score")));
						((ItemViewHolder) holder).tvInfo.setText((String.format("%.2f",((ItemViewHolder) holder).score) + " ・ " + imgUrl.get(0).get("name")));
						}
				}
				((ItemViewHolder) holder).ID = String.valueOf(position);
				//String str = ((ItemViewHolder) holder).title.getText().toString();
				if(holder.getItemViewType()==TYPE_FIRST){
					if(position == 0) {
						// Log.d("RecyclerView TYPE_ITEM Test","before "+holder.getItemViewType());
						//holder = new ItemViewHolder(view_copy,false);
						//Log.d("RecyclerView TYPE_ITEM Test","after "+holder.getItemViewType());
						((ItemViewHolder) holder).title.setText(R.string.rank_no);
						String s = ((ItemViewHolder) holder).title.getText() + "" + (position+1);
						((ItemViewHolder) holder).title.setText(s);
					}
				}
				else{
						//Log.d("RecyclerView TYPE_ITEM Test","Ibefore "+holder.getItemViewType());
						// holder = new ItemViewHolder(view_copy_first,true);

						//Log.d("RecyclerView TYPE_ITEM Test","Iafter "+holder.getItemViewType());
						((ItemViewHolder) holder).title.setText(R.string.rank_no);
						String s = ((ItemViewHolder) holder).title.getText() + ((position + 1) < 10 ? "  " + (position+1) : String.valueOf(position+1));
						((ItemViewHolder) holder).title.setText(s);
						if(position+1 == 2 ){
							((ItemViewHolder) holder).award.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.sliver32));
						}else if(position+1 ==3){
							((ItemViewHolder) holder).award.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.bronze32));
						}
				}


                final ViewHolder finalHolder = holder;
                ((ItemViewHolder) holder).ib.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = finalHolder.getLayoutPosition();
                        Log.d("RecyclerView Position Test", String.valueOf(finalHolder.getItemViewType()) + "position " + position);
                        onItemClickListener.onItemClick((ItemViewHolder) finalHolder ,((ItemViewHolder) finalHolder).ib, position);
                    }
                });

				((ItemViewHolder) holder).ib_camera.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						int position = finalHolder.getLayoutPosition();
						Log.d("RecyclerView Position Test", String.valueOf(finalHolder.getItemViewType()) + "position " + position);
						onItemClickListener.onCameraClick((ItemViewHolder) finalHolder ,((ItemViewHolder) finalHolder).ib, position);
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
            ((ItemViewHolder) holder).ib.setTag(position);
			//holder.setIsRecyclable(false);
		}
    }


    static class ItemViewHolder extends ViewHolder {

        ImageButton ib;
        ImageButton ib_camera;
        TextView tvInfo;
        TextView title;
        ImageButton award;
        CardView root;

        RetrieveImageTask retrieveImageTask;
        double rating = 0;
        double score = 0 ;
        String ID = "null";
        public ItemViewHolder(View view,Boolean IsFirstPosition) {
            super(view);
            view.setVisibility(View.VISIBLE);
            ib = view.findViewById(R.id.ib_image);
            tvInfo = view.findViewById(R.id.tv_info);
            title = view.findViewById(R.id.tv_date);
            award = view.findViewById(R.id.ib_award);
            root = view.findViewById(R.id.cardView);
            ib_camera = view.findViewById(R.id.ib_camera);
        }
    }

    static class FootViewHolder extends ViewHolder {

        public FootViewHolder(View view) {
            super(view);
            view.setVisibility(View.VISIBLE);
        }
    }
}