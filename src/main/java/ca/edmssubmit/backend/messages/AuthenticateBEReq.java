package ca.edmssubmit.backend.messages;

import ca.edmssubmit.api.Authenticate;
import ca.edmssubmit.backend.tasktypes.HttpRequestTask;

public class AuthenticateBEReq extends HttpRequestTask<String> {
	private final String username;
	private final String password;
	
	public AuthenticateBEReq(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	@Override
	public String doCall() throws Exception {
		return Authenticate.authenticate(username, password);
	}

}
