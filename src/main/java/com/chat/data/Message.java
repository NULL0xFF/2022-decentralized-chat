package com.chat.data;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Optional;

public class Message implements Serializable {

    private final Authentication authentication;
    private final String message;

    public Message(Authentication auth, String msg) {
        authentication = auth;
        message = msg;
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    @Override
    public String toString() {
        return MessageFormat.format("{0}{1}", Optional.ofNullable(authentication).map(auth -> (auth.getUsername() + ": ")).orElse(""), message);
    }

}
