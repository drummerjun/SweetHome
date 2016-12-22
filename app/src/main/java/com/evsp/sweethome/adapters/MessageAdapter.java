package com.evsp.sweethome.adapters;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.evsp.sweethome.R;

import java.util.ArrayList;

public class MessageAdapter extends BaseAdapter {
    private ArrayList<MessageItem> mListItems;
    private LayoutInflater mLayoutInflater;

    public MessageAdapter(Context context, ArrayList<MessageItem> itemList) {
        mListItems = itemList;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        boolean incoming = mListItems.get(position).getIncoming();
        if (view == null) {
            view = mLayoutInflater.inflate(R.layout.list_item, null);
        }

        String stringItem = mListItems.get(position).getMessage();
        String timeItem = mListItems.get(position).getTime();
        String serverAddr = mListItems.get(position).getOrigin();
        if (stringItem != null) {
            TextView origin = (TextView)view.findViewById(R.id.origin);
            TextView body = (TextView)view.findViewById(R.id.body);
            TextView time = (TextView)view.findViewById(R.id.time);
            if (body != null) {
                if (incoming) {
                    body.setGravity(Gravity.LEFT);
                    body.setTextColor(view.getResources().getColor(R.color.blue));
                } else {
                    body.setGravity(Gravity.RIGHT);
                    body.setTextColor(view.getResources().getColor(R.color.green));
                }
                body.setText(stringItem);
            }
            if(origin != null) {
                if (incoming) {
                    origin.setGravity(Gravity.LEFT);
                    origin.setText(serverAddr.isEmpty() ? "Server Socket:" : serverAddr + ":");
                } else {
                    origin.setGravity(Gravity.RIGHT);
                    origin.setText("localhost:");
                }
            }
            if(time != null) {
                if(incoming) {
                    time.setGravity(Gravity.LEFT);
                } else {
                    time.setGravity(Gravity.RIGHT);
                }
                time.setText(timeItem);
            }
        }
        return view;
    }
}