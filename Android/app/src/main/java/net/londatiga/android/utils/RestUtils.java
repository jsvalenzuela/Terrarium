package net.londatiga.android.utils;

import android.os.StrictMode;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class RestUtils {
    private String urlApi;

    public RestUtils(String urlParametro){
        this.urlApi = urlParametro;
    }

    public JSONObject consumirApi() throws MalformedURLException,IOException,JSONException{

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        URL url = null;
        HttpURLConnection con;
        JSONObject jsonObject = null;
        try {
            url = new URL(this.urlApi);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.connect();
            BufferedReader in =  new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            String json = "";
            while((inputLine = in.readLine()) != null){
                response.append(inputLine);
            }

            json = response.toString();
            jsonObject = new JSONObject(json);
        }
         catch (MalformedURLException e) {
            e.printStackTrace();
            throw e;
        } catch (IOException e1) {
            e1.printStackTrace();
            throw e1;
        }
        catch (JSONException e2) {
            e2.printStackTrace();
            throw e2;
        }
        return jsonObject;
    }


}
