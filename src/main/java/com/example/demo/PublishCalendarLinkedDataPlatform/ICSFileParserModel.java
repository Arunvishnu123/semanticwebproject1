package com.example.demo.PublishCalendarLinkedDataPlatform;

public class ICSFileParserModel {
    private String location;

    private String identifierString;


    public ICSFileParserModel() {
    }

    public ICSFileParserModel(String location, String identifierString) {
        this.location = location;
        this.identifierString = identifierString;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getIdentifierString() {
        return identifierString;
    }

    public void setIdentifierString(String identifierString) {
        this.identifierString = identifierString;
    }
}
