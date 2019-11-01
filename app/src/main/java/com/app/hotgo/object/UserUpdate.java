package com.app.hotgo.object;



public class UserUpdate {
    private String lat;
    private String lng;
    private String status;
    private String taskType;

    public UserUpdate(String lat, String lng, String status, String taskType) {
        this.lat = lat;
        this.lng = lng;
        this.status = status;
        this.taskType = taskType;
    }

    public String getStatus() {
        return status;
    }

    public String getLat() {
        return lat;
    }

    public String getLng() {
        return lng;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }
}
