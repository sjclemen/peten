package ca.edmssubmit.ui.pieces;

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
import org.eclipse.swt.widgets.Text;

public class LoginScreen {
	private final Composite loginScreenComposite;
	private final Composite inputBoxComposite;
	private final Composite okCancelComposite;
	private final Text urlText;
	private final Text usernameText;
	private final Text passwordText;
	private final Button okButton;
	private final Button cancelButton;
	private final Shell shell;

	public LoginScreen(Display display) {
		shell = new Shell(display);
		shell.setLayout(new FillLayout());
		shell.setText("Peten EDMS Submit: Login");
		
		loginScreenComposite = new Composite(shell, SWT.BORDER);
		GridLayout loginScreenCompositeLayout = new GridLayout();
		loginScreenCompositeLayout.numColumns = 1;
		loginScreenComposite.setLayout(loginScreenCompositeLayout);
		
		inputBoxComposite = new Composite(loginScreenComposite, SWT.BORDER);
		inputBoxComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		GridLayout inputBoxCompositeLayout = new GridLayout();
		inputBoxCompositeLayout.makeColumnsEqualWidth = false;
		inputBoxCompositeLayout.numColumns = 2;
		inputBoxComposite.setLayout(inputBoxCompositeLayout);
		
		Label urlLabel = new Label(inputBoxComposite, SWT.NONE);
		urlLabel.setText("API URL");
		urlText = new Text(inputBoxComposite, SWT.SINGLE);
		urlText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		Label usernameLabel = new Label(inputBoxComposite, SWT.NONE);
		usernameLabel.setText("Username");
		usernameText = new Text(inputBoxComposite, SWT.SINGLE);
		usernameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		Label passwordLabel = new Label(inputBoxComposite, SWT.NONE);
		passwordLabel.setText("Password");
		passwordText = new Text(inputBoxComposite, SWT.SINGLE | SWT.PASSWORD);
		passwordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		okCancelComposite = new Composite(loginScreenComposite, SWT.NONE);
		GridLayout okCancelCompositeLayout = new GridLayout();
		okCancelCompositeLayout.numColumns = 2;
		okCancelComposite.setLayout(okCancelCompositeLayout);
		
		okButton = new Button(okCancelComposite, SWT.NONE);
		okButton.setText("OK");
		cancelButton = new Button(okCancelComposite, SWT.NONE);
		cancelButton.setText("Cancel");
		
		shell.pack();
		Point shellSize = shell.getSize();
		shell.setMinimumSize((int)(shellSize.x * 1.5), shellSize.y);
		shell.open();
	}
	
	public void disableInput() {
		urlText.setEditable(false);
		usernameText.setEditable(false);
		passwordText.setEditable(false);
		okButton.setEnabled(false);
	}
	
	public void enableInput() {
		urlText.setEditable(true);
		usernameText.setEditable(true);
		passwordText.setEditable(true);
		okButton.setEnabled(true);
	}

	public Text getUrlText() {
		return urlText;
	}

	public Text getUsernameText() {
		return usernameText;
	}

	public Text getPasswordText() {
		return passwordText;
	}

	public Button getOkButton() {
		return okButton;
	}

	public Button getCancelButton() {
		return cancelButton;
	}

	public Shell getShell() {
		return shell;
	}
}
