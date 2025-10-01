package com.pastebin.config;

import com.pastebin.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Override
    public void run(String... args) throws Exception {
        userService.createAdminIfNotExists("lilluucore", "admin@pastebin.com", "01180730");
        System.out.println("Admin user initialized: lilluucore");
    }
}
