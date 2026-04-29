package com.community.jpremium.proxy.api.resolver;

public class ResolverException
extends RuntimeException {
    public ResolverException(String text) {
        super(text);
    }

    public ResolverException(Throwable throwable) {
        super(throwable);
    }
}

