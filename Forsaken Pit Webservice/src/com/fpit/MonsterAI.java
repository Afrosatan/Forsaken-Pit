package com.fpit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fpit.webservice.CommonResource;

/**
 * Create/update monster actors.
 */
public class MonsterAI extends CommonResource implements Runnable {
	private static final Logger logger = LoggerFactory
			.getLogger(MonsterAI.class);

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(1000);

				long currentMillis = System.currentTimeMillis();

			} catch (Throwable th) {
				logger.error("error in monsterai", th);
			}
		}
	}
}