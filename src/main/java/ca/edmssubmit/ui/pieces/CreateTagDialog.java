package ca.edmssubmit.ui.pieces;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog box prompting users to create a new tag.
 * TODO: show meaningful error messages for when people create dupe tags and the like.
 * TODO: prevent users from hitting OK when form not complete by switching to wizard
 */
public class CreateTagDialog extends Dialog {
	private final String suggestion;
	private Text tagInput;
	private ColorSelector colourSelector;

	private String newTagName;
	private RGB newTagColour;

	/**
	 * Creates a tag dialog, but does not open it. Call {@link open} for that.
	 * @param parentShell Shell to create this as a child of.
	 * @param suggestion A suggestion for the tag, not the tag category.
	 */
	CreateTagDialog(Shell parentShell, String suggestion) {
		super(parentShell);
		this.suggestion = suggestion;
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Create new tag");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);

		Label tagNameLabel = new Label(container, SWT.NONE);
		tagNameLabel.setText("Tag name");
		
		tagInput = new Text(container, SWT.SINGLE | SWT.BORDER);
		tagInput.setText(suggestion);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		tagInput.setLayoutData(gridData);
		
		Label tagCategoryLabel = new Label(container, SWT.NONE);
		tagCategoryLabel.setText("Colour");
		
		colourSelector = new ColorSelector(container);

		return container;
	}

	@Override
	protected void okPressed() {
		newTagName = tagInput.getText();
		newTagColour = colourSelector.getColorValue();
		super.okPressed();
	}

	public String getNewTagName() {
		return newTagName;
	}

	public RGB getNewTagColour() {
		return newTagColour;
	}

}
