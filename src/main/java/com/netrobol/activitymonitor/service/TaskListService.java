package com.netrobol.activitymonitor.service;

import java.io.*;
import java.time.*;
import java.util.*;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TaskListService {

	protected final static String TASKLIST_CMD = "tasklist /FI \"STATUS eq running\" /FO CSV";
	public static String DATA_STORAGE = System.getProperty("user.home") + "/" + "activityMonitor.csv";

	Set<String> initialProcessesToExclude = new HashSet<>();
	public LocalDateTime lastRunTime = LocalDateTime.now();

	@Getter
	Map<String, Long> appRunningTimes = new HashMap<>();

	@Value("${app.excluded.extra}")
	String additionalProgramsToIgnore = "";

	public void init() {
		log.debug("Initializing service storage: {}", DATA_STORAGE);
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
		File dataStorage = new File(DATA_STORAGE);
		if (!dataStorage.exists()) {
			return;
		}

		try {
			Reader in = new FileReader(dataStorage);
			Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in);
			for (CSVRecord record : records) {
				String key = record.get(0) + ":" + record.get(1);
				appRunningTimes.put(key, Long.valueOf(record.get(2)));
			}
			log.debug("Finished loading previous results: {}", appRunningTimes);
		} catch (IOException e) {
			log.warn("Problems loading previous results. Storage will be deleted", e);
			dataStorage.delete();
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
		saveTimingInfo();
		lastRunTime = LocalDateTime.now();
	}

	private void saveTimingInfo() {
		StringBuilder data = new StringBuilder("Date, AppName, Seconds\n");
		for (Map.Entry<String, Long> entry : appRunningTimes.entrySet()) {
			String key = entry.getKey();
			String date = key.substring(0, key.indexOf(":"));
			String appName = key.substring(key.indexOf(":") + 1);
			data.append(date + "," + appName + "," + entry.getValue() + "\n");
		}
		try {
			FileUtils.writeStringToFile(new File(DATA_STORAGE), data.toString(), "UTF-8");
		} catch (IOException e) {
			log.warn("Problems writing to storage: {}", e.getMessage());
		}

	}

	private void processRunningTimes(Set<String> runningProcesses) {
		long timeInterval = Duration.between(lastRunTime, LocalDateTime.now()).getSeconds();
		LocalDate today = LocalDate.now();
		Map<String, Long> updatedApps = new HashMap<>();
		for (String processName : runningProcesses) {
			String key = today.toString() + ":" + processName;
			if (appRunningTimes.containsKey(key)) {
				appRunningTimes.replace(key, appRunningTimes.get(key) + timeInterval);
				updatedApps.put(key, appRunningTimes.get(key));
			} else {
				appRunningTimes.put(key, timeInterval);
			}
		}

		if (updatedApps.size() > 0) {
			log.debug("Running apps: {}", updatedApps);
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
		Runtime rt = Runtime.getRuntime();
		StringBuffer buf = new StringBuffer();
		BufferedReader input = null;
		try {
			Process pr = rt.exec(command);
			if (waitForResponse) {
				input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
				String line = null;
				while ((line = input.readLine()) != null) {
					buf.append(line + "\n");
				}

				checkForErrorCode(pr);
			}
			return buf.toString();
		} catch (IOException e) {
			log.error("Problems running command '{}': {}", command, e.getMessage());
			return null;
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
				}
			}
		}
	}

	private void checkForErrorCode(Process pr) {
		int returnCode = pr.exitValue();
		if (returnCode > 0) {
			log.error("Command not executed sucessfully. Error code:{}", returnCode);
		}
	}

	public String runSystemCommand(String command) {
		return runSystemCommand(command, true);
	}

	public Long getRunningTimeForApp(String testAppName, LocalDate date) {
		return appRunningTimes.get(date.toString() + ":" + testAppName);
	}

	public void resetLastRun() {
		lastRunTime = LocalDateTime.now();

	}

}
