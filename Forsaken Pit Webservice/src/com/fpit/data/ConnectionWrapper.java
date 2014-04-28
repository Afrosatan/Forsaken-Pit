package com.fpit.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fpit.data.DBMetaData.DBFieldData;

/**
 * Wrapper around a single database Connection.
 */
public class ConnectionWrapper {
	private static final Logger logger = LoggerFactory
			.getLogger(ConnectionWrapper.class);

	private final Connection connection;
	private int transactionDepth = 0;

	public ConnectionWrapper(Connection connection) {
		this.connection = connection;
	}

	public List<DBRow> query(String sql, Object... params) throws SQLException {
		try (Statement statement = queryPre(sql, params)) {
			return getRowsFromResultSet(queryExecute(statement, sql));
		}
	}

	private Statement queryPre(String sql, Object... params)
			throws SQLException {
		logger.trace("SQL: " + sql);
		logger.trace("Parameters: " + Arrays.toString(params));
		Statement statement;
		if (params == null || params.length == 0) {
			statement = connection.createStatement();
		} else {
			PreparedStatement ps = null;
			statement = ps = connection.prepareStatement(sql);
			for (int i = 1; i <= params.length; i++) {
				ps.setObject(i, params[i - 1]);
			}
		}
		return statement;
	}

	private ResultSet queryExecute(Statement statement, String sql)
			throws SQLException {
		ResultSet rs = null;
		if (statement instanceof PreparedStatement) {
			rs = ((PreparedStatement) statement).executeQuery();
		} else {
			rs = statement.executeQuery(sql);
		}
		return rs;
	}

	public static List<DBRow> getRowsFromResultSet(ResultSet rs)
			throws SQLException {
		DBMetaData dbm = new DBMetaData(rs.getMetaData());
		List<DBRow> retval = new ArrayList<DBRow>();
		while (rs.next()) {
			retval.add(new DBRow(dbm, rs));
		}
		return retval;
	}

	/**
	 * Insert a record into the database, with some preset column values, returning the generated key or null if there wasn't one.<br>
	 * The fieldValues map will not be modified.
	 * @param tableName The name of the table to insert a record into.
	 * @param fieldValues Any column key-value pairs that should be set on the new record.
	 * @return Generated key or null if there wasn't one.
	 * @throws SQLException
	 */
	public Object directInsert(String tableName, Map<String, Object> fieldValues)
			throws SQLException {
		final List<Object> parameters = new ArrayList<Object>();
		final StringBuilder sql = new StringBuilder("INSERT INTO ");
		sql.append(tableName);
		sql.append(" (");
		boolean first = true;
		List<String> fields = new ArrayList<String>(fieldValues.keySet());
		for (String field : fields) {
			if (fieldValues.get(field) == null) {
				continue;
			}
			if (first) {
				first = false;
			} else {
				sql.append(", ");
			}
			sql.append(field);
		}
		sql.append(") values (");
		first = true;
		for (String field : fields) {
			if (fieldValues.get(field) == null) {
				continue;
			}
			if (first) {
				first = false;
			} else {
				sql.append(", ");
			}
			sql.append("?");
			parameters.add(fieldValues.get(field));
		}
		sql.append(")");

		logger.trace("SQL: " + sql);
		logger.trace("Parameters: " + Arrays.toString(parameters.toArray()));

		class KeyStore {
			Object key;
		}
		final KeyStore key = new KeyStore();

		inTransaction(new RunInTransaction() {
			@Override
			public void run(ConnectionWrapper connect) throws SQLException {
				try (PreparedStatement ps = connection.prepareStatement(
						sql.toString(), Statement.RETURN_GENERATED_KEYS)) {
					for (int i = 1; i <= parameters.size(); i++) {
						ps.setObject(i, parameters.get(i - 1));
					}
					ps.executeUpdate();

					try (ResultSet gkeys = ps.getGeneratedKeys()) {
						if (gkeys.next()) {
							key.key = gkeys.getObject(1);
						}
					}
				}
			}
		});

		return key.key;
	}

	/**
	 * Update the table/row provided with new values.
	 * @param tableName The name of the table to update a record in.
	 * @param row Row with the current DB values of the row to be updated. This row will be updated with values from fieldValues if the update succeeds.
	 * @param fieldValues Map with the new column values for the row.
	 */
	public void update(String tableName, DBRow row,
			Map<String, Object> fieldValues) throws SQLException {
		final List<Object> parameters = new ArrayList<Object>();
		final StringBuilder sql = new StringBuilder("UPDATE ");
		sql.append(tableName);
		sql.append(" SET ");
		boolean first = true;
		for (Entry<String, Object> entry : fieldValues.entrySet()) {
			Object obj = row.getObject(entry.getKey());
			if (obj == null) {
				if (entry.getValue() == null) {
					continue;
				}
			} else if (entry.getValue() != null) {
				if (entry.getValue().equals(obj)) {
					continue;
				}
			}
			if (first) {
				first = false;
			} else {
				sql.append(", ");
			}
			sql.append(entry.getKey());
			sql.append(" = ? ");
			parameters.add(entry.getValue());
		}
		if (first) { // no changes
			return;
		}
		sql.append(" WHERE ");
		first = true;
		for (DBFieldData field : row.getMetadata().getFieldData().values()) {
			if (first) {
				first = false;
			} else {
				sql.append(" AND ");
			}
			Object obj = row.getObject(field.getName());
			if (obj != null) {
				sql.append(field.getName());
				sql.append(" = ? ");
				parameters.add(obj);
			} else {
				sql.append(field.getName());
				sql.append(" IS NULL ");
			}
		}

		logger.trace("SQL: " + sql);
		logger.trace("Parameters: " + Arrays.toString(parameters.toArray()));

		inTransaction(new RunInTransaction() {
			@Override
			public void run(ConnectionWrapper connect) throws SQLException {
				try (PreparedStatement ps = connection.prepareStatement(sql
						.toString())) {
					for (int i = 1; i <= parameters.size(); i++) {
						ps.setObject(i, parameters.get(i - 1));
					}
					int n = ps.executeUpdate();
					if (n == 0) {
						throw new SQLException(
								"No rows affected during update, rolling back");
					} else if (n > 1) {
						throw new SQLException(
								"Multiple rows affected during update, rolling back");
					}
				}
			}
		});

		for (Entry<String, Object> entry : fieldValues.entrySet()) {
			row.setObject(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Execute arbitrary sql.
	 */
	public void directExecute(String sql) throws SQLException {
		logger.trace("Direct SQL: " + sql);
		try (Statement statement = connection.createStatement()) {
			statement.execute(sql);
		}
	}

	/**
	 * Execute arbitrary sql.
	 */
	public void directExecute(String sql, Object... params) throws SQLException {
		logger.trace("Direct SQL: " + sql);
		logger.trace("Parameters: " + Arrays.toString(params));
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			if (params != null) {
				for (int i = 1; i <= params.length; i++) {
					ps.setObject(i, params[i - 1]);
				}
			}
			ps.execute();
		}
	}

	private void startTransaction() throws SQLException {
		if (transactionDepth == 0) {
			connection.setAutoCommit(false);
		}
		transactionDepth++;
	}

	private void commitTransaction() throws SQLException {
		if (transactionDepth == 1) {
			// no try/catch - if the commit fails, it should rollback which will
			// reset the autocommit flag and transaction depth
			connection.commit();
			connection.setAutoCommit(true);
			transactionDepth--;
		} else if (transactionDepth == 0) {
			throw new SQLException("Transaction depth passed on commit");
		} else {
			transactionDepth--;
		}
	}

	private void rollbackTransaction() throws SQLException {
		if (transactionDepth == 1) {
			try {
				connection.rollback();
			} finally {
				connection.setAutoCommit(true);
				// if setting the autocommit fails after failing to rollback, 
				// we really should just exit the application because it's totally screwed most likely...
				transactionDepth--;
			}
		} else if (transactionDepth == 0) {
			throw new SQLException("Transaction depth passed on rollback");
		} else {
			transactionDepth--;
		}
	}

	public void inTransaction(RunInTransaction trans) throws SQLException {
		try {
			startTransaction();
			trans.run(this);
			commitTransaction();
		} catch (Throwable th) {
			logger.error("Error in transaction, rolling back", th);
			rollbackTransaction();
			throw th;
		}
	}

	public void close() throws SQLException {
		connection.close();
	}
}
