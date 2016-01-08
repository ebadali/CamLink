package com.quickblox.sample.videochatwebrtcnew.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.quickblox.core.io.IOUtils;
import com.quickblox.sample.videochatwebrtcnew.R;
import com.quickblox.sample.videochatwebrtcnew.activities.BaseActivity;
import com.quickblox.sample.videochatwebrtcnew.holder.DataHolder;
import com.quickblox.users.model.QBUser;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tereha on 27.01.15.
 */
public class OpponentsAdapter extends BaseAdapter {

    private List<QBUser> opponents;
    private LayoutInflater inflater;
    public static int i;
    public List<QBUser> selected = new ArrayList<>();
    private String TAG = "OpponentsAdapter";
    Context cntx;
    private Target loadtarget;

    public OpponentsAdapter(Context context, List<QBUser> users) {
        this.cntx = context;
        this.opponents = users;
        this.inflater = LayoutInflater.from(context);


    }

    public List<QBUser> getSelected() {
        return selected;
    }

    public int getCount() {
        return opponents.size();
    }

    public QBUser getItem(int position) {
        return opponents.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, final ViewGroup parent) {

        ViewHolder holder = new ViewHolder();
        View row = convertView;
        if (row == null) {

            row = inflater.inflate(R.layout.list_item_opponents, parent, false);

            holder.opponentsNumber = (ImageView) row.findViewById(R.id.opponentsNumber);
            holder.opponentsName = (TextView) row.findViewById(R.id.opponentsName);
            holder.opponentsRadioButton = (RadioButton) row.findViewById(R.id.opponentsCheckBox);


            row.setTag(holder);

        } else {
            holder = (ViewHolder) row.getTag();
        }

        final QBUser user = opponents.get(position);
        if (user != null) {
            Log.i(TAG,"user "+ user.getLogin());
//            holder.opponentsNumber.setText(String.valueOf(
//                    DataHolder.getUserIndexByID(user.getId()) + 1));
            ImageView image = holder.opponentsNumber;
            if (user.getFileId() != null && user.getCustomData() != null  ) {
                Log.i(TAG,"user have to load image");

                Picasso.with(cntx)
                        .load(user.getCustomData())
                        .resize(50, 50)
                        .centerCrop()
                        .into(image);
            }
            else
            {
                holder.opponentsNumber.setImageDrawable(null);
            }

            holder.opponentsName.setText(user.getLogin());
            holder.opponentsRadioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        i = user.getId();
                        selected.removeAll(selected);
                        selected.add(user);
                    } else {
                        if (i == user.getId()) {
                            i = 0;
                        }
                        selected.remove(user);
                        //holder.opponentsRadioButton.setChecked(false);
                    }
                    notifyDataSetChanged();
                }
            });

            holder.opponentsRadioButton.setChecked(i == user.getId());

        }

        return row;
    }


    public static class ViewHolder {
        ImageView opponentsNumber;
        TextView opponentsName;
        RadioButton opponentsRadioButton;
        boolean imageSet = false;
    }

}
