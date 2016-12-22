package com.evsp.sweethome;

import android.app.Activity;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;

import com.evsp.sweethome.adapters.AlertItem;
import com.evsp.sweethome.adapters.GUIAdapter;
import com.evsp.sweethome.adapters.GUIItem;

import java.util.ArrayList;
import java.util.Locale;

public class FragmentGUI extends Fragment {
    private static final String TAG = "FragmentGUI";
    private static final int REQ_CODE_SPEECH_INPUT = 1;

    private ArrayList<GUIItem> itemList;
    private GUIAdapter mAdapter;
    private GridView mGrid;
    private ImageButton mVoiceButton;
    private Button mListButton;
    private SwipeRefreshLayout mSwipeView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gui, container, false);
        setHasOptionsMenu(true);

        itemList = new ArrayList<GUIItem>();
        mGrid = (GridView) view.findViewById(R.id.gridview);
        mAdapter = new GUIAdapter(view.getContext(), itemList);
        mGrid.setAdapter(mAdapter);
        mGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GUIItem item = itemList.get(position);
                item.setUnread(false);
                Intent intent = new Intent();
                intent.setAction(Constants.ACTION_SEE_DETAILS);
                intent = setDeviceParams(item, intent);
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
            }
        });
        mGrid.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
        mGrid.setMultiChoiceModeListener(new GridView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                int selectCount = mGrid.getCheckedItemCount();
                mAdapter.toggleSelection(position, checked);
                switch (selectCount) {
                    case 1:
                        mode.setSubtitle(getResources().getString(R.string.selected_singular));
                        break;
                    default:
                        mode.setSubtitle("" + selectCount + " " + getResources().getString(R.string.selected_plural));
                        break;
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.setTitle(getResources().getString(R.string.remove));
                mode.setSubtitle(getResources().getString(R.string.selected_singular));
                mode.getMenuInflater().inflate(R.menu.menu_cab, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menuitem_remove:
                        SparseBooleanArray selected = mAdapter.getSelectedIds();
                        for (int i = (selected.size() - 1); i >= 0; i--) {
                            if (selected.valueAt(i)) {
                                GUIItem selecteditem = itemList.get(selected.keyAt(i));
                                sendMessageToConsole(".remove " + selecteditem.getId(), false);
                            }
                        }
                        mode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                mAdapter.removeSelection();
            }
        });

        mSwipeView = (SwipeRefreshLayout)view.findViewById(R.id.swipe);
        mSwipeView.setColorSchemeResources(
                android.R.color.holo_red_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_green_light,
                android.R.color.holo_blue_light);
        mSwipeView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                sendMessageToConsole(Constants.CMD_L, true);
            }
        });

        mVoiceButton = (ImageButton)view.findViewById(R.id.button_voice);
        mVoiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                //intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_prompt));
                try {
                    startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
                } catch (ActivityNotFoundException a) {
                }
            }
        });

        mListButton = (Button)view.findViewById(R.id.button_list);
        mListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessageToConsole(Constants.CMD_LIST, true);
            }
        });

        setViewEnabled();
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == Activity.RESULT_OK && null != data) {
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    Log.w(TAG, "onActivityResult: " + result.get(0));
                    sendMessageToConsole(result.get(0), false);
                }
                break;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.gui).toUpperCase());
        refreshViews();
        if(((SweetHome)getActivity().getApplication()).getConnected()) {
            //sendMessageToConsole(".speak json", true);
            //sendMessageToConsole(SweetHome.CMD_LIST, true);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.findItem(R.id.menuitem_bind).setVisible(true);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private boolean isRegisterCommand(String message) {
        if(message.contains(Constants.VOICE_BIND)
                || message.contains(Constants.VOICE_REGISTER)
                || message.contains(Constants.VOICE_WELCOME)
                || message.contains(Constants.VOICE_BIND_ERR1)
                || message.contains(Constants.VOICE_BIND_ERR2)
                || message.contains(Constants.VOICE_BIND_ERR3)
                || message.contains(Constants.VOICE_BIND_ERR4)
                || message.contains(Constants.VOICE_BIND_ERR5)) {
            return true;
        } else {
            return false;
        }
    }

    private void sendMessageToConsole(String message, boolean saved) {
        Intent intent = new Intent();
        if(isRegisterCommand(message)) {
            intent = new Intent(Constants.ACTION_VOICE_BIND);
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
            return;
        } else if(message.equals(Constants.CMD_LIST)) {
            mSwipeView.setRefreshing(true);
        }
        intent.setAction(Constants.ACTION_SEND_MESSAGE);
        intent.putExtra(Constants.MESSAGE, message);
        intent.putExtra(Constants.SAVED, saved);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
    }

    private void setViewEnabled() {
        boolean connected = ((SweetHome)getActivity().getApplication()).getConnected();
        try {
            if (connected) {
                mGrid.setVisibility(View.VISIBLE);
                mVoiceButton.setVisibility(View.VISIBLE);
                mListButton.setVisibility(View.VISIBLE);
                sendMessageToConsole(".speak json", true);
                sendMessageToConsole(Constants.CMD_LIST, true);
            } else {
                mGrid.setVisibility(View.INVISIBLE);
                mVoiceButton.setVisibility(View.GONE);
                mListButton.setVisibility(View.GONE);
            }
        } catch(NullPointerException e) {
            e.printStackTrace();
        }
    }

    private Intent setDeviceParams(GUIItem item, Intent intent) {
        String type = item.getType();
        intent.putExtra(Constants.JSON_ID, item.getId());
        intent.putExtra(Constants.JSON_NAME, item.getName());
        intent.putExtra(Constants.JSON_TYPE, type);

        AlertItem alert = item.getAlert();
        if(alert != null) {
            intent.putExtra(Constants.ARGS_ALERT_TIME, item.getAlert().getTime());
            intent.putExtra(Constants.ARGS_ALERT_BODY, item.getAlert().getMessage());
        }

        switch(type) {
            case Constants.DEVICE_PLUG:
                intent.putExtra(Constants.JSON_WATT, item.getWatt());
                intent.putExtra(Constants.JSON_POWER, item.getPower());
                break;
            case Constants.DEVICE_WALL_SWITCH:
                intent.putExtra(Constants.JSON_POWER, item.getPower());
                intent.putExtra(Constants.JSON_BATT_LEVEL, item.getBattery());
                break;
            case Constants.DEVICE_MOTION:
                intent.putExtra(Constants.JSON_POWER, item.getPower());
                intent.putExtra(Constants.JSON_BATT_LEVEL, item.getBattery());
                break;
            case Constants.DEVICE_FLOOD:
                break;
            case Constants.DEVICE_MAGNETIC:
                intent.putExtra(Constants.JSON_BATT_LEVEL, item.getBattery());
                break;
            case Constants.DEVICE_THERMO:
                SharedPreferences prefs = getActivity().getSharedPreferences(Constants.PREF_KEY, Context.MODE_PRIVATE);
                double temp = item.getTemp();
                if(temp == Double.MIN_VALUE) {
                    long longTemp = prefs.getLong(Constants.JSON_TEMP, Long.MIN_VALUE);
                    if(longTemp > Long.MIN_VALUE) {
                        temp = Double.longBitsToDouble(longTemp);
                    }
                }
                intent.putExtra(Constants.JSON_TEMP, temp);

                int humid = item.getHumidity();
                if(humid == Integer.MIN_VALUE) {
                    humid = prefs.getInt(Constants.JSON_HUMIDITY, Integer.MIN_VALUE);
                }
                intent.putExtra(Constants.JSON_HUMIDITY, humid);
                intent.putExtra(Constants.JSON_BATT_LEVEL, item.getBattery());
                break;
            case Constants.DEVICE_SMOKE:
                intent.putExtra(Constants.JSON_BATT_LEVEL, item.getBattery());
                break;
            case Constants.DEVICE_SIREN:
                intent.putExtra(Constants.JSON_POWER, item.getPower());
                intent.putExtra(Constants.JSON_BATT_LEVEL, item.getBattery());
                intent.putExtra(Constants.JSON_SECURITY, item.getSecurity());
                break;
            case Constants.DEVICE_REMOTE:
            case Constants.DEVICE_KEYPAD:
                intent.putExtra(Constants.JSON_BATT_LEVEL, item.getBattery());
                break;
            default:
                break;
        }
        return intent;
    }

    public void clear() {
        itemList.clear();
        mGrid.setAdapter(null);
    }

    public void updateList(GUIItem item) {
        mSwipeView.setRefreshing(false);
        if(item == null) {
            return;
        }
        //boolean updated = false;
        int index = -1;
        for (GUIItem candidate : itemList) {
            if (candidate.getId() == item.getId() && candidate.getType().equals(item.getType())) {
                index = itemList.indexOf((candidate));
                //candidate.update(item);
                //updated = true;
                break;
            }
        }
        //if(!updated) {
            itemList.add(item);
        //}
        mGrid.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    public void modifyItem(GUIItem item) {
        int index = 0;
        for (GUIItem candidate : itemList) {
            if (candidate.getId() == item.getId() && candidate.getType().equals(item.getType())) {
                index = itemList.indexOf(candidate);
                //candidate.update(item);
                break;
            }
        }
        itemList.set(index, item);
        mAdapter.notifyDataSetChanged();
    }

    public boolean removeItem(int id, String type) {
        int index = -1;
        for (GUIItem candidate : itemList) {
            if (candidate.getId() == id && candidate.getType().equals(type)) {
                index = itemList.indexOf(candidate);
                break;
            }
        }
        if(index > -1) {
            itemList.remove(index);
            mAdapter.notifyDataSetChanged();
            return true;
        } else {
            return false;
        }
    }

    public void refreshViews() {
        setViewEnabled();
    }

    public GUIItem getItem(int id, String type) {
        for (GUIItem candidate : itemList) {
            if (candidate.getId() == id && candidate.getType().equals(type)) {
                return candidate;
            }
        }
        return null;
    }
}
