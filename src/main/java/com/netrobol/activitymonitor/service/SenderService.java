package com.netrobol.activitymonitor.service;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SenderService {

	public void send(String content, String destinationType) {
		log.debug("Sending content to {}", destinationType);
	}

	protected void sendContentAsEmail(String content) {

	}
}
