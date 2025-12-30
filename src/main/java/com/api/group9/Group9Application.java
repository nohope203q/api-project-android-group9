package com.api.group9;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class Group9Application {

	public static void main(String[] args) {

	Dotenv dotenv = Dotenv.configure()
        .directory("C:/Users/GiaHuan/Downloads/src/api-project-android-group9") 
        .ignoreIfMalformed()
        .ignoreIfMissing()
        .load();
    dotenv.entries().forEach(entry -> {
        System.setProperty(entry.getKey(), entry.getValue());
    });
	
		SpringApplication.run(Group9Application.class, args);


	}
	

}
