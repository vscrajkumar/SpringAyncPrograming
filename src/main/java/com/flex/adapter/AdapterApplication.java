package com.flex.adapter;

import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan({ "com.flex.adapter" })
@EnableAsync
@EnableScheduling
public class AdapterApplication {
	private static Logger LOG = LoggerFactory.getLogger(AdapterApplication.class);

	public static void main(String[] args) {
		LOG.info("------------------------------------Hello Rajkumar--------------------------------");
		System.setProperty("server.servlet.context-path", "/epi");
		SpringApplication.run(AdapterApplication.class, args);
	}
	
}
