package com.novugrid.fortos.adapters.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.novugrid.fortos.R;
import com.novugrid.fortos.datamodel.ImageData;
import com.novugrid.fortos.listeners.OnPopupMenuBtnClickListener;

/**
 * Created by WeaverBird on 9/27/2016.
 */
public class ImageViewHolder extends RecyclerView.ViewHolder {

    public TextView caption, date;
    public ImageButton btnPopupMenu;
    public ImageView image;

    public ImageViewHolder(View itemView) {
        super(itemView);

        image = (ImageView) itemView.findViewById(R.id.image);
        date = (TextView) itemView.findViewById(R.id.date);
        caption = (TextView) itemView.findViewById(R.id.caption);
        btnPopupMenu = (ImageButton) itemView.findViewById(R.id.btn_popup_menu);

    }

    public void bindOnPopupMenuClick(final ImageData data, final int itemPositionInAdapter, final OnPopupMenuBtnClickListener listener){

        btnPopupMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onPopupMenuBtnClick(data, itemPositionInAdapter, v);
            }
        });

    }

}
