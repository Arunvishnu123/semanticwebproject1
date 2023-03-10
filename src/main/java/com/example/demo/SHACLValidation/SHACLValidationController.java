package com.example.demo.SHACLValidation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping(path = "ldp/validation/")
public class SHACLValidationController {

    @Autowired
    private SHACLEventValidationService shaclEventValidationService;

    @Autowired
    private SHACLOrganizerValidatorService shaclOrganizerValidatorService;

    @GetMapping(value="/eventValidation/event/{id}", produces={"text/turtle"})
    public String eventValidation(@PathVariable("id") String serialNumber) throws IOException {
        return  shaclEventValidationService.eventValidation(serialNumber);
    }

    @GetMapping(value="/organizerValidation/event/{id}", produces={"text/turtle"})
    public String organizerValidation(@PathVariable("id") String serialNumber) throws IOException {
        return  shaclOrganizerValidatorService.organizerValidator(serialNumber);
    }
}
