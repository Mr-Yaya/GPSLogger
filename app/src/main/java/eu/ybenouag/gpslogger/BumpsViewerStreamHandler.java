package eu.ybenouag.gpslogger;

import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.IOUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.net.ProtocolException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;

public class BumpsViewerStreamHandler extends AsyncTask<Object, Void, Object> {

    final private URI bumpsViewerServerEndPoint = URI.create("https://bumps-viewer-server.azurewebsites.net");
    // final private URI bumpsViewerServerEndPoint = URI.create("http://172.31.144.1:8091");

    public BumpsViewerStreamHandler() {
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected Object doInBackground(Object... params) {

        URI serverEndPoint =  bumpsViewerServerEndPoint.resolve("/api/data-collection");
        HttpsURLConnection connection = null;
        try {
            connection = (HttpsURLConnection) serverEndPoint.toURL().openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert connection != null;
        
        connection.setRequestProperty("User-Agent", "android-app-gps-logger");
        connection.setRequestProperty("Content-Type", "application/json");
        try {
            connection.setRequestMethod("POST");
        } catch (ProtocolException ex) {
            ex.printStackTrace();
        }
        JSONObject body = new JSONObject();
        try {
            body.put("crewId", (String) params[0]);
            body.put("crewName", (String) params[1]);
            body.put("latitude", (double) params[2]);
            body.put("longitude", (double) params[3]);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        connection.setDoOutput(true);
        try {
            connection.getOutputStream().write(body.toString().getBytes());
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        Log.w("Streaming","Sending the data to the web server: " + body);

        try {
            if (connection.getResponseCode() == 200) {
                InputStream responseBody = connection.getInputStream();
                String responseBodyReader =
                        IOUtils.toString(responseBody, StandardCharsets.UTF_8);
                JSONParser parser = new JSONParser();
                Object json= parser.parse(responseBodyReader);
                connection.disconnect();
                return json;
            } else {
                Log.w("DB Error","Got reponse" + connection.getResponseCode());
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

}
