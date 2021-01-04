package com.bimport.ashrae.common.courier;

import com.bimport.ashrae.common.courier.utils.Sender;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ActivateUserNotification {

    private String COURIER_NOTIFICATION_ID = "ZYDNSJFWFE4SBZJ6BZE4CXJSQT28";
    private String SENDGRID_TEMPLATE_ID = "";
    @Autowired
    Sender sender;

    public void sendEmail(String emailTo, String userName, String activationLink){
        JsonObject data = new JsonObject();
        data.addProperty("user_name", userName);
        data.addProperty("activation_link", activationLink);
        sender.sendEmail(data, emailTo, COURIER_NOTIFICATION_ID);

        //todo sendGrid sample
//        Personalization obj = new Personalization();
//        obj.addDynamicTemplateData("user_name", userName);
//        obj.addDynamicTemplateData("activation_link", activationLink);
//        sender.sendGridEmail(obj, SENDGRID_TEMPLATE_ID, emailTo);
    }
}
