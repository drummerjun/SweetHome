package com.evsp.sweethome.adapters;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.evsp.sweethome.Constants;
import com.evsp.sweethome.R;

import java.util.ArrayList;

public class AlertAdapter extends BaseAdapter {
    private ArrayList<AlertItem> mListItems;
    private LayoutInflater mLayoutInflater;
    private SparseBooleanArray mSelectedItemIds;

    public AlertAdapter(Context context, ArrayList<AlertItem> itemList) {
        mListItems = itemList;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mSelectedItemIds = new SparseBooleanArray();
    }

    @Override
    public int getCount() {
        return mListItems.size();
    }

    @Override
    //get the data of an item from a specific position
    //i represents the position of the item in the list
    public Object getItem(int i) {
        return null;
    }

    @Override
    //get the position id of the item from the list
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = mLayoutInflater.inflate(R.layout.alert_item, null);
        }
        AlertItem item = mListItems.get(position);
        //int id = item.getId();
        String type = item.getType();
        String stringItem = item.getMessage();
        String timeItem = item.getTime();

        ImageView icon = (ImageView)view.findViewById(R.id.alert_icon);
        icon.setVisibility(View.VISIBLE);
        switch(type) {
            case Constants.DEVICE_PLUG:
                icon.setImageDrawable(view.getResources().getDrawable(R.drawable.picto_plug_on_alert));
                break;
            case Constants.DEVICE_WALL_SWITCH:
                icon.setImageDrawable(view.getResources().getDrawable(R.drawable.picto_switch_alert));
                break;
            case Constants.DEVICE_MOTION:
                icon.setImageDrawable(view.getResources().getDrawable(R.drawable.picto_pir_sensor_alert));
                break;
            case Constants.DEVICE_FLOOD:
                icon.setImageDrawable(view.getResources().getDrawable(R.drawable.picto_flood_alert));
                break;
            case Constants.DEVICE_MAGNETIC:
                icon.setImageDrawable(view.getResources().getDrawable(R.drawable.picto_door_magnetic_sensor_alert));
                break;
            case Constants.DEVICE_THERMO:
                icon.setImageDrawable(view.getResources().getDrawable(R.drawable.picto_temperature_alert));
                break;
            case Constants.DEVICE_SMOKE:
                icon.setImageDrawable(view.getResources().getDrawable(R.drawable.picto_smoke_sensor_alert));
                break;
            case Constants.DEVICE_SIREN:
                icon.setImageDrawable(view.getResources().getDrawable(R.drawable.picto_siren));
                break;
            case Constants.DEVICE_REMOTE:
                icon.setImageDrawable(view.getResources().getDrawable(R.drawable.picto_remote_control_alert));
                break;
            case Constants.DEVICE_KEYPAD:
                icon.setImageDrawable(view.getResources().getDrawable(R.drawable.picto_keypad_alert));
                break;
            default:
                icon.setVisibility(View.INVISIBLE);
                break;
        }

        ImageButton delete = (ImageButton)view.findViewById(R.id.ic_delete);
        delete.setVisibility(View.GONE);
        delete.setTag(position);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = (Integer)v.getTag();
                mListItems.remove(index);
                notifyDataSetChanged();
            }
        });

        if (stringItem != null) {
            TextView body = (TextView)view.findViewById(R.id.log);
            TextView time = (TextView)view.findViewById(R.id.date);
            if (body != null) {
                body.setText(stringItem);
            }
            if(time != null) {
                time.setText(timeItem);
            }
        }
        return view;
    }

    public void toggleSelection(int position, boolean checked) {
        selectView(position, !mSelectedItemIds.get(position));
    }

    public void selectView(int position, boolean value) {
        if (value) {
            mSelectedItemIds.put(position, value);
        }
        else {
            mSelectedItemIds.delete(position);
        }
        notifyDataSetChanged();
    }

    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemIds;
    }

    public void removeSelection() {
        mSelectedItemIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }
}