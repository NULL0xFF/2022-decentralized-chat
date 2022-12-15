package com.chat.data;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.Optional;

public class Message implements Serializable {

    private final Authentication authentication;
    private final String message;

    public Message(Authentication auth, String msg) {
        authentication = auth;
        message = Objects.requireNonNullElse(msg, "");
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    public String getMessage() {
        return message;
    }

    public boolean isCommand() {
        return message.length() > 2 && message.charAt(0) == '/';
    }

    @Override
    public String toString() {
        return MessageFormat.format("{0}{1}", Optional.ofNullable(authentication).map(auth -> (auth.getUsername() + ": ")).orElse(""), message);
    }
}
