package com.example.easist;

public class SavedItem {
    private String type;
    private String title;
    private String date;
    private String time;
    private Long eventId;
    private String description;

    public SavedItem(String type, String title, String date, String time, Long eventId) {
        this.type = type;
        this.title = title;
        this.date = date;
        this.time = time;
        this.eventId = eventId;
    }
    public SavedItem(String type, String title,String description, String date, String time, Long eventId) {
        this.type = type;
        this.description = description;
        this.title = title;
        this.date = date;
        this.time = time;
        this.eventId = eventId;
    }

    // Gettery
    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public Long getEventId() { return eventId; }

    // Settery potrzebne przy edycji
    public void setType(String type) { this.type = type; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setDate(String date) { this.date = date; }
    public void setTime(String time) { this.time = time; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
}
