package com.example.demo.AddtionalRequirements;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping(path = "getEvents/")
public class GetEventController {

    @Autowired
    private GetAllEventsInSelectedRoom getAllEventsInSelectedRoom;

    @Autowired
    private GetAllAttendeesOfAnEvent getAllAttendeesOfAnEvent;


    @GetMapping(value="/{roomNumber}", produces={"text/turtle"})
    public String eventInRoom(@PathVariable("roomNumber") String roomNumber) throws IOException {
        return getAllEventsInSelectedRoom.getAllEventsInTheSelectedRoom(roomNumber);
    }

    @GetMapping(value="/course/attendees", produces={"text/turtle"})
    public String getAttendeesOfAEvents(AttendeeModel attendeeModel) throws IOException {
        return getAllAttendeesOfAnEvent.getAttendeeListOfAnEvent(attendeeModel.getCourseName(),attendeeModel.getDate());
    }
}
