package ca.edmssubmit.backend.dirwatcher;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.google.common.eventbus.AsyncEventBus;

public class FileAlterationHandler implements FileCreateOrDeleteHandler {
	protected final static Logger logger = Logger.getLogger(FileAlterationHandler.class);
	private final AsyncEventBus eventBus;
	
	public FileAlterationHandler(AsyncEventBus eventBus) {
		this.eventBus = eventBus;
	}

	@Override
	public void onFileCreate(File file) {
		try {
			eventBus.post(new FileCreatedMessage(file.getName(), file.getParent(), file.getCanonicalPath()));
		} catch (IOException e) {
			logger.error("IOException while getting canonical path.", e);
		}
	}
	
	@Override
	public void onFileDelete(File file) {
		try {
			eventBus.post(new FileDeletedMessage(file.getName(), file.getParent(), file.getCanonicalPath()));
		} catch (IOException e) {
			logger.error("IOException while getting canonical path.", e);
		}
	}
	
	public static class FileMessage {
		public final String name;
		public final String directory;
		public final String canonicalPath;
		
		public FileMessage(String name, String directory, String canonicalPath) {
			this.name = name;
			this.directory = directory;
			this.canonicalPath = canonicalPath;
		}
	}
	
	public static class FileCreatedMessage extends FileMessage {

		public FileCreatedMessage(String name, String directory, String canonicalPath) {
			super(name, directory, canonicalPath);
		}
		
	}
	
	public static class FileDeletedMessage extends FileMessage {

		public FileDeletedMessage(String name, String directory, String canonicalPath) {
			super(name, directory, canonicalPath);
		}
		
	}
}
