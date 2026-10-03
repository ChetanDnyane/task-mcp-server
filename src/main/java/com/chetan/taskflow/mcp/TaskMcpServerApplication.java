package com.chetan.taskflow.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TaskMcpServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(
				TaskMcpServerApplication.class,
				args
		);
	}
}