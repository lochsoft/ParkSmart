package com.lochana.parkingassistant;

public class User {
    private String nickname;
    private int points;
    private String email;
    private Long rank;

    public User() {}  // Needed for Firestore
        public User(String nickname, int points, String email, Long rank) {
            this.nickname = nickname;
            this.points = points;
            this.email = email;
            this.rank = rank;
        }

        public String getNickname() { return nickname; }
        public int getPoints() { return points; }
        public String getEmail() { return email; }
        public Long getRank() { return rank; }

        public void setRank(Long rankValue) {
            this.rank = rankValue;
        }
    }
