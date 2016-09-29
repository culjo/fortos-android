package com.novugrid.fortos.adapters;

import android.content.Context;
import android.graphics.Color;
import android.media.Image;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.novugrid.fortos.AppConfig;
import com.novugrid.fortos.adapters.viewholder.ImageViewHolder;
import com.novugrid.fortos.adapters.viewholder.ProgressViewHolder;
import com.novugrid.fortos.datamodel.ImageData;
import com.novugrid.fortos.listeners.OnPopupMenuBtnClickListener;
import com.novugrid.fortos.listeners.RvItemClickListener;
import com.novugrid.fortos.R;
import com.novugrid.fortos.listeners.OnLoadMoreListener;
import com.novugrid.fortos.utils.DateHelper;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by WeaverBird on 8/9/2016.
 */

public class ImagesAdapter extends RecyclerView.Adapter{

    private final int VIEW_IS_ITEM = 1;
    private final int VIEW_IS_PROGRESS = 0;

    List<ImageData> mainDataList;
    private ArrayList<Integer> userFavorites;


    private boolean loading;
    int totalItemCount, lastVisibleItemPosition;
    int visibleThreshold = 3;//The min amount of items to av below ur current scroll pos b4 loading more
    private OnLoadMoreListener onLoadMoreListener;
    private RecyclerView recyclerView;
    private Context context;

    private DateHelper dateHelper;

    private final String TAG = ImagesAdapter.class.getSimpleName();

    // SET UP THE CLICK LISTENERS HERE..


    public RvItemClickListener rvItemClickListener;
    private OnPopupMenuBtnClickListener onPopupMenuBtnClickListener;


    public void setRvItemClickListener(RvItemClickListener rvItemClickListener){
        this.rvItemClickListener = rvItemClickListener;
    }

    public void setOnPopupMenuBtnClickListener(OnPopupMenuBtnClickListener popupMenuBtnClickListener){
        this.onPopupMenuBtnClickListener = popupMenuBtnClickListener;
    }


    public ImagesAdapter(List<ImageData> dataList){
        mainDataList = dataList;
        dateHelper = new DateHelper();
    }

    @Override
    public int getItemViewType(int position) {
        return mainDataList.get(position) != null ? VIEW_IS_ITEM : VIEW_IS_PROGRESS;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        context = parent.getContext();
        RecyclerView.ViewHolder viewHolder;
        View layout;

        if(viewType == VIEW_IS_ITEM){
            layout = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_image_item, parent, false);
            viewHolder = new ImageViewHolder(layout);
        }else {
            layout = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_progress_more_data_loading, parent, false);
            viewHolder = new ProgressViewHolder(layout);
        }

        return viewHolder;//return artistListViewHolderOb
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holderOb, int position) {

        if(holderOb instanceof ImageViewHolder){

            ImageViewHolder holder = ((ImageViewHolder) holderOb);

            if(mainDataList.get(position).getCaption().isEmpty()){
                holder.caption.setVisibility(View.GONE);
            }else {holder.caption.setVisibility(View.VISIBLE); }

            holder.caption.setText(mainDataList.get(position).getCaption());
            holder.date.setText( dateHelper.getDayName(context, mainDataList.get(position).getCreatedOn()));

            Picasso.with(context)
                    .load(AppConfig.URL_PHOTO_DIR + mainDataList.get(position).getUrl())
                    .into(holder.image);

            if(onPopupMenuBtnClickListener != null){
                holder.bindOnPopupMenuClick(mainDataList.get(position), position, onPopupMenuBtnClickListener);
            }


        }else{
            //Perform load finishe test here for leaving a space for fab button
            //((ProgressViewHolder) holderOb).progressBar.setIndeterminate(true);

        }


    }

    @Override
    public int getItemCount() {
        return mainDataList.size();
    }

    public void addItem(ImageData newData, int index){
        this.mainDataList.add(index, newData);//Adds a new data Object to the list
        notifyItemInserted(index);
    }

    public void addItemToEnd(ImageData testimonyData){

        mainDataList.add(testimonyData);
        //Log.e("ART ADAPTER DATA_COUNT", mainDataList.size() + " : " + (mainDataList.size() - 1));
        notifyItemInserted(mainDataList.size());
    }

    public void deleteItem(int index){
        this.mainDataList.remove(index);
        notifyItemRemoved(index);

    }

    public void deleteLastItem(){
        deleteItem(mainDataList.size() - 1);
    }



    /**
     *
     * @param rv
     */
    public void setRecyclerView(RecyclerView rv){
        this.recyclerView = rv;
    }

    /**
     * Reset the loading when data has finished loading
     */
    public void loadMoreResetState(){
//        Log.e(TAG, "Change Load More STate" + state);
        loading = false;
    }

    /**
     * Set the OnLoadMoreListener that the adapter will use here
     * @param listener the OnLoadMoreListener
     */
    public void setOnLoadMoreListener(OnLoadMoreListener listener){
        this.onLoadMoreListener = listener;

        if(recyclerView != null) {
            if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {

                final LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                //Add the onScrollListener
                recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);
                    }

                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);

                        totalItemCount = layoutManager.getItemCount();//to items in layout
                        lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                        //Log.e("LOADING PARAMS", totalItemCount + " : " + lastVisibleItemPosition + " + " + visibleThreshold);


                        if (!loading && totalItemCount <= (lastVisibleItemPosition + visibleThreshold)) {
                            // End has been reached
                            // Do something
                            if (onLoadMoreListener != null) {
                                onLoadMoreListener.onLoadMore();
                            }
                            loading = true;
                        }

                    }
                });

            }
        }

    }

}
