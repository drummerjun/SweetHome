package com.evsp.sweethome;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.evsp.sweethome.adapters.AlertItem;
import com.evsp.sweethome.adapters.CustomDrawerAdapter;
import com.evsp.sweethome.adapters.DrawerItem;
import com.evsp.sweethome.adapters.GUIItem;
import com.evsp.sweethome.adapters.MessageItem;
import com.evsp.sweethome.database.AlertDataSource;
import com.evsp.sweethome.database.LogDataSource;
import com.evsp.sweethome.services.ConnectionService;
import com.evsp.sweethome.services.TCPClient;
import com.evsp.sweethome.services.TCPConnectTask;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends ActionBarActivity {
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "main";

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar mToolbar;
    private Fragment fragment;
    private FragmentHome homeFragment;
    private FragmentGUI guiFragment;
    private FragmentLock lockFragment;
    private FragmentConsole consoleFragment;
    private FragmentAlert alertFragment;
    private FragmentOne webFragment;
    private FragmentSub1 detailsFragment;
    private CharSequence mDrawerTitle;
	private CharSequence mTitle;
    private CustomDrawerAdapter adapter;
    private List<DrawerItem> dataList;

    private Dialog mBindDialog;
    private CountDownTimer mTimer;
    private TCPClient client;

    private Menu mMenu;
    private TextToSpeech tts;

    private GoogleCloudMessaging gcm;
    private String regid;
    private int notifications = 0;

    private SharedPreferences mPreferences;
    private LogDataSource mLogDataSource;
    private AlertDataSource mAlertDataSource;
    private AlertDialog mDialog;

    private BroadcastReceiver mainReceiver = new BroadcastReceiver() {
        private CountDownTimer connectionTimer = new CountDownTimer(180000, 180000) { // 2min
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                // timeout, no message received, connection lost
                Log.d(TAG, "TIMEOUT! Terminating client...");
                setGatewayStatus(false);
                stopClient();
                new TCPConnectTask(MainActivity.this, getApplication()).execute("");
            }
        };

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "onReceive:" + action);
            if(action.equals(Constants.ACTION_LOGIN_FEEDBACK)) {
                String message = intent.getStringExtra(Constants.MESSAGE);
                if(message.equals(Constants.MESSAGE_ERROR)) {
                    String error = intent.getStringExtra(Constants.ERROR);
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                            .setTitle(getResources().getString(R.string.fail))
                            .setMessage(error)
                            .setNegativeButton(getResources().getString(R.string.back), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent i;
                                    i = new Intent(MainActivity.this, LoginActivity.class);
                                    gotoActivity(i);
                                }
                            })
                            .setPositiveButton(getResources().getString(R.string.offline), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });
                    if(mDialog == null) {
                        mDialog = builder.create();
                    }
                    mDialog.show();
                }
            } else if(action.equals(Constants.ACTION_READ_MESSAGE)) {
                //connectionTimer.cancel();
                String message = intent.getStringExtra(Constants.MESSAGE);
                readSocketMessage(message);
                //connectionTimer.start();
            } else if(action.equals(Constants.ACTION_SEND_MESSAGE)) {
                String message = intent.getStringExtra(Constants.MESSAGE);
                boolean logSaved = intent.getBooleanExtra(Constants.SAVED, false);
                sendMessageToSocket(message, logSaved);
            } else if(action.equals((Constants.ACTION_SEE_DETAILS))) {
                Bundle args = intent.getExtras();
                openDetailsFragment(args);
            } else if(action.equals(Constants.ACTION_ALERT_LOG)) {
                SelectItem(Constants.INDEX_ALERTS, true);
            } else if(action.equals(Constants.ACTION_VOICE_BIND)) {
                showBindTimer();
            } else if(action.equals(Constants.ACTION_SECURITY_SETTINGS)) {
                SelectItem(Constants.INDEX_ALARM, true);
            } else if(action.equals(Constants.ACTION_GOTO_SYSTEM)) {
                SelectItem(Constants.INDEX_GUI, true);
            }
        }
    };

    private FragmentManager.OnBackStackChangedListener mBackStackListener = new FragmentManager.OnBackStackChangedListener() {
        @Override
        public void onBackStackChanged() {
            setActionBarArrowDependingOnFragmentsBackStack();
        }
    };

    @Override
	protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_LOGIN_FEEDBACK);
        filter.addAction(Constants.ACTION_READ_MESSAGE);
        filter.addAction(Constants.ACTION_SEND_MESSAGE);
        filter.addAction(Constants.ACTION_SEE_DETAILS);
        filter.addAction(Constants.ACTION_ALERT_LOG);
        filter.addAction(Constants.ACTION_VOICE_BIND);
        filter.addAction(Constants.ACTION_SECURITY_SETTINGS);
        filter.addAction(Constants.ACTION_GOTO_SYSTEM);
        LocalBroadcastManager.getInstance(this).registerReceiver(mainReceiver, filter);

        // Initializing
		dataList = new ArrayList<DrawerItem>();
		mTitle = mDrawerTitle = getTitle();
        mToolbar = (Toolbar)findViewById(R.id.tool_bar);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerLayout.setScrimColor(getResources().getColor(android.R.color.transparent));

		// Add Drawer Item to dataList
        dataList.add(new DrawerItem(getResources().getString(R.string.home), R.drawable.ic_launcher, Constants.INDEX_HOME));
		dataList.add(new DrawerItem(getResources().getString(R.string.devices))); // adding a header to the list
        dataList.add(new DrawerItem(getResources().getString(R.string.gui), R.drawable.selector_drawer_system, Constants.INDEX_GUI));
        dataList.add(new DrawerItem(getResources().getString(R.string.lock), R.drawable.selector_drawer_lock, Constants.INDEX_ALARM));
        dataList.add(new DrawerItem(getResources().getString(R.string.console), R.drawable.selector_drawer_console, Constants.INDEX_CONSOLE));
        dataList.add(new DrawerItem(getResources().getString(R.string.alerts), R.drawable.selector_drawer_alerts, Constants.INDEX_ALERTS));
        dataList.add(new DrawerItem(getResources().getString(R.string.webview), R.drawable.selector_drawer_web, Constants.INDEX_WEB));
        dataList.add(new DrawerItem(getResources().getString(R.string.account))); // adding a header to the list
        dataList.add(new DrawerItem(getResources().getString(R.string.logout), R.drawable.selector_drawer_default, Constants.INDEX_EXIT));
		adapter = new CustomDrawerAdapter(this, R.layout.custom_drawer_item, dataList);
		mDrawerList.setAdapter(adapter);
        getNotifications();

		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar,
                R.string.drawer_open, R.string.drawer_close) {
			public void onDrawerClosed(View view) {
                setActionBarArrowDependingOnFragmentsBackStack();
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
			public void onDrawerOpened(View drawerView) {
                setGatewayStatus(((SweetHome)getApplication()).getConnected());
                hideKeyboard(MainActivity.this);
                mDrawerToggle.setDrawerIndicatorEnabled(true);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}
		};

		mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.setDrawerIndicatorEnabled(false);
        mDrawerToggle.setHomeAsUpIndicator(getV7DrawerToggleDelegate().getThemeUpIndicator());
        setActionBarArrowDependingOnFragmentsBackStack();
        setSupportActionBar(mToolbar);

        getFragmentManager().addOnBackStackChangedListener(mBackStackListener);
		if (savedInstanceState == null) {
            SelectItem(Constants.INDEX_HOME, false);
        }

        // Check device for Play Services APK. If check succeeds, proceed with GCM registration.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(getApplicationContext());
            if (regid.isEmpty()) {
                registerInBackground();
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }

        client = ((SweetHome)getApplication()).getClient();
        if(client == null) {
            Toast.makeText(this, "Connection Lost!", Toast.LENGTH_LONG);
        }

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(TextToSpeech.SUCCESS == status) {
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            int result = tts.setLanguage(Locale.getDefault());
                            if (result == TextToSpeech.LANG_MISSING_DATA
                                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                                Log.e(TAG, "tts failure result=" + result);
                            }
                        }
                    });
                    thread.start();
                } else {
                    Log.e(TAG, "tts failure status=" + status);
                }
            }
        });

        //Log.d(TAG, "start ConnectionService");
        //ConnectionService.shouldContinue = true;
        //Intent i = new Intent(this, ConnectionService.class);
        //startService(i);
    }

    @Override
    public void onResume() {
        super.onResume();
        checkPlayServices();
        setGatewayStatus(((SweetHome) getApplication()).getConnected());
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "main onDestroy");
        if(mDialog != null) {
            mDialog.dismiss();
        }
        if(mainReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mainReceiver);
        }
        if(mBackStackListener != null) {
            getFragmentManager().removeOnBackStackChangedListener(mBackStackListener);
        }
        if(tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.menuitem_bind).setVisible(false);
        menu.findItem(R.id.menuitem_clear).setVisible(false);
        menu.findItem(R.id.menuitem_clearalert).setVisible(false);
        menu.findItem(R.id.menuitem_test).setVisible(false);
        menu.findItem(R.id.menuitem_refresh).setVisible(false);

        if(mPreferences == null) {
            mPreferences = getSharedPreferences(Constants.PREF_KEY, Context.MODE_PRIVATE);
        }
        boolean tts_enabled = mPreferences.getBoolean(Constants.KEY_TTS, false);
        if (tts_enabled) {
            menu.findItem(R.id.tts_enabled).setVisible(false);
            menu.findItem(R.id.tts_disabled).setVisible(true);
        } else {
            menu.findItem(R.id.tts_enabled).setVisible(true);
            menu.findItem(R.id.tts_disabled).setVisible(false);
        }
        mMenu = menu;
        return true;
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if((mDrawerToggle.isDrawerIndicatorEnabled())
                && (mDrawerToggle.onOptionsItemSelected(item))) {
            return true;
        } else if((item.getItemId() == android.R.id.home)
                && (getFragmentManager().popBackStackImmediate())) {
            return true;
        } else {
            //return mDrawerToggle.onOptionsItemSelected(item);
            boolean ret = false;
            if(mPreferences == null) {
                mPreferences = getSharedPreferences(Constants.PREF_KEY, Context.MODE_PRIVATE);
            }
            SharedPreferences.Editor editor = mPreferences.edit();
            switch(item.getItemId()) {
                case R.id.menuitem_clear:
                    clearLog();
                    ret = true;
                    break;
                case R.id.menuitem_clearalert:
                    clearAlerts();
                    ret = true;
                    break;
                case R.id.menuitem_bind:
                    showBindTimer();
                    ret = true;
                    break;
                case R.id.menuitem_refresh:
                    webFragment.refresh();
                    ret = true;
                    break;
                case R.id.tts_disabled:
                    editor.putBoolean(Constants.KEY_TTS, false);
                    editor.apply();
                    mMenu.findItem(R.id.tts_disabled).setVisible(false);
                    mMenu.findItem(R.id.tts_enabled).setVisible(true);
                    break;
                case R.id.tts_enabled:
                    editor.putBoolean(Constants.KEY_TTS, true);
                    editor.apply();
                    mMenu.findItem(R.id.tts_enabled).setVisible(false);
                    mMenu.findItem(R.id.tts_disabled).setVisible(true);
                    break;
                default:
                    break;
            }
            return ret;
        }
    }

/*
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        boolean ret = super.onMenuItemSelected(featureId, item);
        if(mPreferences == null) {
            mPreferences = getSharedPreferences(SweetHome.PREF_KEY, Context.MODE_PRIVATE);
        }
        SharedPreferences.Editor editor = mPreferences.edit();
        switch(item.getItemId()) {
            case R.id.menuitem_clear:
                clearLog();
                ret = true;
                break;
            case R.id.menuitem_clearalert:
                clearAlerts();
                ret = true;
                break;
            case R.id.menuitem_bind:
                showBindTimer();
                ret = true;
                break;
            case R.id.menuitem_refresh:
                webFragment.refresh();
                ret = true;
                break;
            case R.id.tts_disabled:
                editor.putBoolean(SweetHome.KEY_TTS, false);
                editor.apply();
                mMenu.findItem(R.id.tts_disabled).setVisible(false);
                mMenu.findItem(R.id.tts_enabled).setVisible(true);
                break;
            case R.id.tts_enabled:
                editor.putBoolean(SweetHome.KEY_TTS, true);
                editor.apply();
                mMenu.findItem(R.id.tts_enabled).setVisible(false);
                mMenu.findItem(R.id.tts_disabled).setVisible(true);
                break;
            default:
                break;
        }
        return ret;
    }
*/

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggles
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

    @Override
    public void onBackPressed() {
        if(mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else if(fragment instanceof FragmentOne) {
            try {
                if (webFragment.canGoBack()) {
                    webFragment.goBack();
                } else {
                    SelectItem(Constants.INDEX_HOME, false);
                    //super.onBackPressed();
                    //overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_up);
                }
            } catch (NullPointerException e) {
                SelectItem(Constants.INDEX_HOME, false);
                //super.onBackPressed();
                //overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_up);
            }
        } else if(fragment instanceof FragmentHome) {
            Log.d(TAG, "back from home");
            super.onBackPressed();
        } else if(getFragmentManager().getBackStackEntryCount() > 0) {
            Log.d(TAG, "backStack Sum=" + getFragmentManager().getBackStackEntryCount());
            getFragmentManager().popBackStackImmediate();
            //super.onBackPressed();
        } else {
            SelectItem(Constants.INDEX_HOME, false);
            //super.onBackPressed();
            //overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_up);
        }
    }

    public void SelectItem(int position, boolean addToBackStack) {
        adapter.selectItem(position);
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        switch(position) {
            case Constants.INDEX_HOME:
                if(homeFragment == null) {
                    fragment = new FragmentHome();
                    homeFragment = (FragmentHome)fragment;
                } else {
                    fragment = homeFragment;
                }

                manager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                transaction.replace(R.id.content_frame, fragment).commit();
                break;
            case Constants.INDEX_GUI:
                if(guiFragment == null) {
                    fragment = new FragmentGUI();
                    guiFragment = (FragmentGUI)fragment;
                } else {
                    fragment = guiFragment;
                }

                if(addToBackStack) {
                    transaction.replace(R.id.content_frame, fragment);
                    transaction.addToBackStack(null);
                } else {
                    manager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    transaction.replace(R.id.content_frame, fragment);
                }
                transaction.commit();
                break;
            case Constants.INDEX_ALARM:
                if(lockFragment == null) {
                    fragment = new FragmentLock();
                    lockFragment = (FragmentLock)fragment;
                } else {
                    fragment = lockFragment;
                }

                if(addToBackStack) {
                    Log.d(TAG, "addToBackStack");
                    transaction.replace(R.id.content_frame, fragment);
                    transaction.addToBackStack(null);
                } else {
                    manager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    transaction.replace(R.id.content_frame, fragment);
                }
                transaction.commit();
                break;
            case Constants.INDEX_CONSOLE:
                if(consoleFragment == null) {
                    fragment = new FragmentConsole();
                    consoleFragment = (FragmentConsole)fragment;
                } else {
                    fragment = consoleFragment;
                }
                manager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                transaction.replace(R.id.content_frame, fragment).commit();
                break;
            case Constants.INDEX_ALERTS:
                resetNotifications();
                if(alertFragment == null) {
                    fragment = new FragmentAlert();
                    alertFragment = (FragmentAlert)fragment;
                } else {
                    fragment = alertFragment;
                }

                if(addToBackStack) {
                    transaction.replace(R.id.content_frame, fragment);
                    transaction.addToBackStack(null);
                } else {
                    manager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    transaction.replace(R.id.content_frame, fragment);
                }
                transaction.commit();
                break;
            case Constants.INDEX_WEB:
                if(webFragment == null) {
                    fragment = new FragmentOne();
                    webFragment = (FragmentOne) fragment;
                } else {
                    fragment = webFragment;
                }
                manager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                transaction.replace(R.id.content_frame, fragment).commit();
                break;
            case Constants.INDEX_EXIT:
                stopClient();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        clearGUI();
                        clearLog();
                        clearAlerts();
                    }
                }).run();
                finish();
                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_up);
                break;
            default:
                break;
        }
        mDrawerList.setItemChecked(position, true);
        mTitle = dataList.get(position).getItemName().toUpperCase();
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    private static void hideKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if(view == null) {
            view = new View(activity);
        }
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private String getValue(String string) {
        String[] split = string.split("\\s+");
        return split[0];
    }

    private void say(String toSpeech) {
        try {
            if(mPreferences == null) {
                mPreferences = getSharedPreferences(Constants.PREF_KEY, Context.MODE_PRIVATE);
            }
            boolean tts_enabled = mPreferences.getBoolean(Constants.KEY_TTS, false);
            if (tts_enabled) {
                if (tts != null) {
                    tts.speak(toSpeech, TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void showBindTimer() {
        mBindDialog = new Dialog(this, R.style.TimerDialogTheme);
        mBindDialog.setContentView(R.layout.timer_dialog);
        mBindDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (mTimer != null) {
                    mTimer.cancel();
                }
            }
        });

        final TextView t1 = (TextView)mBindDialog.findViewById(R.id.timerTitle);
        final TextView t2 = (TextView)mBindDialog.findViewById(R.id.timerTextView);
        final Button abortButton = (Button)mBindDialog.findViewById(R.id.dismiss);
        final ImageView notFoundImage = (ImageView)mBindDialog.findViewById(R.id.not_found);
        final ProgressBar spinner = (ProgressBar)mBindDialog.findViewById(R.id.progress);
        mTimer = new CountDownTimer(120000, 1000) { // 2min, 1sec decrement
            public void onTick(long millisUntilFinished) {
                TextView text1 = (TextView)mBindDialog.findViewById(R.id.timerTextView);
                text1.setText("" + String.format("%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
            }
            @Override
            public void onFinish() {
                t1.setText(getResources().getString(R.string.nada));
                t2.setText("");
                spinner.setVisibility(View.INVISIBLE);
                notFoundImage.setVisibility(View.VISIBLE);
                abortButton.setText(getResources().getString(R.string.retry));
            }
        };

        abortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(abortButton.getText().equals(getResources().getString(R.string.retry))) {
                    t1.setText(getResources().getString(R.string.timer_hint));
                    abortButton.setText(getResources().getString(R.string.abort));
                    spinner.setVisibility(View.VISIBLE);
                    notFoundImage.setVisibility(View.INVISIBLE);
                    sendMessageToSocket(Constants.CMD_BIND, true);
                    mTimer.start();
                } else if(abortButton.getText().equals(getResources().getString(R.string.abort))) {
                    mBindDialog.dismiss();
                }
            }
        });

        sendMessageToSocket(Constants.CMD_BIND, false);
        mBindDialog.show();
        mTimer.start();
    }

    private void stopClient() {
        ConnectionService.shouldContinue = false;
        if(client != null) {
            client.stopClient();
            ((SweetHome)getApplication()).setClient(null);
            ((SweetHome)getApplication()).setConnected(false);
            if(mPreferences == null) {
                mPreferences = getSharedPreferences(Constants.PREF_KEY, Context.MODE_PRIVATE);
            }
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.remove(Constants.KEY_SERVER);
            editor.remove(Constants.KEY_PORT);
            editor.apply();
        }
    }

    private void setActionBarArrowDependingOnFragmentsBackStack() {
        int backStackEntryCount = getFragmentManager().getBackStackEntryCount();
        mDrawerToggle.setDrawerIndicatorEnabled(backStackEntryCount == 0);
    }

    private void openDetailsFragment(Bundle args) {
        detailsFragment = new FragmentSub1();
        detailsFragment.setArguments(args);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, detailsFragment);
        ft.addToBackStack(null);
        ft.commit();

        int deviceId = args.getInt(Constants.JSON_ID, 0);
        String deviceType = args.getString(Constants.JSON_TYPE);
        String title = deviceId + " - " + deviceType;
        mTitle = title.toUpperCase();
    }

    private GUIItem parseJsonAttr(JSONObject obj, String cmd) {
        GUIItem item = null;
        try {
            int id = obj.getInt(Constants.JSON_ID);
            String type = obj.getString(Constants.JSON_TYPE).replace(" ", "");
            JSONObject attr = obj.getJSONObject(Constants.JSON_ATTRIBUTES);
            if(cmd.equals(Constants.ADD)) {
                //String name = obj.getString(SweetHome.JSON_NAME);
                //Log.e(TAG, "Parsed device: id=" + id + ", name=" + name + ", type=" + type);
                //item = new GUIItem(id, name, type);
                Log.e(TAG, "Parsed device: id=" + id + ", type=" + type);
                item = new GUIItem(id, type);
            } else {
                Log.e(TAG, "Parsed device: id=" + id + ", type=" + type);
                if(guiFragment == null) {
                    return null;
                } else {
                    item = guiFragment.getItem(id, type);
                    if (item == null) {
                        return null;
                    }
                }
            }

            if(cmd.equals(Constants.CMD_EVENT)){
                String message = "";
                switch(type) {
                    case Constants.DEVICE_PLUG:
                        String loaded =  attr.getString(Constants.JSON_LOAD);
                        if(loaded.equals(Constants.CONNECTED)) {
                            message = id + " - Plug connected";
                            //say("Plug connected");
                            item.setLoaded(true);
                        } else if(loaded.equals(Constants.REMOVED)) {
                            message = id + " - Plug disconnected";
                            //say("Plug disconnected");
                            item.setLoaded(false);
                        }
                        break;
                    case Constants.DEVICE_WALL_SWITCH:
                        item.setValue(attr.getString(Constants.JSON_SWITCH_VALUE));
                        message = id + " - Switch hit!" + attr.getString(Constants.JSON_SWITCH_VALUE);
                        //say("Switch hit! " + attr.getString(SweetHome.JSON_SWITCH_VALUE));
                        break;
                    case Constants.DEVICE_MOTION:
                        try {
                            int motion = attr.getInt(Constants.JSON_MOTION);
                            if (motion == 1) {
                                message = id + " - Motion detected";
                                //say("Motion detected");
                                item.setTrigger(true);
                            }
                        } catch(JSONException e) {
                            item.setTrigger(false);
                        }
                        try {
                            if(attr.getInt(Constants.JSON_TAMPER) == 1) {
                                item.setTamper(true);
                                message = id + " - Someone is tampering with the motion sensor!";
                                //say("Someone is tampering with the motion sensor!");
                            }
                        } catch(JSONException e) {
                            item.setTamper(false);
                        }
                        break;
                    case Constants.DEVICE_FLOOD:
                        String flood = attr.getString(Constants.JSON_FLOOD);
                        if(flood.equals(Constants.ON)) {
                            message = id + " - Flood detected";
                            //say("Flood! Flood!");
                            item.setTrigger(true);
                        } else if(flood.equals(Constants.OFF)) {
                            item.setTrigger(false);
                        }
                        break;
                    case Constants.DEVICE_MAGNETIC:
                        String magnetic = attr.getString(Constants.JSON_MAGNETIC);
                        if(magnetic.equals(Constants.OPEN)) {
                            message = id + " - Door opened";
                            //say("Door opened");
                            item.setTrigger(true);
                        } else if(magnetic.equals(Constants.CLOSE)) {
                            message = id + " - Door closed";
                            //say("Door closed");
                            item.setTrigger(false);
                        }
                        break;
                    case Constants.DEVICE_THERMO:
                        break;
                    case Constants.DEVICE_SMOKE:
                        try {
                            String fire = attr.getString(Constants.JSON_FIRE);
                            if (fire.equals(Constants.ALARM)) {
                                message = id + " - Fire detected";
                                //say("Fire! Call 911! Run!");
                                item.setTrigger(true);
                            }
                        } catch (JSONException e) {
                            item.setTrigger(false);
                        }
                        try {
                            String batt = attr.getString(Constants.JSON_SMOKE_BATTERY);
                            item.setBattery(Integer.parseInt(getValue(batt)));
                        } catch(JSONException e) {
                            item.setBattery(0);
                        }
                        try {
                            item.setTamper(attr.getBoolean(Constants.JSON_TAMPER));
                            if(attr.getBoolean(Constants.JSON_TAMPER)) {
                                message = id + " - Someone is tampering with smoke detector";
                                //say("Someone is tampering with smoke detector!");
                            }
                        } catch(JSONException e) {
                            item.setTamper(false);
                        }
                        break;
                    case Constants.DEVICE_SIREN:
                        try {
                            String lowBatt = attr.getString(Constants.JSON_BATT_LEVEL);
                            if (lowBatt.equals(Constants.LOW_BATT)) {
                                message = id + " - Low Battery";
                                item.setLowBatt(true);
                            }
                        } catch(JSONException e) {
                            item.setLowBatt(false);
                        }

                        try {
                            item.setTamper(attr.getBoolean(Constants.JSON_TAMPER));
                            if(attr.getBoolean(Constants.JSON_TAMPER)) {
                                message = id + " - Siren compromised";
                                //say("Siren compromised!");
                            }
                        } catch(JSONException e) {
                            item.setTamper(false);
                        }
                        break;
                    case Constants.DEVICE_KEYPAD:
                        try {
                            item.setTamper(attr.getBoolean(Constants.JSON_TAMPER));
                            if(attr.getBoolean(Constants.JSON_TAMPER)) {
                                message = id + " - Someone is messing with the key pad";
                                //say("Someone is messing with the key pad");
                            }
                        } catch (JSONException e) {
                            item.setTamper(false);
                        }
                    case Constants.DEVICE_REMOTE:
                        item.setSecurity(attr.getString(Constants.JSON_SECURITY));
                        message = id + " - " + attr.getString(Constants.JSON_SECURITY);
                        //say("System " + attr.getString(SweetHome.JSON_SECURITY));
                        break;
                    default:
                        break;
                }
                if(!message.isEmpty()) {
                    AlertItem alert = new AlertItem(id, type, message, getCurrentTime());
                    item.setAlert(alert);
                    saveAlertList(alert);
                    if(alertFragment != null && alertFragment.isVisible()) {
                        alertFragment.updateList(getApplicationContext(), alert);
                    }
                }

            } else if(cmd.equals(Constants.CMD_REPORT)) {
                switch(type.replace(" ", "")) {
                    case Constants.DEVICE_PLUG:
                        try {
                            item.setOverload(attr.getBoolean(Constants.JSON_OVERLOAD));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            String power = attr.getString(Constants.JSON_POWER);
                            if (power.equals(Constants.ON)) {
                                item.setPower(true);
                            } else if (power.equals(Constants.OFF)) {
                                item.setPower(false);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                        try {
                            String active_power = attr.getString(Constants.JSON_WATT);
                            item.setWatt(Integer.parseInt(getValue(active_power)));
                        } catch(JSONException e) {
                            e.printStackTrace();
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                        break;
                    case Constants.DEVICE_THERMO:
                        try {
                            String temp_str = attr.getString(Constants.JSON_TEMP);
                            String hum_str = attr.getString(Constants.JSON_HUMIDITY);
                            double temp = Double.parseDouble(getValue(temp_str));
                            int humidity = Integer.parseInt(getValue(hum_str));
                            if(mPreferences == null) {
                                mPreferences = getSharedPreferences(Constants.PREF_KEY, Context.MODE_PRIVATE);
                            }
                            item.setThermo(temp, humidity);
                            SharedPreferences.Editor editor = mPreferences.edit();
                            long longTemp = Double.doubleToRawLongBits(temp);
                            editor.putLong(Constants.JSON_TEMP, longTemp);
                            editor.putInt(Constants.JSON_HUMIDITY, humidity);
                            editor.apply();
                            if(homeFragment != null && homeFragment.isVisible()) {
                                homeFragment.setThermo(temp, humidity);
                            }
                        } catch(JSONException e) {
                            e.printStackTrace();
                        }
                    case Constants.DEVICE_WALL_SWITCH:
                    case Constants.DEVICE_MOTION:
                    case Constants.DEVICE_MAGNETIC:
                    case Constants.DEVICE_SMOKE:
                    case Constants.DEVICE_KEYPAD:
                    case Constants.DEVICE_REMOTE:
                        String batt = attr.getString(Constants.JSON_BATT_LEVEL);
                        item.setBattery(Integer.parseInt(getValue(batt)));
                        break;
                    default:
                        break;
                }
            } else if(cmd.equals(Constants.ADD)) {
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return item;
    }

    private void parseJsonModify(JSONObject obj, String cmd) {
        GUIItem item = parseJsonAttr(obj, cmd);
        if(item == null) {
        } else {
            if(guiFragment != null && guiFragment.isVisible()) {
                guiFragment.modifyItem(item);
            }
            if(detailsFragment != null && detailsFragment.isVisible()) {
                detailsFragment.updateContent(item);
            }
        }
    }

    private void parseJsonRemove(int id, String type) {
        if((guiFragment != null) && guiFragment.isVisible()) {
            if(!guiFragment.removeItem(id, type)) {
                sendMessageToSocket(Constants.CMD_LIST, true);
            }
        }
    }

    private void parseJsonAdd(JSONObject obj) {
        try {
            JSONArray device_array = obj.getJSONArray(Constants.JSON_DEVICES);
            int length = device_array.length();
            if(mPreferences == null) {
                mPreferences = getSharedPreferences(Constants.PREF_KEY, MODE_PRIVATE);
            }
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putInt(Constants.JSON_DEVICES, length).apply();
            for(int i=0; i<length; i++) {
                GUIItem item = parseJsonAttr(device_array.getJSONObject(i), Constants.ADD);
                if(item != null && guiFragment != null && guiFragment.isVisible()) {
                    guiFragment.updateList(item);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String parseJsonCmd(String message) {
        int startIndex = message.indexOf("{");
        String jsonString;
        if(startIndex > -1) {
            jsonString = message.substring(startIndex);
        } else {
            jsonString = message;
        }
        try {
            JSONObject obj = new JSONObject(jsonString);
            //english = obj.getString(SweetHome.JSON_ENGLISH);
            //say(english);
            //Log.d(TAG, "console message=" + english);
            //if(english.equals(SweetHome.MESSAGE_IN_USE)) {
                //return english;
            //}

            String cmd = obj.getString(Constants.JSON_CMD);
            Log.d(TAG, "JSON cmd=" + cmd);
            switch(cmd) {
                case Constants.CMD_LIST:
                case Constants.CMD_L:
                    if(obj.getInt(Constants.JSON_RESULT) == 0) {
                        clearGUI();
                        parseJsonAdd(obj);
                    } else {
                        Log.e(TAG, "error return code");
                    }
                    break;
                case Constants.CMD_BIND:
                    if(obj.getInt(Constants.JSON_RESULT) == 0) {
                        say("New device registered");
                        sendMessageToSocket(Constants.CMD_LIST, true);
                        //parseJsonAdd(obj);
                        if(mBindDialog != null) {
                            mBindDialog.dismiss();
                        }
                    } else {
                        Log.e(TAG, "error return code");
                    }
                    break;
                case Constants.CMD_REMOVE:
                    if(obj.getInt(Constants.JSON_RESULT) == 0) {
                        say(obj.getString(Constants.JSON_TYPE) + " removed");
                        //parseJsonRemove(obj.getInt(SweetHome.JSON_ID), obj.getString(SweetHome.JSON_TYPE));
                        sendMessageToSocket(Constants.CMD_L, true);
                    } else {
                        Log.e(TAG, "error return code");
                    }
                    break;
                case Constants.CMD_EVENT:
                case Constants.CMD_REPORT:
                    parseJsonModify(obj, cmd);
                    break;
                case Constants.CMD_SEND:
                    break;
                case Constants.CMD_ARM:
                case Constants.CMD_PARM1:
                case Constants.CMD_PARM2:
                case Constants.CMD_DISARM:
                    break;
                default:
                    break;
            }
        } catch (JSONException e) {
            Log.e(TAG, "!!!!!JSON EXCEPTION!!!" + e);
        }
        return "something";//english;
    }

    private void readSocketMessage(String message) {
        Log.d(TAG, "readSocketMessage:" + message);
        String consoleMessage = "";
        if(message.equals("hi")) {
            return;
        } else if(message.equals(Constants.MESSAGE_NO_DEVICE)) {
            if(mPreferences == null) {
                mPreferences = getSharedPreferences(Constants.PREF_KEY, MODE_PRIVATE);
            }
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putInt(Constants.JSON_DEVICES, 0).apply();

            clearGUI();
            if(guiFragment != null && guiFragment.isVisible()) {
                guiFragment.updateList(null);
            }
        } else if(!message.isEmpty()) {
            //consoleMessage = message;
            consoleMessage = parseJsonCmd(message);
            //Log.w(TAG, "consoleMessage" + consoleMessage);
        }

        if (/*!consoleMessage.isEmpty() &&*/
                !consoleMessage.equals(Constants.MESSAGE_IN_USE)) {
            //MessageItem listItem = new MessageItem(consoleMessage, true, getCurrentTime());
            say(message);
            MessageItem listItem = new MessageItem(message, true, getCurrentTime());
            listItem.setOrigin(client.getServerAddr() + "/" + String.valueOf(client.getPort()));
            saveLogList(listItem);
            if(consoleFragment != null && consoleFragment.isVisible()) {
                consoleFragment.updateList(getApplicationContext(), listItem);
            }
        }
    }

    private void sendMessageToSocket(String message, boolean logSaved) {
        if(message != null && !message.isEmpty()) {
            MessageItem item = new MessageItem(message, false, getCurrentTime());
            if(!logSaved) {
                saveLogList(item);
            }

            if(mPreferences == null) {
                mPreferences = getSharedPreferences(Constants.PREF_KEY, Context.MODE_PRIVATE);
            }

            boolean usbConnected = mPreferences.getBoolean(Constants.USB_CONNECTION, false);
            if(usbConnected) {
                Log.d(TAG, "message to USB");
                Intent intent = new Intent(Constants.ACTION_USB_MESSAGE);
                intent.putExtra(Constants.MESSAGE, message);
                sendBroadcast(intent);
            } else {
                if (client != null) {
                    int gatewayID = mPreferences.getInt(Constants.KEY_ID, -1);
                    Log.d(TAG, "message to Gateway " + gatewayID);
                    client.sendMessage(gatewayID, message);
                }
            }
        }
    }

    private void clearGUI() {
        if(guiFragment != null) {
            guiFragment.clear();
        }
    }

    private void saveAlertList(final AlertItem item) {
        incNotifications();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    if (mAlertDataSource == null) {
                        mAlertDataSource = new AlertDataSource(getApplicationContext());
                    }
                    mAlertDataSource.open();
                    mAlertDataSource.insertLogtoDB(item);
                } catch(IOException e) {
                    e.printStackTrace();
                } catch(NullPointerException e) {
                    e.printStackTrace();
                } finally {
                    mAlertDataSource.close();
                }
                return null;
            }
        }.execute();

        /*
        final SharedPreferences prefs = getSharedPreferences(SweetHome.PREF_ALERT, Context.MODE_PRIVATE);
        synchronized (prefs) {
            Thread t1 = new Thread(new Runnable() {
                @Override
                public void run() {
                    ArrayList<AlertItem> alertList;

                    //load log from SP
                    String savedJsonString = prefs.getString(SweetHome.ALERT_PROPERTY, "");
                    Gson gson = new Gson();
                    if(savedJsonString.isEmpty()) {
                        alertList = new ArrayList<AlertItem>();
                    } else {
                        Type type = new TypeToken<ArrayList<AlertItem>>(){}.getType();
                        alertList = gson.fromJson(savedJsonString, type);
                    }

                    //add new message
                    int size = alertList.size();
                    if(size > SweetHome.MAX_RECORD_NUM) {
                        alertList.remove(size - 1);
                    }
                    alertList.add(0, item);
                    SharedPreferences.Editor editor = prefs.edit();
                    savedJsonString = gson.toJson(alertList);
                    editor.putString(SweetHome.ALERT_PROPERTY, savedJsonString);
                    editor.apply();
                }
            });
            t1.run();
        }
        */
    }

    private void clearAlerts() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                if(mAlertDataSource == null) {
                    mAlertDataSource = new AlertDataSource(getApplicationContext());
                }

                try {
                    mAlertDataSource.open();
                    mAlertDataSource.clearLogDB();
                } catch(IOException e) {
                    e.printStackTrace();
                } finally {
                    mAlertDataSource.close();
                }
                return null;
            }
        }.execute();

        SharedPreferences prefs = getSharedPreferences(Constants.PREF_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        //editor.remove(SweetHome.ALERT_PROPERTY);
        editor.remove(Constants.ALERT_UNREAD).apply();
        if(alertFragment != null) {
            alertFragment.clearLog();
        }
        notifications = 0;
        dataList.get(Constants.INDEX_ALERTS).setNotifications(notifications);
        adapter.notifyDataSetChanged();
    }

    private void resetNotifications() {
        notifications = 0;
        dataList.get(Constants.INDEX_ALERTS).setNotifications(notifications);
        adapter.notifyDataSetChanged();
        SharedPreferences prefs = getSharedPreferences(Constants.PREF_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(Constants.ALERT_UNREAD).apply();
    }

    private void incNotifications() {
        if(notifications <= Constants.MAX_RECORD_NUM) {
            if (alertFragment == null || !alertFragment.isVisible()) {
                SharedPreferences prefs = getSharedPreferences(Constants.PREF_KEY, Context.MODE_PRIVATE);
                notifications = prefs.getInt(Constants.ALERT_UNREAD, 0);
                notifications++;

                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(Constants.ALERT_UNREAD, notifications).apply();
                dataList.get(Constants.INDEX_ALERTS).setNotifications(notifications);
                adapter.notifyDataSetChanged();
            }
        }
        if(notifications > 0) {
            if(homeFragment != null && homeFragment.isVisible()) {
                homeFragment.blinkAlerts();
            }
        }
    }

    private void getNotifications() {
        SharedPreferences prefs = getSharedPreferences(Constants.PREF_KEY, Context.MODE_PRIVATE);
        notifications = prefs.getInt(Constants.ALERT_UNREAD, 0);
        dataList.get(Constants.INDEX_ALERTS).setNotifications(notifications);
        adapter.notifyDataSetChanged();
    }

    private void setGatewayStatus(boolean status) {
        dataList.get(Constants.INDEX_HOME).setGatewayStatus(status);
        adapter.notifyDataSetChanged();
        if(homeFragment != null && homeFragment.isVisible()) {
            homeFragment.setGatewayStatus(status);
        }
    }

    private void saveLogList(final MessageItem item) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    if (mLogDataSource == null) {
                        mLogDataSource = new LogDataSource(getApplicationContext());
                    }
                    mLogDataSource.open();
                    mLogDataSource.insertLogtoDB(item);
                } catch(IOException e) {
                    e.printStackTrace();
                } catch(NullPointerException e) {
                    e.printStackTrace();
                } finally {
                    mLogDataSource.close();
                }
                return null;
            }
        }.execute();
        /*
        final SharedPreferences prefs = getSharedPreferences(SweetHome.PREF_LOG, Context.MODE_PRIVATE);
        synchronized (prefs) {
            Thread t1 = new Thread(new Runnable() {
                @Override
                public void run() {
                    ArrayList<MessageItem> logList;

                    //load log from SP
                    String savedJsonString = prefs.getString(SweetHome.LOG_PROPERTY, "");
                    Gson gson = new Gson();
                    if(savedJsonString.isEmpty()) {
                        logList = new ArrayList<MessageItem>();
                    } else {
                        Type type = new TypeToken<ArrayList<MessageItem>>(){}.getType();
                        logList = gson.fromJson(savedJsonString, type);
                    }

                    //add new message
                    if(logList.size() > SweetHome.MAX_RECORD_NUM) {
                        logList.remove(0);
                    }
                    logList.add(item);
                    SharedPreferences.Editor editor = prefs.edit();
                    savedJsonString = gson.toJson(logList);
                    editor.putString(SweetHome.LOG_PROPERTY, savedJsonString);
                    editor.apply();
                }
            });
            t1.run();
        }
        */
    }

    private void clearLog() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    if (mLogDataSource == null) {
                        mLogDataSource = new LogDataSource(getApplicationContext());
                    }
                    mLogDataSource.open();
                    mLogDataSource.clearLogDB();
                } catch(IOException e) {
                    e.printStackTrace();
                } catch(NullPointerException e) {
                    e.printStackTrace();
                } finally {
                    mLogDataSource.close();
                }
                return null;
            }
        }.execute();
        /*
        SharedPreferences prefs = getSharedPreferences(SweetHome.PREF_LOG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(SweetHome.LOG_PROPERTY).apply();
        */
        if(consoleFragment != null) {
            consoleFragment.clearLog();
        }
    }

    private String getCurrentTime() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        return df.format(c.getTime());
    }

    private void gotoActivity(Intent i) {
        startActivity(i);
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    private String getRegistrationId(Context context) {
        if(mPreferences == null) {
            mPreferences = getSharedPreferences(Constants.PREF_KEY, Context.MODE_PRIVATE);
        }
        String registrationId = mPreferences.getString(Constants.PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = mPreferences.getInt(Constants.PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    }
                    regid = gcm.register(Constants.SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
                    sendRegistrationIdToBackend(regid);

                    // Persist the regID - no need to register again.
                    storeRegistrationId(getApplicationContext(), regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Log.d(TAG, "GCM Message=" + msg);
                //mDisplay.append(msg + "\n");
            }
        }.execute(null, null, null);
    }

    private void sendRegistrationIdToBackend(String registration_id) {
        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost("http://www.everspring.com");

        try {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
            nameValuePairs.add(new BasicNameValuePair("registration_id", registration_id));
            //nameValuePairs.add(new BasicNameValuePair("stringdata", "Hi"));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void storeRegistrationId(Context context, String regId) {
        if(mPreferences == null) {
            mPreferences = getSharedPreferences(Constants.PREF_KEY, Context.MODE_PRIVATE);
        }
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(Constants.PROPERTY_REG_ID, regId);
        editor.putInt(Constants.PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            if (dataList.get(position).getTitle() == null) {
                SelectItem(position, false);
            }
        }
    }
}