package com.fpit.webservice.rest;

import java.util.List;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fpit.data.DBRow;
import com.fpit.util.WSUtil;
import com.fpit.webservice.CommonResource;

/**
 * Not to be confused with REpresentational State Transfer architecture
 */
@Path("/pitapi/rest")
public class RestResource extends CommonResource {
	private static final Logger logger = LoggerFactory
			.getLogger(RestResource.class);

	@POST
	public Response rest(RestRequest request) {
		if (request == null || request.player_key == null) {
			return WSUtil.badRequest("Invalid request");
		}
		try {
			RestResponse response = new RestResponse();
			List<DBRow> rows = getDb()
					.query("select a.id, a.health, a.max_health, a.next_action_time "
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
				response.waitMillis = nextMillis - currMillis;
				return WSUtil.success(response);
			}

			getDb().directExecute(
					"update actor set next_action_time = ?, health = ? where id = ? and next_action_time = ? and health = ?",
					currMillis + 1000,
					Math.min(row.getInt("max_health"),
							row.getInt("health") + 20), row.getLong("id"),
					nextMillis, row.getInt("health"));

			response.waitMillis = 1000;

			return WSUtil.success(response);
		} catch (Throwable th) {
			logger.error("move error", th);
			return WSUtil.error();
		}
	}
}
