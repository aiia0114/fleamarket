package com.example.fleamarketsystem;

import org.springframework.boot.SpringApplication;

public class TestFleamarketsystemApplication {

	public static void main(String[] args) {
		SpringApplication.from(FleamarketsystemApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
