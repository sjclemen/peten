package ca.edmssubmit.api.json;

import java.util.List;

public class AuthResponse {
	public String token;
	public List<String> nonFieldErrors;
	public String username;
	public String password;
}
