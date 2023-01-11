package com.example.demo.EventsInSaintEtienne;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping(path = "Event/saintetienne/")
public class EventController {

    @Autowired
    private EventsInSaintEtienneNotCoursesServices eventsInSaintEtienneNotCoursesServices ;
    @Autowired
    private EventThatAreCourses eventThatAreCourses ;


    @GetMapping(value="/notCourses", produces={"text/turtle"})
    public String getEventsThatAreNotCourses() throws IOException {
        return eventsInSaintEtienneNotCoursesServices.eventsThatAreNotCourses();
    }

    @GetMapping(value="/courses", produces={"text/turtle"})
    public String getEventsThatAreCourses() throws IOException {
        return eventThatAreCourses.eventsThatAreCourses();
    }
}
