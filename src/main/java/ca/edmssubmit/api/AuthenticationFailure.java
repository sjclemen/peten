package ca.edmssubmit.api;

public class AuthenticationFailure extends Exception {
	public AuthenticationFailure(String message) {
		super(message);
	}

	public AuthenticationFailure(String message, Exception e) {
		super(message, e);
	}
}
