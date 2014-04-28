package com.fpit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fpit.webservice.CommonResource;

/**
 * Ongoing cleanup, removing old event/actor/player records from the database
 */
public class Cleanup extends CommonResource implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(Cleanup.class);

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(10000);

				long currentMillis = System.currentTimeMillis();

				getDb().directExecute(
						"delete from game_event where event_time < ?",
						currentMillis - 10000);

				getDb().directExecute(
						"delete from player p where p.actor_id in (select a.id from actor a where a.next_action_time < ?)",
						currentMillis - 300000);
				getDb().directExecute(
						"delete from actor where next_action_time < ?",
						currentMillis - 300000);
			} catch (Throwable th) {
				logger.error("error in cleanup", th);
			}
		}
	}
}
