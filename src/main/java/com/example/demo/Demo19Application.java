package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.example.demo", "uz.freight.bot"})
public class Demo19Application {
    public static void main(String[] args) {
        SpringApplication.run(Demo19Application.class, args);
    }
}
