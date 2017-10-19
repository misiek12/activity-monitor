package com.netrobol.activitymonitor.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.*;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.junit.Test;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TaskListServiceTest {

	private static final String TEST_APP_NAME = "notepad.exe";
	TaskListService testObj = new TaskListService();

	@Test
	public void testInitMethod() throws Exception {
		testObj.init();
	}

	@Test
	public void testExecutionMethod() throws Exception {
		testObj.init();
		testObj.runSystemCommand("cmd /c start /MIN notepad.exe", false);
		log.debug("Notepad started");

		for (int i = 0; i < 5; i++) {
			testObj.execute();
			if (i == 3) {
				killProcess(TEST_APP_NAME);
			}
			Thread.sleep(5000);
		}

		assertThat(testObj.getRunningTimes().get(TEST_APP_NAME), equalTo(15L));
	}

	private void killProcess(String processName) {
		log.debug("Killing {}", processName);
		String commandOutput = testObj
				.runSystemCommand(TaskListService.TASKLIST_CMD + " /FI  \"IMAGENAME eq " + processName + "\"");
		try {
			Reader in = new StringReader(commandOutput);
			Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in);
			for (CSVRecord record : records) {
				String processId = record.get(1);
				if (StringUtils.isEmpty(processId)) {
					continue;
				}
				testObj.runSystemCommand("taskkill /F /pid " + processId);
			}
		} catch (IOException e) {
			log.error("Problems processing kill command", e);
		}
	}
}
