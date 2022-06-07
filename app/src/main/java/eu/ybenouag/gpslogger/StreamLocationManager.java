package eu.ybenouag.gpslogger;

import org.json.simple.JSONObject;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class StreamLocationManager {

    private static StreamLocationManager singleton;

    public static StreamLocationManager getInstance() {
        return singleton;
    }

    private BumpsViewerStreamHandler bumpsViewerStreamHandler;
    private BumpsViewerDBHandler bumpsViewerDBHandler;

    private int trackNumber;

    public StreamLocationManager() {
        singleton = this;
        resetTrackNumber();
    }

    public void streamLocationData(double latitude,double longitude) throws ExecutionException, InterruptedException {
        bumpsViewerStreamHandler= new BumpsViewerStreamHandler();
        TeamDetailsManager teamDetailsManager = TeamDetailsManager.getInstance();
        JSONObject statusStream = (JSONObject) this.bumpsViewerStreamHandler.execute(
                teamDetailsManager.getCrewId(),
                teamDetailsManager.getCrewName(),
                latitude,
                longitude).get();
        if(Objects.equals(statusStream.get("status"), 1L)) {
            bumpsViewerDBHandler = new BumpsViewerDBHandler();
            JSONObject statusDB = (JSONObject) this.bumpsViewerDBHandler.execute(
                    "saveDebugData",
                    teamDetailsManager.getCrewId(),
                    String.valueOf(latitude),
                    String.valueOf(longitude),
                    String.valueOf(getCurrentTrackNumber())).get();
        }
;    }

    public int getCurrentTrackNumber() {
        return this.trackNumber;
    }

    public void incrementTrackNumber() {
        this.trackNumber += 1;
    }

    public void resetTrackNumber() {
        this.trackNumber = 0;
    }
}
