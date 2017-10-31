package com.netrobol.activitymonitor;

import static org.mockito.Mockito.*;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.netrobol.activitymonitor.service.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class MocksApplication {
	public static void main(String[] args) {
		SpringApplication.run(MocksApplication.class, args);
	}

	@Bean
	@Primary
	public TaskListService getTaskListServiceBean() {
		log.debug("Creating {} bean mock", TaskListService.class.getSimpleName());
		return mock(TaskListService.class);
	}

	@Bean
	@Primary
	public ReportService getReportServiceBean() {
		log.debug("Creating {} bean mock", ReportService.class.getSimpleName());
		return mock(ReportService.class);
	}

	@Bean
	@Primary
	public SenderService getSenderServiceBean() {
		log.debug("Creating {} bean mock", SenderService.class.getSimpleName());
		return mock(SenderService.class);
	}
}