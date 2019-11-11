package com.db.awmd.challenge.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model class to represent Error Details.
 * 
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorDetails {

	private int status;
	private String error;
	private String message;
	private String path;

}
