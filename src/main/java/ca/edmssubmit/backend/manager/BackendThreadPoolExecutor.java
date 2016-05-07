package ca.edmssubmit.backend.manager;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.eventbus.AsyncEventBus;

/**
 * Thread pool executor which uses a priority blocking queue.
 */
public class BackendThreadPoolExecutor extends ThreadPoolExecutor {
	private final AsyncEventBus eventBus;

	public BackendThreadPoolExecutor(AsyncEventBus eventBus) {
		super(2, 6, 60, TimeUnit.SECONDS, new PriorityBlockingQueue<Runnable>());
		this.eventBus = eventBus;
	}

	@Override
	public void beforeExecute(Thread t, Runnable r) {
		super.beforeExecute(t, r);
		if (r instanceof BackendListenableFutureTask) {
			BackendListenableFutureTask<?> task = (BackendListenableFutureTask<?>)r;
			task.setEventBus(eventBus);
		}
	}
}
