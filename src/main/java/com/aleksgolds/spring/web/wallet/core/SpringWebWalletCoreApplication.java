package com.aleksgolds.spring.web.wallet.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;


@SpringBootApplication
@EnableRetry
public class SpringWebWalletCoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringWebWalletCoreApplication.class, args);
	}

}
