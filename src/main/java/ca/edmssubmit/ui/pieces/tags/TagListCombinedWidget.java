package ca.edmssubmit.ui.pieces.tags;

import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

public class TagListCombinedWidget extends Composite {
	private final TagListCombo tagList;
	private final TagListSelected tagListSelected;

	public TagListCombinedWidget(Composite parent, int style, Composite callLayoutOnMe) {
		super(parent, style);
		FormLayout layout = new FormLayout();
		setLayout(layout);
		
		FormData selfFormData = new FormData();
		selfFormData.top = new FormAttachment(0, 0);
		selfFormData.left = new FormAttachment(0, 0);
		selfFormData.right = new FormAttachment(100, 0);
		setLayoutData(selfFormData);
				
		tagList = new TagListCombo(this, SWT.NONE);
		FormData tagListFormData = new FormData();
		tagListFormData.top = new FormAttachment(0, 0);
		tagListFormData.left = new FormAttachment(0, 0);
		tagListFormData.right = new FormAttachment(100, 0);
		tagList.getControl().setLayoutData(tagListFormData);
		
		tagListSelected = new TagListSelected(this, SWT.NONE, callLayoutOnMe);
		FormData tagListSelectedFormData = new FormData();
		tagListSelectedFormData.top = new FormAttachment(tagList.getControl(), 0);
		tagListSelectedFormData.left = new FormAttachment(0, 0);
		tagListSelectedFormData.right = new FormAttachment(100, 0);
		tagListSelected.getWidget().setLayoutData(tagListSelectedFormData);
	}

	public TagListCombo getTagListCombo() {
		return tagList;
	}

	public TagListSelected getTagListSelected() {
		return tagListSelected;
	}

	public WritableSet getSelectedTags() {
		return tagListSelected.getSelectedTags();
	}

}
