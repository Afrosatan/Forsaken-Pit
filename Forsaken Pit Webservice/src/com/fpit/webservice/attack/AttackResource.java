package com.fpit.webservice.attack;

import java.sql.SQLException;
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

/**
 * Endpoint for a player to attack an actor
 */
@Path("/pitapi/attack")
public class AttackResource extends CommonResource {
	private static final Logger logger = LoggerFactory
			.getLogger(AttackResource.class);

	@POST
	public Response attack(final AttackRequest request) {
		if (request == null || request.player_key == null
				|| request.actor_id == null) {
			return WSUtil.badRequest("Invalid request");
		}

		try {
			AttackResponse response = new AttackResponse();
			List<DBRow> rows = getDb()
					.query("select a.id, a.name, a.level_depth, a.x, a.y, a.next_action_time, a.firepower, a.max_health, p.points, a.health "
							+ "from player p join actor a on a.id = p.actor_id "
							+ "where p.player_key = ? ", request.player_key);
			if (rows.size() != 1) {
				return WSUtil.badRequest("Invalid player_key");
			}
			DBRow row = rows.get(0);
			if (row.getInt("health") <= 0) {
				return WSUtil.badRequest("You died");
			}

			Long nextMillis = row.getLong("next_action_time");
			long currMillis = System.currentTimeMillis();
			if (nextMillis > currMillis) {
				response.attacked = false;
				response.waitMillis = nextMillis - currMillis;
				return WSUtil.success(response);
			}

			rows = getDb().query(
					"select a.id, a.name, a.level_depth, a.x, a.y, a.health from actor a "
							+ "where a.id = ? ", request.actor_id);
			if (rows.size() != 1) {
				response.invalidTarget = true;
				return WSUtil.success(response);
			}
			DBRow actorRow = rows.get(0);

			if (!row.getInt("level_depth").equals(
					actorRow.getInt("level_depth"))) {
				response.invalidTarget = true;
				return WSUtil.success(response);
			}

			int dx = Math.abs(row.getInt("x") - actorRow.getInt("x"));
			int dy = Math.abs(row.getInt("y") - actorRow.getInt("y"));
			if (dx > Constants.MAP_SIZE / 2) {
				dx = Constants.MAP_SIZE - dx;
			}
			if (dy > Constants.MAP_SIZE / 2) {
				dy = Constants.MAP_SIZE - dy;
			}

			if (dx > 5 || dy > 5) {
				response.invalidTarget = true;
				return WSUtil.success(response);
			}

			if (actorRow.getInt("health") <= 0) {
				response.invalidTarget = true;
				return WSUtil.success(response);
			}

			getDb().directExecute(
					"update actor set health = ? where id = ? and health = ?",
					actorRow.getInt("health") - row.getInt("firepower"),
					actorRow.getLong("id"), actorRow.getInt("health"));
			getDb().directExecute(
					"update actor set next_action_time = ? where id = ? and next_action_time = ?",
					currMillis + 1500, row.getLong("id"), nextMillis);

			int newPoints = 1;
			if (actorRow.getInt("health") <= row.getInt("firepower")) {
				newPoints += 5;
			}
			addPlayerPoints(row, request.player_key, newPoints);

			Util.createDamageEvent(getDb(), row, actorRow);

			response.attacked = true;
			response.waitMillis = 1500;

			return WSUtil.success(response);
		} catch (Throwable th) {
			logger.error("move error", th);
			return WSUtil.error();
		}
	}

	private void addPlayerPoints(DBRow playerRow, String player_key,
			int newPoints) throws SQLException {
		int modPoints = playerRow.getInt("points") % 50;
		if (modPoints + newPoints >= 50) {
			getDb().directExecute(
					"update actor set firepower = ?, max_health = ? where id = ? and firepower = ? and max_health = ? ",
					playerRow.getInt("firepower") + 2,
					playerRow.getInt("max_health") + 10,
					playerRow.getLong("id"), playerRow.getInt("firepower"),
					playerRow.getInt("max_health"));
		}
		getDb().directExecute(
				"update player set points = ? where player_key = ? and points = ?",
				playerRow.getInt("points") + newPoints, player_key,
				playerRow.getInt("points"));
	}
}
