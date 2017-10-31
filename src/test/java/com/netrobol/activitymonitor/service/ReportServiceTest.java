package com.netrobol.activitymonitor.service;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.time.LocalDate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.netrobol.activitymonitor.MocksApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MocksApplication.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class ReportServiceTest {

	private final static String dataFileName = "test-data/exampleActivityMonitor.csv";

	@Mock
	SenderService mockSenderService;

	@InjectMocks
	ReportService testObj = new ReportService();

	@Before
	public void beforeTest() {
		MockitoAnnotations.initMocks(this);
		testObj.getReportDates().clear();
	}

	@Test
	public void testGenerateBasicReport() throws Exception {
		File rawDataFile = new File(getClass().getClassLoader().getResource(dataFileName).getFile());
		LocalDate testReportDate = LocalDate.of(2017, 10, 30);
		assertThat(testObj.generateReport(rawDataFile, testReportDate, ""), is(true));
		assertThat(testObj.getLastReport().length(), equalTo(112));
		assertThat(testObj.generateReport(rawDataFile, testReportDate, ""), is(false));
		verify(mockSenderService).send(anyString(), anyString());
	}

}
