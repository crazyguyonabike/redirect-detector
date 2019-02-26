package com.ufp.redirect;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.concurrent.CompletableFuture;

import com.ufp.redirect.service.MainService;

import org.apache.log4j.Logger;

public class Main {
    private static Logger logger = Logger.getLogger(Main.class);

    public static void main(String [] args) {
        AbstractApplicationContext context = new ClassPathXmlApplicationContext(new String[] {"applicationContext.xml"});
        MainService mainService = (MainService)context.getBean("mainService");
        CompletableFuture completableFuture = CompletableFuture.supplyAsync(() -> mainService.run());
        completableFuture.join();
        context.close();
    }
}
