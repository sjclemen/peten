package ca.edmssubmit.backend.messages;

import ca.edmssubmit.api.CreateTag;
import ca.edmssubmit.backend.tasktypes.HttpRequestTask;

public class CreateTagBEReq extends HttpRequestTask<Integer> {
	private final String apiToken;
	private final String name;
	private final String colour;
	public CreateTagBEReq(String apiToken, String name, String colour) {
		this.apiToken = apiToken;
		this.name = name;
		this.colour = colour;
	}
	
	@Override
	public Integer doCall() throws Exception {
		return CreateTag.createTag(apiToken, name, colour).id;
	}
	
}
