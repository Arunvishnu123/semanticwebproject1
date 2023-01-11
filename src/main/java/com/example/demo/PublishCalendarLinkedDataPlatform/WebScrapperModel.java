package com.example.demo.PublishCalendarLinkedDataPlatform;

public class WebScrapperModel {
    private String webURL;

    private String identifierString;

    public WebScrapperModel () {
    }

    public WebScrapperModel (String webURL, String identifierString) {
        this.webURL = webURL;
        this.identifierString = identifierString;
    }

    public String getWebURL() {
        return webURL;
    }

    public void setWebURL(String webURL) {
        this.webURL = webURL;
    }

    public String getIdentifierString() {
        return identifierString;
    }

    public void setIdentifierString(String identifierString) {
        this.identifierString = identifierString;
    }

}
