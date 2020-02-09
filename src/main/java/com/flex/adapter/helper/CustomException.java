/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flex.adapter.helper;

public class CustomException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String customMessage = "";

	public CustomException(String friendlyMessage, Throwable t, String customMsg) {
		super(friendlyMessage, t);
		customMessage = customMsg;
	}

	public CustomException(String friendlyMessage, String customMsg) {
		super(friendlyMessage);
		customMessage = customMsg;
	}

	public String toString() {

		return customMessage;
	}

}
