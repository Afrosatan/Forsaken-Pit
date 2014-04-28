package com.fpit;

import java.awt.Rectangle;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fpit.data.DBRow;
import com.fpit.util.Util;
import com.fpit.webservice.CommonResource;

/**
 * Create/update monster actors.
 */
public class MonsterAI extends CommonResource implements Runnable {
	private static final Logger logger = LoggerFactory
			.getLogger(MonsterAI.class);

	private Random rand = new Random();
	private MonsterType[] mtypes = MonsterType.values();

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(1000);

				long currentMillis = System.currentTimeMillis();

				int mcnt = ((Number) getDb()
						.query("select count(*) cnt from actor where actor_type like 'monster%' and health > 0 ")
						.get(0).getObject("cnt")).intValue();
				for (; mcnt < Constants.MAP_SIZE * Constants.MAP_SIZE / 100; mcnt++) {
					createMonster();
				}

				for (DBRow monster : getDb()
						.query("select * from actor where actor_type like 'monster%' and next_action_time <= ? and health > 0 ",
								currentMillis)) {
					updateMonster(monster);
				}
			} catch (Throwable th) {
				logger.error("error in monsterai", th);
			}
		}
	}

	private void updateMonster(DBRow monster) throws SQLException {
		long currentMillis = System.currentTimeMillis();
		MonsterType type = MonsterType.valueOf(monster.getString("actor_type")
				.substring(8));
		List<Rectangle> rects = Util.getNearbyRects(monster.getInt("x"),
				monster.getInt("y"));
		List<DBRow> players = Util.getNearbyActors(getDb(),
				monster.getInt("level_depth"), rects,
				" and actor_type like 'player%' ");
		if (players.size() > 0) {
			DBRow row = players.get(rand.nextInt(players.size()));
			int dx = monster.getInt("x") - row.getInt("x");
			if (dx < -5) {
				dx += Constants.MAP_SIZE;
			} else if (dx > 5) {
				dx -= Constants.MAP_SIZE;
			}
			int dy = monster.getInt("y") - row.getInt("y");
			if (dy < -5) {
				dy += Constants.MAP_SIZE;
			} else if (dy > 5) {
				dy -= Constants.MAP_SIZE;
			}
			if (type.melee && (Math.abs(dx) > 1 || Math.abs(dy) > 1)) {
				//move closer
				if (dx > 0) {
					dx = 1;
				} else if (dx < 0) {
					dx = -1;
				}
				if (dy > 0) {
					dy = 1;
				} else if (dy < 0) {
					dy = -1;
				}

				Map<String, Object> fvs = new HashMap<>();
				fvs.put("next_action_time", currentMillis + 1100);
				fvs.put("x", Util.mymod(monster.getInt("x") - dx));
				fvs.put("y", Util.mymod(monster.getInt("y") - dy));
				getDb().update("actor", monster, fvs);
			} else {
				//attack player
				Map<String, Object> fvs = new HashMap<>();
				fvs.put("health",
						row.getInt("health") - monster.getInt("firepower"));
				getDb().update("actor", row, fvs);

				fvs.clear();
				fvs.put("next_action_time", currentMillis + 1500);
				getDb().update("actor", monster, fvs);

				Util.createDamageEvent(getDb(), monster, row);
			}
		} else {
			//move randomly
			int dx = 0;
			int dy = 0;
			int r = rand.nextInt(8);
			switch (r) {
			case 0:
			case 1:
			case 2:
				dy = -1;
				break;
			case 5:
			case 6:
			case 7:
				dy = 1;
				break;
			default:
				dy = 0;
				break;
			}
			switch (r) {
			case 0:
			case 3:
			case 5:
				dx = -1;
				break;
			case 2:
			case 4:
			case 7:
				dx = 1;
				break;
			default:
				dx = 0;
				break;
			}

			Map<String, Object> fvs = new HashMap<>();
			fvs.put("next_action_time", currentMillis + 1100);
			fvs.put("x", Util.mymod(monster.getInt("x") + dx));
			fvs.put("y", Util.mymod(monster.getInt("y") + dy));
			getDb().update("actor", monster, fvs);
		}
	}

	private void createMonster() throws SQLException {
		MonsterType type = mtypes[rand.nextInt(mtypes.length)];
		Map<String, Object> fvs = new HashMap<>();
		fvs.put("actor_type", "monster_" + type.toString());
		fvs.put("name", type.name);
		fvs.put("level_depth", 1);
		fvs.put("x", rand.nextInt(Constants.MAP_SIZE));
		fvs.put("y", rand.nextInt(Constants.MAP_SIZE));
		fvs.put("next_action_time", System.currentTimeMillis());
		fvs.put("firepower", type.firepower);
		fvs.put("health", type.health);
		fvs.put("max_health", type.health);
		getDb().directInsert("actor", fvs);
	}

	private enum MonsterType {
		ogre("Ogre", true, 20, 160),
		slug("Laser Slug", false, 20, 100);

		String name;
		boolean melee;
		int firepower;
		int health;

		private MonsterType(String name, boolean melee, int firepower,
				int health) {
			this.name = name;
			this.melee = melee;
			this.firepower = firepower;
			this.health = health;
		}
	}
}