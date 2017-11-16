package com.netrobol.activitymonitor.service;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.*;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;

import com.netrobol.activitymonitor.MockApplication;
import com.netrobol.activitymonitor.repository.ActivityEntityRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MockApplication.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class TaskListServiceTest {

	private static final String TEST_APP_NAME = "notepad.exe";

	@Mock
	ActivityEntityRepository repository;

	@InjectMocks
	TaskListService testObj = new TaskListService();

	@Before
	public void initTest() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testInitMethod() throws Exception {
		testObj.init();
	}

	@Test
	public void testExecutionMethod() throws Exception {
		testObj.init();
		testObj.runSystemCommand("cmd /c start /MIN " + TEST_APP_NAME, false);
		testObj.resetLastRun();
		for (int i = 0; i < 5; i++) {
			testObj.execute();
			if (i == 3) {
				killProcess(TEST_APP_NAME);
				break;
			}
			Thread.sleep(5000);
		}

		assertThat(testObj.getRunningTimeForApp(TEST_APP_NAME), lessThanOrEqualTo(15L));
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
