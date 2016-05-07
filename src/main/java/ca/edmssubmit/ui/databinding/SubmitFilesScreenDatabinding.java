package ca.edmssubmit.ui.databinding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Table;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;

import ca.edmssubmit.api.json.TagResponse;
import ca.edmssubmit.backend.manager.BackendConnection;
import ca.edmssubmit.backend.messages.GetTagsBEReq;
import ca.edmssubmit.backend.messages.SubmitFilesBEReq;
import ca.edmssubmit.backend.messages.SubmitFilesBEResp;
import ca.edmssubmit.ui.pieces.SubmitFilesScreen;
import ca.edmssubmit.ui.pieces.tags.TagListVMBinding;
import ca.edmssubmit.ui.viewmodel.FileVM;
import ca.edmssubmit.ui.viewmodel.TagListVM;
import ca.edmssubmit.ui.viewmodel.TagVM;

public class SubmitFilesScreenDatabinding {
	public static void databind(final SubmitFilesScreen submitFilesScreen, final BackendConnection connection,
			final String authKey, final List<FileVM> files, final TagListVM tagListVm, final Integer requestId, final List<String> sources) {
		
		final WritableList orderedFiles = bindTable(submitFilesScreen, files);
		
		String uploadFilename = determineFilename(files);
		submitFilesScreen.getNameText().setText(uploadFilename);
		
		final TagListVMBinding tagListBinding = new TagListVMBinding(tagListVm, submitFilesScreen.getTagsCombo());
		
		submitFilesScreen.getSourceCombo().setItems(sources.toArray(new String[0]));
		
		submitFilesScreen.getTagsCombo().addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent arg0) {
				tagListVm.removeListener(tagListBinding.getEventListener());
			}
			
		});
		
		submitFilesScreen.getReloadTagsButton().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				submitFilesScreen.getReloadTagsButton().setEnabled(false);
				GetTagsBEReq tagsBEReq = new GetTagsBEReq(authKey);
				tagsBEReq.addCallback(new FutureCallback<List<TagResponse>>() {

					@Override
					public void onFailure(Throwable arg0) {
						if (submitFilesScreen.getShell().isDisposed()) {
							return;
						}
						MessageDialog.openError(submitFilesScreen.getShell(), "Failed to refresh tags", "Tag refresh failed: " + arg0.getMessage());
					}

					@Override
					public void onSuccess(List<TagResponse> tagResponseList) {
						tagListVm.updateTags(tagResponseList);
						if (!submitFilesScreen.getShell().isDisposed()) {
							submitFilesScreen.getReloadTagsButton().setEnabled(true);
						}
					}
					
				});
				connection.enqueueTask(tagsBEReq);
			}
		});

		
		submitFilesScreen.getCancelButton().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				submitFilesScreen.getShell().dispose();
			}
		});
		
		submitFilesScreen.getOkButton().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				submitFilesScreen.getCancelButton().setEnabled(false);
				submitFilesScreen.getOkButton().setEnabled(false);

				List<String> fileAbsolutePaths = new ArrayList<String>();
				for (Object fvm : orderedFiles) {
					fileAbsolutePaths.add(((FileVM)fvm).getCanonicalPath());
				}
				
				// collect up the tags
				HashMap<Integer, String> tagsForSubmission = new HashMap<Integer, String>();
				WritableSet tagSet = submitFilesScreen.getTagsCombo().getTagListSelected().getSelectedTags();
				Iterator i = tagSet.iterator();
				while (i.hasNext()) {
					Object tagVmUncast = i.next();
					if (tagVmUncast instanceof TagVM) {
						TagVM tvm = (TagVM)tagVmUncast;
						tagsForSubmission.put(tvm.getId(), tvm.getName());
					}
				}
				
				// remember our freshly-entered source
				if (!sources.contains(submitFilesScreen.getSourceCombo().getText())) {
					sources.add(submitFilesScreen.getSourceCombo().getText());
				}
				
				SubmitFilesBEReq backendRequest = new SubmitFilesBEReq(authKey, requestId, fileAbsolutePaths,
						submitFilesScreen.getNameText().getText(),
						tagsForSubmission, submitFilesScreen.getSourceCombo().getText());
				
				backendRequest.addCallback(new FutureCallback<SubmitFilesBEResp>() {

					@Override
					public void onFailure(Throwable arg0) {
						if (!submitFilesScreen.getShell().isDisposed()) {
							MessageDialog.openError(submitFilesScreen.getShell(), "Failed to submit document.", "Document not created: " + arg0.getMessage());
							submitFilesScreen.getCancelButton().setEnabled(true);
							submitFilesScreen.getOkButton().setEnabled(true);
						}
					}

					@Override
					public void onSuccess(SubmitFilesBEResp arg0) {						
						if (submitFilesScreen.getShell().isDisposed()) {
							return;
						}
						if (arg0.warnings.size() > 0) {
							StringBuilder warningStringBuilder = new StringBuilder();
							for (String warn : arg0.warnings) {
								warningStringBuilder.append(warn);
								warningStringBuilder.append(System.getProperty("line.separator"));
							}
							MessageDialog.openWarning(submitFilesScreen.getShell(), "Document creation warnings", "Document was created, but the following issues occured: " + warningStringBuilder.toString());
						} else {
							MessageDialog.openInformation(submitFilesScreen.getShell(), "Document created", "Document created successfully.");
						}
						submitFilesScreen.getShell().dispose();
					}
					
				});
				
				connection.enqueueTask(backendRequest);
			}
		});
		
		
	}
	
	/**
	 * Decides on a filename based on the list of files. Assumes files is of
	 * size at least one.
	 * @param files A list of FileVM objects. The first item in the list will
	 * be used in most cases, with an extension swap, and sometimes slight
	 * modification for page numbers.
	 * @return A uploadable filename.
	 */
	private static String determineFilename(List<FileVM> files) {
		Preconditions.checkArgument(files.size() > 0);
		String firstName = files.get(0).getName();
		if (files.size() == 1) {
			if (firstName.endsWith(".bmp")) {
				return firstName.substring(0, firstName.length()-4) + ".png";
			}
			return firstName;
		}
		
		// no dot? WTF? this is probably going to fail later on anyways but here we go... 
		int dotLocation = firstName.lastIndexOf('.');
		if (dotLocation == -1) {
			return firstName + ".pdf";
		}
		
		String pdfFirstName = firstName.substring(0, dotLocation) + ".pdf";
		
		int pLocation = firstName.lastIndexOf('p', dotLocation);
		if (pLocation == -1) {
			return pdfFirstName;
		}
		
		String firstStringPrefix = firstName.substring(0, pLocation);
		try {
			for (FileVM file : files) {
				String name = file.getName();	
				boolean prefixMatches = name.substring(0, pLocation).equals(firstStringPrefix);
				boolean numericIdentifier = name.substring(pLocation+1, dotLocation).matches("\\p{Digit}");
				if (!prefixMatches || !numericIdentifier) {
					return pdfFirstName;
				}
				
			}
		} catch (IndexOutOfBoundsException ioobe) {
			return pdfFirstName;
		}
		return firstStringPrefix.trim() + ".pdf";
	}

	public static WritableList bindTable(final SubmitFilesScreen submitFilesScreen, List<FileVM> files) {
		Preconditions.checkArgument(files.size() > 0);
		
		// sort list based on filenames
		Collections.sort(files, new Comparator<FileVM> () {

			@Override
			public int compare(FileVM arg0, FileVM arg1) {
				return arg0.getName().compareTo(arg1.getName());
			}
			
		});
		// assign them all an order ID
		for (int i = 0; i < files.size(); i++) {
			files.get(i).setOrder(i+1);
		}
		
		final TableViewer fileTableViewer = new TableViewer(submitFilesScreen.getFilesTable());
		final WritableList filesWritableList = new WritableList((Collection<FileVM>)files, null);
		ViewerSupport.bind(fileTableViewer, filesWritableList, BeanProperties.values(new String[] { "order", "name" }));
		fileTableViewer.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				if (e1 instanceof FileVM && e2 instanceof FileVM) {
					return ((FileVM)e1).getOrder().compareTo(((FileVM)e2).getOrder());
				} else {
					throw new RuntimeException("Non FileVM objects in filetable.");
				}
			}
		});

		
		// buttons!
		submitFilesScreen.getMoveOrderUp().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection)fileTableViewer.getSelection();
				if (selection.isEmpty()) {
					return;
				}
				FileVM selectedRow = (FileVM)selection.getFirstElement();
				int index = filesWritableList.indexOf(selectedRow);
				if (index < 1) {
					return;
				}
				FileVM destinationRow = (FileVM)filesWritableList.get(index-1);
				filesWritableList.move(index, index-1);
				
				int destinationOriginalOrder = destinationRow.getOrder();
				destinationRow.setOrder(selectedRow.getOrder());
				selectedRow.setOrder(destinationOriginalOrder);
				
				fileTableViewer.refresh();
			}
		});
		
		submitFilesScreen.getMoveOrderDown().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection)fileTableViewer.getSelection();
				if (selection.isEmpty()) {
					return;
				}
				FileVM selectedRow = (FileVM)selection.getFirstElement();
				int index = filesWritableList.indexOf(selectedRow);
				if (index > (filesWritableList.size() - 2)) {
					return;
				}
				FileVM destinationRow = (FileVM)filesWritableList.get(index+1);
				filesWritableList.move(index, index+1);
				
				int destinationOriginalOrder = destinationRow.getOrder();
				destinationRow.setOrder(selectedRow.getOrder());
				selectedRow.setOrder(destinationOriginalOrder);
				
				fileTableViewer.refresh();
			}
		});
		
		// adjust layout data
		Table filesTable = submitFilesScreen.getFilesTable();
		filesTable.layout();
		int tableBestHeight = filesTable.getHeaderHeight() + filesTable.getItemCount()*filesTable.getItemHeight();
		if (tableBestHeight < 100) {
			tableBestHeight = 60;
		}
		int tableBestWidth = filesTable.computeSize(SWT.DEFAULT, SWT.DEFAULT).x * 3;
		GridData tableLayoutData = (GridData)submitFilesScreen.getTableComposite().getLayoutData();
		tableLayoutData.heightHint = tableBestHeight;
		tableLayoutData.widthHint = tableBestWidth;
		
		
		return filesWritableList;
	}
}
