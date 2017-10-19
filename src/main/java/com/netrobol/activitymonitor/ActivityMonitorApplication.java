package com.netrobol.activitymonitor;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.netrobol.activitymonitor.service.TaskListService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableScheduling
@SpringBootApplication
public class ActivityMonitorApplication {

	@Autowired
	ApplicationContext ctx;

	public static void main(String[] args) {
		log.debug("Starting...");
		SpringApplication.run(ActivityMonitorApplication.class, args);
	}

	@PostConstruct
	public void init() {
		try {
			TaskListService service = ctx.getBean(TaskListService.class);
			service.init();
		} catch (Exception e) {
			log.error("Problems initializing service", e);
		}
	}

	@Scheduled(fixedRateString = "${app.schedule.frequency}", initialDelay = 1000)
	public void checkActiviti() {
		try {
			TaskListService service = ctx.getBean(TaskListService.class);
			service.execute();
		} catch (Exception e) {
			log.error("Problems running service", e);
		}
	}
}
