package com.evsp.sweethome;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListView;

import com.evsp.sweethome.adapters.AlertAdapter;
import com.evsp.sweethome.adapters.AlertItem;
import com.evsp.sweethome.database.AlertDataSource;

import java.io.IOException;
import java.util.ArrayList;

public class FragmentAlert extends Fragment {
    private static final String TAG = "FragAlerts";
    private ArrayList<AlertItem> itemList;
    private AlertAdapter mAdapter;
    private AlertDataSource mDataSource;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_alert, container, false);
        final ListView listView = (ListView)view.findViewById(R.id.lv_alerts);
        final Handler handler = new Handler();
        setHasOptionsMenu(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mDataSource == null) {
                        mDataSource = new AlertDataSource(getActivity().getApplicationContext());
                    }
                    mDataSource.open();
                    itemList = mDataSource.loadLogs();
                    mAdapter = new AlertAdapter(getActivity(), itemList);

                    /*
                    SharedPreferences pref = getActivity().getSharedPreferences(SweetHome.PREF_ALERT, Context.MODE_PRIVATE);
                    String savedJsonString = pref.getString(SweetHome.ALERT_PROPERTY, "");
                    Gson gson = new Gson();
                    if (savedJsonString.isEmpty()) {
                        itemList = new ArrayList<AlertItem>();
                    } else {
                        Type type = new TypeToken<ArrayList<AlertItem>>() {}.getType();
                        itemList = gson.fromJson(savedJsonString, type);
                    }
                    mAdapter = new AlertAdapter(view.getContext(), itemList);
                    */
                } catch(IOException e) {
                    e.printStackTrace();
                } finally {
                    mDataSource.close();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listView.setAdapter(mAdapter);
                        }
                    });
                }
            }
        }).start();
        listView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new GridView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                int selectCount = listView.getCheckedItemCount();
                mAdapter.toggleSelection(position, checked);
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.setTitle(getResources().getString(R.string.erase));
                mode.setSubtitle("");
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
                                itemList.remove(i);
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

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((ActionBarActivity)getActivity()).getSupportActionBar()
                .setTitle(getResources().getString(R.string.alerts).toUpperCase());
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.findItem(R.id.menuitem_clearalert).setVisible(true);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void updateList(final Context context, final AlertItem item) {
        int size = itemList.size();
        if(size > Constants.MAX_RECORD_NUM) {
            itemList.remove(size - 1);
        }
        itemList.add(0, item);
        mAdapter.notifyDataSetChanged();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mDataSource == null) {
                        mDataSource = new AlertDataSource(context);
                    }
                    mDataSource.open();
                    mDataSource.insertLogtoDB(item);
                    /*
                    Gson gson = new Gson();
                    String savedJsonString = gson.toJson(itemList);
                    SharedPreferences prefs = context.getSharedPreferences(SweetHome.PREF_ALERT, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(SweetHome.ALERT_PROPERTY, savedJsonString);
                    editor.apply();
                    */
                } catch(IOException e) {
                    e.printStackTrace();
                } catch(NullPointerException e) {
                    e.printStackTrace();
                } finally {
                    mDataSource.close();
                }
            }
        });
    }

    public void clearLog() {
        itemList.clear();
        mAdapter.notifyDataSetChanged();
    }
}
