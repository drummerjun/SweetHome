package com.evsp.sweethome;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.evsp.sweethome.services.TCPClient;

import static android.widget.CompoundButton.OnCheckedChangeListener;

public class SignInActivity extends Activity {
    private static final String TAG = SignInActivity.class.getSimpleName();
    private EditText emailET;
    private EditText passwordET;
    private CheckBox showPwCB;
    private EditText gatewayET;
    private Button scanButton;
    private Button signInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        emailET = (EditText)findViewById(R.id.editText);
        passwordET = (EditText)findViewById(R.id.editText2);
        showPwCB = (CheckBox)findViewById(R.id.showPassWordCheckbox);
        gatewayET = (EditText)findViewById(R.id.editText3);
        scanButton = (Button)findViewById(R.id.scanButton);
        signInButton = (Button)findViewById(R.id.button);

        showPwCB.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(showPwCB.isChecked()) {
                    passwordET.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    passwordET.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
            }
        });

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailET.getText().toString();
                String password = passwordET.getText().toString();
                TCPClient client = ((SweetHome)getApplication()).getClient();
                client.sendMessage(-1, constructSignInCredentialsJSONString(email, password));
            }
        });
    }

    private String constructSignInCredentialsJSONString(String email, String password) {
        String json = "url:/\"login\",data:JSON.stringify({l:" +
                email.trim() + ",p:" + password + "}),type:\"POST\",datatype:\"json\"";
        return json;
    }
}
