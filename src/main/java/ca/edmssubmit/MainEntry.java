package ca.edmssubmit;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.util.concurrent.FutureCallback;

import ca.edmssubmit.api.json.TagResponse;
import ca.edmssubmit.backend.dirwatcher.DirWatcherManager;
import ca.edmssubmit.backend.manager.BackendConnection;
import ca.edmssubmit.backend.manager.BackendConnectionDisplay;
import ca.edmssubmit.backend.manager.BackendManager;
import ca.edmssubmit.backend.messages.GetSourceIndexesBEReq;
import ca.edmssubmit.backend.messages.GetTagsBEReq;
import ca.edmssubmit.ui.RealmHelper;
import ca.edmssubmit.ui.databinding.LoginScreenDatabinding;
import ca.edmssubmit.ui.databinding.ScannerSelectScreenDatabinding;
import ca.edmssubmit.ui.pieces.LoginScreen;
import ca.edmssubmit.ui.pieces.ScannerSelectScreen;
import ca.edmssubmit.ui.viewmodel.ScannerViewModel;
import ca.edmssubmit.ui.viewmodel.TagListVM;

public class MainEntry {
	protected final static Logger logger = Logger.getLogger(MainEntry.class);

	public static void main(String[] args) {
		// SWT init, event bus
		final Display display = new Display();
		AsyncEventBus eventBus = new AsyncEventBus("to gui",
				new BackendConnectionDisplay.DisplayExecutor(display));
		
		// init realm, to satisfy jface's whining
		Realm realm = SWTObservables.getRealm(display);
		@SuppressWarnings("unused")
		RealmHelper rh = new RealmHelper(realm);

		// start database manager
		BackendManager backend = new BackendManager(eventBus);

		// main UI, setup
		final BackendConnection connection = backend.getDisplayConnection(display);
		
		// login, before the main UI
		final LoginScreen loginScreen = new LoginScreen(display);
		final Shell loginShell = loginScreen.getShell();
		final StringBuilder authStringBuilder = new StringBuilder();
		LoginScreenDatabinding.databind(loginScreen, loginShell, connection, authStringBuilder);

		while (!loginShell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
		
		if (!authStringBuilder.toString().isEmpty()) {			
			// begin scanning
			final DirWatcherManager watcherManager = new DirWatcherManager(eventBus);
			final TagListVM tagListVm = new TagListVM(authStringBuilder.toString(), connection);
			final ScannerViewModel scannerViewModel = new ScannerViewModel(null, eventBus, tagListVm);
			scannerViewModel.addPropertyChangeListener("scanDirectory", new PropertyChangeListener() {

				@Override
				public void propertyChange(PropertyChangeEvent arg0) {
					watcherManager.setWatchDirectory((String) arg0.getNewValue());
				}
				
			});

			final ScannerSelectScreen selectScreen = new ScannerSelectScreen(display);
			ScannerSelectScreenDatabinding.databind(selectScreen, authStringBuilder.toString(), connection, scannerViewModel);
			
			// get tags
			GetTagsBEReq getTags = new GetTagsBEReq(authStringBuilder.toString());
			getTags.addCallback(new FutureCallback<List<TagResponse>> () {

				@Override
				public void onFailure(Throwable arg0) {
					logger.error("Failed to get tags.", arg0);
					if (!selectScreen.getShell().isDisposed()) {
						MessageDialog.openError(selectScreen.getShell(), "Failed to retrieve tags", "Tag retrieval failed: " + arg0.getMessage());
					}
				}

				@Override
				public void onSuccess(List<TagResponse> arg0) {
					scannerViewModel.getTagListVm().updateTags(arg0);
				}
				
			});
			connection.enqueueTask(getTags);
			
			// get sources
			GetSourceIndexesBEReq getSources = new GetSourceIndexesBEReq(authStringBuilder.toString());
			getSources.addCallback(new FutureCallback<List<String>> () {

				@Override
				public void onFailure(Throwable arg0) {
					logger.error("Failed to get sources.", arg0);
					if (!selectScreen.getShell().isDisposed()) {
						MessageDialog.openError(selectScreen.getShell(), "Failed to retrieve sources", "Source retrieval failed: " + arg0.getMessage());
					}
				}

				@Override
				public void onSuccess(List<String> arg0) {
					scannerViewModel.setSources(new ArrayList<String>(arg0));
				}
				
			});
			connection.enqueueTask(getSources);
			
			// main loop
			while (!selectScreen.getShell().isDisposed()) {
				if (!display.readAndDispatch()) display.sleep();
			}
			
			// stop the file watcher
			watcherManager.stop();
		}
		
		display.dispose();
				
		// prevent enqueueing of new connections
		backend.requestShutdown();

		// stop database manager, close connections
		try {
			backend.waitForShutdown(60, TimeUnit.SECONDS);
		} catch (InterruptedException ie) {
			logger.info("Failed to shutdown backend, interrupted.", ie);
		}
		System.exit(0);
	}

}
