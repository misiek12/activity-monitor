package com.netrobol.activitymonitor.service;

import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TaskListService {

	protected static final org.slf4j.Logger eventLog = org.slf4j.LoggerFactory.getLogger("eventLog");

	public static final String TASKLIST_CMD = "tasklist /FI \"STATUS eq running\" /FO CSV";

	Set<String> excludedProcesses = null;
	public LocalDateTime lastRunTime = LocalDateTime.now();

	@Getter
	Map<String, Long> runningTimes = new HashMap<>();

	public void init() {
		log.debug("Initializing service");
		String commandOutput = runSystemCommand(TASKLIST_CMD);
		excludedProcesses = processTaskListData(commandOutput);
		eventLog.info("Inititialized with {} processes: {}", excludedProcesses.size(), excludedProcesses);
	}

	public void execute() {
		String commandOutput = runSystemCommand(TASKLIST_CMD);
		Set<String> runningProcesses = processTaskListData(commandOutput);
		runningProcesses.removeAll(excludedProcesses);
		if (runningProcesses.size() > 0) {
			processRunningTimes(runningProcesses);
			log.debug("Timing info: {}", runningTimes);
		}
		lastRunTime = LocalDateTime.now();
	}

	private void processRunningTimes(Set<String> runningProcesses) {
		long timeInterval = Duration.between(lastRunTime, LocalDateTime.now()).getSeconds();

		for (String processName : runningProcesses) {
			if (runningTimes.containsKey(processName)) {
				runningTimes.replace(processName, runningTimes.get(processName) + timeInterval);
			} else {
				runningTimes.put(processName, timeInterval);
			}
		}
	}

	private Set<String> processTaskListData(String commandOutput) {
		Set<String> processNames = new HashSet<>();

		if (StringUtils.isEmpty(commandOutput)) {
			log.warn("Input data for tasklist processing is empty. No action");
			return processNames;
		}

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
		log.debug("Running: {}", command);
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

}
