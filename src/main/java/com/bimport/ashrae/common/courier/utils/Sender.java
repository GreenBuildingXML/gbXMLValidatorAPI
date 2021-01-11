package com.bimport.ashrae.common.courier.utils;

import com.google.gson.JsonObject;
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

    @Value("${email.sender-from}")
    private String EMAIL_FROM;

    @Value("${email.sender-password}")
    private String sendGridAPIKey;


    private static final Logger logger = LoggerFactory.getLogger(Sender.class);
    // send email via sendGrid
    public boolean sendGridEmail(Personalization pl, String template_ID, String emailTo) {
        SendGrid sg = new SendGrid(sendGridAPIKey);
        Request request = new Request();
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
            if (response.getStatusCode() == 202) {
                logger.info("Send Successful: " + response.getBody());
            } else {
                logger.error("Send Failed");
            }

        } catch (IOException ex) {
            logger.error(ex.toString());
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
