package com.campus.take_out;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.HashMap;

@Slf4j
@SpringBootApplication
@ServletComponentScan
@EnableTransactionManagement
public class CampusTakeOutApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusTakeOutApplication.class, args);
        log.info("runnning!");
    }

}
