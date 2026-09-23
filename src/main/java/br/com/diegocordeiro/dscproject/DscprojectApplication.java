package br.com.diegocordeiro.dscproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class DscprojectApplication {

	public static void main(String[] args) {
		SpringApplication.run(DscprojectApplication.class, args);
	}

}
