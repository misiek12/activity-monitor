package com.netrobol.activitymonitor;

import java.io.File;
import java.time.LocalDate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.netrobol.activitymonitor.service.ReportService;
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

	@Autowired
	TaskListService taskInfoService;

	@Autowired
	ReportService reportService;

	@PostConstruct
	public void init() {
		try {
			taskInfoService.init();
		} catch (Exception e) {
			log.error("Problems initializing service", e);
		}
	}

	@PreDestroy
	public void destroy() {
		log.debug("Application closing");
	}

	@Scheduled(fixedRateString = "${app.schedule.frequency.check}", initialDelayString = "${app.schedule.initialDelay}")
	public void checkActiviti() {
		try {
			taskInfoService.execute();
		} catch (Exception e) {
			log.error("Problems running service", e);
		}
	}

	@Scheduled(cron = "${app.schedule.report.cron}")
	public void generateReport() {
		LocalDate reportDate = LocalDate.now().minusDays(1);
		boolean result = reportService.generateReport(new File(TaskListService.DATA_STORAGE), reportDate, "");
		if (result) {
			log.debug("Report generated sucessfully for {}", reportDate);
		}
	}
}
