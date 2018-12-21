package com.internalpositioning.find3.find3app;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity {
    // logging
    private final String TAG = "MainActivity";
    // background manager
    private PendingIntent recurringLl24 = null;
    private Intent ll24 = null;
    AlarmManager alarms = null;
    WebSocketClient mWebSocketClient = null;
    Timer timer = null;
    private RemindTask oneSecondTimer = null;
    private String[] autocompleteLocations = new String[]{"bedroom", "living room", "kitchen", "bathroom", "office"};
    private SharedPreferences prefs;

    private Map<String, String> loc_map;

    @Override
    protected void onDestroy() {
        Log.d(TAG, "MainActivity onDestroy()");
        if (alarms != null) alarms.cancel(recurringLl24);
        if (timer != null) timer.cancel();
        if (mWebSocketClient != null) {
            mWebSocketClient.close();
        }
        android.app.NotificationManager mNotificationManager = (android.app.NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(0);
        Intent scanService = new Intent(this, ScanService.class);
        stopService(scanService);
        super.onDestroy();
    }

    class RemindTask extends TimerTask {
        private Integer counter = 0;

        public void resetCounter() {
            counter = 0;
        }

        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    counter++;
                    if (mWebSocketClient != null) {
                        if (mWebSocketClient.isClosed()) {
                            connectWebSocket();
                        }
                    }
                    TextView rssi_msg = (TextView) findViewById(R.id.textOutput);
                    String currentText = rssi_msg.getText().toString();
                    if (currentText.contains("ago: ")) {
                        String[] currentTexts = currentText.split("ago: ");
                        currentText = currentTexts[1];
                    }
                    rssi_msg.setText(counter + " seconds ago: " + currentText);
                }
            });
        }
    }

    //set the server address and residence name
    private void set() {
        final SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View serverfamily = View.inflate(this, R.layout.content_serverfamily, null);
        final EditText server = serverfamily.findViewById(R.id.EnterServer);
        final EditText residence = serverfamily.findViewById(R.id.EnterResidence);
        builder.setTitle(getString(R.string.settings));
        server.setText(prefs.getString("serverurl", ""));
        residence.setText(prefs.getString("residence", ""));
        builder.setView(serverfamily);
        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String serverInput = server.getText().toString();
                String ResidenceInput = residence.getText().toString();
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("serverurl", serverInput);
                edit.putString("residence", ResidenceInput);
                edit.apply();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = getSharedPreferences("settings", MODE_PRIVATE);

    /*    Button buttonDelete = findViewById(R.id.btdelete);
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DeleteLocationActivity.class);
                String family = prefs.getString("residence", "");
                //EditText editText = (EditText) findViewById(R.id.familyName);
                EditText editText = (EditText) findViewById(R.id.locationName);
                //String family = editText.getText().toString();
                String location = editText.getText().toString();
               intent.putExtra("family", family);
               intent.putExtra("location", location);
                startActivity(intent);
            }
        });*/

        Button buttonadd = findViewById(R.id.btadd);
        buttonadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //String family =((EditText) findViewById(R.id.familyName)).getText().toString();
                String family = prefs.getString("residence", "");
                loc_map = new HashMap<>();
                loc_map.put("family", family);
                //loc_map.put("location", location);
                Map<String, String> familyname = loc_map;
                JSONArray listlocations = AllLocations(familyname);
                String location = ((EditText) findViewById(R.id.locationName)).getText().toString();
                TextView rssi_msg = findViewById(R.id.textOutput);
                //Set the condition to set the distance
                //1. The residence name has been entered
                //2. Enter a location name
                //3. This location is already been saved in FIND server,
                // which means we had already did "scaning" for this location

                int finded = Locationexits (family,listlocations,location);
                //if the location is not found in the "listlocations", we request the user to enter
                //a valid location
                if (finded == 0) {
                    rssi_msg.setText(R.string.EnterLocation);
                } else {
                    Intent intent = new Intent(MainActivity.this, PageaddActivity.class);
                    intent.putExtra("family", family);
                    intent.putExtra("location", location);
                    startActivity(intent);
                }
            }
        });

        Button deleteAllButton = findViewById(R.id.btreset);
        deleteAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNotification();
            }
        });

        Button deleteOneButton = findViewById(R.id.btdelete);
        deleteOneButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               /* Intent intent = getIntent();
                final String message1 = intent.getStringExtra("family");
                final String message2 = intent.getStringExtra("location");*/
                String family = prefs.getString("residence", "");
                String location = ((EditText) findViewById(R.id.locationName)).getText().toString();
                Map<String, String> deleteone = new HashMap<>();
                deleteone.put("family", family);
                Map<String, String> familymap = deleteone;
                deleteone.put("location", location);
                String ServerUrl = prefs.getString("serverurl", "");
                String url = "http://" + ServerUrl + "/Omega/Findnearest/SettingDeleteOne.php";
                TextView rssi_msg = findViewById(R.id.textOutput);
                JSONArray listlocations = AllLocations(familymap);
                int finded = Locationexits (family,listlocations,location);
                if(finded ==0){
                    rssi_msg.setText(R.string.EnterLocation);
                }
                else{
                Answer(deleteone, url);
                }
            }
        });
        // check permissions
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WAKE_LOCK, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.ACCESS_WIFI_STATE}, 1);
        }
        TextView rssi_msg = (TextView) findViewById(R.id.textOutput);
        rssi_msg.setText("not running");

        AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.locationName);
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, autocompleteLocations);
        textView.setAdapter(adapter);

        // Button ButtonLearning = (Button) findViewById(R.id.learningButton);
        ToggleButton toggleButton2 = (ToggleButton) findViewById(R.id.toggleButton2);

        toggleButton2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                TextView rssi_msg = (TextView) findViewById(R.id.textOutput);
                rssi_msg.setText("not running");
                Log.d(TAG, "toggle set to false");
                if (alarms != null) alarms.cancel(recurringLl24);
                android.app.NotificationManager mNotificationManager = (android.app.NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.cancel(0);
                if (timer != null) timer.cancel();

                CompoundButton scanButton = (CompoundButton) findViewById(R.id.toggleButton);
                scanButton.setChecked(false);
            }
        });


        ToggleButton toggleButton = (ToggleButton) findViewById(R.id.toggleButton);
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //////////////////////////////////////ckecked means is scaning
                if (isChecked) {
                    TextView rssi_msg = (TextView) findViewById(R.id.textOutput);
                    String familyName = prefs.getString("residence", "");
                    // String familyName = ((EditText) findViewById(R.id.familyName)).getText().toString().toLowerCase();
                    if (familyName.equals("")) {
                        rssi_msg.setText(R.string.EnterResidence);
                        buttonView.toggle();
                        return;
                    }

                    // String serverAddress = ((EditText) findViewById(R.id.serverAddress)).getText().toString().toLowerCase();
                    String ServerUrl = prefs.getString("serverurl", "");
                   String serverAddress = "http://" + ServerUrl + ":8005";
                    //String serverAddress = "http://"+ ServerUrl;
                    if (ServerUrl.equals("")) {
                        rssi_msg.setText(R.string.EnterServer);
                        buttonView.toggle();
                        return;
                    }
                    String deviceName = ((EditText) findViewById(R.id.deviceName)).getText().toString().toLowerCase();
                    if (deviceName.equals("")) {
                        rssi_msg.setText(R.string.EnterDevice);
                        buttonView.toggle();
                        return;
                    }
                    boolean allowGPS = ((CheckBox) findViewById(R.id.allowGPS)).isChecked();
                    Log.d(TAG, "allowGPS is checked: " + allowGPS);
                    String locationName = ((EditText) findViewById(R.id.locationName)).getText().toString().toLowerCase();
                   /* if (locationName.equals("")) {
                        rssi_msg.setText("location name cannot be empty");
                        buttonView.toggle();
                        return;
                    }*/
                    //////////////////////////////////////////////
                    CompoundButton trackingButton = (CompoundButton) findViewById(R.id.toggleButton2);
                    if (trackingButton.isChecked() == false) {
                        locationName = "";
                    } else {
                        if (locationName.equals("")) {
                            rssi_msg.setText(R.string.EnterLocation);
                            buttonView.toggle();
                            return;
                        }
                    }
                    rssi_msg.setText("running");
                    // 24/7 alarm
                    ll24 = new Intent(MainActivity.this, AlarmReceiverLife.class);
                    Log.d(TAG, "setting familyName to [" + familyName + "]");
                    ll24.putExtra("familyName", familyName);
                    ll24.putExtra("deviceName", deviceName);
                    ll24.putExtra("serverAddress", serverAddress);
                    ll24.putExtra("locationName", locationName);
                    ll24.putExtra("allowGPS", allowGPS);
                    //
                    recurringLl24 = PendingIntent.getBroadcast(MainActivity.this, 0, ll24, PendingIntent.FLAG_CANCEL_CURRENT);
                    alarms = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    alarms.setRepeating(AlarmManager.RTC_WAKEUP, SystemClock.currentThreadTimeMillis(), 60000, recurringLl24);
                    //
                    timer = new Timer();
                    oneSecondTimer = new RemindTask();
                    timer.scheduleAtFixedRate(oneSecondTimer, 1000, 1000);
                    ///////////////////////////////////////////////////////////
                    connectWebSocket();

                    String scanningMessage = "Scanning for " + familyName + "/" + deviceName;
                    if (locationName.equals("") == false) {
                        scanningMessage += " at " + locationName;
                    }
                    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(MainActivity.this)
                            .setSmallIcon(R.drawable.ic_stat_name)
                            .setContentTitle(scanningMessage)
                            .setContentIntent(recurringLl24);
                    //specifying an action and its category to be triggered once clicked on the notification
                    Intent resultIntent = new Intent(MainActivity.this, MainActivity.class);
                    resultIntent.setAction("android.intent.action.MAIN");
                    resultIntent.addCategory("android.intent.category.LAUNCHER");
                    PendingIntent resultPendingIntent = PendingIntent.getActivity(MainActivity.this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    notificationBuilder.setContentIntent(resultPendingIntent);

                    android.app.NotificationManager notificationManager =
                            (android.app.NotificationManager) MainActivity.this.getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());

                    /*
                    final TextView myClickableUrl = (TextView) findViewById(R.id.textInstructions);
                    myClickableUrl.setText("See your results in realtime: " + serverAddress + "/view/location/" + familyName + "/" + deviceName);
                    Linkify.addLinks(myClickableUrl, Linkify.WEB_URLS);*/
                } else {
                    TextView rssi_msg = (TextView) findViewById(R.id.textOutput);
                    rssi_msg.setText("not running");
                    Log.d(TAG, "toggle set to false");
                    alarms.cancel(recurringLl24);
                    android.app.NotificationManager mNotificationManager = (android.app.NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.cancel(0);
                    timer.cancel();
                }
            }
        });

        // Get if server url is set, if not, ask the user to set it
        // final SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        String ServerUrl = prefs.getString("serverurl", "");
        String Residence = prefs.getString("residence", "");

        // If the server url is not set
        if (ServerUrl.equals("") || Residence.equals("")) {
            set();
        }
        //Use of setting button
        Button settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                set();
            }
        });
    }

    private void connectWebSocket() {
        URI uri;
        try {
            //  String serverAddress = ((EditText) findViewById(R.id.serverAddress)).getText().toString();
            //  String familyName = ((EditText) findViewById(R.id.familyName)).getText().toString();
            String familyName = prefs.getString("residence", "");
            String ServerUrl = prefs.getString("serverurl", "");
            String serverAddress = "http://" + ServerUrl + ":8005";
           // String serverAddress = "http://" + ServerUrl;
            String deviceName = ((EditText) findViewById(R.id.deviceName)).getText().toString();
            serverAddress = serverAddress.replace("http", "ws");
            uri = new URI(serverAddress + "/ws?family=" + familyName + "&device=" + deviceName);
            Log.d("Websocket", "connect to websocket at " + uri.toString());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened");
                mWebSocketClient.send("Hello");
            }

            @Override
            public void onMessage(String s) {
                final String message = s;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("Websocket", "message: " + message);
                        JSONObject json = null;
                        JSONObject fingerprint = null;
                        JSONObject sensors = null;
                        JSONObject bluetooth = null;
                        JSONObject wifi = null;
                        String deviceName = "";
                        String locationName = "";
                        String familyName = "";
                        try {
                            json = new JSONObject(message);
                        } catch (Exception e) {
                            Log.d("Websocket", "json error: " + e.toString());
                            return;
                        }
                        try {
                            fingerprint = new JSONObject(json.get("sensors").toString());
                            Log.d("Websocket", "fingerprint: " + fingerprint);
                        } catch (Exception e) {
                            Log.d("Websocket", "json error: " + e.toString());
                        }
                        try {
                            sensors = new JSONObject(fingerprint.get("s").toString());
                            deviceName = fingerprint.get("d").toString();
                            familyName = fingerprint.get("f").toString();
                            locationName = fingerprint.get("l").toString();
                            Log.d("Websocket", "sensors: " + sensors);
                        } catch (Exception e) {
                            Log.d("Websocket", "json error: " + e.toString());
                        }
                        try {
                            wifi = new JSONObject(sensors.get("wifi").toString());
                            Log.d("Websocket", "wifi: " + wifi);
                        } catch (Exception e) {
                            Log.d("Websocket", "json error: " + e.toString());
                        }
                        try {
                            bluetooth = new JSONObject(sensors.get("bluetooth").toString());
                            Log.d("Websocket", "bluetooth: " + bluetooth);
                        } catch (Exception e) {
                            Log.d("Websocket", "json error: " + e.toString());
                        }
                        Log.d("Websocket", bluetooth.toString());
                        Integer bluetoothPoints = bluetooth.length();
                        Integer wifiPoints = wifi.length();
                        Long secondsAgo = null;
                        try {
                            secondsAgo = fingerprint.getLong("t");
                        } catch (Exception e) {
                            Log.w("Websocket", e);
                        }

                        if ((System.currentTimeMillis() - secondsAgo) / 1000 > 3) {
                            return;
                        }
                        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd HH:mm:ss");
                        Date resultdate = new Date(secondsAgo);
                        // String message = sdf.format(resultdate) + ": " + bluetoothPoints.toString() + " bluetooth and " + wifiPoints.toString() + " wifi points inserted for " + familyName + "/" + deviceName;
                        String message = "1 second ago: added " + bluetoothPoints.toString() + " bluetooth and " + wifiPoints.toString() + " wifi points for " + familyName + "/" + deviceName;
                        oneSecondTimer.resetCounter();
                        if (locationName.equals("") == false) {
                            message += " at " + locationName;
                        }
                        TextView rssi_msg = (TextView) findViewById(R.id.textOutput);
                        Log.d("Websocket", message);
                        rssi_msg.setText(message);
                    }
                });
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("Websocket", "Closed " + s);
            }

            @Override
            public void onError(Exception e) {
                Log.i("Websocket", "Error " + e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView rssi_msg = (TextView) findViewById(R.id.textOutput);
                        rssi_msg.setText("cannot connect to server, fingerprints will not be uploaded");
                    }
                });
            }
        };
        mWebSocketClient.connect();
    }

    //notification for delete all locations
    private void showNotification() {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(MainActivity.this);
        builder.setTitle("notice");
        builder.setMessage(R.string.clearLocs);
        builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String ServerUrl = prefs.getString("serverurl", "");
                String Residence = prefs.getString("residence", "");
                String url = "http://" + ServerUrl + "/Omega/Findnearest/SetingDeleteAll.php";
                Map<String, String> arguments = new HashMap<>();
                arguments.put("family", Residence);
                final Map<String, String> finalArguments = arguments;
                Answer(finalArguments, url);

            }
        });
        builder.setNegativeButton("no", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), R.string.notdone, Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }

    private void Answer(final Map<String, String> mess, String url) {
        try {
            JSONObject response = null;
            SendToServer sendAnswer = new SendToServer(mess, url);
            response = new JSONObject(sendAnswer.execute().get());
            final JSONObject finalResponse = response;
            if (finalResponse.getString("message").equals("done"))
                Toast.makeText(getApplicationContext(), R.string.done, Toast.LENGTH_SHORT).show();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

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

    private int Locationexits(String family, JSONArray listlocations, String location) {
        //Before add the distance to one location, or delete a location,
        // we must ensure this location already exists
        // if this function returns 0(location not legaled)
        //we request the user to enter a valid location
        int finded = 0;
        try {
            TextView rssi_msg = findViewById(R.id.textOutput);
            if (family.equals("")) {//if the residence name is not entered
                rssi_msg.setText(R.string.EnterResidence);
            } else {
                if (listlocations != null) {
                    //if the location we entered has been saved in the FIND server, it is legaled, we
                    //can find it in the "listlocations"
                    for (int i = 0; i < listlocations.length(); i++) {
                        if (listlocations.getString(i).equals(location)) {
                            finded = 1;
                            break;
                        }
                    }
                }
                else{// if the residence name is entered, but there are not any locations saved in FIND server
                rssi_msg.setText(R.string.EnterLocation);
                }
            }
            return finded;
        }catch (JSONException e){
            e.printStackTrace();
            return 0;
        }
    }

}