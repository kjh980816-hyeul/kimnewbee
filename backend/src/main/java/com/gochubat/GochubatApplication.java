package com.gochubat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class GochubatApplication {

	public static void main(String[] args) {
		SpringApplication.run(GochubatApplication.class, args);
	}

}
