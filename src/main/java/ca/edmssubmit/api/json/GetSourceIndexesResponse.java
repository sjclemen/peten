package ca.edmssubmit.api.json;

import java.util.List;

public class GetSourceIndexesResponse {
	public Integer count;
	public String next;
	public String previous;
	public List<GetSourceIndexesResponseResult> results;
	
	public static class GetSourceIndexesResponseResult {
		public Integer id;
		public GetSourceIndexesResponseInstance instanceRoot;
		public String label;
		// nodeTemplates not relevant
	}
	
	public static class GetSourceIndexesResponseInstance {
		public String documents;
		public Integer documentsCount;
		public List<GetSourceIndexesResponseInstance> children;
		public Integer id;
		public Integer level;
		public Integer parent;
		public String value;
	}
}
