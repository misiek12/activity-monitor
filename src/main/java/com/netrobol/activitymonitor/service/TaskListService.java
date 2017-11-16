package com.netrobol.activitymonitor.service;

import java.io.*;
import java.time.*;
import java.util.*;

import javax.annotation.Resource;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.exec.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.netrobol.activitymonitor.repository.ActivityEntity;
import com.netrobol.activitymonitor.repository.ActivityEntityRepository;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TaskListService implements ExecuteResultHandler {

	final static String TASKLIST_CMD = "tasklist /FI \"STATUS eq running\" /FO CSV";

	Set<String> initialProcessesToExclude = new HashSet<>();
	LocalDateTime lastRunTime = LocalDateTime.now();

	@Getter
	Map<String, Long> appRunningTimes = new HashMap<>();

	@Value("${app.excluded.extra}")
	String additionalProgramsToIgnore = "";

	@Resource
	ActivityEntityRepository repository;

	public void init() {
		log.debug("Initializing service");
		String commandOutput = runSystemCommand(TASKLIST_CMD);
		initialProcessesToExclude = processTaskListData(commandOutput);
		addAdditionalProgramsToExclude();
		loadPreviousRunningTimes();
		log.info("Inititialized with {} processes: {}", initialProcessesToExclude.size(), initialProcessesToExclude);
	}

	private void addAdditionalProgramsToExclude() {
		String[] programNames = additionalProgramsToIgnore.split(",");
		for (String programName : programNames) {
			initialProcessesToExclude.add(programName.trim());
		}
	}

	private void loadPreviousRunningTimes() {
		List<ActivityEntity> appsRunningToday = repository.findByRecordDate(LocalDate.now());
		log.debug("Found {} records already processed today", appsRunningToday.size());
		for (ActivityEntity entity : appsRunningToday) {
			appRunningTimes.put(entity.getAppName(), entity.getTotalSeconds());
		}
	}

	public void execute() {
		String commandOutput = runSystemCommand(TASKLIST_CMD);
		Set<String> runningProcesses = processTaskListData(commandOutput);
		runningProcesses.removeAll(initialProcessesToExclude);
		if (runningProcesses.size() == 0) {
			lastRunTime = LocalDateTime.now();
			return;
		}
		processRunningTimes(runningProcesses);
		saveTimingInfo(runningProcesses);
		lastRunTime = LocalDateTime.now();
	}

	private void saveTimingInfo(Set<String> runningProcesses) {
		try {
			log.debug("Saving timing info for {} processes", runningProcesses.size());
			LocalDate today = LocalDate.now();

			List<ActivityEntity> entitiesToSave = new ArrayList<>();
			for (String processName : runningProcesses) {
				List<ActivityEntity> entities = repository
						.findByLocator(ActivityEntity.generateLocator(processName, today));
				if (entities == null || entities.isEmpty()) {
					ActivityEntity entity = new ActivityEntity(processName, today, appRunningTimes.get(processName));
					entitiesToSave.add(entity);
				} else {
					entities.get(0).setTotalSeconds(appRunningTimes.get(processName));
					entitiesToSave.add(entities.get(0));
				}
			}
			entitiesToSave = repository.save(entitiesToSave);
			repository.flush();

			log.debug("Saved process info: {}", entitiesToSave);
		} catch (Exception e) {
			log.error("Problems saving execution info", e);
		}
	}

	private void processRunningTimes(Set<String> runningProcesses) {
		long timeInterval = Duration.between(lastRunTime, LocalDateTime.now()).getSeconds();
		LocalDate today = LocalDate.now();

		for (String processName : runningProcesses) {
			String key = processName;
			if (appRunningTimes.containsKey(key)) {
				appRunningTimes.replace(key, appRunningTimes.get(key) + timeInterval);
			} else {
				appRunningTimes.put(key, timeInterval);
			}
		}

		log.debug("{} running apps: {}", appRunningTimes.size(), appRunningTimes.keySet());
		for (Map.Entry<String, Long> entry : appRunningTimes.entrySet()) {
			log.debug("{} running for {} secs today", entry.getKey(), entry.getValue());
		}

	}

	private Set<String> processTaskListData(String commandOutput) {
		Set<String> processNames = new HashSet<>();
		try {
			Reader in = new StringReader(commandOutput);
			Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(in);
			for (CSVRecord record : records) {
				processNames.add(record.get(0));
			}
		} catch (IOException e) {
			log.error("Problems processing CSV data", e);
		}
		return processNames;
	}

	protected String runSystemCommand(String command, boolean waitForResponse) {
		try {
			log.debug("Executing: {}", command);
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			CommandLine commandline = CommandLine.parse(command);
			DefaultExecutor exec = new DefaultExecutor();
			PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
			exec.setStreamHandler(streamHandler);
			if (waitForResponse) {
				int returnCode = exec.execute(commandline);
				log.debug("Command finished code: {}", returnCode);
			} else {
				exec.execute(commandline, this);
			}
			return (outputStream.toString());
		} catch (Exception e) {
			log.error("Problems executing: {}", command, e);
			return null;
		}
	}

	public String runSystemCommand(String command) {
		return runSystemCommand(command, true);
	}

	public Long getRunningTimeForApp(String testAppName) {
		return appRunningTimes.get(testAppName);
	}

	public void resetLastRun() {
		lastRunTime = LocalDateTime.now();

	}

	@Override
	public void onProcessComplete(int arg0) {

	}

	@Override
	public void onProcessFailed(ExecuteException arg0) {
		log.debug("Command failed: {}", arg0);

	}

}
