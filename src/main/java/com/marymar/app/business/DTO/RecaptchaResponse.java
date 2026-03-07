package com.marymar.app.business.DTO;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class RecaptchaResponse {

    private boolean success;

    @JsonProperty("error-codes")
    private List<String> errorCodes;

    private String hostname;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<String> getErrorCodes() {
        return errorCodes;
    }

    public void setErrorCodes(List<String> errorCodes) {
        this.errorCodes = errorCodes;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
}