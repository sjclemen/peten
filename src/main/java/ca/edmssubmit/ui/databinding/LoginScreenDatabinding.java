package ca.edmssubmit.ui.databinding;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;

import com.google.common.util.concurrent.FutureCallback;

import ca.edmssubmit.backend.manager.BackendConnection;
import ca.edmssubmit.backend.messages.AuthenticateBEReq;
import ca.edmssubmit.ui.pieces.LoginScreen;

public class LoginScreenDatabinding {
	public static void databind(final LoginScreen loginScreen, final Shell loginShell,
			final BackendConnection connection, final StringBuilder authStringBuilder) {
		loginScreen.getCancelButton().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				loginShell.dispose();
			}
		});
		
		loginScreen.getOkButton().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// disable controls
				loginScreen.disableInput();
				AuthenticateBEReq authRequest = new AuthenticateBEReq(loginScreen.getUsernameText().getText(), loginScreen.getPasswordText().getText());
				authRequest.addCallback(new FutureCallback<String> () {

					@Override
					public void onFailure(Throwable arg0) {
						MessageDialog.openError(loginShell, "Authentication Failure", "Something went wrong with authentication: " + arg0.getMessage());
						loginScreen.enableInput();
					}

					@Override
					public void onSuccess(String arg0) {
						authStringBuilder.append(arg0);
						loginShell.dispose();
					}
					
				});
				connection.enqueueTask(authRequest);
			}
		});
	}
}
