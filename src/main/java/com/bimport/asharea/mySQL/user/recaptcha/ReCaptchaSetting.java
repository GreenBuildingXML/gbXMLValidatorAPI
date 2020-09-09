package com.bimport.asharea.mySQL.user.recaptcha;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "google.recaptcha.key")
public class ReCaptchaSetting {
    private String site;
    private String secret;
    private float threshold;

    public String getSite() {
        return site;
    }

    public String getSecret() {
        return secret;
    }

    public float getThreshold() {
        return threshold;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public void setThreshold(float threshold) {
        this.threshold = threshold;
    }
}
