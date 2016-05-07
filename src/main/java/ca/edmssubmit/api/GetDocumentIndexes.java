package ca.edmssubmit.api;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import ca.edmssubmit.api.json.GetSourceIndexesResponse;

public class GetDocumentIndexes {
	public static GetSourceIndexesResponse getTags(String authToken, Integer page) throws ClientProtocolException, IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		
		HttpGet httpGet = new HttpGet(Globals.API_BASE + "document_indexing/indexes/?page=" + page + "&format=json");
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
		
		GetSourceIndexesResponse indexesResponse = Globals.GSON.fromJson(responseBody, GetSourceIndexesResponse.class);
		return indexesResponse;
	}

}
