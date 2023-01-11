package com.example.demo.PublishCalendarLinkedDataPlatform;


import net.fortuna.ical4j.data.ParserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping(path = "publishterritoire/")
public class PublishController {

    @Autowired
    private PublishICSCalendarData publishICSCalendarData;

    @Autowired
    private WebScrapperRDFExtractor webScrapperRDFExtractor ;

    @PostMapping(value = "/uploadFile")
    public String uploadFile(ICSFileParserModel fileLocation) throws ParserException, IOException, InterruptedException {
        publishICSCalendarData.ICSCalendarDataPublisher(fileLocation.getLocation(),fileLocation.getIdentifierString());
        return "Data successfully published to platform territoire" ;
    }


    @PostMapping(value = "/webURL")
    public String attachWebURL(WebScrapperModel webScrapperModel) throws ParserException, IOException, InterruptedException {
        webScrapperRDFExtractor.webScraperRDFExtractorFunction(webScrapperModel.getWebURL(),webScrapperModel.getIdentifierString());
        return "Data successfully published to platform territoire" ;
    }

}
