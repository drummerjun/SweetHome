package com.evsp.sweethome;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class FragmentLock extends Fragment implements View.OnClickListener {
    private ImageButton armButton;
    private ImageButton parm1Button;
    private ImageButton parm2Button;
    private ImageButton disarmButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lock, container, false);

        armButton = (ImageButton)view.findViewById(R.id.arm);
        parm1Button = (ImageButton)view.findViewById(R.id.parm1);
        parm2Button = (ImageButton)view.findViewById(R.id.parm2);
        disarmButton = (ImageButton)view.findViewById(R.id.disarm);
        armButton.setOnClickListener(this);
        parm1Button.setOnClickListener(this);
        parm2Button.setOnClickListener(this);
        disarmButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.lock).toUpperCase());
        SharedPreferences prefs = getActivity().getSharedPreferences(Constants.PREF_KEY, Context.MODE_PRIVATE);
        String state = prefs.getString(Constants.JSON_SECURITY, Constants.SECURITY_DISARM);
        setIconImage(state);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onClick(View v) {
        String input = "";
        String state = "";
        if(v == armButton) {
            state = Constants.SECURITY_ARM;
            input = "";
        } else if(v == parm1Button) {
            state = Constants.SECURITY_PARM1;
            input = "";
        } else if(v == parm2Button) {
            state = Constants.SECURITY_PARM2;
            input = "";
        } else if(v == disarmButton) {
            state = Constants.SECURITY_DISARM;
            input = "";
        } else {
            return;
        }
        setIconImage(state);

        SharedPreferences prefs = getActivity().getSharedPreferences(Constants.PREF_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.JSON_SECURITY, state);
        editor.apply();

        Intent intent = new Intent(Constants.ACTION_SEND_MESSAGE);
        intent.putExtra(Constants.MESSAGE, input);
        intent.putExtra(Constants.SAVED, false);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
    }

    private void setIconImage(String state) {
        armButton.setImageDrawable(getResources().getDrawable(R.drawable.btn_security_arm_off));
        parm1Button.setImageDrawable(getResources().getDrawable(R.drawable.btn_security_partial_1_off));
        parm2Button.setImageDrawable(getResources().getDrawable(R.drawable.btn_security_partial_2_off));
        disarmButton.setImageDrawable(getResources().getDrawable(R.drawable.btn_security_disarm_off));
        switch(state) {
            case Constants.SECURITY_ARM:
                armButton.setImageDrawable(getResources().getDrawable(R.drawable.btn_security_arm_on));
                break;
            case Constants.SECURITY_PARM1:
                parm1Button.setImageDrawable(getResources().getDrawable(R.drawable.btn_security_partial_1_on));
                break;
            case Constants.SECURITY_PARM2:
                parm2Button.setImageDrawable(getResources().getDrawable(R.drawable.btn_security_partial_2_on));
                break;
            case Constants.SECURITY_DISARM:
                disarmButton.setImageDrawable(getResources().getDrawable(R.drawable.btn_security_disarm_on));
                break;
            default:
                break;
        }
    }
}