package com.techie.shoppingstore.controller;

import com.techie.shoppingstore.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestMailController {

    @Autowired
    private MailService mailService;

    @GetMapping("/sendemail")
    public void sendSampleMail() {
        mailService.sendMail("rc@abc.com", "Hello");
    }

}
