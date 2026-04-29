package com.community.jpremium.common.exception;

public class UserMessageException
extends Exception {
    private final String messagePath;
    private final String messageArgument;
    private final Boolean suppressed;

    public String getMessagePath() {
        return this.messagePath;
    }

    public String getMessageArgument() {
        return this.messageArgument;
    }

    public Boolean isSuppressed() {
        return this.suppressed;
    }

    public UserMessageException(String messagePath) {
        this(messagePath, "", false);
    }

    public UserMessageException(String messagePath, String messageArgument) {
        this(messagePath, messageArgument, false);
    }

    public UserMessageException(String messagePath, Boolean suppressed) {
        this(messagePath, "", suppressed);
    }

    public UserMessageException(String messagePath, String messageArgument, Boolean suppressed) {
        this.messagePath = messagePath;
        this.messageArgument = messageArgument;
        this.suppressed = suppressed;
    }
}

