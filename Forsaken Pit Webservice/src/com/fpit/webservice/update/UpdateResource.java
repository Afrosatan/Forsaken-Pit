package com.fpit.webservice.update;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fpit.Constants;
import com.fpit.data.DBRow;
import com.fpit.util.Util;
import com.fpit.util.WSUtil;
import com.fpit.webservice.CommonResource;
import com.fpit.webservice.player.PlayerType;

/**
 * Endpoint for status updates for a player.
 */
@Path("/pitapi/update")
public class UpdateResource extends CommonResource {
	private static final Logger logger = LoggerFactory
			.getLogger(UpdateResource.class);

	@POST
	public Response update(UpdateRequest request) {
		if (request == null || request.player_key == null) {
			return WSUtil.badRequest("Invalid request");
		}

		try {
			List<DBRow> rows = getDb()
					.query("select a.id, a.actor_type, a.name, a.level_depth, a.x, a.y, "
							+ "a.firepower, a.health, a.max_health, p.points "
							+ "from player p join actor a on a.id = p.actor_id "
							+ "where p.player_key = ? ", request.player_key);
			if (rows.size() != 1) {
				return WSUtil.badRequest("Invalid player_key");
			}

			DBRow playerRow = rows.get(0);
			UpdateResponse response = new UpdateResponse();
			response.player_id = playerRow.getLong("id");
			response.player_type = PlayerType.valueOf(playerRow.getString(
					"actor_type").substring(7));
			response.player_name = playerRow.getString("name");
			response.depth = playerRow.getInt("level_depth");
			response.firepower = playerRow.getInt("firepower");
			response.health = playerRow.getInt("health");
			response.max_health = playerRow.getInt("max_health");
			response.points = playerRow.getInt("points");
			response.objs = new ArrayList<>();

			int plevel = playerRow.getInt("level_depth");
			int px = playerRow.getInt("x");
			int py = playerRow.getInt("y");

			int minx = Util.mymod(px - 5);
			int maxx = Util.mymod(px + 5);
			int miny = Util.mymod(py - 5);
			int maxy = Util.mymod(py + 5);

			List<Rectangle> rects = new ArrayList<>();
			if (minx > maxx && miny > maxy) {
				rects.add(new Rectangle(minx, miny, Constants.MAP_SIZE - 1,
						Constants.MAP_SIZE - 1));
				rects.add(new Rectangle(0, miny, maxx, Constants.MAP_SIZE - 1));
				rects.add(new Rectangle(minx, 0, Constants.MAP_SIZE - 1, maxy));
				rects.add(new Rectangle(0, 0, maxx, maxy));
			} else if (minx > maxx) {
				rects.add(new Rectangle(minx, miny, Constants.MAP_SIZE - 1,
						maxy));
				rects.add(new Rectangle(0, miny, maxx, maxy));
			} else if (miny > maxy) {
				rects.add(new Rectangle(minx, miny, maxx,
						Constants.MAP_SIZE - 1));
				rects.add(new Rectangle(minx, 0, maxx, maxy));
			} else {
				rects.add(new Rectangle(minx, miny, maxx, maxy));
			}

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
				query.append("select a.id, a.actor_type, a.name, a.x, a.y "
						+ "from actor a "
						+ "where a.level_depth = ? and a.x >= ? and a.x <= ? "
						+ "and a.y >= ? and a.y <= ? and a.health > 0 ");
				params[pi++] = plevel;
				params[pi++] = rect.x;
				params[pi++] = rect.width; // the names aren't right i stored the right/bottom bounds
				params[pi++] = rect.y;
				params[pi++] = rect.height;
			}
			List<DBRow> actorRows = getDb().query(query.toString(), params);
			for (DBRow actorRow : actorRows) {
				PitObject obj = new PitObject();
				obj.id = actorRow.getLong("id");
				obj.name = actorRow.getString("name");
				obj.type = actorRow.getString("actor_type");
				obj.x = actorRow.getInt("x") - px;
				if (obj.x < -5) {
					obj.x += Constants.MAP_SIZE;
				} else if (obj.x > 5) {
					obj.x -= Constants.MAP_SIZE;
				}
				obj.x += 5;
				obj.y = actorRow.getInt("y") - py;
				if (obj.y < -5) {
					obj.y += Constants.MAP_SIZE;
				} else if (obj.y > 5) {
					obj.y -= Constants.MAP_SIZE;
				}
				obj.y += 5;
				response.objs.add(obj);
			}

			query = new StringBuilder();
			first = true;
			params = new Object[5 * rects.size()];
			pi = 0;
			for (Rectangle rect : rects) {
				if (first) {
					first = false;
				} else {
					query.append("\nUNION ALL ");
				}
				query.append("select e.event_type, e.message, e.event_time "
						+ "from game_event e "
						+ "where e.level_depth = ? and e.x >= ? and e.x <= ? "
						+ "and e.y >= ? and e.y <= ? ");
				params[pi++] = plevel;
				params[pi++] = rect.x;
				params[pi++] = rect.width; // the names aren't right i stored the right/bottom bounds
				params[pi++] = rect.y;
				params[pi++] = rect.height;
			}
			List<DBRow> eventRows = getDb().query(
					query.toString() + " order by event_time desc limit 10",
					params);
			response.events = new ArrayList<>();
			for (DBRow eventRow : eventRows) {
				PitEvent event = new PitEvent();
				event.event_type = eventRow.getString("event_type");
				event.message = eventRow.getString("message");
				response.events.add(event);
			}

			return WSUtil.success(response);
		} catch (Throwable th) {
			logger.error("update error", th);
			return WSUtil.error();
		}
	}
}
