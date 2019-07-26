package com.shinmashita.checkly.settings;

public class Preference {

    private int preferenceId;
    private String preferenceValue;

    public Preference(String preferenceValue) {
        this.preferenceValue = preferenceValue;
    }

    public void setPreferenceId(int preferenceId) {
        this.preferenceId = preferenceId;
    }

    public void setPreferenceValue(String preferenceValue) {
        this.preferenceValue = preferenceValue;
    }

    public int getPreferenceId() {
        return preferenceId;
    }

    public String getPreferenceValue() {
        return preferenceValue;
    }
}
