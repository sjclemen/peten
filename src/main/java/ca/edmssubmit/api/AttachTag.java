package ca.edmssubmit.api;

import java.io.IOException;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import ca.edmssubmit.api.json.AttachTagRequest;

public class AttachTag {
	public static void attachTag(String apiToken, Integer documentId, Integer tagId) throws IOException {
		AttachTagRequest atr = new AttachTagRequest();
		atr.tag = tagId;
		String attachTagSerialized = Globals.GSON.toJson(atr);
		
		CloseableHttpClient httpclient = HttpClients.createDefault();
		
		HttpPost httpPost = new HttpPost(Globals.API_BASE + "tags/document/" + documentId + "/tags/?format=json");
		httpPost.addHeader("Authorization", "Token " + apiToken);
		httpPost.addHeader("Content-Type", "application/json");
		httpPost.addHeader("Accepts", "application/json");
		httpPost.setEntity(new StringEntity(attachTagSerialized));
		CloseableHttpResponse httpResponse = httpclient.execute(httpPost);

		try {
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
				return;
			} else {
				throw new IOException("Failed to attach tag, HTTP " + httpResponse.getStatusLine().getStatusCode());
			}
		} finally {
			httpResponse.close();
		}
	}
}
