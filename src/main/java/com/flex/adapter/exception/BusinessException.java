package com.flex.adapter.exception;

public class BusinessException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String message;

	public BusinessException(String message) {
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
