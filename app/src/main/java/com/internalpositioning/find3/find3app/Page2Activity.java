package com.internalpositioning.find3.find3app;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Page2Activity extends AppCompatActivity {

    private Button button1,button3;

    public Handler handler = new Handler(){};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ////////////////////////////////////////////////////////////////////15112018
       // Intent intent = getIntent();
       // final String message = intent.getStringExtra("family");

        button1 = (Button) findViewById(R.id.bt1);
        button1.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                //显示方式声明Intent，直接启动SecondActivity
                // connectWithHttpURLConnectionPOST(message);
                Intent intent = new Intent(Page2Activity.this, PageaddActivity.class);
                startActivity(intent);
            }
        });



        button3 = (Button) findViewById(R.id.bt3);

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //显示方式声明Intent，直接启动SecondActivity
               // connectWithHttpURLConnectionPOST(message);
                showNotification();
            }
        });
        //////////////////////////////////////////////////////////////////
    }
    private void showNotification() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Page2Activity.this);

        builder.setTitle("notice");
        builder.setMessage("Are you sure to clear all the locations");
        builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = getIntent();
                final String message = intent.getStringExtra("family");
                Answer(message);


            }
        });
        builder.setNegativeButton("no", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), "not clear", Toast.LENGTH_SHORT).show();

            }
        });
        builder.show();


    }

    private void Answer(final String message) {
        Log.i("debug", message);
        Map<String, String> arguments = new HashMap<>();
        arguments.put("family", message);

        final Map<String, String> finalArguments = arguments;
        try {
            JSONObject response = null;

            //SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(AcceptedActivity.this);
            //String serveurPhp = sharedPref.getString("pref_key_server_ip", "http://192.168.1.99/omega/");
            //String url = serveurPhp + "AccepterAlerte.php";
            String url = "http://192.168.1.40/Omega/Findnearest/SetingDeleteAll.php";
            SendToServer sendAnswer = new SendToServer(finalArguments, url);
            response = new JSONObject(sendAnswer.execute().get());

            final JSONObject finalResponse = response;

            if (finalResponse.getString("message").equals("done"))
                Toast.makeText(getApplicationContext(), "already clear", Toast.LENGTH_SHORT).show();


        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }



}
