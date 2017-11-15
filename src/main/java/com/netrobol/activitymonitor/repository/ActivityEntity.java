package com.netrobol.activitymonitor.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.*;

import lombok.*;

@ToString
@Data
@Entity
@Table(name = "ACTIVITIES_INFO")
@NoArgsConstructor
public class ActivityEntity {

	@Id
	@GeneratedValue
	private Long id;

	@Column(name = "LOCATOR")
	private Integer locator;

	@Column(name = "APP_NAME")
	private String appName;

	@Column(name = "DATE_INFO")
	@Convert(converter = LocalDateAttributeConverter.class)
	private LocalDate recordDate = LocalDate.now();

	@Column(name = "LAST_UPDATED")
	@Convert(converter = LocalDateTimeAttributeConverter.class)
	private LocalDateTime lastUpdated = LocalDateTime.now();

	@Column(name = "TOTAL_SECONDS")
	private Long totalSeconds;

	public ActivityEntity(String appName, LocalDate date, Long totalSeconds) {
		this.appName = appName;
		this.totalSeconds = totalSeconds;
		this.recordDate = date;
		this.locator = generateLocator();
	}

	@PreUpdate
	public void preUpdate() {
		lastUpdated = LocalDateTime.now();
	}

	@PrePersist
	public void prePersist() {
		lastUpdated = LocalDateTime.now();
		this.locator = generateLocator();
	}

	private int generateLocator() {
		return (recordDate.toString() + appName).hashCode() & 0xfffffff;
	}

}
