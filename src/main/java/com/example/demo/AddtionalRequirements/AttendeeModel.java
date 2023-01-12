package com.example.demo.AddtionalRequirements;

public class AttendeeModel {
    private  String courseName;
    private String Date;

    public AttendeeModel() {
    }

    public AttendeeModel(String courseName, String date) {
        this.courseName = courseName;
        Date = date;
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
}
