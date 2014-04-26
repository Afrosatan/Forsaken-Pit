package com.fpit.data;

import java.sql.SQLException;

/**
 * Interface to use a ConnectionWrapper in a transaction.
 */
public interface RunInTransaction {
	void run(ConnectionWrapper connect) throws SQLException;
}