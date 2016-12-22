package com.evsp.sweethome;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.Calendar;

public class FragmentHome extends Fragment implements View.OnClickListener{
    private static final String TAG = FragmentHome.class.getSimpleName();
    private View mBackground;
    private final int MAX_OPACITY = 150;
    private final int HALF = 50;
    private String[] weekDays;
    private String[] months;
    TextView timeTextView, dayTextView, dateTextView, cityTextView, tempTextView;
    TextView humidityTextView, gateStatusTV, deviceNumTV;
    View divider;
    ImageButton lockButton, alertButton;
    ImageView gatewayIcon;

    @Override
    public void onClick(View view) {
        String action = "";
        if(view == lockButton) {
            action = Constants.ACTION_SECURITY_SETTINGS;
        } else if(view == alertButton) {
            action = Constants.ACTION_ALERT_LOG;
        } else if(view == deviceNumTV) {
            action = Constants.ACTION_GOTO_SYSTEM;
            Log.d(TAG, "action=" + action);
        }

        if(!action.isEmpty()) {
            Intent intent = new Intent(action);
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
            Log.d(TAG, "broadcast sent");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unregisterReceiver(homeReceiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        initGlobals(view);
        IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        getActivity().registerReceiver(homeReceiver, filter);

        setGatewayTimeDisplay();
        SeekBar brightnessBar = (SeekBar) view.findViewById(R.id.brightness_bar);
        brightnessBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setBackgroundAlpha(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.home).toUpperCase());
        boolean connected = ((SweetHome)getActivity().getApplication()).getConnected();
        setGatewayStatus(connected);

        SharedPreferences pref = getActivity().getSharedPreferences(Constants.PREF_KEY, Context.MODE_PRIVATE);
        int notifications = pref.getInt(Constants.ALERT_UNREAD, 0);
        if(notifications > 0) {
            blinkAlerts();
        } else {
            alertButton.setVisibility(View.INVISIBLE);
        }

        long longTemp = pref.getLong(Constants.JSON_TEMP, Long.MIN_VALUE);
        double temp = Double.MIN_VALUE;
        if(longTemp != Long.MIN_VALUE) {
            temp = Double.longBitsToDouble(longTemp);
        }
        int humid = pref.getInt(Constants.JSON_HUMIDITY, Integer.MIN_VALUE);
        setThermo(temp, humid);

        if(connected) {
            int devices = pref.getInt(Constants.JSON_DEVICES, 0);
            deviceNumTV.setText("" + devices + " " + getResources().getString(R.string.device_num));
            deviceNumTV.setVisibility(View.VISIBLE);
        } else {
            deviceNumTV.setVisibility(View.INVISIBLE);
        }

        String securityState = pref.getString(Constants.JSON_SECURITY, Constants.SECURITY_DISARM);
        switch(securityState) {
            case Constants.SECURITY_ARM:
                lockButton.setImageDrawable(getResources().getDrawable(R.drawable.btn_security_arm_on));
                break;
            case Constants.SECURITY_PARM1:
                lockButton.setImageDrawable(getResources().getDrawable(R.drawable.btn_security_partial_1_on));
                break;
            case Constants.SECURITY_PARM2:
                lockButton.setImageDrawable(getResources().getDrawable(R.drawable.btn_security_partial_2_on));
                break;
            case Constants.SECURITY_DISARM:
                lockButton.setImageDrawable(getResources().getDrawable(R.drawable.btn_security_disarm_on));
                break;
            default:
                break;
        }
    }

    private BroadcastReceiver homeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch(intent.getAction()) {
                case Intent.ACTION_TIME_TICK:
                case Intent.ACTION_TIME_CHANGED:
                    setGatewayTimeDisplay();
                    break;
            }
        }
    };

    private void initGlobals(View view) {
        mBackground = view.findViewById(R.id.background_home);
        timeTextView = (TextView)view.findViewById(R.id.time_text);
        dayTextView = (TextView)view.findViewById(R.id.day_text);
        dateTextView = (TextView)view.findViewById(R.id.date_text);
        cityTextView = (TextView)view.findViewById(R.id.city_text);
        tempTextView = (TextView)view.findViewById(R.id.temp_text);
        humidityTextView = (TextView)view.findViewById(R.id.humid_text);
        divider = view.findViewById(R.id.divider);
        gateStatusTV = (TextView)view.findViewById(R.id.status_text);
        deviceNumTV = (TextView)view.findViewById(R.id.devices_text);
        weekDays = getResources().getStringArray(R.array.weekdays);
        months = getResources().getStringArray(R.array.months);
        lockButton = (ImageButton)view.findViewById(R.id.security_icon);
        alertButton = (ImageButton) view.findViewById(R.id.alert_icon);
        gatewayIcon = (ImageView)view.findViewById(R.id.gateway_icon);

        deviceNumTV.setOnClickListener(this);
        alertButton.setOnClickListener(this);
        lockButton.setOnClickListener(this);
    }

    private void setGatewayTimeDisplay() {
        Calendar c = Calendar.getInstance();
        DecimalFormat format = new DecimalFormat("00");
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        timeTextView.setText(format.format(hour) + ":" + format.format(minute));

        int day = c.get(Calendar.DAY_OF_WEEK);
        dayTextView.setText(weekDays[day].toUpperCase() + ", ");

        int dayMonth = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH);
        int year = c.get(Calendar.YEAR);
        dateTextView.setText(format.format(dayMonth) + " " + months[month] + " "
                + new DecimalFormat("0000").format(year));
    }

    private void setBackgroundAlpha(int progress) {
        int black = getResources().getColor(android.R.color.black);
        int white = getResources().getColor(android.R.color.white);
        int new_bg = 0;
        setTextColor(progress);
        if(progress == HALF) {
            mBackground.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        } else if (progress < HALF) {
            new_bg = Color.argb((MAX_OPACITY/HALF) * (HALF-progress), Color.red(black), Color.green(black), Color.blue(black));
            mBackground.setBackgroundColor(new_bg);
        } else if (progress > HALF) {
            new_bg = Color.argb((MAX_OPACITY/HALF) * (progress-HALF), Color.red(white), Color.green(white), Color.blue(white));
            mBackground.setBackgroundColor(new_bg);
        }
    }

    private void setTextColor(int color) {
        try {
            color = Color.argb(255, (255/100) * (255-color), (255/100) * (255-color), (255/100) * (255-color));
            timeTextView.setTextColor(color);
            dayTextView.setTextColor(color);
            dateTextView.setTextColor(color);
            cityTextView.setTextColor(color);
            tempTextView.setTextColor(color);
            humidityTextView.setTextColor(color);
            divider.setBackgroundColor(color);
            //gateStatusTV.setTextColor(color);
            deviceNumTV.setTextColor(color);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void setGatewayStatus(boolean connected) {
        if(connected) {
            gatewayIcon.setImageDrawable(getResources().getDrawable(R.drawable.picto_gateway_network_on));
            gateStatusTV.setText(getResources().getString(R.string.online));
            gateStatusTV.setTextColor(getResources().getColor(android.R.color.holo_green_light));
        } else {
            gatewayIcon.setImageDrawable(getResources().getDrawable(R.drawable.picto_gateway_network_off));
            gateStatusTV.setText(getResources().getString(R.string.offline));
            gateStatusTV.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    public void setThermo(Double temp, int humid) {
        if(temp == Double.MIN_VALUE) {
            tempTextView.setText("--.-\u00B0C");
        } else {
            tempTextView.setText(String.valueOf(temp) + "\u00B0C");
        }

        if(humid == Integer.MIN_VALUE) {
            humidityTextView.setText("--%");
        } else {
            humidityTextView.setText(String.valueOf(humid + "%"));
        }
    }

    public void blinkAlerts() {
        Animation blinker = AnimationUtils.loadAnimation(getActivity(), R.anim.blink);
        alertButton.setVisibility(View.VISIBLE);
        alertButton.startAnimation(blinker);
    }
}
