package com.carry_guide.carry_guide_admin.infrastructure.persistence.service;

import com.carry_guide.carry_guide_admin.infrastructure.config.TwilioConfig;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service
public class SmsService {

    @Autowired
    TwilioConfig twilioConfig;
    public void sendSms(String mobileNumber, String message) {
        Message.creator(
                new PhoneNumber(mobileNumber),
                new PhoneNumber(twilioConfig.getFromNumber()),
                message
        ).create();
    }
}
