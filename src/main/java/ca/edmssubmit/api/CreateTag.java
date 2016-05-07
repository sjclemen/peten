package ca.edmssubmit.api;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import ca.edmssubmit.api.json.CreateTagRequest;
import ca.edmssubmit.api.json.CreateTagResponse;

public class CreateTag {
	public static CreateTagResponse createTag(String apiToken, String name, String colour) throws IOException {
		CreateTagRequest ctr = new CreateTagRequest();
		ctr.color = colour;
		ctr.label = name;
		String createTagSerialized = Globals.GSON.toJson(ctr);
		
		CloseableHttpClient httpclient = HttpClients.createDefault();
		
		HttpPost httpPost = new HttpPost(Globals.API_BASE + "tags/tags/?format=json");
		httpPost.addHeader("Authorization", "Token " + apiToken);
		httpPost.addHeader("Content-Type", "application/json");
		httpPost.addHeader("Accepts", "application/json");
		httpPost.setEntity(new StringEntity(createTagSerialized));
		CloseableHttpResponse httpResponse = httpclient.execute(httpPost);

		String response = null;
		try {
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
				    HttpEntity entity = httpResponse.getEntity();
				    response = EntityUtils.toString(entity);
				    EntityUtils.consume(entity);
			}
		} finally {
			httpResponse.close();
		}
		CreateTagResponse createTagResponse = Globals.GSON.fromJson(response, CreateTagResponse.class);
		
		if (createTagResponse.id == null) {
			throw new IOException("Failed to create tag, ID blank. Duplicate tag? " + createTagResponse.label);
		}
		return createTagResponse;
	}

}
