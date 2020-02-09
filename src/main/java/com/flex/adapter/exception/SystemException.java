package com.flex.adapter.exception;

public class SystemException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String message;

	public SystemException(String message) {
		super(message);
		this.message = message;

	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return message;

	}
}
