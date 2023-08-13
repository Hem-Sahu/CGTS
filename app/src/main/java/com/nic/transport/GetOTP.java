package com.nic.transport;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.commons.lang3.StringUtils;

import cz.msebera.android.httpclient.Header;


public class GetOTP extends AppCompatActivity {
    Button submit;
    EditText mobile,password;
    TextView tv_register;
    SharedPreferenceData sharedPreferenceData;
    ProgressDialog progressDialog;
    @SuppressLint({"ResourceAsColor", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sendotp);
        //reference declaration
        submit=findViewById(R.id.submit_btn);
        mobile= findViewById(R.id.mobile_no);
        password = findViewById(R.id.password);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please wait...");
        progressDialog.setTitle("Loading");
        tv_register=findViewById(R.id.tv_register);
        sharedPreferenceData = new SharedPreferenceData(this);
      //  submit.setOnClickListener(view->{
      //  otp.setVisibility(View.VISIBLE);
      //  submitOTP.setVisibility(View.VISIBLE);
       // mobile.setEnabled(false);
     //   submit.setEnabled(false);
      //  submit.setBackgroundColor(R.color.button_bg_disabled);

      //  });

        submit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mobile.getText().toString().trim().equals("")){
                    Toast.makeText(GetOTP.this, "Enter mobile number", Toast.LENGTH_SHORT).show();
                } else if (mobile.getText().toString().trim().length() != 10) {
                    Toast.makeText(GetOTP.this, "Enter valid mobile number", Toast.LENGTH_SHORT).show();
                } else if (password.getText().toString().trim().equals("")) {
                    Toast.makeText(GetOTP.this, "Enter Password", Toast.LENGTH_SHORT).show();
                }else {
                    GetLoginDetails();
                }
            }
        });


    }



    public void openNewActivity(){
        startActivity(new Intent(GetOTP.this, DashboardActivity.class));
        finishAffinity();
    }

    private void GetLoginDetails() {
        progressDialog.show();
        RequestParams params = new RequestParams();
        params.put("Mobile",mobile.getText().toString());
        params.put("Password",password.getText().toString());
        //  params.put("RegId", mResultEt.getText().toString().replaceAll(" ","").trim());
        new AsyncHttpClient().get(ServiceManager.UserLogin, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                progressDialog.dismiss();
                String responseStr = new String(responseBody);
                // Toast.makeText(OTPVerification.this, ""+responseStr, Toast.LENGTH_SHORT).show();
                String Authentication = StringUtils.substringBetween(responseStr, ">\"", "\"<");
                if(Authentication.equals("Authenticated")){
                    sharedPreferenceData.setMobile("mobile",mobile.getText().toString());
                    openNewActivity();
                }
                else {
                    Toast.makeText(GetOTP.this, ""+Authentication, Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                progressDialog.dismiss();
                Toast.makeText(GetOTP.this, "Connection Failed Try Again Later", Toast.LENGTH_LONG).show();
            }
        });
    }

}