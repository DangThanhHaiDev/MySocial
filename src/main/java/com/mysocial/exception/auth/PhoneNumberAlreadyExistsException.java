package com.mysocial.exception.auth;

public class PhoneNumberAlreadyExistsException extends RuntimeException {
    public PhoneNumberAlreadyExistsException(String phone) {
        super("Phone number already exists: " + phone);
    }
}