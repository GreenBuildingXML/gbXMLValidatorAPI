package com.bimport.ashrae.common.courier;

import com.bimport.ashrae.common.courier.utils.Sender;
import com.google.gson.JsonObject;
import com.sendgrid.helpers.mail.objects.Personalization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ForgotPasswordNotification {

    private String SENDGRID_TEMPLATE_ID = "d-5ac2b5cca97a49e5a9d384bfd195b68b";
    @Autowired
    Sender sender;

    public void sendEmail(String emailTo, String userName, String activationLink){
        Personalization obj = new Personalization();
        obj.addDynamicTemplateData("user_name", userName);
        obj.addDynamicTemplateData("activation_link", activationLink);
        sender.sendGridEmail(obj, SENDGRID_TEMPLATE_ID, emailTo);
    }
}
