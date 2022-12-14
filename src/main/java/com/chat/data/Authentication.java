package com.chat.data;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Authentication implements Serializable {

    private final String username;
    private final String password;

    public Authentication(String username, String password) {
        this.username = username;

        String saltedPassword = username + password;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(saltedPassword.getBytes(StandardCharsets.UTF_8));
            byte[] hash = messageDigest.digest();
            for (byte b : hash) {
                stringBuilder.append(Integer.toString((0xff & b) + 0x100, 16).substring(1));
            }
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        this.password = stringBuilder.toString();
        System.out.println(this.password);
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Authentication auth && auth.username.equals(username) && auth.password.equals(password);
    }
}
