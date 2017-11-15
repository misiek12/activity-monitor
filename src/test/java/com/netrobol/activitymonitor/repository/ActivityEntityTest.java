package com.netrobol.activitymonitor.repository;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.netrobol.activitymonitor.MockApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MockApplication.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class ActivityEntityTest {

	@Resource
	private ActivityEntityRepository repository;

	@Test
	public void testCreationOfOneItem() {
		ActivityEntity activity = new ActivityEntity("test.exe", LocalDate.now(), 100L);

		ActivityEntity activitySaved = repository.save(activity);
		assertNotNull(activitySaved.getId());

		ActivityEntity activityRetrived = repository.findOne(activitySaved.getId());
		assertThat(activityRetrived, equalTo(activitySaved));
	}

	@After
	public void testCleanup() {
		repository.deleteAllInBatch();
	}

	@Test
	public void testQueryForSpecificDateAndAppNameUsingLocator() {
		List<ActivityEntity> activities = new ArrayList<>();

		ActivityEntity todaysActivity = new ActivityEntity("test.exe", LocalDate.now(), 100L);
		activities.add(todaysActivity);
		activities.add(new ActivityEntity("test.exe", LocalDate.now().minusDays(1), 50L));
		activities.add(new ActivityEntity("test.exe", LocalDate.now().minusDays(2), 60L));

		List<ActivityEntity> savedActivities = repository.save(activities);
		assertThat(savedActivities, hasSize(3));
		assertNotNull(todaysActivity.getId());

		ActivityEntity queryResults = repository.getOne(todaysActivity.getId());
		queryResults.setTotalSeconds(200L);
		ActivityEntity updatedResults = repository.save(queryResults);
		assertThat(updatedResults.getTotalSeconds(), equalTo(200L));

	}
}
