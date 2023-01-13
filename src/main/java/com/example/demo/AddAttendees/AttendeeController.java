package com.example.demo.AddAttendees;

import net.fortuna.ical4j.data.ParserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping(path = "addattendee/")
public class AttendeeController {

    @Autowired
    private AddAttendeesService addAttendee ;

    @PostMapping(value = "/new",produces={"text/turtle"})
    public String addAttendee(AttendeeModel attendeeModel) throws ParserException, IOException {
        return addAttendee.addAttendee(attendeeModel.getAttendeeName(), attendeeModel.getCourseName(), attendeeModel.getDate());
    }
}
