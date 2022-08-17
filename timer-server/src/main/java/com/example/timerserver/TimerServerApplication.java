package com.example.timerserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TimerServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TimerServerApplication.class, args);
	}

}
