package com.permissionsmanager;

/**
 * Created by Osama Rao on 24-Nov-15.
 */
public class PermissionMap {
    String api;
    String permission;
    Double confidence;


    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public String getXMLPermissionTag(){
        String xmlTag = " <uses-permission android:name=\"android.permission." + getPermission() + "\" />";
        return xmlTag;
    }

    @Override
    public String toString() {
        return "com.permissionsmanager.PermissionMap{" +
                "api='" + api + '\'' +
                ", permission='" + permission + '\'' +
                ", confidence=" + confidence +
                '}';
    }
}
