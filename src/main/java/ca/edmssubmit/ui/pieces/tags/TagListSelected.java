package ca.edmssubmit.ui.pieces.tags;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.core.databinding.observable.set.ISetChangeListener;
import org.eclipse.core.databinding.observable.set.SetChangeEvent;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import ca.edmssubmit.ui.viewmodel.TagVM;

public class TagListSelected extends Composite {
	// Selected tags in the writeable set which is watchable.
	private final WritableSet selectedTags = new WritableSet(new ArrayList<TagVM>(), TagVM.class);
	// Selected tags, in a string -> composite pair.
	private final TreeMap<String, Composite> tagTree = new TreeMap<String, Composite>();
	// Layout for tagComposite.
	private final RowLayout tagRowLayout;
	
	private final Composite callLayoutOnMe;
	private TagToolItemConfigurator configurator;

	public TagListSelected(Composite parent, int style, Composite callLayout) {
		super(parent, style);
		
		this.callLayoutOnMe = callLayout;

		tagRowLayout = new RowLayout();
		tagRowLayout.wrap = true;
		tagRowLayout.pack = true;
		tagRowLayout.fill = true;
		setLayout(tagRowLayout);
		
		/**
		 * We keep track of the selected tags through a set change listener.
		 * This widget doesn't actually know which issue it's looking at tags
		 * for.
		 */
		selectedTags.addSetChangeListener(new ISetChangeListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void handleSetChange(SetChangeEvent arg0) {
				Set<TagVM> removals = arg0.diff.getRemovals();
				for (TagVM removeMe : removals) {
					removeTagFromSelected(removeMe.getName());
				}
				Set<TagVM> additions = arg0.diff.getAdditions();
				for (TagVM addMe : additions) {
					addTagToSelected(addMe);
				}
				callLayoutOnMe.layout(true, true);
				callLayoutOnMe.pack(true);
			}

		});
	}	

	public WritableSet getSelectedTags() {
		return selectedTags;
	}
	
	public Composite getWidget() {
		return this;
	}
	
	public void setItemActionConfigurator(TagToolItemConfigurator configurator) {
		this.configurator = configurator;
	}
	
	private void addTagToSelected(TagVM tagVm) {
		if (configurator == null) {
			configurator = new TagToolItemDefaultDeleter(selectedTags);
		}
		Composite individualTagComposite = createTagComposite(this, tagVm, configurator);
		String tagName = tagVm.getName();
		tagTree.put(tagName, individualTagComposite);
		
		Entry<String, Composite> previousComposite = tagTree.lowerEntry(tagName);

		if (previousComposite == null) {
			Entry<String, Composite> followingComposite = tagTree.higherEntry(tagName);
			if (followingComposite != null) {
				individualTagComposite.moveAbove(followingComposite.getValue());
			}
		} else {
			individualTagComposite.moveBelow(previousComposite.getValue());
		}
	}
	
	private void removeTagFromSelected(String tagName) {
		Composite removalComposite = tagTree.get(tagName);
		removalComposite.dispose();
		tagTree.remove(tagName);
	}
	
	/**
	 * Creates the actual GUI widgets.
	 */
	private static Composite createTagComposite(Composite parent, TagVM tagVm, TagToolItemConfigurator configurator) {
		Composite tagContainer = new Composite(parent, SWT.BORDER);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		tagContainer.setLayout(layout);

		Label tagLabel = new Label(tagContainer, SWT.NONE);
		tagLabel.setText(tagVm.getName());
		GridData gdl = new GridData();
		gdl.verticalAlignment = GridData.CENTER;
		tagLabel.setLayoutData(gdl);

		ToolBar toolBar = new ToolBar(tagContainer, SWT.FLAT);
		Rectangle clientArea = tagContainer.getClientArea();
		toolBar.setLocation(clientArea.x, clientArea.y);
		configurator.setupToolbar(toolBar, tagVm);

		toolBar.pack();
		return tagContainer;
	}
	
	public static interface TagToolItemConfigurator {
		public void setupToolbar(ToolBar toolBar, TagVM tag);
	}
	
	public static class TagToolItemDefaultDeleter implements TagToolItemConfigurator {
		private final WritableSet selectedTags;
		
		public TagToolItemDefaultDeleter(WritableSet selectedTags) {
			this.selectedTags = selectedTags;
		}
		
		public void setupToolbar(ToolBar toolBar, final TagVM tag) {
			ToolItem item = new ToolItem(toolBar, SWT.PUSH);
			item.setText("X");
			item.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
					selectedTags.remove(tag);
				}

				@Override
				public void widgetSelected(SelectionEvent arg0) {
					selectedTags.remove(tag);
				}
				
			});

		}
	}
}
