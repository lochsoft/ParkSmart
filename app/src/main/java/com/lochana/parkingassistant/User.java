package com.lochana.parkingassistant;

public class User {
    private String nickname;
    private int points;
    private String email;

    public User() {}  // Needed for Firestore

    public User(String nickname, int points, String email) {
        this.nickname = nickname;
        this.points = points;
        this.email = email;
    }

    public String getNickname() { return nickname; }
    public int getPoints() { return points; }
    public String getEmail() { return email; }
}
