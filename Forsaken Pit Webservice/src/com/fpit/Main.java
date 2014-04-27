package com.fpit;

import java.io.FileInputStream;
import java.net.URI;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fpit.data.DBException;
import com.fpit.util.GsonMessageBodyHandler;
import com.fpit.webservice.CommonResource;
import com.fpit.webservice.attack.AttackResource;
import com.fpit.webservice.move.MoveResource;
import com.fpit.webservice.player.CreatePlayerResource;
import com.fpit.webservice.rest.RestResource;
import com.fpit.webservice.update.UpdateResource;

/**
 * Main executable class to bootstrap the grizzly http server using jersey endpoints.
 */
public class Main {
	private static final Logger logger = LoggerFactory.getLogger(Main.class);

	private static ResourceConfig createResourceConfig() {
		final ResourceConfig resourceConfig = new ResourceConfig();

		//web service endpoints
		resourceConfig.register(CreatePlayerResource.class);
		resourceConfig.register(UpdateResource.class);
		resourceConfig.register(MoveResource.class);
		resourceConfig.register(AttackResource.class);
		resourceConfig.register(RestResource.class);

		//other resources
		resourceConfig.register(GsonMessageBodyHandler.class);

		return resourceConfig;
	}

	public static void main(String[] args) {
		if (args.length == 0) {
			logger.error("properties not provided", new Exception());
			return;
		}

		Properties prop = new Properties();
		try {
			FileInputStream in = new FileInputStream(args[0]);
			prop.load(in);
			in.close();
		} catch (Exception ex) {
			logger.error("Error loading properties", ex);
			return;
		}

		final URI uri = URI.create(prop.getProperty("fpit.webserviceurl"));

		try {
			CommonResource.initDb(prop.getProperty("fpit.dburl"),
					prop.getProperty("fpit.dbuser"),
					prop.getProperty("fpit.dbpassword"));
		} catch (DBException | SQLException ex) {
			logger.error("Error initializing database", ex);
			return;
		}

		final HttpServer httpServer = GrizzlyHttpServerFactory
				.createHttpServer(uri, createResourceConfig());

		final Thread cleanupThread = new Thread(new Cleanup());
		cleanupThread.setDaemon(true);
		cleanupThread.start();

		logger.info("Application started... {}", uri);
		try {
			while (true) {
				Thread.sleep(10000);
			}
		} catch (InterruptedException ex) {
			logger.error("Interrupted?", ex);
		} finally {
			try {
				httpServer.shutdown().get();
			} catch (InterruptedException | ExecutionException ex) {
				logger.error("Error shutting down gracefully?", ex);
				httpServer.shutdownNow();
			}
		}
	}
}
