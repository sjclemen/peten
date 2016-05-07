package ca.edmssubmit.backend.manager;

import java.util.concurrent.TimeUnit;

import org.eclipse.swt.widgets.Display;

import com.google.common.eventbus.AsyncEventBus;

/**
 * Manages the backend by firing it up on creation. Contains an executor.
 */
public class BackendManager {
	private final BackendThreadPoolExecutor executor;
	
	public BackendManager(AsyncEventBus eventBus) {
		executor = new BackendThreadPoolExecutor(eventBus);
		executor.prestartCoreThread();
		executor.setKeepAliveTime(60, TimeUnit.SECONDS);
	}
	
	public BackendConnection getRegularConnection() {
		return new BackendConnectionRegular(executor);
	}
	
	public BackendConnection getDisplayConnection(Display display) {
		return new BackendConnectionDisplay(executor, display);
	}
		
	public void requestShutdown() {
		executor.shutdown();
	}
	
	public void waitForShutdown(long timeout, TimeUnit unit) throws InterruptedException {
		executor.awaitTermination(timeout, unit);
	}
	
}
