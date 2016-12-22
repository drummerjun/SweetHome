package com.evsp.sweethome;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.evsp.sweethome.adapters.GUIItem;

public class FragmentSub1 extends Fragment {
    private static final String TAG = "Sub1";

    private EditText sNameEditText;
    private Button sAlertLogButton;
    private Switch sPowerSwitch;
    private TextView sBatteryText;
    private ImageView sBatteryImage;
    private ImageView sDeviceImage;
    private TextView sAttr1Text;
    private TextView sAttr2Text;

    private int deviceId;
    private String deviceType;
    private String deviceName;
    private int deviceBattery;
    private boolean devicePower;
    private int plugWatt;
    private double valueTemp;
    private int valueHumidity;
    private boolean smokeAttached;
    private String securityLevel;

    private String alertMessage;
    private String alertTime;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sub1_details, container, false);
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(sNameEditText.getWindowToken(), 0);
                return true;
            }
        });
        setHasOptionsMenu(true);
        getDeviceParams(getArguments());
        initViews(view);
        configureViews();
        Button removeButton = (Button)view.findViewById(R.id.button_remove);
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(sNameEditText.getWindowToken(), 0);

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                        .setTitle(getResources().getString(R.string.remove))
                        .setMessage(getResources().getString(R.string.discard) + " " + deviceName + "?")
                        .setNegativeButton(getResources().getString(android.R.string.no), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setPositiveButton(getResources().getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                sendMessageToConsole(Constants.CMD_REMOVE + " " + deviceId, false);
                                getFragmentManager().popBackStackImmediate();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        Button saveButton = (Button)view.findViewById(R.id.button_save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(sNameEditText.getWindowToken(), 0);

                String newName = sNameEditText.getText().toString();
                newName = newName.replace(" ", "");
                if(!newName.equals(deviceName)) {
                    sendMessageToConsole(".alias " + deviceId + " " + newName, false);
                }
            }
        });

        Log.d(TAG, "device name=" + deviceName);
        sNameEditText.setText(deviceName);
        sPowerSwitch.setChecked(devicePower);
        setBattery();
        setAttributeText();
        int start = alertMessage.indexOf("-");
        if(start > -1) {
            String sub = alertMessage.substring(start);
            alertMessage = sub.replace("- ", "");
        }
        sAlertLogButton.setText(alertTime + "\n" + alertMessage);
        sAlertLogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Constants.ACTION_ALERT_LOG);
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        String title = deviceId + " - " + deviceType;
        ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle(title.toUpperCase());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.findItem(R.id.menuitem_test).setVisible(true);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void sendMessageToConsole(String message, boolean saved) {
        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_SEND_MESSAGE);
        intent.putExtra(Constants.MESSAGE, message);
        intent.putExtra(Constants.SAVED, saved);
        Log.w(TAG, "sendBroadcast: " + message);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
    }

    private void setBattery() {
        sBatteryImage.setVisibility(View.VISIBLE);
        sBatteryText.setVisibility(View.VISIBLE);
        sBatteryText.setText(String.valueOf(deviceBattery) + "%");
        if(deviceType.equals(Constants.DEVICE_PLUG)) {
            sBatteryImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.picto_battery_100));
            sBatteryText.setVisibility(View.INVISIBLE);
        } else if(deviceBattery > 90 && deviceBattery <= 100) {
            sBatteryImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.picto_battery_100));
        } else if(deviceBattery > 70 && deviceBattery <= 90) {
            sBatteryImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.picto_battery_80));
        } else if(deviceBattery > 50 && deviceBattery <= 70) {
            sBatteryImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.picto_battery_60));
        } else if(deviceBattery > 30 && deviceBattery <= 50) {
            sBatteryImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.picto_battery_40));
        } else if(deviceBattery > 10 && deviceBattery <= 30) {
            sBatteryImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.picto_battery_20));
        } else if(deviceBattery > 0 && deviceBattery <= 10) {
            sBatteryImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.picto_battery_0));
        } else if(deviceBattery == 0) {
            sBatteryImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.picto_battery_no));
        } else if(deviceBattery < 0) {
            sBatteryImage.setVisibility(View.INVISIBLE);
            sBatteryText.setVisibility(View.INVISIBLE);
        }
    }

    private String parseSecurityText(String security) {
        String ret = "";
        if(security.equals(Constants.SECURITY_ARM)) {
            ret = getResources().getString(R.string.arm);
        } else if(security.equals(Constants.SECURITY_PARM1)) {
            ret = getResources().getString(R.string.parm1);
        } else if(security.equals(Constants.SECURITY_PARM2)) {
            ret = getResources().getString(R.string.parm2);
        } else if(security.equals(Constants.SECURITY_DISARM)) {
            ret = getResources().getString(R.string.disarm);
        }
        return ret;
    }

    private void setThermoDisplay(double temp, int humidity) {
        if(temp > Double.MIN_VALUE) {
            sAttr1Text.setVisibility(View.VISIBLE);
            sAttr1Text.setText(getResources().getString(R.string.temp) + temp + "C");
        } else {
            sAttr1Text.setVisibility(View.GONE);
        }

        if(humidity > Integer.MIN_VALUE) {
            sAttr2Text.setVisibility(View.VISIBLE);
            sAttr2Text.setText(getResources().getString(R.string.humidity) + humidity + "%");
        } else {
            sAttr2Text.setVisibility(View.GONE);
        }
    }

    private void setAttributeText() {
        switch (deviceType) {
            case Constants.DEVICE_PLUG:
                sAttr1Text.setText("Watt: " + plugWatt);
                break;
            case Constants.DEVICE_THERMO:
                setThermoDisplay(valueTemp, valueHumidity);
                break;
            case Constants.DEVICE_SMOKE:
                String attached = smokeAttached ? "OK" : "No!";
                sAttr1Text.setText("Detector attached: " + attached);
                if(smokeAttached) {
                    sAttr1Text.setTextColor(getResources().getColor(android.R.color.white));
                } else {
                    sAttr1Text.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                }
                break;
            case Constants.DEVICE_SIREN:
                if (securityLevel.equals(Constants.SECURITY_ARM)) {
                    sAttr1Text.setText("Armed");
                } else if (securityLevel.equals(Constants.SECURITY_PARM1)) {
                    sAttr1Text.setText("Partial 1 Armed");
                } else if (securityLevel.equals(Constants.SECURITY_PARM2)) {
                    sAttr1Text.setText("Partial 2 Armed");
                } else if (securityLevel.equals(Constants.SECURITY_DISARM)) {
                    sAttr1Text.setText("Disarmed");
                }
                break;
            default:
                break;
        }
    }

    private void configureViews() {
        try {
            switch (deviceType) {
                case Constants.DEVICE_PLUG:
                    sDeviceImage.setImageDrawable(getResources().getDrawable(R.drawable.picto_plug_on));
                    sBatteryText.setVisibility(View.INVISIBLE);
                    sBatteryImage.setVisibility(View.VISIBLE);
                    sPowerSwitch.setVisibility(View.VISIBLE);
                    sAttr1Text.setVisibility(View.VISIBLE);
                    sAttr2Text.setVisibility(View.INVISIBLE);
                    break;
                case Constants.DEVICE_WALL_SWITCH:
                    sDeviceImage.setImageDrawable(getResources().getDrawable(R.drawable.picto_switch));
                    sBatteryText.setVisibility(View.VISIBLE);
                    sBatteryImage.setVisibility(View.VISIBLE);
                    sPowerSwitch.setVisibility(View.VISIBLE);
                    sAttr1Text.setVisibility(View.INVISIBLE);
                    sAttr2Text.setVisibility(View.INVISIBLE);
                    break;
                case Constants.DEVICE_MOTION:
                    sDeviceImage.setImageDrawable(getResources().getDrawable(R.drawable.picto_pir_sensor));
                    sBatteryText.setVisibility(View.VISIBLE);
                    sBatteryImage.setVisibility(View.VISIBLE);
                    sPowerSwitch.setVisibility(View.VISIBLE);
                    sAttr1Text.setVisibility(View.INVISIBLE);
                    sAttr2Text.setVisibility(View.INVISIBLE);
                    break;
                case Constants.DEVICE_FLOOD:
                    sDeviceImage.setImageDrawable(getResources().getDrawable(R.drawable.picto_flood));
                    sBatteryText.setVisibility(View.INVISIBLE);
                    sBatteryImage.setVisibility(View.INVISIBLE);
                    sPowerSwitch.setVisibility(View.GONE);
                    sAttr1Text.setVisibility(View.INVISIBLE);
                    sAttr2Text.setVisibility(View.INVISIBLE);
                    break;
                case Constants.DEVICE_MAGNETIC:
                    sDeviceImage.setImageDrawable(getResources().getDrawable(R.drawable.picto_door_magnetic_sensor));
                    sBatteryText.setVisibility(View.VISIBLE);
                    sBatteryImage.setVisibility(View.VISIBLE);
                    sPowerSwitch.setVisibility(View.GONE);
                    sAttr1Text.setVisibility(View.INVISIBLE);
                    sAttr2Text.setVisibility(View.INVISIBLE);
                    break;
                case Constants.DEVICE_THERMO:
                    sDeviceImage.setImageDrawable(getResources().getDrawable(R.drawable.picto_temperature));
                    sBatteryText.setVisibility(View.VISIBLE);
                    sBatteryImage.setVisibility(View.VISIBLE);
                    sPowerSwitch.setVisibility(View.GONE);
                    sAttr1Text.setVisibility(View.VISIBLE);
                    sAttr2Text.setVisibility(View.VISIBLE);
                    break;
                case Constants.DEVICE_SMOKE:
                    sDeviceImage.setImageDrawable(getResources().getDrawable(R.drawable.picto_smoke_sensor));
                    sBatteryText.setVisibility(View.VISIBLE);
                    sBatteryImage.setVisibility(View.VISIBLE);
                    sPowerSwitch.setVisibility(View.GONE);
                    sAttr1Text.setVisibility(View.VISIBLE);
                    sAttr2Text.setVisibility(View.INVISIBLE);
                    break;
                case Constants.DEVICE_KEYPAD:
                    sDeviceImage.setImageDrawable(getResources().getDrawable(R.drawable.picto_keypad));
                case Constants.DEVICE_REMOTE:
                    sDeviceImage.setImageDrawable(getResources().getDrawable(R.drawable.picto_remote_control));
                    sBatteryText.setVisibility(View.VISIBLE);
                    sBatteryImage.setVisibility(View.VISIBLE);
                    sPowerSwitch.setVisibility(View.GONE);
                    sAttr1Text.setVisibility(View.INVISIBLE);
                    sAttr2Text.setVisibility(View.INVISIBLE);
                    break;
                case Constants.DEVICE_SIREN:
                    sDeviceImage.setImageDrawable(getResources().getDrawable(R.drawable.picto_siren));
                    sBatteryText.setVisibility(View.VISIBLE);
                    sBatteryImage.setVisibility(View.VISIBLE);
                    sPowerSwitch.setVisibility(View.VISIBLE);
                    sAttr1Text.setVisibility(View.VISIBLE);
                    sAttr2Text.setVisibility(View.INVISIBLE);
                    break;
                default:
                    break;
            }
            if(alertMessage.isEmpty()) {
                sAlertLogButton.setVisibility(View.GONE);
            } else {
                sAlertLogButton.setVisibility(View.VISIBLE);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void initViews(View view) {
        sNameEditText = (EditText)view.findViewById(R.id.et_name);
        sNameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(v.getId() == R.id.et_name && !hasFocus) {
                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });
        sPowerSwitch = (Switch)view.findViewById(R.id.switch_toggle);
        sPowerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(sNameEditText.getWindowToken(), 0);
                if (isChecked) {
                    sendMessageToConsole("." + deviceType + " on", false);
                } else {
                    sendMessageToConsole("." + deviceType + " off", false);
                }
            }
        });
        sBatteryText = (TextView)view.findViewById(R.id.tv_battery_level);
        sBatteryImage = (ImageView)view.findViewById(R.id.iv_battery);
        sDeviceImage = (ImageView)view.findViewById(R.id.img_device);
        sAttr1Text = (TextView)view.findViewById(R.id.tv_attr1);
        sAttr2Text = (TextView)view.findViewById(R.id.tv_attr2);
        sAlertLogButton = (Button)view.findViewById(R.id.btn_alert);
    }

    private void getDeviceParams(Bundle args) {
        deviceId = args.getInt(Constants.JSON_ID, 0);
        deviceType = args.getString(Constants.JSON_TYPE);
        deviceName = args.getString(Constants.JSON_NAME);
        alertMessage = args.getString(Constants.ARGS_ALERT_BODY, "");
        alertTime = args.getString(Constants.ARGS_ALERT_TIME, "");
        switch(deviceType) {
            case Constants.DEVICE_PLUG:
                plugWatt = args.getInt(Constants.JSON_WATT, 0);
                devicePower = args.getBoolean(Constants.JSON_POWER, false);
                break;
            case Constants.DEVICE_WALL_SWITCH:
                devicePower = args.getBoolean(Constants.JSON_POWER, false);
                deviceBattery = args.getInt(Constants.JSON_BATT_LEVEL, 0);
                break;
            case Constants.DEVICE_MOTION:
                devicePower = args.getBoolean(Constants.JSON_POWER, false);
                deviceBattery = args.getInt(Constants.JSON_BATT_LEVEL, 0);
                break;
            case Constants.DEVICE_FLOOD:
                break;
            case Constants.DEVICE_MAGNETIC:
                deviceBattery = args.getInt(Constants.JSON_BATT_LEVEL, 0);
                break;
            case Constants.DEVICE_THERMO:
                valueTemp = args.getDouble(Constants.JSON_TEMP);
                valueHumidity = args.getInt(Constants.JSON_HUMIDITY, 0);
                deviceBattery = args.getInt(Constants.JSON_BATT_LEVEL, 0);
                break;
            case Constants.DEVICE_SMOKE:
                deviceBattery = args.getInt(Constants.JSON_BATT_LEVEL, 0);
                break;
            case Constants.DEVICE_SIREN:
                devicePower = args.getBoolean(Constants.JSON_POWER, false);
                securityLevel = args.getString(Constants.JSON_SECURITY);
                break;
            case Constants.DEVICE_REMOTE:
            case Constants.DEVICE_KEYPAD:
                deviceBattery = args.getInt(Constants.JSON_BATT_LEVEL, 0);
                break;
            default:
                break;
        }
    }

    public void updateContent(GUIItem item) {
        if(deviceId == item.getId() && deviceType.equals(item.getType())) {
            String name = item.getName();
            if(name != deviceName) {
                deviceName = name;
                sNameEditText.setText(deviceName);
            }
            boolean power = false;
            int battery = 0;
            switch(deviceType) {
                case Constants.DEVICE_PLUG:
                    int watt = item.getWatt();
                    if(watt != plugWatt) {
                        plugWatt = item.getWatt();
                    }
                    power = item.getPower();
                    if(power != devicePower) {
                        devicePower = item.getPower();
                        sPowerSwitch.setChecked(devicePower);
                    }
                    break;
                case Constants.DEVICE_WALL_SWITCH:
                    power = item.getPower();
                    if(power != devicePower) {
                        devicePower = power;
                        sPowerSwitch.setChecked(devicePower);
                    }
                    battery = item.getBattery();
                    if(battery != deviceBattery) {
                        deviceBattery = battery;
                        setBattery();
                    }
                    break;
                case Constants.DEVICE_MOTION:
                    power = item.getPower();
                    if(power != devicePower) {
                        devicePower = power;
                        sPowerSwitch.setChecked(devicePower);
                    }
                    battery = item.getBattery();
                    if(battery != deviceBattery) {
                        deviceBattery = battery;
                        setBattery();
                    }
                    break;
                case Constants.DEVICE_FLOOD:
                    break;
                case Constants.DEVICE_MAGNETIC:
                    battery = item.getBattery();
                    if(battery != deviceBattery) {
                        deviceBattery = battery;
                        setBattery();
                    }
                    break;
                case Constants.DEVICE_THERMO:
                    boolean changed = false;
                    double temp = item.getTemp();
                    if(temp != valueTemp) {
                        valueTemp = temp;
                        changed = true;
                    }
                    int hum = item.getHumidity();
                    if(hum != valueHumidity) {
                        valueHumidity = hum;
                        changed = true;
                    }
                    if(changed) {
                        setThermoDisplay(valueTemp, valueHumidity);
                    }
                    battery = item.getBattery();
                    if(battery != deviceBattery) {
                        deviceBattery = battery;
                        setBattery();
                    }
                    break;
                case Constants.DEVICE_SMOKE:
                    //smokeAttached = args.getBoolean(SweetHome.JSON_ATTACHED, false);
                    battery = item.getBattery();
                    if(battery != deviceBattery) {
                        deviceBattery = battery;
                        setBattery();
                    }
                    break;
                case Constants.DEVICE_SIREN:
                    power = item.getPower();
                    if(power != devicePower) {
                        devicePower = power;
                        sPowerSwitch.setChecked(devicePower);
                    }
                    String security = item.getSecurity();
                    if(!security.equals(securityLevel)) {
                        securityLevel = security;
                        String displayText = parseSecurityText(securityLevel);
                        if(displayText.isEmpty()) {
                            sAttr1Text.setVisibility(View.GONE);
                        } else {
                            sAttr1Text.setVisibility(View.VISIBLE);
                            sAttr1Text.setText(displayText);
                        }
                    }
                    break;
                case Constants.DEVICE_REMOTE:
                case Constants.DEVICE_KEYPAD:
                    battery = item.getBattery();
                    if(battery != deviceBattery) {
                        deviceBattery = battery;
                        setBattery();
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
