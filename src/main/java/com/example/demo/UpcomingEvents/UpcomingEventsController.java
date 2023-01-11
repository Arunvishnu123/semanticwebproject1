package com.example.demo.UpcomingEvents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping(path = "upcomingevents/")
public class UpcomingEventsController {

    @Autowired
    private UpcomingEventService upcomingEventService ;

    @GetMapping(value="/{eventDate}", produces={"text/turtle"})
    public String getEventsByDate(@PathVariable("eventDate") String eventDate) throws IOException {
        return upcomingEventService.upcomingEvents(eventDate);
    }

}
