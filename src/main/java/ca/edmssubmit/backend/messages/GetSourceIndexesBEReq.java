package ca.edmssubmit.backend.messages;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import com.google.common.base.Preconditions;

import ca.edmssubmit.api.GetDocumentIndexes;
import ca.edmssubmit.api.json.GetSourceIndexesResponse;
import ca.edmssubmit.api.json.GetSourceIndexesResponse.GetSourceIndexesResponseInstance;
import ca.edmssubmit.api.json.GetSourceIndexesResponse.GetSourceIndexesResponseResult;
import ca.edmssubmit.backend.tasktypes.HttpRequestTask;

public class GetSourceIndexesBEReq extends HttpRequestTask<List<String>> {
	private final String authToken;
	
	public GetSourceIndexesBEReq(String authToken) {
		this.authToken = authToken;
	}

	@Override
	public List<String> doCall() throws Exception {
		GetSourceIndexesResponse response = GetDocumentIndexes.getTags(authToken, 1);
		Preconditions.checkArgument(response.next == null);
		
		// we return the sources by use count		
		PriorityQueue<LabelWithUseCount> sources = new PriorityQueue<LabelWithUseCount>();
		for (GetSourceIndexesResponseResult index : response.results) {
			Preconditions.checkArgument(index.id == 1);
			for (GetSourceIndexesResponseInstance child : index.instanceRoot.children) {
				if (child.documentsCount > 0) {
					sources.add(new LabelWithUseCount(child.value, child.documentsCount));
				}
			}
		}
		
		List<String> stringSources = new ArrayList<String>();

		while (sources.peek() != null) {
			stringSources.add(sources.poll().label);
		}
		return stringSources;
	}

	private static class LabelWithUseCount implements Comparable<LabelWithUseCount> {
		public String label;
		public Integer useCount;
		
		public LabelWithUseCount(String label, Integer useCount) {
			this.label = label;
			this.useCount = (1000000 - useCount); // who needs more than a million documents, anyways?
		}
		
		@Override
		public int compareTo(LabelWithUseCount arg0) {
			int compareResult = useCount.compareTo(arg0.useCount);
			if (compareResult == 0) {
				return label.compareTo(arg0.label);
			}
			return compareResult;
		}
	}
}
