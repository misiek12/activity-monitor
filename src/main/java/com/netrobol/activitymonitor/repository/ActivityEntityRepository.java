package com.netrobol.activitymonitor.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityEntityRepository extends JpaRepository<ActivityEntity, Long> {

	List<ActivityEntity> findByLocator(Integer locator);
}