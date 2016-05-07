package ca.edmssubmit.api;

import java.io.IOException;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import ca.edmssubmit.api.json.CreateMetadataRequest;

public class CreateMetadata {
	public static void createMetadata(String apiToken, Integer documentId, Integer metadataType, String value) throws IOException {
		CreateMetadataRequest cmr = new CreateMetadataRequest();
		cmr.document = documentId;
		cmr.metadataType = metadataType;
		cmr.value = value;
		String createMetadataSerialized = Globals.GSON.toJson(cmr);
		
		CloseableHttpClient httpclient = HttpClients.createDefault();
		
		HttpPost httpPost = new HttpPost(Globals.API_BASE + "metadata/document/" + documentId + "/metadata/?format=json");
		httpPost.addHeader("Authorization", "Token " + apiToken);
		httpPost.addHeader("Content-Type", "application/json");
		httpPost.addHeader("Accepts", "application/json");
		httpPost.setEntity(new StringEntity(createMetadataSerialized));
		CloseableHttpResponse httpResponse = httpclient.execute(httpPost);

		// TODO: see if there's a body s.t. we can grab the error message
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
