package com.example.giftmoney;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class GiftMoneyApplication {

    public static void main(String[] args) {
        SpringApplication.run(GiftMoneyApplication.class, args);
    }

}
