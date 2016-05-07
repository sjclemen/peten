package ca.edmssubmit.backend.messages;

import java.util.List;

public class SubmitFilesBEResp {
	public Integer documentId;
	public List<String> warnings;
	
	public SubmitFilesBEResp(Integer documentId, List<String> warnings) {
		this.documentId = documentId;
		this.warnings = warnings;
	}
}
