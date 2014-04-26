package com.fpit.webservice.player;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fpit.data.ConnectionWrapper;
import com.fpit.data.RunInTransaction;
import com.fpit.util.StringUtil;
import com.fpit.util.WSUtil;
import com.fpit.webservice.CommonResource;

/**
 * Endpoint for creating a new player
 */
@Path("/pitapi/player")
public class CreatePlayerResource extends CommonResource {
	private static final Logger logger = LoggerFactory
			.getLogger(CreatePlayerResource.class);

	@POST
	@Path("create")
	public Response createPlayer(final PlayerCreate request) {
		if (request == null || request.name == null || request.type == null) {
			return WSUtil.badRequest("Invalid request");
		}

		final String name = request.name.trim();
		if (name.length() == 0 || name.length() > 20) {
			return WSUtil.badRequest("Invalid name (must be 1-20 letters)");
		}

		try {
			final String player_key = StringUtil.randomAlphaNum(10);
			getDb().inTransaction(new RunInTransaction() {
				@Override
				public void run(ConnectionWrapper connect) throws SQLException {
					Random rand = new Random();

					Map<String, Object> fvs = new HashMap<>();
					fvs.put("actor_type", "player_"
							+ request.type.toString().toLowerCase());
					fvs.put("name", name);
					fvs.put("level_depth", 1);
					fvs.put("x", rand.nextInt(100));
					fvs.put("y", rand.nextInt(100));
					Object actor_id = connect.directInsert("actor", fvs);

					fvs.clear();
					fvs.put("player_key", player_key);
					fvs.put("actor_id", actor_id);
					connect.directInsert("player", fvs);
				}
			});
			NewPlayer player = new NewPlayer();
			player.player_key = player_key;
			return WSUtil.success(player);
		} catch (Throwable th) {
			logger.error("Error creating player", th);
			return WSUtil.error();
		}
	}
}
