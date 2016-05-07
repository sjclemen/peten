package ca.edmssubmit.ui.pieces.tags;

import org.eclipse.jface.dialogs.MessageDialog;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import ca.edmssubmit.ui.pieces.CreateTagAction;
import ca.edmssubmit.ui.pieces.tags.TagListCombo.TagListSelectedEventHandler;
import ca.edmssubmit.ui.viewmodel.TagListVM;
import ca.edmssubmit.ui.viewmodel.TagListVMEventListener;
import ca.edmssubmit.ui.viewmodel.TagVM;

public class TagListVMBinding {
	private final TagListVM tagListVM;
	private final TagListCombo tagList;
	private final TagListSelected tagListSelected;
	private TagListVMEventListener eventListener;
	
	public TagListVMBinding(TagListVM tagListVM, TagListCombo tagList, TagListSelected tagListSelected) {
		this.tagListVM = tagListVM;
		this.tagList = tagList;
		this.tagListSelected = tagListSelected;
		bindToTagList();
	}
	
	public TagListVMBinding(TagListVM tagListVM, TagListCombinedWidget combined) {
		this(tagListVM, combined.getTagListCombo(), combined.getTagListSelected());
	}

	private void bindToTagList() {
		tagList.addEventHandler(new TagListSelectedEventHandler() {

			@Override
			public void addTagToSet(String name) {
				TagVM newTag = tagListVM.getTagVMByName(name);
				if (newTag != null) {
					tagListSelected.getSelectedTags().add(newTag);
				}
			}

			@Override
			public void requestNewTag(String name) {
				createNewTag(name);
			}
			
		});
		
		eventListener = new TagListVMEventListener() {

			@Override
			public void onRename(TagVM tag, String oldName, String newName) {
				tagList.renameTagInDropdown(oldName, newName);
			}

			@Override
			public void onAdd(TagVM tag) {
				tagList.addTagToDropdown(tag.getName());
			}

			@Override
			public void onRemove(TagVM tag) {
				tagList.removeTagFromDropdown(tag.getName());
			}
			
		};
		tagListVM.addListener(eventListener);
		
		tagList.bindToViewModel(tagListVM.getContentProposals(),
				tagListVM.getNamesSorted());
	}
	
	private void createNewTag(String name) {
		CreateTagAction createTagAction = new CreateTagAction(tagListSelected.getShell(), tagListVM, name);
		Optional<ListenableFuture<TagVM>> newTag = createTagAction.getNewTag();
		
		if (newTag.isPresent()) {
			ListenableFuture<TagVM> tagVmFuture = newTag.get();
			// TODO: this could come after we've already switched our view...
			Futures.addCallback(tagVmFuture, new FutureCallback<TagVM>() {

				@Override
				public void onSuccess(TagVM result) {
					if (!tagListSelected.isDisposed()) {
						tagListSelected.getSelectedTags().add(result);
					}
				}

				@Override
				public void onFailure(Throwable t) {
					// it got logged somewhere else, but let's tell the user.
					MessageDialog.openError(tagList.getControl().getShell(), "Failed to create tag", "Tag not created: " + t.getMessage());
				}
				
			});
		}
	}

	public TagListVMEventListener getEventListener() {
		return eventListener;
	}

}
