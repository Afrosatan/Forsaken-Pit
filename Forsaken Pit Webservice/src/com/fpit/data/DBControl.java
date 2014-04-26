package com.fpit.data;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * Wrapper around a ComboPooledDataSource to initialize and create ConnectionWrappers for connections to a postgresql database.
 */
public class DBControl {
	private ComboPooledDataSource pool = null;

	/**
	 * @param readonly This is an option that causes runtime exceptions on any method calls of the DBControl object other than query.
	 */
	public void init(String url, String user, String password, boolean readonly)
			throws SQLException, DBException {
		if (pool != null) {
			throw new DBException("Connection already initialized");
		}

		pool = new ComboPooledDataSource();
		try {
			pool.setDriverClass("org.postgresql.Driver");
		} catch (PropertyVetoException ex) {
			throw new DBException("driver vetoed?", ex);
		}
		pool.setJdbcUrl(url);
		pool.setUser(user);
		pool.setPassword(password);
		pool.setMinPoolSize(5);
		pool.setAcquireIncrement(5);
		pool.setMaxPoolSize(20);
		// pool.setTestConnectionOnCheckin(true);
		pool.setTestConnectionOnCheckout(true);
		//pool.setIdleConnectionTestPeriod(60);
		pool.setPreferredTestQuery("SELECT 1");
		pool.setAcquireRetryAttempts(3);

		Connection connect = null;
		try {
			connect = getConnection();
		} finally {
			if (connect != null) {
				connect.close();
			}
		}
	}

	private Connection getConnection() throws SQLException {
		Connection connect = pool.getConnection();
		if (connect == null) {
			throw new SQLException("Connection was not acquired");
		}
		return connect;
	}

	/**
	 * @see ConnectionWrapper#inTransaction(RunInTransaction)
	 */
	public void inTransaction(RunInTransaction trans) throws SQLException {
		ConnectionWrapper wrapper = null;
		try {
			wrapper = new ConnectionWrapper(getConnection());
			wrapper.inTransaction(trans);
		} finally {
			if (wrapper != null) {
				wrapper.close();
			}
		}
	}

	/**
	 * @see ConnectionWrapper#query(String, Object...)
	 */
	public List<DBRow> query(String sql, Object... params) throws SQLException {
		ConnectionWrapper wrapper = null;
		try {
			wrapper = new ConnectionWrapper(getConnection());
			return wrapper.query(sql, params);
		} finally {
			if (wrapper != null) {
				wrapper.close();
			}
		}
	}

	/**
	 * @see ConnectionWrapper#update(String, DBRow, Map)
	 */
	public void update(String tableName, DBRow row,
			Map<String, Object> fieldValues) throws SQLException {
		ConnectionWrapper wrapper = null;
		try {
			wrapper = new ConnectionWrapper(getConnection());
			wrapper.update(tableName, row, fieldValues);
		} finally {
			if (wrapper != null) {
				wrapper.close();
			}
		}
	}

	/**
	 * @see ConnectionWrapper#insert(String, String, DBRow, Map)
	 */
	public void insert(String tableName, String generatedKeyField, DBRow row,
			Map<String, Object> fieldValues) throws SQLException {
		ConnectionWrapper wrapper = null;
		try {
			wrapper = new ConnectionWrapper(getConnection());
			wrapper.insert(tableName, generatedKeyField, row, fieldValues);
		} finally {
			if (wrapper != null) {
				wrapper.close();
			}
		}
	}

	/**
	 * @see ConnectionWrapper#directInsert(String, Map)
	 */
	public Object directInsert(String tableName, Map<String, Object> fieldValues)
			throws SQLException {
		ConnectionWrapper wrapper = null;
		try {
			wrapper = new ConnectionWrapper(getConnection());
			return wrapper.directInsert(tableName, fieldValues);
		} finally {
			if (wrapper != null) {
				wrapper.close();
			}
		}
	}

	/**
	 * @see ConnectionWrapper#delete(String, DBRow)
	 */
	public void delete(String tableName, DBRow row) throws SQLException {
		ConnectionWrapper wrapper = null;
		try {
			wrapper = new ConnectionWrapper(getConnection());
			wrapper.delete(tableName, row);
		} finally {
			if (wrapper != null) {
				wrapper.close();
			}
		}
	}

	/**
	 * @see ConnectionWrapper#directExecute(String, Object...)
	 */
	public void directExecute(String sql, Object... params) throws SQLException {
		ConnectionWrapper wrapper = null;
		try {
			wrapper = new ConnectionWrapper(getConnection());
			wrapper.directExecute(sql, params);
		} finally {
			if (wrapper != null) {
				wrapper.close();
			}
		}
	}

	public void close() {
		pool.close();
		pool = null;
	}

	public DBMetaData getTableMetaData(String tableName) throws SQLException {
		ConnectionWrapper wrapper = null;
		try {
			wrapper = new ConnectionWrapper(getConnection());
			return wrapper.getTableMetaData(tableName);
		} finally {
			if (wrapper != null) {
				wrapper.close();
			}
		}
	}
}
