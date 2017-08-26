package com.example.yuuura87.minesweeper;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

/**
 * Created by yuuura87 on 8/17/17.
 */

public class GridAdapter extends BaseAdapter {

    private Context mContext;
    private Integer image;
    private int amount;

    public GridAdapter(Context c, Integer image, int amount) {
        mContext = c;
        this.image = image;
        this.amount = amount;
    }

    @Override
    public int getCount() {
        return amount;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(mContext);
            if(amount == 100) {
                imageView.setLayoutParams(new GridView.LayoutParams(amount, amount));
            }
            else if( amount == 25 ) {
                imageView.setLayoutParams(new GridView.LayoutParams(200, 200));
            }
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageResource(image);
        return imageView;
    }
}
