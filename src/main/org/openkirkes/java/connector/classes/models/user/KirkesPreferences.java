package org.openkirkes.java.connector.classes.models.user;

import com.google.gson.annotations.SerializedName;

public class KirkesPreferences {

    @SerializedName("email")
    private String email;

    @SerializedName("nickname")
    private String nickname;

    public KirkesPreferences(String email, String nickname) {
        this.email = email;
        this.nickname = nickname;
    }

    public KirkesPreferences() {

    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
