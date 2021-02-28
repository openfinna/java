package org.openfinna.java.connector.classes;

import org.openfinna.java.connector.classes.models.UserType;

public class UserAuthentication {
    private UserType userType;
    private String username;
    private String password;
    private String session;

    public UserAuthentication(UserType userType, String username, String password) {
        this.userType = userType;
        this.username = username;
        this.password = password;
    }

    public UserAuthentication(UserType userType, String username, String password, String session) {
        this.userType = userType;
        this.username = username;
        this.password = password;
        this.session = session;
    }

    public UserType getUserType() {
        return userType;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
