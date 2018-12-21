/*package com.internalpositioning.find3.find3app;
// former version, the delete operation in the second page
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class DeleteLocationActivity extends AppCompatActivity {

    private Button deleteOneButton,deleteAllButton;

    //public Handler handler = new Handler(){};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_location);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        deleteOneButton = findViewById(R.id.btdelete);
        deleteOneButton.setOnClickListener(new View.OnClickListener(){

            public void onClick(View v) {
                // connectWithHttpURLConnectionPOST(message);
                Intent intent = getIntent();
                final String message1 = intent.getStringExtra("family");
                final String message2 = intent.getStringExtra("location");
                Map<String, String> deleteone = new HashMap<>();
                deleteone.put("family",message1);
                deleteone.put("location",message2);
                SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
                String ServerUrl = prefs.getString("serverurl", "none");
                String url = "http://"+ ServerUrl +"/Omega/Findnearest/SettingDeleteOne.php";
                Answer(deleteone,url);
            }
        });
        deleteAllButton = findViewById(R.id.btreset);
        deleteAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNotification();
            }
        });
    }
    private void showNotification() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DeleteLocationActivity.this);
        builder.setTitle("notice");
        builder.setMessage("Are you sure to clear all the locations");
        builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = getIntent();
                SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
                String ServerUrl = prefs.getString("serverurl", "none");
                String url = "http://"+ ServerUrl +"/Omega/Findnearest/SetingDeleteAll.php";
               // SharedPreferences pref = DeleteLocationActivity.this.getPreferences(Context.MODE_PRIVATE);
                //String message = pref.getString("familyName", "");
               final String message = intent.getStringExtra("family");
                Map<String, String> arguments = new HashMap<>();
                arguments.put("family", message);
                final Map<String, String> finalArguments = arguments;
                Answer(finalArguments,url);

            }
        });
        builder.setNegativeButton("no", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), "not clear", Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }

    private void Answer(final Map<String,String> mess, String url) {
        try {
            JSONObject response = null;
            SendToServer sendAnswer = new SendToServer(mess, url);
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


*/