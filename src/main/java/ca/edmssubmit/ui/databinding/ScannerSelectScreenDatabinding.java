package ca.edmssubmit.ui.databinding;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;

import ca.edmssubmit.backend.manager.BackendConnection;
import ca.edmssubmit.ui.pieces.ScannerSelectScreen;
import ca.edmssubmit.ui.pieces.SubmitFilesScreen;
import ca.edmssubmit.ui.viewmodel.FileVM;
import ca.edmssubmit.ui.viewmodel.ScannerViewModel;

public class ScannerSelectScreenDatabinding {
	public static void databind(final ScannerSelectScreen selectScreen, final String authKey,
			final BackendConnection connection, final ScannerViewModel scannerViewModel) {
		final CheckboxTableViewer fileViewer = new CheckboxTableViewer(selectScreen.getFileTable());
				
		ViewerSupport.bind(fileViewer, scannerViewModel.getNewFiles(), BeanProperties.values(new String[] { "name" }));
		
		// bind directory to label
		IObservableValue modelObservable = BeansObservables.observeValue(scannerViewModel, "scanDirectory");
		modelObservable.addChangeListener(new IChangeListener() {

			@Override
			public void handleChange(ChangeEvent arg0) {
				selectScreen.getWatchDirectoryLabel().setText(scannerViewModel.getScanDirectory());
				selectScreen.getWatchDirectoryLabel().getParent().layout();
			}
				
		});
	
		selectScreen.getUploadButton().addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				Object[] checkedElements = fileViewer.getCheckedElements();
				List<FileVM> fvmCheckedElements = new ArrayList<FileVM>();
				for (Object o : checkedElements) {
					if (o instanceof FileVM) {
						fvmCheckedElements.add((FileVM) o);
					}
				}
				if (fvmCheckedElements.isEmpty()) {
					IStructuredSelection selection = (IStructuredSelection)fileViewer.getSelection();
					if (selection.isEmpty()) {
						return;
					}
					FileVM selectedRow = (FileVM)selection.getFirstElement();
					fvmCheckedElements.add(selectedRow);
				}
				for (FileVM fvm : fvmCheckedElements) {
					scannerViewModel.getNewFiles().remove(fvm);
				}
				SubmitFilesScreen submitFiles = new SubmitFilesScreen(selectScreen.getShell().getDisplay());
				SubmitFilesScreenDatabinding.databind(submitFiles, connection, authKey, fvmCheckedElements, scannerViewModel.getTagListVm(), scannerViewModel.getRequestId(), scannerViewModel.getSources());
				submitFiles.getShell().pack();
				submitFiles.getShell().open();
			}
			
		});
		
		selectScreen.getSelectDirectoryButton().addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog directoryDialog = new DirectoryDialog(selectScreen.getShell());
				String directorySelected = directoryDialog.open();
				if (directorySelected != null) {
					scannerViewModel.setScanDirectory(directorySelected);
					scannerViewModel.getNewFiles().clear();
				}
			}
			
		});
		
		selectScreen.getSelectFilesButton().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(selectScreen.getShell(), SWT.OPEN | SWT.MULTI);
				if (scannerViewModel.getScanDirectory() != null) {
					dialog.setFilterPath(scannerViewModel.getScanDirectory());
				}
				dialog.open();
				String filterPath = dialog.getFilterPath();
				String[] filenames = dialog.getFileNames();
				if (filenames.length == 0) {
					return;
				}
				
				List<FileVM> fileVms = new ArrayList<FileVM>();
				for (int i = 0; i < filenames.length; i++) {
					fileVms.add(new FileVM(filenames[i], filterPath, filterPath + File.separator + filenames[i], i));
				}
				
				SubmitFilesScreen submitFiles = new SubmitFilesScreen(selectScreen.getShell().getDisplay());
				SubmitFilesScreenDatabinding.databind(submitFiles, connection, authKey, fileVms, scannerViewModel.getTagListVm(), scannerViewModel.getRequestId(), scannerViewModel.getSources());
				submitFiles.getShell().pack();
				submitFiles.getShell().open();

			}
		});
	}
}
