package ca.edmssubmit.backend.manager;

import java.util.concurrent.Executor;

import org.eclipse.swt.widgets.Display;

public class BackendConnectionDisplay extends BackendConnection {
	private final Executor displayExecutor;

	public BackendConnectionDisplay(BackendThreadPoolExecutor backendThreadPool, Display display) {
		super(backendThreadPool);
		displayExecutor = new DisplayExecutor(display);
	}

	@Override
	public Executor getExecutor() {
		return displayExecutor;
	}
	
	public static class DisplayExecutor implements Executor {
		private final Display display;
		
		public DisplayExecutor(Display display) {
			this.display = display;
		}

		@Override
		public void execute(Runnable command) {
			display.asyncExec(command);
		}
		
	}

}
