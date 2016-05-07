package ca.edmssubmit.api;

import java.io.File;
import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import ca.edmssubmit.api.json.CreateDocumentResponse;

public class CreateDocument {
	public static CreateDocumentResponse createDocument(String apiToken, String filename, File document, String mimeType, String description, Integer documentType) throws ClientProtocolException, IOException, AuthenticationFailure {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		
		HttpPost httpPost = new HttpPost(Globals.API_BASE + "documents/documents/?format=json");
		httpPost.addHeader("Authorization", "Token " + apiToken);
		httpPost.addHeader("Accepts", "application/json");
		
		MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
			.addTextBody("description", description)
			.addTextBody("document_type", new Integer(1).toString())
			.addBinaryBody("file", document, ContentType.create(mimeType), filename)
			.addTextBody("label", filename)
			.addTextBody("language", "eng");
		httpPost.setEntity(entityBuilder.build());
		CloseableHttpResponse httpResponse = httpclient.execute(httpPost);

		String response;
		try {
		    HttpEntity entity = httpResponse.getEntity();
		    response = EntityUtils.toString(entity);
		    EntityUtils.consume(entity);
		} finally {
			httpResponse.close();
		}
		
		CreateDocumentResponse responseDeserialized;
		
		try {
			responseDeserialized = Globals.GSON.fromJson(response, CreateDocumentResponse.class);
		} catch (RuntimeException e) {
			throw new AuthenticationFailure("Failed to read response: " + response, e);
		}
		
		return responseDeserialized;
	}

}
