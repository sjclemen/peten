package ca.edmssubmit.ui.pieces;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class ScannerSelectScreen {
	private final Shell shell;
	private final Composite screenComposite;
	private final Composite tableComposite;
	private final Table fileTable;
	private final Button uploadButton;
	private final Label watchDirectoryLabel;
	private final Button selectDirectoryButton;
	private final Button selectFilesButton;
	
	public ScannerSelectScreen(Display display) {
		shell = new Shell(display);
		shell.setLayout(new FillLayout());
		shell.setText("Peten EDMS Submit: Scanning...");
		
		screenComposite = new Composite(shell, SWT.NONE);
		GridLayout screenCompositeLayout = new GridLayout();
		screenComposite.setLayout(screenCompositeLayout);
		
		tableComposite = new Composite(screenComposite, SWT.NONE);
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		tableComposite.setLayout(tableColumnLayout);
		
		fileTable = new Table(tableComposite, SWT.V_SCROLL | SWT.H_SCROLL | SWT.CHECK | SWT.BORDER);
		fileTable.setHeaderVisible(true);
		
		TableColumn columnFilename = new TableColumn(fileTable, SWT.NONE);
		columnFilename.setText("Filename");
		tableColumnLayout.setColumnData(columnFilename, new ColumnWeightData(1));
				
		uploadButton = new Button(screenComposite, SWT.PUSH);
		uploadButton.setText("Upload Selected");
		
		Label separator = new Label(screenComposite, SWT.HORIZONTAL);
		
		watchDirectoryLabel = new Label(screenComposite, SWT.NONE);
		watchDirectoryLabel.setText("dingus");
		
		selectDirectoryButton = new Button(screenComposite, SWT.PUSH);
		selectDirectoryButton.setText("Select Directory");
		selectFilesButton = new Button(screenComposite, SWT.PUSH);
		selectFilesButton.setText("Select Files");
		
		shell.pack();
		Point shellSize = shell.getSize();
		shell.setMinimumSize(shellSize.x, (int)(shellSize.y * 1.5));
		
		shell.open();

	}

	public Shell getShell() {
		return shell;
	}

	public Composite getScreenComposite() {
		return screenComposite;
	}

	public Composite getTableComposite() {
		return tableComposite;
	}

	public Table getFileTable() {
		return fileTable;
	}

	public Button getUploadButton() {
		return uploadButton;
	}

	public Label getWatchDirectoryLabel() {
		return watchDirectoryLabel;
	}

	public Button getSelectDirectoryButton() {
		return selectDirectoryButton;
	}

	public Button getSelectFilesButton() {
		return selectFilesButton;
	}
}
