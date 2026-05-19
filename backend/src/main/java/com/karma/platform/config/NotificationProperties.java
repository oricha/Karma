package com.karma.platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "karma.notification")
public class NotificationProperties {

    private String unsubscribeBaseUrl = "https://karma.local";
    private int hourlyLimit = 500;
    private int dailyLimit = 100;

    public String getUnsubscribeBaseUrl() {
        return unsubscribeBaseUrl;
    }

    public void setUnsubscribeBaseUrl(String unsubscribeBaseUrl) {
        this.unsubscribeBaseUrl = unsubscribeBaseUrl;
    }

    public int getHourlyLimit() {
        return hourlyLimit;
    }

    public void setHourlyLimit(int hourlyLimit) {
        this.hourlyLimit = hourlyLimit;
    }

    public int getDailyLimit() {
        return dailyLimit;
    }

    public void setDailyLimit(int dailyLimit) {
        this.dailyLimit = dailyLimit;
    }
}
