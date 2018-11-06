package com.duplicall.screenAnalyse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
public class Application {

    public static ExecutorService executorService;
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        executorService = Executors.newFixedThreadPool(4);
    }
}
