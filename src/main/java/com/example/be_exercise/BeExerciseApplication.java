package com.example.be_exercise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class BeExerciseApplication {

	public static void main(String[] args) {
		SpringApplication.run(BeExerciseApplication.class, args);
	}

}
