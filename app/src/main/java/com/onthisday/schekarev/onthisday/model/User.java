package com.onthisday.schekarev.onthisday.model;

import java.util.Date;
import java.util.UUID;

public class User {

    private String mName;
    private String mEmail;
    private String mPass;
    private Date mBirthDate;
    private Date mRegDate;
    private String mAvatar;

    public User() {
        String UUID = java.util.UUID.randomUUID().toString();
        mRegDate = new Date();
        mBirthDate = new Date();
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    public String getPass() {
        return mPass;
    }

    public void setPass(String pass) {
        mPass = pass;
    }

    public Date getBirthDate() {
        return mBirthDate;
    }

    public void setBirthDate(Date birthDate) {
        mBirthDate = birthDate;
    }

    public Date getRegDate() {
        return mRegDate;
    }

    public void setRegDate(Date regDate) {
        mRegDate = regDate;
    }

    public String getAvatar() {
        return mAvatar;
    }

    public void setAvatar(String avatar) {
        mAvatar = avatar;
    }
}
