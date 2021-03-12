package com.potato.timetable.model;

public class ShareModel {
    private String key;
    private String shareUrl;
    private String quickAccess;

    public ShareModel() {
    }

    public ShareModel(String key, String shareUrl, String quickAccess) {
        this.key = key;
        this.shareUrl = shareUrl;
        this.quickAccess = quickAccess;
    }

    public String getKey() {
        return key;
    }

    public ShareModel setKey(String key) {
        this.key = key;
        return this;
    }

    public String getShareUrl() {
        return shareUrl;
    }

    public ShareModel setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
        return this;
    }

    public String getQuickAccess() {
        return quickAccess;
    }

    public ShareModel setQuickAccess(String quickAccess) {
        this.quickAccess = quickAccess;
        return this;
    }
}
