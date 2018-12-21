package com.internalpositioning.find3.find3app;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class PageaddActivity extends AppCompatActivity implements View.OnClickListener {

    private Map<String, String> loc_map;
    private JSONArray listlocations ;
    private LinearLayout allcontent;
    boolean flag = false;
    //jsonarray jsonobject used for saving and sending message to server
    JSONArray jsonArray = new JSONArray();
    JSONObject jsonObject = new JSONObject();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pageadd);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        String family = intent.getStringExtra("family");
        String location = intent.getStringExtra("location");

        loc_map = new HashMap<>();
        loc_map.put("family", family);
        //loc_map.put("location", location);
        Map<String, String> Family = loc_map;

        //return all the locations which are saved in the find server
        listlocations = AllLocations(Family);
        try {
        /*    if (listlocations == null){
                validLocation();
            }
            else{
            for(int i =0;i<listlocations.length();i++){
                if (listlocations.getString(i).equals(location)){
                    flag = true;
                    break;
                }
            }
            if (flag = false){
                validLocation();
            }
        }*/
        jsonObject.put("family", family);
        jsonObject.put("location", location);
        // This page is a scrollview, inside the scrollview, there is a linearview and a button send, inside the linearview, there are several same item,
        // if we want to add item, first we need to find the linearview, here "allcontent" is the linearview contains all items
        allcontent = findViewById(R.id.list_view);
        findViewById(R.id.buttonSend).setOnClickListener(this) ;
       // as long as we open this page, one item will be created
        addViewItem(null);

        }catch(JSONException e){
            e.printStackTrace();
        }

    }
    // button listener, if it is "addanother", add a new item, if it is "send", send the information to server
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.Addanother:
                addViewItem(v);
                break;
            case R.id.buttonSend:
                sendMessage(v);
                break;
        }

    }

  /*  public  void validLocation(){
        AlertDialog.Builder builder = new AlertDialog.Builder(PageaddActivity.this);
        builder.setTitle("notice");
        builder.setMessage(R.string.EnterLocation);
        builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PageaddActivity.this.finish();
            }
        });
        builder.show();
    }*/

    //inside each item, there is a spinner, which shows all the locations already been saved
    private void addViewItem(View view) {
            View onecontent = View.inflate(this, R.layout.content_item, null);
            Button btn_add = onecontent.findViewById(R.id.Addanother);
            allcontent.addView(onecontent);
            final Spinner spinner = onecontent.findViewById(R.id.allLocation);
            final TextView tv = onecontent.findViewById(R.id.Enterloc);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
      /*  try {
            //listlocations = AllLocations(FamilyAndLoc); this function returns the listlocations
            for (int i = 0; i < listlocations.length(); i++){
                adapter.add(listlocations.getString(i));}
        }catch(JSONException e){
            e.printStackTrace();
        }*/
        try {
       /* if(listlocations.length()==0)
            adapter.add(null); */
            for (int i = 0; i < listlocations.length(); i++){
                adapter.add(listlocations.getString(i));
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        // When we click at one option in the list of spinner, it will be showed automatically in the "EnterLoc"
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String str = (String) spinner.getSelectedItem();
                // how we get the tv : final TextView tv = onecontent.findViewById(R.id.Enterloc);
                tv.setText(str);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
            btn_add.setOnClickListener(this);
        }

    //send json object to adddistance.php
    public void sendMessage(View view) {
    try {
        for(int i =0; i<allcontent.getChildCount();i++){
            View OneLocAndDis = allcontent.getChildAt(i);
            String loc = ((EditText)OneLocAndDis.findViewById(R.id.Enterloc)).getText().toString();
            Log.i("deBugloc",loc);
            String dis = ((EditText)OneLocAndDis.findViewById(R.id.Enterdis)).getText().toString();
            Log.i("deBugdis",dis);
            JSONObject jsonlocDis = new JSONObject();
            jsonlocDis.put("loc",loc);
            jsonlocDis.put("dis",dis);
            jsonArray.put(jsonlocDis);
           /* loc_map.put("loc"+i, loc);
            loc_map.put("dis"+i, dis);*/
        }
        /*for debug
        int length = jsonArray.length();
        Log.i("deBuglength", Integer.toString(length));
        jsonObject.put("nearby",jsonArray);*/
        final JSONObject info = jsonObject;
        Answer(info) ;
    } catch(JSONException e){
        e.printStackTrace();
    }
    /*    String loc2 = ((EditText) findViewById(R.id.Enterloc2)).getText().toString();
        String dis2 = ((EditText) findViewById(R.id.Enterdis2)).getText().toString();
        String loc3 = ((EditText) findViewById(R.id.Enterloc3)).getText().toString();
        String dis3 = ((EditText) findViewById(R.id.Enterdis3)).getText().toString();*/
     /*   loc_map.put("loc2", loc2);
        loc_map.put("dis2", dis2);
        loc_map.put("loc3", loc3);
        loc_map.put("dis3", dis3);*/

        // Do something in response to button
    }

    private void Answer(JSONObject mes) {
        try {
            SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
            String ServerUrl = prefs.getString("serverurl", "none");
            String url = "http://"+ ServerUrl +"/Omega/Findnearest/AddDistance.php";
            SendToServerJson sendAnswerjson = new SendToServerJson(mes, url);
            JSONObject response = new JSONObject(sendAnswerjson.execute().get());
            final JSONObject finalResponse = response;
            if (finalResponse.getString("message").equals("fini"))
                Toast.makeText(getApplicationContext(), R.string.done, Toast.LENGTH_SHORT).show();
            } catch (InterruptedException e) {
                    e.printStackTrace();
            } catch (ExecutionException e) {
                    e.printStackTrace();
            } catch (JSONException e) {
                    e.printStackTrace();
            }
    }

   //return all the locations already been saved
    private JSONArray AllLocations(final Map<String, String> mes) {
        try {
            SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
            String ServerUrl = prefs.getString("serverurl", "");
            String url = "http://" + ServerUrl + "/Omega/Findnearest/AllLocationExits.php";
            SendToServer sendAnswer = new SendToServer(mes, url);
            JSONArray response = new JSONArray(sendAnswer.execute().get());
            return response;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

    }



}



