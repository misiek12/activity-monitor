package com.netrobol.activitymonitor.service;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.*;
import java.time.LocalDate;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;

import com.netrobol.activitymonitor.ActivityMonitorApplication;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ActivityMonitorApplication.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class TaskListServiceTest {

	private static final String TEST_APP_NAME = "notepad.exe";

	@ClassRule
	public static TemporaryFolder testFolder = new TemporaryFolder();
	private final static String dataFileName = "testData.csv";

	@BeforeClass
	public static void initTestClass() {
		TaskListService.DATA_STORAGE = testFolder.getRoot().getAbsolutePath() + "/" + dataFileName;
	}

	@InjectMocks
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
		testObj.resetLastRun();
		for (int i = 0; i < 5; i++) {
			testObj.execute();
			if (i == 3) {
				killProcess(TEST_APP_NAME);
				break;
			}
			Thread.sleep(3000);
		}

		assertThat(testObj.getRunningTimeForApp(TEST_APP_NAME, LocalDate.now()), lessThanOrEqualTo(10L));
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
