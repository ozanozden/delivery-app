package com.learn.drivers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class DriverTrackerApplication {
    public static void main(String[] args) {
        SpringApplication.run(DriverTrackerApplication.class, args);
    }
}
