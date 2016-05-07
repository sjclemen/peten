package ca.edmssubmit.backend.dirwatcher;

import java.io.File;
import java.io.FileFilter;
import java.util.HashSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.google.common.base.Optional;

public class DirWatcherThread implements Runnable {
	protected final static Logger logger = Logger.getLogger(DirWatcherThread.class);

	private final FileAlterationHandler alterationHandler;
	private final LinkedBlockingQueue<WatcherThreadMessage> messages = new LinkedBlockingQueue<WatcherThreadMessage>();
	private final long sleepIntervalMillis;
	
	private Optional<String> directory;
	private Optional<HashSet<File>> directoryContents;
	
	public DirWatcherThread(FileAlterationHandler alterationHandler) {
		this(alterationHandler, 2000);
	}
	
	public DirWatcherThread(FileAlterationHandler alterationHandler, long sleepIntervalMillis) {
		this.alterationHandler = alterationHandler;
		this.sleepIntervalMillis = sleepIntervalMillis;
		this.directory = Optional.absent();
		this.directoryContents = Optional.absent();
	}
	
	public void enqueueMessage(WatcherThreadMessage message) {
		if (!messages.offer(message)) {
			throw new RuntimeException("Failed to queue message for directory watcher.");
		}
	}
	
	@Override
	public void run() {
		while (true) {
			try {
				WatcherThreadMessage message = messages.poll(sleepIntervalMillis, TimeUnit.MILLISECONDS);
				if (message != null) {
					if (message instanceof WatcherThreadPoisonPill) {
						break;
					}
					if (message instanceof WatcherThreadChangeDirectory) {
						WatcherThreadChangeDirectory cdMessage = (WatcherThreadChangeDirectory) message;
						if (cdMessage.directory == null) {
							directory = Optional.absent();
							directoryContents = Optional.absent();
						} else {
							this.directory = Optional.of(cdMessage.directory);
							directoryContents = Optional.of(getFilesFromDirectory(directory.get()));
						}
					}
				}
				// scan motherfucker
				if (this.directory.isPresent()) {
					HashSet<File> newDirectoryContents = getFilesFromDirectory(this.directory.get());
					// this case might occur if we hit an IO exception somewhere
					if (!directoryContents.isPresent()) {
						directoryContents = Optional.of(newDirectoryContents);
						continue;
					} else {
						for (File oldExistingFile : directoryContents.get()) {
							if (!newDirectoryContents.contains(oldExistingFile)) {
								// create delete message
								alterationHandler.onFileDelete(oldExistingFile);
							}
						}
						
						for (File newExistingFile : newDirectoryContents) {
							if (!directoryContents.get().contains(newExistingFile)) {
								// create new message
								alterationHandler.onFileCreate(newExistingFile);
							}
						}
						this.directoryContents = Optional.of(newDirectoryContents);
					}
				}
			} catch (Exception e) {
				logger.error("Error in DirWatcherThread", e);
			}
		}
	}
	
	private static HashSet<File> getFilesFromDirectory(String directory) {
		File directoryFile = new File(directory);
		if (!directoryFile.exists() || !directoryFile.isDirectory()) {
			return new HashSet<File>();
		}
		
		File[] interestingFiles = directoryFile.listFiles(new FileFilter() {
			private final String[] WANTED_EXTENSIONS = { ".bmp", ".pdf", ".png", ".jpg", ".odt" };
			
			@Override
			public boolean accept(File arg0) {
				if (!arg0.isFile()) {
					return false;
				}
				for (int i = 0 ; i < WANTED_EXTENSIONS.length; i++) {
					if (arg0.getName().endsWith(WANTED_EXTENSIONS[i])) {
						return true;
					}
				}
				return false;
			}
			
		});
		HashSet<File> ret = new HashSet<File>();
		for (int i = 0; i < interestingFiles.length; i++) {
			ret.add(interestingFiles[i]);
		}
		return ret;
	}	
	
	public interface WatcherThreadMessage {
		
	}
	
	public static class WatcherThreadPoisonPill implements WatcherThreadMessage {
		
	}
	
	public static class WatcherThreadChangeDirectory implements WatcherThreadMessage {
		public final String directory;
		public WatcherThreadChangeDirectory(String directory) {
			this.directory = directory;
		}
	}
	
}

