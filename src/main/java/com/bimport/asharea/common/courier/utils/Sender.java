package com.bimport.asharea.common.courier.utils;

import com.bimport.asharea.common.StringUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class Sender {

    private String url = "https://api.trycourier.app/send";
    private String COURIER_AUTH_TOKEN = "69XDZRQXG6M7G8HN4Y7CB1AMC44S";

    private static final String SENDER_NAME = "Sender_Name";
    private static final String SENDER_ADDRESS = "Sender_Address";
    private static final String SENDER_CITY = "Sender_City";
    private static final String SENDER_STATE = "Sender_State";
    private static final String SENDER_ZIP = "Sender_Zip";

    private static final String EMAIL_FROM = "";

    @Value("${email.sender-password}")
    private String sendGridAPIKey;

    @Value("${email.sender-name}")
    private String senderName;

    @Value("${email.sender-address}")
    private String senderAddress;

    @Value("${email.sender-city}")
    private String senderCity;

    @Value("${email.sender-state}")
    private String senderState;

    @Value("${email.sender-zip}")
    private String senderZip;

    private static final Logger logger = LoggerFactory.getLogger(Sender.class);
    // send email via sendGrid
    public boolean sendGridEmail(Personalization pl, String template_ID, String emailTo) {
        SendGrid sg = new SendGrid(sendGridAPIKey);
        Request request = new Request();

        pl.addDynamicTemplateData(SENDER_NAME, senderName);
        pl.addDynamicTemplateData(SENDER_ADDRESS, senderAddress);
        pl.addDynamicTemplateData(SENDER_CITY, senderCity);
        pl.addDynamicTemplateData(SENDER_STATE, senderState);
        pl.addDynamicTemplateData(SENDER_ZIP, senderZip);
        pl.addTo(new Email(emailTo));

        Mail mail = new Mail();
        mail.setFrom(new Email(EMAIL_FROM));
        mail.setTemplateId(template_ID);
        mail.addPersonalization(pl);
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            if (response.getStatusCode() != 202) {
                System.out.println(response.getStatusCode());
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return true;
    }
    // send email via courier
    public boolean sendEmail(JsonObject dataObj, String emailTo, String templateId){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(COURIER_AUTH_TOKEN);
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HttpHeaders.ACCEPT_CHARSET, StandardCharsets.UTF_8.name());

        JsonObject profile = new JsonObject();
        profile.addProperty("email", emailTo);

        dataObj.addProperty(SENDER_NAME, senderName);
        dataObj.addProperty(SENDER_ADDRESS, senderAddress);
        dataObj.addProperty(SENDER_CITY, senderCity);
        dataObj.addProperty(SENDER_STATE, senderState);
        dataObj.addProperty(SENDER_ZIP, senderZip);

        String uuid = UUID.randomUUID().toString();
        JsonObject body = new JsonObject();
        body.add("profile", profile);
        body.add("data", dataObj);
        body.addProperty("recipient", uuid);
        body.addProperty("event", templateId);

        RestTemplate restTemplate = new RestTemplate();
        try{
            HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                logger.info("Send Successful: " + response.getBody());
            } else {
                logger.error("Send Failed");
            }
        }catch (Exception ex){
            logger.error(ex.toString());
        }

        return true;

    }
}
