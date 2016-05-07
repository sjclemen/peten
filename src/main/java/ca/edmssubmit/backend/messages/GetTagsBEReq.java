package ca.edmssubmit.backend.messages;

import java.util.List;

import ca.edmssubmit.api.GetTags;
import ca.edmssubmit.api.json.TagResponse;
import ca.edmssubmit.backend.tasktypes.HttpRequestTask;

public class GetTagsBEReq extends HttpRequestTask<List<TagResponse>> {
	private final String authKey;
	
	public GetTagsBEReq(String authKey) {
		this.authKey = authKey;
	}

	@Override
	public List<TagResponse> doCall() throws Exception {
		List<TagResponse> tags = GetTags.getAllTags(authKey);
		return tags;
	}

}
