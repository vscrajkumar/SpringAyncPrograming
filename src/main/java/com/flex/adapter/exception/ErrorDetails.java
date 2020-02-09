package com.flex.adapter.exception;

import java.util.Date;

import lombok.Data;

@Data
public class ErrorDetails {

	public ErrorDetails(Date timestamp, String message, String details) {
		super();
	}
}
