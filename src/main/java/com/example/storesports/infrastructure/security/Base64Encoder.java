package com.example.storesports.infrastructure.security;

import java.util.Base64;

public class Base64Encoder {
    public static void main(String[] args) {
        String secret = "3G9k7mX4pQ8rT2vW9yZ1aB5cD6eF8gH2jK4mN5pQ7rT9vW0yZ1aB2cD3eF4gH5jK6mN7pQ8rT";
        String encoded = Base64.getEncoder().encodeToString(secret.getBytes());
        System.out.println(encoded);
    }
    // lấy Secret để làm JWT
}
