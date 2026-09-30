package com.spoony.backend.infrastructure.web;

import java.io.IOException;

public class PayloadTooLargeException extends IOException {

    public PayloadTooLargeException(long maxBytes) {
        super("Request body exceeds the configured limit of " + maxBytes + " bytes");
    }
}
