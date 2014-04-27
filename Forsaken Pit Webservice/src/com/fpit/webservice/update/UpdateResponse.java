package com.fpit.webservice.update;

import java.util.List;

import com.fpit.webservice.player.PlayerType;

/**
 * Response object for a success update call.
 */
public class UpdateResponse {
	public long player_id;
	public PlayerType player_type;
	public String player_name;
	public int depth;
	public List<PitObject> objs;
}
