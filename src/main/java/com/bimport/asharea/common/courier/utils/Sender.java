package com.bimport.asharea.common.courier.utils;

import com.bimport.asharea.common.StringUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class Sender {

    private String url = "https://api.trycourier.app/send";
    private String SLACK_API = "https://slack.com/api/";
    private String COURIER_AUTH_TOKEN = "69XDZRQXG6M7G8HN4Y7CB1AMC44S";
    private String ACCESS_TOKEN = "xoxb-565836975671-1214125912964-k2evODTCYxHpomONXUQDuPDI";
//    private String CHANNEL = "C0163109WMB";
    private static final String SENDER_NAME = "Sender_Name";
    private static final String SENDER_ADDRESS = "Sender_Address";
    private static final String SENDER_CITY = "Sender_City";
    private static final String SENDER_STATE = "Sender_State";
    private static final String SENDER_ZIP = "Sender_Zip";
    private static final String SLACK_USER = "Slack_User";

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

    public boolean sendEmail(JsonObject dataObj, String emailTo, String templateId){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(COURIER_AUTH_TOKEN);
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HttpHeaders.ACCEPT_CHARSET, StandardCharsets.UTF_8.name());

//        JsonObject slack = new JsonObject();
//        slack.addProperty("access_token", ACCESS_TOKEN);
//        slack.addProperty("channel", CHANNEL);

        JsonObject profile = new JsonObject();
//        profile.add("slack", slack);
        profile.addProperty("email", emailTo);

        dataObj.addProperty(SENDER_NAME, senderName);
        dataObj.addProperty(SENDER_ADDRESS, senderAddress);
        dataObj.addProperty(SENDER_CITY, senderCity);
        dataObj.addProperty(SENDER_STATE, senderState);
        dataObj.addProperty(SENDER_ZIP, senderZip);

//        String memberId = fetchSlackMemberId(emailTo);
//        dataObj.addProperty(SLACK_USER, memberId);

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

    public String fetchSlackMemberId(String email){
        if(StringUtil.isNullOrEmpty(email)){
            return "";
        }
        String url = SLACK_API + "users.lookupByEmail?token="+ACCESS_TOKEN + "&email="+email;
        String memberId;
        try{
            RestTemplate restTemplate = new RestTemplate();

            URI uri = new URI(url);

            ResponseEntity<String> result = restTemplate.getForEntity(uri, String.class);
            String body = result.getBody();
            JsonParser parser = new JsonParser();
            JsonObject res = (JsonObject) parser.parse(body);
            memberId = res.getAsJsonObject("user").get("id").getAsString();
            logger.info("memberId", memberId);
            return memberId;

        }catch (Exception ex){
            logger.error(ex.toString());
            return "";
        }


    }
}
