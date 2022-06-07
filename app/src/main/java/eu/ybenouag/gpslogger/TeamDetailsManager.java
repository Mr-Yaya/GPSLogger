package eu.ybenouag.gpslogger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class TeamDetailsManager {

    private static TeamDetailsManager singleton;

    public static TeamDetailsManager getInstance() {
        return singleton;
    }

    private boolean teamRegistered = false;
    private String crewId;
    private String crewName;
    private String division;

    private BumpsViewerDBHandler dbBumpsViewerDBHandler;

    private ArrayList<String> divisionList;

    public TeamDetailsManager() throws IOException, ExecutionException, InterruptedException {
        singleton = this;
        this.updateDivisionList();
    }

    public ArrayList<String> getDivisionList() {
        return divisionList;
    }

    public void updateDivisionList() throws ExecutionException, InterruptedException {
        this.dbBumpsViewerDBHandler = new BumpsViewerDBHandler();
        JSONArray divisionList = (JSONArray) this.dbBumpsViewerDBHandler.execute("getDivisions", null).get();
        String[] divisionArray = (String[]) divisionList.toArray(new String[0]);
        this.divisionList = new ArrayList<>(Arrays.asList(divisionArray));
    }

    public boolean validateTeamInfo(String teamName, String division) throws ExecutionException, InterruptedException {
        this.dbBumpsViewerDBHandler = new BumpsViewerDBHandler();
        JSONObject team = (JSONObject) this.dbBumpsViewerDBHandler.execute("validateTeamInfo", teamName, division).get();
        if (!team.isEmpty()) {
            this.updateTeamInfo(team);
            return true;
        } else {
            return false;
        }
    }

    private void updateTeamInfo(JSONObject team) {
        this.crewName = (String) team.get("crew_name");
        this.crewId = (String) team.get("crew_id");
        this.division = (String) team.get("division");
        this.teamRegistered = true;
    }

    public boolean isTeamRegistered() {
        return teamRegistered;
    }

    public String getCrewName() {
        return crewName;
    }

    public String getCrewId() {
        return crewId;
    }

    public void resetTeamDetails() {
        this.teamRegistered = false;
        this.crewName = null;
        this.division = null;
        this.crewId = null;
        StreamLocationManager streamLocationManager = StreamLocationManager.getInstance();
        streamLocationManager.resetTrackNumber();
    }

    public String getDivision() {
        return this.division;
    }
}
