package ca.edmssubmit.ui.viewmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.collections4.trie.PatriciaTrie;
import org.eclipse.core.databinding.observable.map.WritableMap;
import org.eclipse.jface.fieldassist.ContentProposal;

import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import ca.edmssubmit.api.json.TagResponse;
import ca.edmssubmit.backend.manager.BackendConnection;
import ca.edmssubmit.backend.manager.BackendListenableFutureTask;
import ca.edmssubmit.backend.messages.CreateTagBEReq;

public class TagListVM {
	private final WritableMap tagMap = new WritableMap(Integer.class, TagVM.class);
	private final HashMap<String, TagVM> nameToTag = new HashMap<String, TagVM>();
	private final PatriciaTrie<ContentProposal> contentProposals = new PatriciaTrie<ContentProposal>();
	private final ArrayList<TagListVMEventListener> eventListeners = new ArrayList<TagListVMEventListener>();
	private final String authToken;
	private final BackendConnection conn;

	public TagListVM(String authToken, BackendConnection conn) {
		this.authToken = authToken;
		this.conn = conn;
	}
	public PatriciaTrie<ContentProposal> getContentProposals() {
		return contentProposals;
	}
	public TagVM getTagVMByName(String name) {
		return nameToTag.get(name);
	}
	public ListenableFuture<TagVM> addTag(final String newTagName, final String newTagColour) {				
		final CreateTagBEReq ctr = new CreateTagBEReq(authToken, newTagName, newTagColour);
		Function<Integer, TagVM> addTagToVm = new Function<Integer, TagVM>() {
			@Override
			public TagVM apply(Integer input) {
				TagVM tagVm = new TagVM();
				tagVm.setId(input);
				tagVm.setName(newTagName);
				tagVm.setColour(newTagColour);
				addTagVm(tagVm);
				return tagVm;
			}
		};
		BackendListenableFutureTask<Integer> enqueued = conn.enqueueTask(ctr);
		return Futures.transform(enqueued, addTagToVm, conn.getExecutor());
	}
	private void addTagVm(TagVM tvm) {
		if (nameToTag.containsKey(tvm.getName()) || tagMap.containsKey(tvm.getId())) {
			throw new RuntimeException("Tried to insert already-existing id or name.");
		}
		
		tagMap.put(tvm.getId(), tvm);
		nameToTag.put(tvm.getName(), tvm);
		contentProposals.put(tvm.getName().toLowerCase(), new ContentProposal(tvm.getName()));
		
		for (TagListVMEventListener eventListener : eventListeners) {
			eventListener.onAdd(tvm);
		}
	}
	private void removeTagVm(TagVM tvm) {
		contentProposals.remove(tvm.getName().toLowerCase());
		nameToTag.remove(tvm.getName());
		tagMap.remove(tvm.getId());
		
		for (TagListVMEventListener eventListener : eventListeners) {
			eventListener.onRemove(tvm);
		}
	}
	private void renameTagVm(TagVM tvm, String oldName, String newName) {
		contentProposals.remove(tvm.getName().toLowerCase());
		contentProposals.put(tvm.getName().toLowerCase(), new ContentProposal(tvm.getName()));
		
		tvm.setName(newName);
		
		nameToTag.remove(tvm.getName());
		nameToTag.put(tvm.getName(), tvm);

		for (TagListVMEventListener eventListener : eventListeners) {
			eventListener.onRename(tvm, oldName, newName);
		}
	}
	public String[] getNamesSorted() {
		String[] names = nameToTag.keySet().toArray(new String[0]);
		Arrays.sort(names);
		return names;
	}
	public void addListener(TagListVMEventListener tagListVMEventListener) {
		eventListeners.add(tagListVMEventListener);
	}
	public void removeListener(TagListVMEventListener eventListener) {
		eventListeners.remove(eventListener);
	}
	public void updateTags(List<TagResponse> tagResponseList) {
		for (TagResponse remoteTag : tagResponseList) {
			if (tagMap.containsKey(remoteTag.id)) {
				TagVM localTag = (TagVM)tagMap.get(remoteTag.id);
				if (!remoteTag.label.equals(localTag.getName())) {
					renameTagVm(localTag, localTag.getName(), remoteTag.label);
				}
			} else {
				TagVM newTag = new TagVM();
				newTag.setId(remoteTag.id);
				newTag.setName(remoteTag.label);
				newTag.setColour(remoteTag.color);
				addTagVm(newTag);
			}
		}
		
		// O(n^2) but tag sizes should be small here
		for (TagVM localTag : new ArrayList<TagVM>(nameToTag.values())) {
			boolean isFoundOnRemote = false;
			for (TagResponse remoteTag : tagResponseList) {
				if (localTag.getId().equals(remoteTag.id)) {
					isFoundOnRemote = true;
				}
			}
			if (!isFoundOnRemote) {
				removeTagVm(localTag);
			}
		}
	}


}
