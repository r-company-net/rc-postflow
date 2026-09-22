package com.rcpostflow;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
@SpringBootApplication
@EnableScheduling
public class RcPostflowApplication {

	public static void main(String[] args) {
		SpringApplication.run(RcPostflowApplication.class, args);
	}
}
