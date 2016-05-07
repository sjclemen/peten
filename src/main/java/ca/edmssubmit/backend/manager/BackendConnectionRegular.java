package ca.edmssubmit.backend.manager;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

public class BackendConnectionRegular extends BackendConnection {
	private final CallbackAtSourceExecutor executor = new CallbackAtSourceExecutor();
	
	public BackendConnectionRegular(BackendThreadPoolExecutor backendThreadPool) {
		super(backendThreadPool);
	}

	public void runCallbacks() {
		Runnable r = executor.getRunnable();
		while (r != null) {
			try {
				r.run();
			} catch (Exception ex) {
				logger.info("Callback threw exception: " + ex.getMessage(), ex);
			}
			r = executor.getRunnable();
		}
	}
	
	private static class CallbackAtSourceExecutor implements Executor {
		private final ConcurrentLinkedQueue<Runnable> localTasks = new ConcurrentLinkedQueue<Runnable>();

		@Override
		public void execute(Runnable command) {
			this.localTasks.add(command);
		}
		
		/**
		 * Returns a runnable or null.
		 */
		public Runnable getRunnable() {
			return this.localTasks.poll();
		}
	}

	@Override
	public Executor getExecutor() {
		return executor;
	}
}
