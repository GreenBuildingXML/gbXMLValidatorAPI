package com.bimport.asharea.mySQL.user.recaptcha;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.sql.Timestamp;

@JsonPropertyOrder({
        "success",
        "score",
        "action",
        "challenge_ts",
        "hostname",
        "error-codes"
})
public class GoogleResponse {
    @JsonProperty("success")
    private boolean success;
    @JsonProperty("score")
    private float score;
    @JsonProperty("action")
    private String action;
    @JsonProperty("challenge_ts")
    private String challengeTS;
    @JsonProperty("hostname")
    private String hostname;

    public boolean isSuccess() {
        return success;
    }

    public float getScore() {
        return score;
    }

    public String getAction() {
        return action;
    }

    public String getChallengeTS() {
        return challengeTS;
    }

    public String getHostname() {
        return hostname;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setChallengeTS(String challengeTS) {
        this.challengeTS = challengeTS;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
}
