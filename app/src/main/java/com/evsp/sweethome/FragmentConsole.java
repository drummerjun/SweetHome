package com.evsp.sweethome;

import android.app.Activity;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.evsp.sweethome.adapters.MessageAdapter;
import com.evsp.sweethome.adapters.MessageItem;
import com.evsp.sweethome.database.LogDataSource;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class FragmentConsole extends Fragment {
    private static final String TAG = "FragmentConsole";
    private static final int REQ_CODE_SPEECH_INPUT = 1;
    private ArrayList<MessageItem> itemList;
    private MessageAdapter mAdapter;
    private ImageButton mVoiceButton, mSendButton;
    private EditText mEditText;
    private View mDivider;
    private LogDataSource mDataSource;

    @Override
    public void onResume() {
        super.onResume();
        ((ActionBarActivity)getActivity()).getSupportActionBar()
                .setTitle(getResources().getString(R.string.console).toUpperCase());
        refreshViews();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_console, container, false);
        setHasOptionsMenu(true);

        mDivider = view.findViewById(R.id.divider);
        mEditText = (EditText) view.findViewById(R.id.editText);

        mVoiceButton = (ImageButton) view.findViewById(R.id.voice_button);
        mVoiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                try {
                    startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
                } catch (ActivityNotFoundException a) {
                }
            }
        });
        mSendButton = (ImageButton) view.findViewById(R.id.send_button);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = mEditText.getText().toString();
                sendMessageToSocket(message);
                mEditText.setText("");
            }
        });
        setViewEnabled();
        return view;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final ListView list = (ListView) view.findViewById(R.id.list);
        final Handler handler = new Handler();

        Thread load = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mDataSource == null) {
                        mDataSource = new LogDataSource(getActivity().getApplicationContext());
                    }
                    mDataSource.open();
                    itemList = mDataSource.loadLogs();
                    mAdapter = new MessageAdapter(getActivity(), itemList);

                    /*
                    SharedPreferences pref = getActivity().getSharedPreferences(SweetHome.PREF_LOG, Context.MODE_PRIVATE);
                    String savedJsonString = pref.getString(SweetHome.LOG_PROPERTY, "");

                    Gson gson = new Gson();
                    if (savedJsonString.isEmpty()) {
                        itemList = new ArrayList<MessageItem>();
                    } else {
                        Type type = new TypeToken<ArrayList<MessageItem>>() {
                        }.getType();
                        itemList = gson.fromJson(savedJsonString, type);
                    }
                    mAdapter = new MessageAdapter(view.getContext(), itemList);
                    */
                } catch(IOException e) {
                    e.printStackTrace();
                } finally {
                    mDataSource.close();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            list.setAdapter(mAdapter);
                        }
                    });
                }
            }
        });
        load.start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == Activity.RESULT_OK && null != data) {
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if(mEditText != null) {
                        sendMessageToSocket(result.get(0));
                    }
                }
                break;
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.findItem(R.id.menuitem_clear).setVisible(true);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void sendMessageToSocket(String message) {
        if(message != null && !message.isEmpty()) {
            Intent intent = new Intent();
            intent.setAction(Constants.ACTION_SEND_MESSAGE);
            intent.putExtra(Constants.MESSAGE, message);
            intent.putExtra(Constants.SAVED, true);
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);

            MessageItem item = new MessageItem(message, false, getCurrentTime());
            saveItemList(getActivity(), item);

            mAdapter.notifyDataSetChanged();
        }
    }

    private String getCurrentTime() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        return df.format(c.getTime());
    }

    private void saveItemList(final Context context, final MessageItem item) {
        if(itemList.size() > Constants.MAX_RECORD_NUM) {
            itemList.remove(0);
        }
        itemList.add(item);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mDataSource == null) {
                        mDataSource = new LogDataSource(context);
                    }
                    mDataSource.open();
                    mDataSource.insertLogtoDB(item);
                    /*
                    Gson gson = new Gson();
                    String savedJsonString = gson.toJson(itemList);
                    SharedPreferences prefs = context.getSharedPreferences(SweetHome.PREF_LOG, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(SweetHome.LOG_PROPERTY, savedJsonString);
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
        }).start();
    }

    private void setViewEnabled() {
        boolean connected = ((SweetHome)getActivity().getApplication()).getConnected();
        try {
            if (connected) {
                mDivider.setVisibility(View.VISIBLE);
                mVoiceButton.setVisibility(View.VISIBLE);
                mSendButton.setVisibility(View.VISIBLE);
                mEditText.setVisibility(View.VISIBLE);
            } else {
                mDivider.setVisibility(View.GONE);
                mVoiceButton.setVisibility(View.GONE);
                mSendButton.setVisibility(View.GONE);
                mEditText.setVisibility(View.GONE);
            }
        } catch(NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void clearLog() {
        itemList.clear();
        mAdapter.notifyDataSetChanged();
    }

    public void updateList(Context context, MessageItem item) {
        saveItemList(context, item);
        mAdapter.notifyDataSetChanged();
    }

    public void refreshViews() {
        setViewEnabled();
    }
}