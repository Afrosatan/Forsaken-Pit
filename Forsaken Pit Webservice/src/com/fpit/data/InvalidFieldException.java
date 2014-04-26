package com.fpit.data;

/**
 * Thrown when an invalid field is requested from a DBRow.
 */
public class InvalidFieldException extends RuntimeException {
	private static final long serialVersionUID = 946129427067275352L;

	public InvalidFieldException(String message) {
		super(message);
	}

	public InvalidFieldException(String message, Throwable ex) {
		super(message, ex);
	}
}
