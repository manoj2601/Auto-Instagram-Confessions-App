package com.manoj.approveconfessions;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    public static final String SHARED_PREFS = loginActivity.SHARED_PREFS;
    public static final String EMAIL_KEY = loginActivity.EMAIL_KEY;
    public static final String PASSWORD_KEY = loginActivity.PASSWORD_KEY;
    public static final String SESSION_KEY = loginActivity.SESSION_KEY;
    public static final String url = loginActivity.url;

    // variable for shared preferences.
    SharedPreferences sharedpreferences;
    String username, password, session;

    private void expireSession() {
        RequestQueue queue = Volley.newRequestQueue(this);
        ProgressBar loadingProgressBar = findViewById(R.id.loading);
        loadingProgressBar.setVisibility(View.VISIBLE);
        String req = url+"/logout?session_id="+session;
        StringRequest sr = new StringRequest(Request.Method.POST, req, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                loadingProgressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Logged Out", Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Error while logging out", Toast.LENGTH_SHORT).show();
            }
        }
        );
        queue.add(sr);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.clear();
        editor.apply();

        // starting new activity.
        Intent i = new Intent(MainActivity.this, loginActivity.class);

        startActivity(i);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // calling the action bar
        ActionBar actionBar = getSupportActionBar();

        // showing the back button in action bar
        actionBar.setDisplayHomeAsUpEnabled(true);


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initializing our shared preferences.
        sharedpreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);

        // getting data from shared prefs and
        // storing it in our string variable.
        username = sharedpreferences.getString(EMAIL_KEY, null);
        password = sharedpreferences.getString(PASSWORD_KEY, null);
        session = sharedpreferences.getString(SESSION_KEY, null);

        TextView welcomeTV = findViewById(R.id.idTVWelcome);
        TextView fetchTV = findViewById(R.id.fetchTV);
        TextView entry = findViewById(R.id.entry);
        RequestQueue queue = Volley.newRequestQueue(this);
        welcomeTV.setText("Welcome " + username);
        Button logoutBtn = findViewById(R.id.idBtnLogout);
        Button fetchBtn = findViewById(R.id.fetch);
        Button fetchSkippedBtn = findViewById(R.id.fetchSkipped);
        ProgressBar loadingProgressBar = findViewById(R.id.loading);

        Button approve = findViewById(R.id.approve);
        Button decline = findViewById(R.id.decline);
        Button skip = findViewById(R.id.skip);

        final Boolean[] wasSkipped = {false};
        final String[] id = {"0"};

        entry.setVisibility(View.GONE);
        approve.setVisibility(View.GONE);
        decline.setVisibility(View.GONE);
        skip.setVisibility(View.GONE);
        loadingProgressBar.setVisibility(View.GONE);

        if(session == null)
        {
            expireSession();
        }
        loadingProgressBar.setVisibility(View.VISIBLE);
        String req = url+"/verify?session_id="+session;
        StringRequest sr = new StringRequest(Request.Method.POST, req, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                loadingProgressBar.setVisibility(View.GONE);
                if(response.equals("0"))
                {
                    Toast.makeText(MainActivity.this, "Session expired", Toast.LENGTH_SHORT).show();
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.clear();
                    editor.apply();

                    // starting new activity.
                    Intent i = new Intent(MainActivity.this, loginActivity.class);

                    startActivity(i);
                    finish();
                }
                else {
                    Toast.makeText(MainActivity.this, "Session verified", Toast.LENGTH_SHORT).show();
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
        fetchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                entry.setVisibility(View.GONE);
                approve.setVisibility(View.GONE);
                decline.setVisibility(View.GONE);
                skip.setVisibility(View.GONE);
                loadingProgressBar.setVisibility(View.VISIBLE);
                String req = url+"/getEntry?session_id="+session;
                JsonObjectRequest sr = new JsonObjectRequest(Request.Method.GET, req, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        loadingProgressBar.setVisibility(View.GONE);
                        String entryText = null;
                        try {
                            entryText = response.getString("text");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        String Id = null;
                        try {
                            Id = response.getString("id");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        entry.setText(entryText);
                        entry.setVisibility(View.VISIBLE);
                        if(Id.equals("-1"))
                        {
                            return;
                        }
                        approve.setVisibility(View.VISIBLE);
                        decline.setVisibility(View.VISIBLE);
                        skip.setVisibility(View.VISIBLE);
                        id[0] = Id;
                        wasSkipped[0] = false;
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "Some Error occured while fetching", Toast.LENGTH_SHORT).show();
                    }
                }
                );
                queue.add(sr);
            }
        });

        fetchSkippedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                entry.setVisibility(View.GONE);
                approve.setVisibility(View.GONE);
                decline.setVisibility(View.GONE);
                skip.setVisibility(View.GONE);
                loadingProgressBar.setVisibility(View.VISIBLE);
                String req = url+"/getEntrySkipped?session_id="+session;
                JsonObjectRequest sr = new JsonObjectRequest(Request.Method.GET, req, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        loadingProgressBar.setVisibility(View.GONE);
                        String entryText = null;
                        try {
                            entryText = response.getString("text");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        String Id = null;
                        try {
                            Id = response.getString("id");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        entry.setText(entryText);
                        entry.setVisibility(View.VISIBLE);
                        if(Id.equals("-1"))
                        {
                            return;
                        }
                        approve.setVisibility(View.VISIBLE);
                        decline.setVisibility(View.VISIBLE);
                        skip.setVisibility(View.VISIBLE);
                        id[0] = Id;
                        wasSkipped[0] = true;
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "Some Error occured", Toast.LENGTH_SHORT).show();
                    }
                }
                );
                queue.add(sr);
            }
        });

        approve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                entry.setVisibility(View.GONE);
                approve.setVisibility(View.GONE);
                decline.setVisibility(View.GONE);
                skip.setVisibility(View.GONE);
                loadingProgressBar.setVisibility(View.VISIBLE);
                String req = "";
                if(wasSkipped[0])
                {
                    req = url+"/getEntrySkipped?id="+ id[0] +"&status=approve&session_id="+session;
                }
                else {
                    req = url+"/getEntry?id="+ id[0] +"&status=approve&session_id="+session;
                }
                StringRequest sr = new StringRequest(Request.Method.POST, req, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(MainActivity.this, "Approved", Toast.LENGTH_SHORT).show();
                        if(wasSkipped[0])
                            fetchSkippedBtn.performClick();
                        else
                            fetchBtn.performClick();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "Some Error occured", Toast.LENGTH_SHORT).show();
                    }
                });
                sr.setRetryPolicy(new DefaultRetryPolicy(
                        0,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                queue.add(sr);
            }
        });

        decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                entry.setVisibility(View.GONE);
                approve.setVisibility(View.GONE);
                decline.setVisibility(View.GONE);
                skip.setVisibility(View.GONE);
                loadingProgressBar.setVisibility(View.VISIBLE);

                String req = "";
                if(wasSkipped[0])
                {
                    req = url+"/getEntrySkipped?id="+ id[0] +"&status=decline&session_id="+session;
                }
                else {
                    req = url+"/getEntry?id="+ id[0] +"&status=decline&session_id="+session;
                }
                StringRequest sr = new StringRequest(Request.Method.POST, req, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(MainActivity.this, "Declined", Toast.LENGTH_SHORT).show();
                        if(wasSkipped[0])
                            fetchSkippedBtn.performClick();
                        else
                            fetchBtn.performClick();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "Some Error occured", Toast.LENGTH_SHORT).show();
                    }
                });
                queue.add(sr);
            }
        });

        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                entry.setVisibility(View.GONE);
                approve.setVisibility(View.GONE);
                decline.setVisibility(View.GONE);
                skip.setVisibility(View.GONE);
                loadingProgressBar.setVisibility(View.VISIBLE);
                String req = "";
                if(wasSkipped[0])
                {
                    req = url+"/getEntrySkipped?id="+ id[0] +"&status=skip&session_id="+session;
                }
                else {
                    req = url+"/getEntry?id="+ id[0] +"&status=skip&session_id="+session;
                }
                StringRequest sr = new StringRequest(Request.Method.POST, req, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(MainActivity.this, "Skipped "+response, Toast.LENGTH_SHORT).show();
                        if(wasSkipped[0])
                            fetchSkippedBtn.performClick();
                        else
                            fetchBtn.performClick();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "Some Error occured", Toast.LENGTH_SHORT).show();
                    }
                });
                queue.add(sr);
            }
        });




        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expireSession();
            }
        });
    }

    //back button function
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                expireSession();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}