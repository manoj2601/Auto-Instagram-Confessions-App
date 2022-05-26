package com.manoj.approveconfessions;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class loginActivity extends AppCompatActivity {

    // shared preferences
    public static final String SHARED_PREFS = "shared_prefs";
    public static final String EMAIL_KEY = "email_key";
    public static final String PASSWORD_KEY = "password_key";
    public static final String SESSION_KEY = "session_key";
    public static final String url = "http://44.193.207.241/";

    SharedPreferences sharedpreferences;
    String email, password, session;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initializing EditTexts and our Button
        EditText usernameEdt = findViewById(R.id.username);
        EditText passwordEdt = findViewById(R.id.password);
        Button loginBtn = findViewById(R.id.idBtnLogin);
        ProgressBar loadingProgressBar = findViewById(R.id.loading);
        // getting the data which is stored in shared preferences.
        sharedpreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);

        // in shared prefs inside het string method
        // we are passing key value as EMAIL_KEY and
        // default value is
        // set to null if not present.
        email = sharedpreferences.getString(EMAIL_KEY, null);
        password = sharedpreferences.getString(PASSWORD_KEY, null);
        session = sharedpreferences.getString(SESSION_KEY, null);

        RequestQueue queue = Volley.newRequestQueue(this);


        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // to check if the user fields are empty or not.
                if (TextUtils.isEmpty(usernameEdt.getText().toString()) && TextUtils.isEmpty((passwordEdt.getText().toString()))) {
                    // this method will call when email and password fields are empty.
                    Toast.makeText(loginActivity.this, "Please Enter Email and Password", Toast.LENGTH_SHORT).show();
                } else {
                    loadingProgressBar.setVisibility(View.VISIBLE);
                    String req = url+"/login?username="+usernameEdt.getText().toString()+"&password="+passwordEdt.getText().toString();
                    StringRequest sr = new StringRequest(Request.Method.POST, req, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            loadingProgressBar.setVisibility(View.GONE);
                            if(!response.equals("0"))
                            {
                                SharedPreferences.Editor editor = sharedpreferences.edit();

                                // below two lines will put values for
                                // email and password in shared preferences.
                                editor.putString(EMAIL_KEY, usernameEdt.getText().toString());
                                editor.putString(PASSWORD_KEY, passwordEdt.getText().toString());
                                editor.putString(SESSION_KEY, response);

                                // to save our data with key and value.
                                editor.apply();

                                // starting new activity.
                                Intent i = new Intent(loginActivity.this, MainActivity.class);
//                                Toast.makeText(loginActivity.this, "Signed in SESSION ID : "+response, Toast.LENGTH_SHORT).show();
                                startActivity(i);
                                finish();
                            }
                            else {
                                Toast.makeText(loginActivity.this, "Incorrect username or Password", Toast.LENGTH_SHORT).show();

                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(loginActivity.this, "Error while receiving response in login", Toast.LENGTH_SHORT).show();
                        }
                    }
                    );
                    queue.add(sr);

                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(email != null && password != null && session != null) {
            Intent i = new Intent(loginActivity.this, MainActivity.class);
            startActivity(i);
        }
    }
}