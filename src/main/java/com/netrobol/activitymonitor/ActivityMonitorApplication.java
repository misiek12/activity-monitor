package com.netrobol.activitymonitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class ActivityMonitorApplication {

	public static void main(String[] args) {
		log.debug("Starting...");
		SpringApplication.run(ActivityMonitorApplication.class, args);
	}
}
