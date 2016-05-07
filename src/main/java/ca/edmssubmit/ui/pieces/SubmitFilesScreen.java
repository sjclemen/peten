package ca.edmssubmit.ui.pieces;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import ca.edmssubmit.ui.pieces.tags.TagListCombinedWidget;

public class SubmitFilesScreen {
	private final Shell shell;
	private final Composite sfComposite;
	private final Composite tableComposite;
	private final Table filesTable;
	private final Button moveOrderUp;
	private final Button moveOrderDown;
	private final Text nameText;
	private final TagListCombinedWidget tagsCombo;
	private final Button reloadTagsButton;
	private final Combo sourceCombo;
	private final Button cancelButton;
	private final Button okButton;
	
	public SubmitFilesScreen(Display display) {
		shell = new Shell(display);
		shell.setLayout(new FillLayout());
		shell.setText("Peten EDMS Submit: Upload file");
		
		sfComposite = new Composite(shell, SWT.NONE);
		GridLayout sfCompositeLayout = new GridLayout();
		sfComposite.setLayout(sfCompositeLayout);
		
		Composite outerTableComposite = new Composite(sfComposite, SWT.NONE);
		outerTableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout outerTableCompositeLayout = new GridLayout();
		outerTableCompositeLayout.numColumns = 2;
		outerTableComposite.setLayout(outerTableCompositeLayout);
		
		tableComposite = new Composite(outerTableComposite, SWT.NONE);
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		tableComposite.setLayout(tableColumnLayout);
		
		Composite tableButtonsComposite = new Composite(outerTableComposite, SWT.NONE);
		tableButtonsComposite.setLayout(new FillLayout(SWT.VERTICAL));
		moveOrderUp = new Button(tableButtonsComposite, SWT.ARROW | SWT.UP);
		moveOrderDown = new Button(tableButtonsComposite, SWT.ARROW | SWT.DOWN);
		
		filesTable = new Table(tableComposite, SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
		filesTable.setHeaderVisible(true);
		TableColumn columnOrder = new TableColumn(filesTable, SWT.NONE);
		columnOrder.setText("Order");
		tableColumnLayout.setColumnData(columnOrder, new ColumnPixelData(60));
		
		TableColumn columnFilename = new TableColumn(filesTable, SWT.NONE);
		columnFilename.setText("Filename");
		tableColumnLayout.setColumnData(columnFilename, new ColumnWeightData(1));
		
		Label nameLabel = new Label(sfComposite, SWT.NONE);
		nameLabel.setText("Name");
		
		nameText = new Text(sfComposite, SWT.NONE);
		nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		Label tagLabel = new Label(sfComposite, SWT.NONE);
		tagLabel.setText("Tags");
		
		tagsCombo = new TagListCombinedWidget(sfComposite, SWT.NONE, shell);
		tagsCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		reloadTagsButton = new Button(sfComposite, SWT.PUSH);
		reloadTagsButton.setText("Reload tags");
		
		Label sourceLabel = new Label(sfComposite, SWT.NONE);
		sourceLabel.setText("Source");
		
		sourceCombo = new Combo(sfComposite, SWT.NONE);
		sourceCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		Composite okCancelComposite = new Composite(sfComposite, SWT.NONE);
		GridLayout okCancelCompositeLayout = new GridLayout();
		okCancelCompositeLayout.numColumns = 2;
		okCancelComposite.setLayout(okCancelCompositeLayout);
		
		okButton = new Button(okCancelComposite, SWT.NONE);
		okButton.setText("OK");
		cancelButton = new Button(okCancelComposite, SWT.NONE);
		cancelButton.setText("Cancel");
	}

	public Composite getTableComposite() {
		return tableComposite;
	}

	public Table getFilesTable() {
		return filesTable;
	}

	public Button getMoveOrderUp() {
		return moveOrderUp;
	}

	public Button getMoveOrderDown() {
		return moveOrderDown;
	}

	public Text getNameText() {
		return nameText;
	}
	
	public TagListCombinedWidget getTagsCombo() {
		return tagsCombo;
	}
	
	public Button getReloadTagsButton() {
		return reloadTagsButton;
	}

	public Combo getSourceCombo() {
		return sourceCombo;
	}

	public Button getCancelButton() {
		return cancelButton;
	}

	public Shell getShell() {
		return shell;
	}

	public Button getOkButton() {
		return okButton;
	}
}
