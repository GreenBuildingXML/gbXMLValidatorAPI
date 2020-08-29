package com.bimport.asharea.common.courier;

import com.bimport.asharea.common.courier.utils.Sender;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ForgotPasswordNotification {

    private String COURIER_NOTIFICATION_ID = "ZYDNSJFWFE4SBZJ6BZE4CXJSQT28";

    @Autowired
    Sender sender;

    public void sendEmail(String emailTo, String userName, String activationLink){
        JsonObject data = new JsonObject();
        data.addProperty("user_name", userName);
        data.addProperty("activation_link", activationLink);
        sender.sendEmail(data, emailTo, COURIER_NOTIFICATION_ID);
    }
}
