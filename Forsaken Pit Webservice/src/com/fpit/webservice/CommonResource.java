package com.fpit.webservice;

import java.sql.SQLException;

import com.fpit.data.DBControl;
import com.fpit.data.DBException;

/**
 * Resource endpoint parent class
 */
public abstract class CommonResource {
	private static DBControl db = null;

	protected DBControl getDb() {
		return db;
	}

	public static void initDb(String dburl, String dbuser, String dbpassword)
			throws DBException, SQLException {
		DBControl db = null;
		try {
			db = new DBControl();
			db.init(dburl, dbuser, dbpassword, false);
		} catch (Throwable th) {
			if (db != null) {
				db.close();
			}
			throw th;
		}
		CommonResource.db = db;
	}
}
