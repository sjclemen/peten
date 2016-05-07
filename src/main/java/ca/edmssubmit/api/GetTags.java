package ca.edmssubmit.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import ca.edmssubmit.api.json.TagResponse;
import ca.edmssubmit.api.json.TagsResponse;

public class GetTags {
	public static List<TagResponse> getAllTags(String authToken) throws ClientProtocolException, IOException {
		List<TagResponse> tagsList = new ArrayList<TagResponse>();
		Integer pageNumber = 1;
		TagsResponse tagsResponse;
		do {
			tagsResponse = getTags(authToken, pageNumber);
			tagsList.addAll(tagsResponse.results);
			pageNumber++;
		} while (tagsResponse.next != null);
		return tagsList;
	}
	
	public static TagsResponse getTags(String authToken, Integer page) throws ClientProtocolException, IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		
		HttpGet httpGet = new HttpGet(Globals.API_BASE + "tags/tags/?page=" + page + "&format=json");
		httpGet.addHeader("Authorization", "Token " + authToken);
		httpGet.addHeader("Accepts", "application/json");
		CloseableHttpResponse httpResponse = httpclient.execute(httpGet);

		String responseBody;
		try {
		    HttpEntity entity2 = httpResponse.getEntity();
		    responseBody = EntityUtils.toString(entity2);
		    EntityUtils.consume(entity2);
		} finally {
			httpResponse.close();
		}
		
		TagsResponse tagsResponse = Globals.GSON.fromJson(responseBody, TagsResponse.class);
		return tagsResponse;
	}
}
