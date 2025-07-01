package com.example.easist;

public class SavedItem {
    private final String type;
    private final String title;
    private final String date;
    private final String time;
    private final Long eventId;

    public SavedItem(String type, String title, String date, String time, Long eventId) {
        this.type = type;
        this.title = title;
        this.date = date;
        this.time = time;
        this.eventId = eventId;
    }

    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public Long getEventId() { return eventId; }
}
