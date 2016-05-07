package ca.edmssubmit.api;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import ca.edmssubmit.api.json.AuthRequest;
import ca.edmssubmit.api.json.AuthResponse;

public class Authenticate {
	public static String authenticate(String username, String password) throws ClientProtocolException, IOException, AuthenticationFailure {
		AuthRequest authRequest = new AuthRequest(username, password);
		String authRequestString = Globals.GSON.toJson(authRequest);
		
		CloseableHttpClient httpclient = HttpClients.createDefault();
		
		HttpPost httpPost = new HttpPost(Globals.API_BASE + "rest/auth/token/obtain/?format=json");
		httpPost.addHeader("Content-Type", "application/json");
		httpPost.addHeader("Accepts", "application/json");
		httpPost.setEntity(new StringEntity(authRequestString));
		CloseableHttpResponse httpResponse = httpclient.execute(httpPost);
		int statusCode = httpResponse.getStatusLine().getStatusCode();
		String response;
		try {
		    HttpEntity entity = httpResponse.getEntity();
		    response = EntityUtils.toString(entity);
		    EntityUtils.consume(entity);
		} finally {
			httpResponse.close();
		}
		
		AuthResponse responseDeserialized;
		
		try {
			responseDeserialized = Globals.GSON.fromJson(response, AuthResponse.class);
		} catch (RuntimeException e) {
			throw new AuthenticationFailure("Failed to read response: " + response, e);
		}
		
		if (statusCode != HttpStatus.SC_OK || responseDeserialized.nonFieldErrors != null) {
			throw new AuthenticationFailure(Globals.implodeString(responseDeserialized.nonFieldErrors));
		}
		
		return responseDeserialized.token;
	}
}
