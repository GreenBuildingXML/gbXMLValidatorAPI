package com.bimport.asharea.mySQL.user.recaptcha;

import com.bimport.asharea.common.Exception.ConflictException;
import com.bimport.asharea.common.Exception.RecaptchaValidateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;


@Service
public class CaptchaService{
    @Autowired
    ReCaptchaAttemptService reCaptchaAttemptService;
    @Autowired
    ReCaptchaSetting reCaptchaSetting;
    @Value("${google.recaptcha.verify.url}")
    public String recaptchaVerifyUrl;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public void validateCaptcha(String response){

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(recaptchaVerifyUrl).queryParam("secret", reCaptchaSetting.getSecret())
                .queryParam("response", response);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<GoogleResponse> rs = restTemplate.exchange(builder
                        .build().encode().toUri(), HttpMethod.GET, entity,
                GoogleResponse.class);

        GoogleResponse googleResponse = rs.getBody();
        logger.info("recapture score: " + googleResponse.getScore());

        if(!googleResponse.isSuccess()
                || googleResponse.getScore() < reCaptchaSetting.getThreshold()) {
            // todo update exceptions: responseStatusException(spring 5 and above)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "reCaptcha was not successfully validated");
        }
    }
}
