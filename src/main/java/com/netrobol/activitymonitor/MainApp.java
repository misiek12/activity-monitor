package com.netrobol.activitymonitor;

import java.sql.SQLException;
import java.time.LocalDate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.netrobol.activitymonitor.service.ReportService;
import com.netrobol.activitymonitor.service.TaskListService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableScheduling
@EnableJpaRepositories(basePackages = "com.netrobol.activitymonitor.repository")
@SpringBootApplication
public class MainApp {

	@Autowired
	ApplicationContext ctx;

	@Autowired
	DataSource mainDataSource;

	public static void main(String[] args) {
		log.debug("Starting...");
		SpringApplication.run(MainApp.class, args);
	}

	@Autowired
	TaskListService taskInfoService;

	@Autowired
	ReportService reportService;

	@PostConstruct
	public void init() {
		try {
			log.debug("DataSource {}", mainDataSource.getConnection().getMetaData().getURL());
		} catch (SQLException e) {
			log.error("Problems getting db info", e);
		}
		taskInfoService.init();
	}

	@PreDestroy
	public void destroy() {
		log.debug("Application closing");
	}

	@Scheduled(fixedRateString = "${app.schedule.frequency.check}", initialDelayString = "${app.schedule.initialDelay}")
	public void checkActiviti() {
		taskInfoService.execute();
	}

	@Scheduled(cron = "${app.schedule.report.cron}")
	public void generateReport() {
		LocalDate reportDate = LocalDate.now().minusDays(1);
		boolean result = reportService.generateReport(reportDate, "");
		if (result) {
			log.debug("Report generated sucessfully for {}", reportDate);
		}
	}
}
