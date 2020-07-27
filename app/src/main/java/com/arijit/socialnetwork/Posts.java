package com.arijit.socialnetwork;

public class Posts {
    public String uid;
    public String time;
    public String date;
    public String post_image;
    public String profile_image;
    public String description;
    public String fullname;

    public Posts() {

    }

    public Posts(String uid, String time, String date, String post_image, String profile_image, String description, String fullname) {
        this.uid = uid;
        this.time = time;
        this.date = date;
        this.post_image = post_image;
        this.profile_image = profile_image;
        this.description = description;
        this.fullname = fullname;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPost_image() {
        return post_image;
    }

    public void setPost_image(String post_image) {
        this.post_image = post_image;
    }

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }
}
