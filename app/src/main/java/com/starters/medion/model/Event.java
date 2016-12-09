package com.starters.medion.model;

/**
 * Created by KeerthiTejaNuthi on 12/3/16.
 */

public class Event {

    private String eventId;
    private String eventName;
    private String eventDate;
    private String eventTime;
    private String admin;
    private String memberList;



    public String getEventId() {
        return eventId;
    }

    public String getAdmin() {
        return admin;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }
    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public String getMemberList() {
        return memberList;
    }

    public void setMemberList(String memberList) {
        this.memberList = memberList;
    }
}
