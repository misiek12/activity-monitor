package com.netrobol.activitymonitor.service;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.netrobol.activitymonitor.MockApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MockApplication.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class ReportServiceTest {

	@Mock
	SenderService mockSenderService;

	@InjectMocks
	ReportService testObj = new ReportService();

	@Before
	public void beforeTest() {
		MockitoAnnotations.initMocks(this);
	}

}
