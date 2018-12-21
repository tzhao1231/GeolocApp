package com.internalpositioning.find3.find3app;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;

// Send to server with format JSON
public class SendToServerJson extends AsyncTask<String, String, String> {
    private JSONObject arguments;
    private String urlS;
    private String TAG = "debugSend";

    public SendToServerJson(JSONObject arguments, String url){
        this.arguments = arguments;
        this.urlS = url;
    }

    protected String doInBackground(String... args) {

        String response = null;
        try {
            response = post();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    private String post() throws Exception {
        Log.i(TAG, "Sending post to " + urlS);
        URL url;
        url = new URL(urlS);
        URLConnection con = url.openConnection();
        con.setConnectTimeout(5 * 1000);
        HttpURLConnection http = (HttpURLConnection)con;
        try {
            http.setRequestMethod("POST"); // PUT is another valid option
            http.setDoOutput(true);
            http.setDoInput(true);
            http.setUseCaches(false);
            http.setInstanceFollowRedirects(true);
            http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            http.connect();

            String json = arguments.toString();
            OutputStream os = http.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(json);
            writer.flush();
            writer.close();
            os.close();

            String sb = "";
            int HttpResult = http.getResponseCode();
            Log.i(TAG, "response code is "+HttpResult);
            if (HttpResult == HttpsURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(http.getInputStream(), "utf-8"));
                String line;
                while ((line = br.readLine()) != null) {
                    sb += line;
                }
                br.close();
                Log.i("sb", sb);
                return sb;
            } else {
                Log.i(TAG, (http.getResponseMessage()));
            }

        }catch (Exception e){
            Log.i("exception", e.toString());
        } finally {
            http.disconnect();
        }
        return null;
    }


}
