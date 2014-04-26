package com.fpit.util;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

/**
 * Methods to assist with common JAX-RS and web service steps.
 */
public class WSUtil {
	public static Response badRequest(String message) {
		return headers(
				Response.status(Response.Status.BAD_REQUEST)
						.entity(new Message(message))
						.type(MediaType.APPLICATION_JSON_TYPE)).build();
	}

	public static Response success(Object entity) {
		return headers(Response.ok(entity, MediaType.APPLICATION_JSON_TYPE))
				.build();
	}

	public static Response error() {
		return headers(Response.status(Response.Status.INTERNAL_SERVER_ERROR))
				.build();
	}

	private static ResponseBuilder headers(ResponseBuilder builder) {
		//internet explorer likes to cache everything...which doesn't make much sense for this webservice
		builder.header("Cache-Control", "no-cache, no-store, must-revalidate");
		builder.header("Pragma", "no-cache");
		builder.header("Expires", "0");
		return builder;
	}
}
