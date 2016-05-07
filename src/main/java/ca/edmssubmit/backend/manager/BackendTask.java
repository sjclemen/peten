package ca.edmssubmit.backend.manager;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import com.google.common.util.concurrent.FutureCallback;

abstract public class BackendTask<T> implements Callable<T> {
	private final static Logger logger = Logger.getLogger(BackendTask.class);

	private final ArrayList<FutureCallback<T>> callbacks = new ArrayList<FutureCallback<T>>();
	
	public void addCallback(FutureCallback<T> callback) {
		callbacks.add(callback);
	}
	
	public ArrayList<FutureCallback<T>> getCallbacks() {
		return callbacks;
	}
	
	public T call() throws Exception {
		try {
			return doCall();
		} catch (Exception e) {
			logger.error("Uncaught exception in " + this.getClass().getCanonicalName() + " on backend.", e);
			throw e;
		} catch (Throwable t) {
			logger.error("Uncaught throwable in " + this.getClass().getCanonicalName() + " on backend.", t);
			throw new Exception("Throwable in BackendTask: " + t.getMessage(), t);
		}
	}
	
	abstract public T doCall() throws Exception;
}
