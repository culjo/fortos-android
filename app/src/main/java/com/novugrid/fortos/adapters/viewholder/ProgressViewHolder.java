package com.novugrid.fortos.adapters.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.novugrid.fortos.R;

/**
 * Created by WeaverBird on 9/20/2016.
 * For Holding view in adapter classes, Hope it healp me next time..
 */
public class ProgressViewHolder extends RecyclerView.ViewHolder{

    public ProgressBar progressBar;

    public ProgressViewHolder(View itemView) {
        super(itemView);
        progressBar = (ProgressBar) itemView.findViewById(R.id.loading_data);
    }

}
