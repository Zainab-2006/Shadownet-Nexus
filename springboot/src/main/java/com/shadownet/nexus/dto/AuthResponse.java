package com.shadownet.nexus.dto;

public class AuthResponse {

    private String token;
    private AuthUser user;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public AuthUser getUser() {
        return user;
    }

    public void setUser(AuthUser user) {
        this.user = user;
    }

    public static class AuthUser {
        private String id;
        private String username;
        private String displayName;

        public AuthUser() {
        }

        public AuthUser(String id, String username, String displayName) {
            this.id = id;
            this.username = username;
            this.displayName = displayName;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }
    }
}
