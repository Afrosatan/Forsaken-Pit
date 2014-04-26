package com.fpit.data;

/**
 * Exception for some errors in DBControl.
 */
public class DBException extends Exception {
	private static final long serialVersionUID = -6958744448681566915L;

	DBException(String message) {
		super(message);
	}

	DBException(String message, Exception ex) {
		super(message, ex);
	}
}
