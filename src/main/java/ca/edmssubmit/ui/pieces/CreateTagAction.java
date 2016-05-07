package ca.edmssubmit.ui.pieces;


import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Shell;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.ListenableFuture;

import ca.edmssubmit.ui.viewmodel.TagListVM;
import ca.edmssubmit.ui.viewmodel.TagVM;

/**
 * Handles the creation of a {@link CreateTagDialog} and then calls the viewmodel for the
 * creation of an a tag, returning a future for it.
 */
public class CreateTagAction {
	private final TagListVM viewModel;
	private final CreateTagDialog dialog;
	
	public CreateTagAction(Shell parentWindow, TagListVM viewModel, String suggestion) {
		this.viewModel = viewModel;
		dialog = new CreateTagDialog(parentWindow, suggestion);
	}
	
	public Optional<ListenableFuture<TagVM>> getNewTag() {
		int windowResults = dialog.open();
		if (windowResults != Window.OK) {
			return Optional.absent();
		}
		String newTagName = dialog.getNewTagName();
		RGB newTagColour = dialog.getNewTagColour();
		ListenableFuture<TagVM> tagFuture =
				viewModel.addTag(newTagName, String.format("#%02x%02x%02x", 
						newTagColour.red, newTagColour.green, newTagColour.blue));
		
		return Optional.of(tagFuture);
	}
}
