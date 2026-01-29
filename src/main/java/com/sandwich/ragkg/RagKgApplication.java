package com.sandwich.ragkg;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.sandwich.ragkg.dao.mapper")
@SpringBootApplication
public class RagKgApplication {

    public static void main(String[] args) {
        SpringApplication.run(RagKgApplication.class, args);
    }

}
