package com.bimport.ashrae.common.courier;

import com.bimport.ashrae.common.courier.utils.Sender;
import com.google.gson.JsonObject;
import com.sendgrid.helpers.mail.objects.Personalization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ActivateUserNotification {

    private String SENDGRID_TEMPLATE_ID = "d-5e859fa61f0b43a9a6d8b40ce2a831e8";

    @Autowired
    Sender sender;

    public void sendEmail(String emailTo, String userName, String activationLink){
        //todo sendGrid sample
        Personalization obj = new Personalization();
        obj.addDynamicTemplateData("user_name", userName);
        obj.addDynamicTemplateData("activation_link", activationLink);
        sender.sendGridEmail(obj, SENDGRID_TEMPLATE_ID, emailTo);
    }
}
