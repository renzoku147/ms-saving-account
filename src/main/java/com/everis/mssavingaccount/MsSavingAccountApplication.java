package com.everis.mssavingaccount;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableEurekaClient
@SpringBootApplication
public class MsSavingAccountApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsSavingAccountApplication.class, args);
	}

}
