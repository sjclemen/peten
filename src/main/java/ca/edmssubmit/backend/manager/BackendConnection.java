package ca.edmssubmit.backend.manager;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import org.apache.log4j.Logger;

import ca.edmssubmit.backend.manager.BackendListenableFutureTask.BackendTaskPriority;

import com.google.common.util.concurrent.FutureCallback;

/**
 * A backend connection is a method of submitting tasks for execution on the backend and
 * providing a path for events from the backend to be executed on the requesting thread.
 */
abstract public class BackendConnection {
	protected final static Logger logger = Logger.getLogger(BackendConnection.class);
	private final BackendThreadPoolExecutor backendThreadPool;
	
	public BackendConnection(BackendThreadPoolExecutor backendThreadPool) {
		this.backendThreadPool = backendThreadPool;
	}
	
	public <T> BackendListenableFutureTask <T> enqueueTask(BackendTask <T> task) {
		return enqueueTask(task, BackendListenableFutureTask.BackendTaskPriority.MEDIUM);
	}
	
	public <T> BackendListenableFutureTask <T> enqueueTask(BackendTask <T> task, BackendTaskPriority priority) {
		final BackendListenableFutureTask <T> listenableFutureTask =
				BackendListenableFutureTask.create(task, getExecutor(), priority);
		for (final FutureCallback<T> callback : task.getCallbacks()) {
			listenableFutureTask.addListener(new Runnable() {

				@Override
				public void run() {
					try {
						callback.onSuccess(listenableFutureTask.get());
					} catch (InterruptedException e) {
						logger.error("Interrupted in backend runnable", e);
					} catch (ExecutionException e) {
						callback.onFailure(e.getCause());
					}
				}
				
			});
		}
		backendThreadPool.execute(listenableFutureTask);
		return listenableFutureTask;
	}
	
	public abstract Executor getExecutor();
	
}
