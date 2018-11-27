package com.internalpositioning.find3.find3app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class PageaddActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pageadd);
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

        Map<String, String> locmap = new HashMap<>();
        String loc1 = ((EditText)findViewById(R.id.nb1loc1)).getText().toString() ;
        String loc2 = ((EditText)findViewById(R.id.nb2loc2)).getText().toString()  ;
        String loc3 = ((EditText)findViewById(R.id.nb3loc3)).getText().toString()  ;
        String dis1 = ((EditText)findViewById(R.id.nb1dis1)).getText().toString() ;
        String dis2 = ((EditText)findViewById(R.id.nb2dis2)).getText().toString() ;
        String dis3 = ((EditText)findViewById(R.id.nb3dis3)).getText().toString() ;

        locmap.put(loc1,dis1);
        locmap.put(loc2,dis2);
        locmap.put(loc3,dis3);

        SharedPreferences pref = PageaddActivity.this.getPreferences(Context.MODE_PRIVATE);

        String name = pref.getString("familyName", "");
        Log.i("PageaddActivity", "name is " + name);
        Map<String, String> arguments = new HashMap<>();
        arguments.put("name", name);
        final Map<String, String> familyName = arguments;
        Answer(familyName);
        Answer(locmap);


        Button button = (Button) findViewById(R.id.btadd);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //显示方式声明Intent，直接启动SecondActivity
                // connectWithHttpURLConnectionPOST(message);



            }
        });
    }

    private void Answer(final Map <String, String> mes) {

        try {
            JSONObject response = null;

            //SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(AcceptedActivity.this);
            //String serveurPhp = sharedPref.getString("pref_key_server_ip", "http://192.168.1.99/omega/");
            //String url = serveurPhp + "AccepterAlerte.php";
            String url = "http://192.168.1.40/Omega/Findnearest/SetingDeleteAll.php";
            SendToServer sendAnswer = new SendToServer(mes, url);
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
