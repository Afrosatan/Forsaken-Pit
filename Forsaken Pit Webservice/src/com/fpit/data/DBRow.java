package com.fpit.data;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.fpit.data.DBMetaData.DBFieldData;

/**
 * A single row of results from a query, or a record from a database table.<br>
 * Just a wrapper around a key-value pair of object names to their values from JDBC.
 */
public class DBRow {
	private DBMetaData metadata = null;
	private Map<String, Object> values = new HashMap<String, Object>();

	public DBRow(DBMetaData dbm, ResultSet rs) throws SQLException {
		this.metadata = dbm;
		if (rs != null) {
			for (DBFieldData field : dbm.getFieldData().values()) {
				values.put(field.getName(),
						rs.getObject(field.getColumnNumber()));
			}
		} else {
			for (DBFieldData field : dbm.getFieldData().values()) {
				values.put(field.getName(), null);
			}
		}
	}

	/**
	 * Return the value from the corresponding getXXX function depending on the type of the given field.
	 */
	public Object getObject(String field) {
		DBFieldData data = getFieldData(field);
		switch (data.getType()) {
		case CHAR:
		case VARCHAR:
			return getString(field);
		case BOOL:
			return getBool(field);
		case DOUBLE:
			return getDouble(field);
		case FLOAT:
			return getFloat(field);
		case DECIMAL:
			return getDecimal(field);
		case INT:
			return getInt(field);
		case LONG:
			return getLong(field);
		case BINARY:
			return getBytes(field);
		default:
			throw new InvalidFieldException("Field [" + field
					+ "] does not have a known representation");
		}
	}

	/**
	 * Change a key-value pair for this row. The class of value will be checked.
	 */
	public void setObject(String field, Object value) {
		DBFieldData data = getFieldData(field);
		if (value != null) {
			boolean correctType;
			switch (data.getType()) {
			case CHAR:
			case VARCHAR:
				correctType = value instanceof String;
				break;
			case BINARY:
				correctType = value instanceof byte[];
				break;
			case BOOL:
				correctType = value instanceof Boolean;
				break;
			case DOUBLE:
				correctType = value instanceof Double;
				break;
			case FLOAT:
				correctType = value instanceof Float;
				break;
			case INT:
				correctType = value instanceof Integer;
				break;
			case LONG:
				correctType = value instanceof Long;
				break;
			case DECIMAL:
				correctType = value instanceof BigDecimal;
				break;
			default:
				correctType = false;
				break;
			}
			if (!correctType)
				throw new InvalidFieldException(
						"Invalid field type for field [" + field + "]: "
								+ data.getType() + " "
								+ (value.getClass().getName()));
		}
		values.put(field, value);
	}

	/**
	 * Returns the String value for CHAR, VARCHAR, NVARCHAR, and CLOB field types.
	 */
	public String getString(String field) {
		DBFieldData data = getFieldData(field);
		switch (data.getType()) {
		case CHAR:
		case VARCHAR:
			return (String) values.get(field);
		default:
			throw new InvalidFieldException("Field [" + field
					+ "] does not have a String representation");
		}
	}

	/**
	 * Returns the Long value for the LONG field type.
	 */
	public Long getLong(String field) {
		DBFieldData data = getFieldData(field);
		switch (data.getType()) {
		case LONG:
			return (Long) values.get(field);
		default:
			throw new InvalidFieldException("Field [" + field
					+ "] does not have a Long representation");
		}
	}

	/**
	 * Returns the byte[] value for the BINARY field type.
	 */
	public byte[] getBytes(String field) {
		DBFieldData data = getFieldData(field);
		switch (data.getType()) {
		case BINARY:
			return (byte[]) values.get(field);
		default:
			throw new InvalidFieldException("Field [" + field
					+ "] does not have a byte[] representation");
		}
	}

	/**
	 * Returns the Integer value for the INT field type.
	 */
	public Integer getInt(String field) {
		DBFieldData data = getFieldData(field);
		switch (data.getType()) {
		case INT:
			return (Integer) values.get(field);
		default:
			throw new InvalidFieldException("Field [" + field
					+ "] does not have a Integer representation");
		}
	}

	/**
	 * Returns the Float value for the FLOAT field type.
	 */
	public Float getFloat(String field) {
		DBFieldData data = getFieldData(field);
		switch (data.getType()) {
		case FLOAT:
			return (Float) values.get(field);
		default:
			throw new InvalidFieldException("Field [" + field
					+ "] does not have a Float representation");
		}
	}

	/**
	 * Returns the Double value for the DOUBLE field type.
	 */
	public Double getDouble(String field) {
		DBFieldData data = getFieldData(field);
		switch (data.getType()) {
		case DOUBLE:
			return (Double) values.get(field);
		default:
			throw new InvalidFieldException("Field [" + field
					+ "] does not have a Float representation");
		}
	}

	/**
	 * Returns the BigDecimal value for the DECIMAL field type.
	 */
	public BigDecimal getDecimal(String field) {
		DBFieldData data = getFieldData(field);
		switch (data.getType()) {
		case DECIMAL: {
			return (BigDecimal) values.get(field);
		}
		default:
			throw new InvalidFieldException("Field [" + field
					+ "] does not have a BigDecimal representation");
		}
	}

	/**
	 * Returns the Boolean value for the BOOL field type.
	 */
	public Boolean getBool(String field) {
		DBFieldData data = getFieldData(field);
		switch (data.getType()) {
		case BOOL:
			return (Boolean) values.get(field);
		default:
			throw new InvalidFieldException("Field [" + field
					+ "] does not have a Boolean representation");
		}
	}

	/**
	 * Get the DBFieldData instance for the provided field.
	 */
	public DBFieldData getFieldData(String field) {
		DBFieldData data = metadata.getFieldData().get(field);
		if (data == null)
			throw new InvalidFieldException("Field [" + field + "] not in row");
		return data;
	}

	/**
	 * Metadata about the fields in the row.
	 */
	public DBMetaData getMetadata() {
		return metadata;
	}

	@Override
	public String toString() {
		return values.toString();
	}
}
