package com.fpit.webservice.move;

import java.util.List;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fpit.data.DBRow;
import com.fpit.util.Util;
import com.fpit.util.WSUtil;
import com.fpit.webservice.CommonResource;
import com.fpit.webservice.update.UpdateResource;

/**
 * Endpoint for moving a player.
 */
@Path("/pitapi/move")
public class MoveResource extends CommonResource {
	private static final Logger logger = LoggerFactory
			.getLogger(UpdateResource.class);

	@POST
	public Response move(final MoveRequest request) {
		if (request == null || request.player_key == null || request.x == null
				|| request.y == null || Math.abs(request.x) > 1
				|| Math.abs(request.y) > 1) {
			return WSUtil.badRequest("Invalid request");
		}

		try {
			MoveResponse response = new MoveResponse();
			List<DBRow> rows = getDb()
					.query("select a.id, a.x, a.y, a.next_action_time "
							+ "from player p join actor a on a.id = p.actor_id "
							+ "where p.player_key = ? ", request.player_key);
			if (rows.size() != 1) {
				return WSUtil.badRequest("Invalid player_key");
			}

			DBRow row = rows.get(0);
			Long nextMillis = row.getLong("next_action_time");
			long currMillis = System.currentTimeMillis();
			if (nextMillis > currMillis) {
				response.moved = false;
				response.waitMillis = nextMillis - currMillis;
				return WSUtil.success(response);
			}

			int nextX = Util.mymod(row.getInt("x") + request.x);
			int nextY = Util.mymod(row.getInt("y") + request.y);
			getDb().directExecute(
					"update actor set x = ?, y = ?, next_action_time = ? "
							+ "where id = ? and x = ? and y = ? and next_action_time = ?",
					nextX, nextY, currMillis + 1000, row.getLong("id"),
					row.getInt("x"), row.getInt("y"), nextMillis);

			response.moved = true;
			response.waitMillis = 1000;

			return WSUtil.success(response);
		} catch (Throwable th) {
			logger.error("move error", th);
			return WSUtil.error();
		}
	}
}
