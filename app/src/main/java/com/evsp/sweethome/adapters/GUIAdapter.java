package com.evsp.sweethome.adapters;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.evsp.sweethome.Constants;
import com.evsp.sweethome.R;

import java.util.ArrayList;

public class GUIAdapter extends BaseAdapter {
    private ArrayList<GUIItem> mListItems;
    private LayoutInflater mLayoutInflater;
    private SparseBooleanArray mSelectedItemIds;
    private TextView mIndex;
    private ImageView mIcon;
    private ImageView img1, img2, img4;
    private TextView attr1, attr2, attr5;

    public GUIAdapter(Context context, ArrayList<GUIItem> itemList) {
        this.mListItems = itemList;
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
    public View getView(final int position, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = mLayoutInflater.inflate(R.layout.grid_item, null);
        }
        initViews(view);
        GUIItem item = mListItems.get(position);
        mIndex.setText(String.valueOf(item.getId()));
        if(item.getPower()) {
            mIndex.setTextColor(view.getResources().getColor(android.R.color.black));
            mIndex.setBackgroundColor(view.getResources().getColor(R.color.light_green));
        } else {
            mIndex.setTextColor(view.getResources().getColor(android.R.color.white));
            mIndex.setBackgroundColor(view.getResources().getColor(R.color.light_transparent_bg));
        }

        ImageView battery = (ImageView)view.findViewById(R.id.battery_level);
        battery.setVisibility(View.VISIBLE);
        int battery_level = item.getBattery();
        String type = item.getType().replace(" ", "");
        switch(type) {
            case Constants.DEVICE_PLUG:
                battery.setImageDrawable(view.getResources().getDrawable(R.drawable.battery_plugged));
                break;
            case Constants.DEVICE_WALL_SWITCH:
            case Constants.DEVICE_MOTION:
            case Constants.DEVICE_MAGNETIC:
            case Constants.DEVICE_THERMO:
            case Constants.DEVICE_SMOKE:
            case Constants.DEVICE_KEYPAD:
            case Constants.DEVICE_REMOTE:
            case Constants.DEVICE_SIREN:
                if(battery_level > 75 && battery_level <= 100) {
                    battery.setImageDrawable(view.getResources().getDrawable(R.drawable.battery_100));
                } else if(battery_level > 50 && battery_level <= 75) {
                    battery.setImageDrawable(view.getResources().getDrawable(R.drawable.battery_75));
                } else if(battery_level > 10 && battery_level <= 50) {
                    battery.setImageDrawable(view.getResources().getDrawable(R.drawable.battery_50));
                } else if(battery_level > 0 && battery_level <= 10) {
                    battery.setImageDrawable(view.getResources().getDrawable(R.drawable.battery_0));
                } else if(battery_level == 0) {
                    mIndex.setTextColor(view.getResources().getColor(android.R.color.white));
                    mIndex.setBackgroundColor(view.getResources().getColor(R.color.light_transparent_bg));
                    battery.setImageDrawable(view.getResources().getDrawable(R.drawable.battery_0));
                }
                break;
            default:
                mIndex.setTextColor(view.getResources().getColor(android.R.color.black));
                mIndex.setBackgroundColor(view.getResources().getColor(R.color.light_green));
                battery.setVisibility(View.INVISIBLE);
                break;
        }

        TextView name = (TextView)view.findViewById(R.id.name);
        name.setText(item.getName());
        //boolean triggered = item.getTrigger();
        //configureViews(type);
        switch(type) {
            case Constants.DEVICE_THERMO:
                mIcon.setImageDrawable(view.getResources().getDrawable(R.drawable.picto_temperature));
                img1.setVisibility(View.VISIBLE);
                img1.setImageDrawable(view.getResources().getDrawable(R.drawable.temperature));
                attr1.setVisibility(View.VISIBLE);
                double temp = item.getTemp();
                if(temp == Double.MIN_VALUE) {
                    attr1.setText("--.-\u00B0C");
                } else {
                    attr1.setText(String.valueOf(temp) + "\u00B0C");
                }
                img2.setVisibility(View.VISIBLE);
                img2.setImageDrawable(view.getResources().getDrawable(R.drawable.humidity));
                attr2.setVisibility(View.VISIBLE);
                int humid = item.getHumidity();
                if(humid == Integer.MIN_VALUE) {
                    attr2.setText("--%");
                } else {
                    attr2.setText(String.valueOf(item.getHumidity()) + "%");
                }
                break;
            case Constants.DEVICE_PLUG:
                mIcon.setImageDrawable(view.getResources().getDrawable(R.drawable.picto_plug_on));
                if(item.getLoaded()) {
                    img4.setVisibility(View.VISIBLE);
                    img4.setImageDrawable(view.getResources().getDrawable(R.drawable.watt));
                    attr5.setVisibility(View.VISIBLE);
                    attr5.setText(String.valueOf(item.getWatt()));
                }
                if(item.getOverload()) {
                    img4.setVisibility(View.VISIBLE);
                    img4.setImageDrawable(view.getResources().getDrawable(R.drawable.alert_high));
                }
                break;
            case Constants.DEVICE_WALL_SWITCH:
                mIcon.setImageDrawable(view.getResources().getDrawable(R.drawable.picto_switch));
                if(item.getUnread()) {
                    img4.setVisibility(View.VISIBLE);
                    img4.setImageDrawable(view.getResources().getDrawable(R.drawable.alert_low));
                } else {
                    String switch_value = item.getValue();
                    if (switch_value.equals(Constants.VALUE_UP_SHORT)
                            || switch_value.equals(Constants.VALUE_UP_LONG)
                            || switch_value.equals(Constants.VALUE_DOWN_SHORT)
                            || switch_value.equals(Constants.VALUE_DOWN_LONG)) {
                        item.setUnread(true);
                        img4.setVisibility(View.VISIBLE);
                        img4.setImageDrawable(view.getResources().getDrawable(R.drawable.alert_low));
                    }
                }
                break;
            case Constants.DEVICE_MOTION:
                mIcon.setImageDrawable(view.getResources().getDrawable(R.drawable.picto_pir_sensor));
                if(item.getTrigger()) {
                    img4.setVisibility(View.VISIBLE);
                    img4.setImageDrawable(view.getResources().getDrawable(R.drawable.alert_mid));
                }

                if(item.getUnread()) {
                    img4.setVisibility(View.VISIBLE);
                    img4.setImageDrawable(view.getResources().getDrawable(R.drawable.alert_high));
                } else {
                    if (item.getTamper()) {
                        item.setUnread(true);
                        img4.setVisibility(View.VISIBLE);
                        img4.setImageDrawable(view.getResources().getDrawable(R.drawable.alert_high));
                    }
                }
                break;
            case Constants.DEVICE_FLOOD:
                mIcon.setImageDrawable(view.getResources().getDrawable(R.drawable.picto_flood));
                if(item.getTrigger()) {
                    img4.setVisibility(View.VISIBLE);
                    img4.setImageDrawable(view.getResources().getDrawable(R.drawable.alert_max));
                } else {
                    img4.setVisibility(View.INVISIBLE);
                }
                break;
            case Constants.DEVICE_MAGNETIC:
                mIcon.setImageDrawable(view.getResources().getDrawable(R.drawable.picto_door_magnetic_sensor));
                img4.setVisibility(View.VISIBLE);
                if(item.getTrigger()) {
                    img4.setImageDrawable(view.getResources().getDrawable(R.drawable.alert_mid));
                } else {
                    img4.setImageDrawable(view.getResources().getDrawable(R.drawable.ok));
                }
                break;
            case Constants.DEVICE_SMOKE:
                mIcon.setImageDrawable(view.getResources().getDrawable(R.drawable.picto_smoke_sensor));
                if(item.getUnread()) {
                    img4.setVisibility(View.VISIBLE);
                    if(item.getTrigger()) {
                        img4.setImageDrawable(view.getResources().getDrawable(R.drawable.alert_max));
                    } else if(item.getTamper()) {
                        img4.setImageDrawable(view.getResources().getDrawable(R.drawable.alert_high));
                    }
                }

                if(item.getTamper()) {
                    item.setUnread(true);
                    img4.setVisibility(View.VISIBLE);
                    img4.setImageDrawable(view.getResources().getDrawable(R.drawable.alert_high));
                }
                if(item.getTrigger()) {
                    item.setUnread(true);
                    img4.setVisibility(View.VISIBLE);
                    img4.setImageDrawable(view.getResources().getDrawable(R.drawable.alert_max));
                }
                break;
            case Constants.DEVICE_KEYPAD:
                mIcon.setImageDrawable(view.getResources().getDrawable(R.drawable.picto_keypad));
                if(item.getUnread()) {
                    img4.setVisibility(View.VISIBLE);
                    img4.setImageDrawable(view.getResources().getDrawable(R.drawable.alert_high));
                } else {
                    if (item.getTamper()) {
                        item.setUnread(true);
                        img4.setVisibility(View.VISIBLE);
                        img4.setImageDrawable(view.getResources().getDrawable(R.drawable.alert_high));
                    }
                }
                break;
            case Constants.DEVICE_SIREN:
                mIcon.setImageDrawable(view.getResources().getDrawable(R.drawable.picto_siren));
                if(item.getUnread()) {
                    img4.setVisibility(View.VISIBLE);
                    if(item.getTamper()) {
                        img4.setImageDrawable(view.getResources().getDrawable(R.drawable.alert_high));
                    } else if(item.getLowBatt()) {
                        img4.setImageDrawable(view.getResources().getDrawable(R.drawable.alert_mid));
                    }
                }

                if(item.getLowBatt()) {
                    item.setUnread(true);
                    img4.setVisibility(View.VISIBLE);
                    img4.setImageDrawable(view.getResources().getDrawable(R.drawable.alert_mid));
                }
                if(item.getTamper()) {
                    item.setUnread(true);
                    img4.setVisibility(View.VISIBLE);
                    img4.setImageDrawable(view.getResources().getDrawable(R.drawable.alert_high));
                }
                break;
            default:
                break;
        }
        return view;
    }

    public void toggleSelection(int position, boolean checked) {
        selectView(position, !mSelectedItemIds.get(position));
        //GUIItem item = mListItems.get(position);
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

    public void removeSelection() {
        mSelectedItemIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemIds;
    }

    private void initViews(View view) {
        mIndex = (TextView)view.findViewById(R.id.index);
        mIcon = (ImageView) view.findViewById(R.id.icon);
        img1 = (ImageView)view.findViewById(R.id.img1);
        img2 = (ImageView)view.findViewById(R.id.img2);
        img4 = (ImageView)view.findViewById(R.id.img4);
        attr1 = (TextView)view.findViewById(R.id.attr1);
        attr2 = (TextView)view.findViewById(R.id.attr2);
        attr5 = (TextView)view.findViewById(R.id.attr5);
    }
}