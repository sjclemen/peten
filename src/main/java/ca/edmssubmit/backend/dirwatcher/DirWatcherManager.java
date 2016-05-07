package ca.edmssubmit.backend.dirwatcher;

import org.apache.log4j.Logger;

import com.google.common.eventbus.AsyncEventBus;

import ca.edmssubmit.backend.dirwatcher.DirWatcherThread.WatcherThreadChangeDirectory;
import ca.edmssubmit.backend.dirwatcher.DirWatcherThread.WatcherThreadPoisonPill;

public class DirWatcherManager {
	protected final static Logger logger = Logger.getLogger(DirWatcherManager.class);

	private final Thread thread;
	private final DirWatcherThread dirWatcher;
	
	public DirWatcherManager(AsyncEventBus eventBus) {
		this(eventBus, null);
	}
	
	public DirWatcherManager(AsyncEventBus eventBus, String directory) {
		this.dirWatcher = new DirWatcherThread(new FileAlterationHandler(eventBus));
		thread = new Thread(dirWatcher);
	
		thread.start();
	}
	
	public void setWatchDirectory(String directory) {
		dirWatcher.enqueueMessage(new WatcherThreadChangeDirectory(directory));
	}
	
	public void stop() {
		dirWatcher.enqueueMessage(new WatcherThreadPoisonPill());
		try {
			thread.join(1000);
		} catch (InterruptedException e) {
			logger.error("Failed to shutdown DirWatcherThread", e);
		}
	}
	
	
}
