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
			List<Rectangle> rects = Util.getNearbyRects(px, py);
			for (DBRow actorRow : Util.getNearbyActors(getDb(), plevel, rects,
					"")) {
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

			response.events = new ArrayList<>();
			for (DBRow eventRow : Util.getNearbyEvents(getDb(), plevel, rects)) {
				PitEvent event = new PitEvent();
				event.event_type = eventRow.getString("event_type");
				event.message = eventRow.getString("message");
				response.events.add(event);
			}

			response.leaderboard = new ArrayList<>();
			for (DBRow row : getDb()
					.query("select a.name, p.points from actor a join player p on p.actor_id = a.id order by p.points desc limit 10")) {
				Leader leader = new Leader();
				leader.name = row.getString("name");
				leader.points = row.getInt("points");
				response.leaderboard.add(leader);
			}

			return WSUtil.success(response);
		} catch (Throwable th) {
			logger.error("update error", th);
			return WSUtil.error();
		}
	}
}
