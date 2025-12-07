package com.api.group9;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class Group9Application {

	public static void main(String[] args) {

	Dotenv dotenv = Dotenv.configure()
        .directory("D:/haoht203/Documents/source_code/api/group9") 
        .ignoreIfMalformed()
        .ignoreIfMissing()
        .load();
    dotenv.entries().forEach(entry -> {
        System.setProperty(entry.getKey(), entry.getValue());
    });
	
		SpringApplication.run(Group9Application.class, args);


	}
	

}
