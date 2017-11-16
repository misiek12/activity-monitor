package com.netrobol.activitymonitor.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ReportService {

	@Getter
	private String lastReport = "";

	@Autowired
	SenderService senderService;

	public boolean generateReport(LocalDate date, String destination) {

		try {
			log.debug("Generating report...");

			StringBuilder reportBuilder = new StringBuilder("Application info for " + date + "\n");

			// Reader in = new FileReader(f);
			// Iterable<CSVRecord> records =
			// CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in);
			// for (CSVRecord record : records) {
			// if (!date.toString().equals(record.get(0))) {
			// continue;
			// }
			// String appName = record.get(1);
			// String runningTime =
			// getStringDuration(Integer.valueOf(record.get(2)));
			// reportBuilder.append(appName + ": " + runningTime + "\n");
			// }

			reportBuilder.append("\nGenerated on " + LocalDateTime.now() + "\n");
			lastReport = reportBuilder.toString();
			senderService.send(lastReport, destination);

		} catch (Exception e) {
			log.error("Problems generating report", e);
			lastReport = null;
			return false;
		}
		return true;
	}

	private String getStringDuration(int aDuration) {
		String result = "";

		int hours = 0, minutes = 0, seconds = 0;

		hours = aDuration / 3600;
		minutes = (aDuration - hours * 3600) / 60;
		seconds = (aDuration - (hours * 3600 + minutes * 60));

		result = String.format("%02d:%02d:%02d", hours, minutes, seconds);
		return result;
	}
}
