package com.example.demo.AddtionalRequirements;

import com.example.demo.AddAttendees.AddAttendeesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping(path = "getEvents/")
public class EventController {

    @Autowired
    private GetAllEventsInSelectedRoom getAllEventsInSelectedRoom;


    @GetMapping(value="/{roomNumber}", produces={"text/turtle"})
    public String eventInRoom(@PathVariable("roomNumber") String roomNumber) throws IOException {
        return getAllEventsInSelectedRoom.getAllEventsInTheSelectedRoom(roomNumber);
    }
}
