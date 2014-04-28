package com.fpit.util;

import java.awt.Rectangle;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fpit.Constants;
import com.fpit.data.DBControl;
import com.fpit.data.DBRow;

public class Util {
	public static int mymod(int x) {
		if (x >= Constants.MAP_SIZE) {
			x -= Constants.MAP_SIZE;
		} else if (x < 0) {
			x += Constants.MAP_SIZE;
		}
		return x;
	}

	public static List<Rectangle> getNearbyRects(int x, int y) {
		int minx = Util.mymod(x - 5);
		int maxx = Util.mymod(x + 5);
		int miny = Util.mymod(y - 5);
		int maxy = Util.mymod(y + 5);

		List<Rectangle> rects = new ArrayList<>();
		if (minx > maxx && miny > maxy) {
			rects.add(new Rectangle(minx, miny, Constants.MAP_SIZE - 1,
					Constants.MAP_SIZE - 1));
			rects.add(new Rectangle(0, miny, maxx, Constants.MAP_SIZE - 1));
			rects.add(new Rectangle(minx, 0, Constants.MAP_SIZE - 1, maxy));
			rects.add(new Rectangle(0, 0, maxx, maxy));
		} else if (minx > maxx) {
			rects.add(new Rectangle(minx, miny, Constants.MAP_SIZE - 1, maxy));
			rects.add(new Rectangle(0, miny, maxx, maxy));
		} else if (miny > maxy) {
			rects.add(new Rectangle(minx, miny, maxx, Constants.MAP_SIZE - 1));
			rects.add(new Rectangle(minx, 0, maxx, maxy));
		} else {
			rects.add(new Rectangle(minx, miny, maxx, maxy));
		}
		return rects;
	}

	public static List<DBRow> getNearbyActors(DBControl db, int level,
			List<Rectangle> rects, String bonusWhere) throws SQLException {
		StringBuilder query = new StringBuilder();
		boolean first = true;
		Object[] params = new Object[5 * rects.size()];
		int pi = 0;
		for (Rectangle rect : rects) {
			if (first) {
				first = false;
			} else {
				query.append("\nUNION ALL ");
			}
			query.append("select a.* from actor a "
					+ "where a.level_depth = ? and a.x >= ? and a.x <= ? "
					+ "and a.y >= ? and a.y <= ? and a.health > 0 "
					+ bonusWhere + " ");
			params[pi++] = level;
			params[pi++] = rect.x;
			params[pi++] = rect.width; // the names aren't right i stored the right/bottom bounds
			params[pi++] = rect.y;
			params[pi++] = rect.height;
		}
		return db.query(query.toString(), params);
	}

	public static List<DBRow> getNearbyEvents(DBControl db, int level,
			List<Rectangle> rects) throws SQLException {
		StringBuilder query = new StringBuilder();
		boolean first = true;
		Object[] params = new Object[5 * rects.size()];
		int pi = 0;
		for (Rectangle rect : rects) {
			if (first) {
				first = false;
			} else {
				query.append("\nUNION ALL ");
			}
			query.append("select * from game_event e "
					+ "where e.level_depth = ? and e.x >= ? and e.x <= ? "
					+ "and e.y >= ? and e.y <= ? ");
			params[pi++] = level;
			params[pi++] = rect.x;
			params[pi++] = rect.width; // the names aren't right i stored the right/bottom bounds
			params[pi++] = rect.y;
			params[pi++] = rect.height;
		}
		return db
				.query(query.toString() + " order by event_time desc limit 10",
						params);
	}

	public static void createDamageEvent(DBControl db, DBRow source,
			DBRow target) throws SQLException {
		Map<String, Object> fvs = new HashMap<>();
		if (target.getInt("health") > source.getInt("firepower")) {
			fvs.put("event_type", "attack");
			fvs.put("message",
					source.getString("name")
							+ " attacked "
							+ target.getString("name")
							+ " for "
							+ source.getInt("firepower")
							+ " ("
							+ (target.getInt("health") - source
									.getInt("firepower")) + " health left)");
		} else {
			fvs.put("event_type", "kill");
			fvs.put("message",
					source.getString("name") + " killed "
							+ target.getString("name"));
		}
		fvs.put("level_depth", source.getInt("level_depth"));
		fvs.put("x", source.getInt("x"));
		fvs.put("y", source.getInt("y"));
		fvs.put("event_time", System.currentTimeMillis());
		fvs.put("source_actor_id", source.getLong("id"));
		fvs.put("target_actor_id", target.getLong("id"));
		db.directInsert("game_event", fvs);
	}
}
