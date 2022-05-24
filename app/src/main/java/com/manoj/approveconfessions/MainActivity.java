package com.manoj.approveconfessions;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class MainActivity extends AppCompatActivity {

    public static final String SHARED_PREFS = loginActivity.SHARED_PREFS;
    public static final String EMAIL_KEY = loginActivity.EMAIL_KEY;
    public static final String PASSWORD_KEY = loginActivity.PASSWORD_KEY;
    public static final String SESSION_KEY = loginActivity.SESSION_KEY;
    public static final String url = loginActivity.url;

    // variable for shared preferences.
    SharedPreferences sharedpreferences;
    String email, password, session;

    private void expireSession() {
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(EMAIL_KEY, null);
        editor.putString(PASSWORD_KEY, null);
        editor.putString(SESSION_KEY, null);
        editor.apply();

        // starting new activity.
        Intent i = new Intent(MainActivity.this, loginActivity.class);
        Toast.makeText(MainActivity.this, "Session expired", Toast.LENGTH_SHORT).show();
        startActivity(i);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initializing our shared preferences.
        sharedpreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);

        // getting data from shared prefs and
        // storing it in our string variable.
        email = sharedpreferences.getString(EMAIL_KEY, null);
        password = sharedpreferences.getString(PASSWORD_KEY, null);
        session = sharedpreferences.getString(SESSION_KEY, null);


        RequestQueue queue = Volley.newRequestQueue(this);

        if(session == null)
        {
            expireSession();
        }
//        loadingProgressBar.setVisibility(View.VISIBLE);
        String req = url+"/verify?session_id="+session;
        StringRequest sr = new StringRequest(Request.Method.POST, req, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
//                loadingProgressBar.setVisibility(View.GONE);
                if(response.equals("0"))
                {
                    expireSession();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Error while verifying session id", Toast.LENGTH_SHORT).show();
            }
        }
        );
        queue.add(sr);
    }
}