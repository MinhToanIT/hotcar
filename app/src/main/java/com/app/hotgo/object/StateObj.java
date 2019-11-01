package com.app.hotgo.object;

import java.util.ArrayList;


public class StateObj {
    private String stateId;
    private String stateName;
    private ArrayList<CityObj> stateCities;

    public String getStateId() {
        return stateId;
    }

    public void setStateId(String stateId) {
        this.stateId = stateId;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public ArrayList<CityObj> getStateCities() {
        return stateCities;
    }

    public void setStateCities(ArrayList<CityObj> stateCities) {
        this.stateCities = stateCities;
    }
}
