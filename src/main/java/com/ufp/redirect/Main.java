package com.ufp.redirect;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.ufp.redirect.service.MainService;

public class Main {
    public static void main(String [] args) {
        AbstractApplicationContext context = new ClassPathXmlApplicationContext(new String[] {"applicationContext.xml"});
        MainService mainService = (MainService)context.getBean("mainService");
        mainService.run();
        context.close();
    }
}
