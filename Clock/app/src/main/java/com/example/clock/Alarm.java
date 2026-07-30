package com.example.clock;

public class Alarm {

    private long id;
    private int hour;
    private int minute;
    private String label;
    private String toneUri;
    private boolean enabled;
    private String repeatDays;

    public Alarm() {
    }

    public Alarm(long id, int hour, int minute, String label, String toneUri,
                 boolean enabled, String repeatDays) {
        this.id = id;
        this.hour = hour;
        this.minute = minute;
        this.label = label;
        this.toneUri = toneUri;
        this.enabled = enabled;
        this.repeatDays = repeatDays;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getToneUri() {
        return toneUri;
    }

    public void setToneUri(String toneUri) {
        this.toneUri = toneUri;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getRepeatDays() {
        return repeatDays;
    }

    public void setRepeatDays(String repeatDays) {
        this.repeatDays = repeatDays;
    }

    public boolean isRepeating() {
        return repeatDays != null && repeatDays.contains("1");
    }
    public String getFormattedTime() {
        int displayHour = hour % 12;
        if (displayHour == 0) displayHour = 12;
        String amPm = hour >= 12 ? "PM" : "AM";
        return String.format("%02d:%02d %s", displayHour, minute, amPm);
    }

    public String getRepeatSummary() {
        if (repeatDays == null || !repeatDays.contains("1")) {
            return "One time";
        }
        String[] names = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        StringBuilder sb = new StringBuilder();
        boolean allSet = true;
        for (int i = 0; i < 7; i++) {
            if (repeatDays.charAt(i) == '1') {
                if (sb.length() > 0) sb.append(", ");
                sb.append(names[i]);
            } else {
                allSet = false;
            }
        }
        if (allSet) return "Every day";
        return sb.toString();
    }
}
