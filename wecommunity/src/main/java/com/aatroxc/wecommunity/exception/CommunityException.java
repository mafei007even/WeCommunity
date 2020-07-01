package com.aatroxc.wecommunity.exception;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * Base exception of the project.
 *
 * @author mafei007
 * @date 2020/4/18 23:51
 */


public abstract class CommunityException extends RuntimeException {

	/**
	 * Error errorData.
	 */
	private Object errorData;

	public CommunityException(String message) {
		super(message);
	}

	public CommunityException(String message, Throwable cause) {
		super(message, cause);
	}

	@NonNull
	public abstract HttpStatus getStatus();

	@Nullable
	public Object getErrorData() {
		return errorData;
	}

	/**
	 * Sets error errorData.
	 *
	 * @param errorData error data
	 * @return current exception.
	 */
	@NonNull
	public CommunityException setErrorData(@Nullable Object errorData) {
		this.errorData = errorData;
		return this;
	}
}
