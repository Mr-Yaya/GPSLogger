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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class BumpsViewerDBHandler extends AsyncTask<String, Void, Object> {

    final private URI bumpsViewerServerEndPoint = URI.create("https://bumps-viewer-server.azurewebsites.net");


    public BumpsViewerDBHandler() {
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected Object doInBackground(String... params) {

        String requestType = params[0];
        URI serverEndPoint;
        switch (requestType) {
            case "validateTeamInfo":
                 serverEndPoint = bumpsViewerServerEndPoint.resolve("/db/team/name");
                break;
            case "getDivisions":
                serverEndPoint = bumpsViewerServerEndPoint.resolve("/db/get-divisions");
                break;
            case "saveDebugData":
                serverEndPoint = bumpsViewerServerEndPoint.resolve("/db/location");
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + requestType);
        }

        HttpsURLConnection connection = null;
        try {
            connection = (HttpsURLConnection) serverEndPoint.toURL().openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert connection != null;

        connection.setRequestProperty("User-Agent", "android-app-gps-logger");
        connection.setRequestProperty("Content-Type", "application/json");

        if (!requestType.equals("getDivisions")) {
            try {
                connection.setRequestMethod("POST");
            } catch (ProtocolException e) {
                e.printStackTrace();
            }

            JSONObject body = new JSONObject();

            if(requestType.equals("validateTeamInfo")) {
                String crewName = params[1];
                String division = params[2];
                try {
                    body.put("crew_name", crewName);
                    body.put("division", division);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if(requestType.equals("saveDebugData")) {
                String crewId = params[1];
                double latitude = Double.parseDouble(params[2]);
                double longitude = Double.parseDouble(params[3]);
                int trackNumber = Integer.parseInt(params[4]);
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()); // Quoted "Z" to indicate UTC, no timezone offset
                String timestamp = df.format(new Date());
                try {
                    body.put("crew_id", crewId);
                    body.put("track_num", trackNumber);
                    body.put("timestamp", timestamp);
                    body.put("latitude", latitude);
                    body.put("longitude", longitude);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                return null;
            }
            connection.setDoOutput(true);
            try {
                connection.getOutputStream().write(body.toString().getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

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
                Log.w("DB Error","Got response" + connection.getResponseCode());
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
