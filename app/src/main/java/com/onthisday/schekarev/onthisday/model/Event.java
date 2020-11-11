package com.onthisday.schekarev.onthisday.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Event {

    private String mKey;
    private String mUserUID;
    private String mTitle;
    private String mDescription;
    private String mImage;
    private int mRateEvent;
    private Date mDate;

    public Event() {
    }

    public Event(String key, String userUID, String title, String description, int rateEvent, Date date, String img) {
        mKey = key;
        mUserUID = userUID;
        mTitle = title;
        mDescription = description;
        mRateEvent = rateEvent;
        mDate = date;
        mImage = img;
    }

    public String getKey() {
        return mKey;
    }

    public void setKey(String key) {
        mKey = key;
    }

    public String getUserUID() {
        return mUserUID;
    }

    public void setUserUID(String userUID) {
        mUserUID = userUID;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public String getImage() {
        return mImage;
    }

    public void setImage(String image) {
        mImage = image;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public int getRateEvent() {
        return mRateEvent;
    }

    public void setRateEvent(int rateEvent) {
        this.mRateEvent = rateEvent;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("key", mKey);
        result.put("userUID", mUserUID);
        result.put("title", mTitle);
        result.put("description", mDescription);
        result.put("date", mDate);
        result.put("rateEvent", mRateEvent);
        result.put("image", mImage);
        return result;
    }
}
