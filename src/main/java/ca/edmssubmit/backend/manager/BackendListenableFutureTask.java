/*
 * Copyright (C) 2008 The Guava Authors, (C) 2014 Stephen Clement
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ca.edmssubmit.backend.manager;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.util.concurrent.ExecutionList;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * A {@link FutureTask} that also implements the {@link ListenableFuture}
 * interface. Unlike {@code FutureTask}, {@code ListenableFutureTask} does not
 * provide an overrideable {@link FutureTask#done() done()} method. For similar
 * functionality, call {@link #addListener}.
 *
 * <p>Differences with the official Guava implementation are primary related
 * to supporting priorities, allowing this item to be compared by the executor.
 *
 * @author Sven Mawson
 * @since 1.0
 */
public class BackendListenableFutureTask<V> extends FutureTask<V> implements
		ListenableFuture<V>, Comparable <BackendListenableFutureTask<?>>  {
	// TODO(cpovirk): explore ways of making ListenableFutureTask final. There
	// are
	// some valid reasons such as BoundedQueueExecutorService to allow extends
	// but it
	// would be nice to make it final to avoid unintended usage.

	// The execution list to hold our listeners.
	private final ExecutionList executionList = new ExecutionList();
	private final BackendTaskPriority taskPriority;
	private final Executor executor;
	private final Callable<V> callable;

	/**
	 * Creates a {@code ListenableFutureTask} that will upon running, execute
	 * the given {@code Callable}.
	 *
	 * @param callable
	 *            the callable task
	 * @since 10.0
	 */
	public static <V> BackendListenableFutureTask<V> create(Callable<V> callable, Executor executor, BackendTaskPriority priority) {
		return new BackendListenableFutureTask<V>(callable, executor, priority);
	}

	BackendListenableFutureTask(Callable<V> callable, Executor executor, BackendTaskPriority priority) {
		super(callable);
		this.callable = callable;
		this.executor = executor;
		this.taskPriority = priority;
	}

	
	public void addListener(Runnable listener) {
		executionList.add(listener, executor);
	}

	@Override
	public void addListener(Runnable listener, Executor exec) {
		executionList.add(listener, exec);
	}

	/**
	 * Internal implementation detail used to invoke the listeners.
	 */
	@Override
	protected void done() {
		executionList.execute();
	}
	
	@SuppressWarnings("rawtypes")
	public void setEventBus(AsyncEventBus eventBus) {
//		if (callable instanceof PersistenceRequest) {
//			PersistenceRequest<?> request = (PersistenceRequest)callable;
//			request.setEventBus(eventBus);
//		}
	}
	
	@Override
	public int compareTo(BackendListenableFutureTask<?> o) {
		return taskPriority.priority.compareTo(o.getPriority().priority);
	}
	
	public BackendTaskPriority getPriority() {
		return taskPriority;
	}
	
	public static enum BackendTaskPriority {
		LOW (64),
		MEDIUM (32),
		HIGH (16);
		
		public final Integer priority;
		
		BackendTaskPriority(Integer priority) {
			this.priority = priority;
		}
	}
}
