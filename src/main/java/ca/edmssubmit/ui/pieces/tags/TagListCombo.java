package ca.edmssubmit.ui.pieces.tags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;

import org.apache.commons.collections4.trie.PatriciaTrie;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

// TODO: limit the # of items in a taglist to some fixed number (1000?) or convert
// entirely to a textbox
public class TagListCombo {
	// Combo box where people enter tags, either via typing or dropdown.
	private final Combo tagEntry;
	private final List<TagListSelectedEventHandler> eventHandlers = new LinkedList<TagListSelectedEventHandler>();
	
	public TagListCombo(Composite parent, int style) {
		tagEntry = new Combo(parent, SWT.NONE);
		// TODO: this belongs in databinding???
		tagEntry.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				addTagInternal(tagEntry.getText());
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				addTagInternal(tagEntry.getText());
			}

		});
	}
	
	// used for setting layout information
	public Control getControl() {
		return tagEntry;
	}
	
	public void addEventHandler(TagListSelectedEventHandler eventHandler) {
		eventHandlers.add(eventHandler);
	}
	
	public void clearText() {
		tagEntry.clearSelection();
		tagEntry.setText("");
		// TODO UGLY HACK: this is required on operating systems which are not windows,
		// otherwise it won't close on hitting enter. on windows, if this code is left in,
		// it'll REopen upon hitting enter.
		if (!System.getProperty("os.name").toLowerCase().contains("win")) {
			tagEntry.setListVisible(false);
		}
	}

	// NOTE: THIS COULD REQUEST A TAG THAT DOESN'T EXIST ANYMORE
	protected void addTagInternal(String text) {
		for (TagListSelectedEventHandler handler: eventHandlers) {
			handler.addTagToSet(text);
		}
		clearText();
	}
	
	public void addTagToDropdown(String name) {
		int insertionLocation = Arrays.binarySearch(tagEntry.getItems(), name);
		insertionLocation = 0 - insertionLocation - 1;
		tagEntry.add(name, insertionLocation);
	}
	
	public void removeTagFromDropdown(String name) {
		tagEntry.remove(name);
	}
	
	public void renameTagInDropdown(String oldName, String newName) {
		tagEntry.remove(oldName);
		int insertionLocation = Arrays.binarySearch(tagEntry.getItems(), newName);
		insertionLocation = 0 - insertionLocation - 1;
		tagEntry.add(newName, insertionLocation);
	}

	public void bindToViewModel(PatriciaTrie<ContentProposal> proposals, String[] sortedNames) {
		final TagListContentProposalProvider proposalProvider = new TagListContentProposalProvider(proposals);
		ContentProposalAdapter proposalAdapter = new ContentProposalAdapter(
				tagEntry, new ComboContentAdapter(), proposalProvider, null, null);
		proposalAdapter.setPropagateKeys(true);
		proposalAdapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);

		proposalAdapter.addContentProposalListener(new IContentProposalListener() {

			@Override
			public void proposalAccepted(IContentProposal userSelectedProposal) {
				if (proposalProvider.getMakeNewProposal() == userSelectedProposal) {
					for (TagListSelectedEventHandler handler: eventHandlers) {
						handler.requestNewTag(userSelectedProposal.getContent());
						
						clearText();
					}
				}
				addTagInternal(userSelectedProposal.getContent());
			}

		});
		
		tagEntry.setItems(sortedNames);
	}
	
	/**
	 * An interface for interacting with a {@link TagListSelected}.
	 */
	public static interface TagListSelectedEventHandler {
		public void addTagToSet(String name);
		public void requestNewTag(String name);
	}

	/**
	 * A custom content proposal that:
	 * <ul>
	 * <li> stores tags in a patricia trie
	 * <li> has a create new option
	 * </ul>
	 */
	private static class TagListContentProposalProvider implements
			IContentProposalProvider {
		private final PatriciaTrie<ContentProposal> contentProposals;
		private ContentProposal makeNewProposal;
		
		public TagListContentProposalProvider(
				PatriciaTrie<ContentProposal> contentProposals) {
			this.contentProposals = contentProposals;
		}
		
		public ContentProposal getMakeNewProposal() {
			return makeNewProposal;
		}

		@Override
		public IContentProposal[] getProposals(String contents, int position) {
			// TODO:
			// cap the number of proposals
			// exclude tags already part of our set
			// if the current selection is a suffix of our last guess, reuse
			// the prefix
			SortedMap<String, ContentProposal> selectedProposals = contentProposals
					.prefixMap(contents.toLowerCase());
			
			ArrayList<ContentProposal> proposals = new ArrayList<ContentProposal>(
					selectedProposals.values());

			if (!selectedProposals.containsKey(contents)) {
				ContentProposal makeNew = new ContentProposal(contents,
						"Create new tag: " + contents, "Creates a new tag");
				makeNewProposal = makeNew;

				proposals.add(makeNew);
			}
			return proposals.toArray(new ContentProposal[0]);
		}

	}

}
