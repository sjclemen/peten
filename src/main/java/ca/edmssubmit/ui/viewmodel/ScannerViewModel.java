package ca.edmssubmit.ui.viewmodel;

import java.util.ArrayList;

import org.eclipse.core.databinding.observable.list.WritableList;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.Subscribe;

import ca.edmssubmit.backend.dirwatcher.FileAlterationHandler.FileCreatedMessage;
import ca.edmssubmit.backend.dirwatcher.FileAlterationHandler.FileDeletedMessage;

public class ScannerViewModel extends ObservableModel {
	private String scanDirectory;
	private final WritableList newFiles = new WritableList();
	private final TagListVM tagListVm;
	private ArrayList<String> sources = new ArrayList<String>(); 
	private int requestCounter = 0;
	
	public ScannerViewModel(String scanDirectory, AsyncEventBus eventBus, TagListVM tagListVm) {
		this.scanDirectory = scanDirectory;
		this.tagListVm = tagListVm;
		eventBus.register(new DirWatcherEventHandler());
	}
	
	public TagListVM getTagListVm() {
		return tagListVm;
	}

	public WritableList getNewFiles() {
		return newFiles;
	}
	
	public String getScanDirectory() {
		return scanDirectory;
	}

	public void setScanDirectory(String scanDirectory) {
		firePropertyChange("scanDirectory", this.scanDirectory, this.scanDirectory = scanDirectory);
	}
	
	public class DirWatcherEventHandler {
		@Subscribe
		public void onFileCreate(FileCreatedMessage message) {
			FileVM fvm = new FileVM(message.name, message.directory, message.canonicalPath, 0);
			if (!newFiles.contains(fvm)) {
				newFiles.add(fvm);
			}
		}
		
		@Subscribe
		public void onFileDelete(FileDeletedMessage message) {
			FileVM fvm = new FileVM(message.name, message.directory, message.canonicalPath, 0);
			newFiles.remove(fvm);
		}
	}

	public Integer getRequestId() {
		requestCounter++;
		return requestCounter;
	}

	public ArrayList<String> getSources() {
		return sources;
	}

	public void setSources(ArrayList<String> sources) {
		this.sources = sources;
	}
}
