package com.example.demo.AddAttendees;

public class AttendeeModel {
    private  String courseName;
    private String Date;
    private String attendeeName;

    public AttendeeModel() {
    }


    public AttendeeModel(String courseName, String date, String attendeeName) {
        this.courseName = courseName;
        Date = date;
        this.attendeeName = attendeeName;
    }


    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getAttendeeName() {
        return attendeeName;
    }

    public void setAttendeeName(String attendeeName) {
        this.attendeeName = attendeeName;
    }

}
