package ncdsearch;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Concurrent {

	private ExecutorService executor;
	private OutputStream commonOutputStream;
	
	/**
	 * Create a concurrent executor that uses a specified number of worker threads.
	 * If a thread is 0, this object directly executes tasks in a single thread.  
	 * @param thread
	 * @param out
	 */
	public Concurrent(int thread, OutputStream out) {
		if (thread > 0) {
			executor = Executors.newFixedThreadPool(thread);
		}
		this.commonOutputStream = out;
	}
		
	/**
	 * Start a task using a working thread (or this thread).
	 * @param task
	 */
	public void execute(Task task) {
		if (executor != null) {
			executor.execute(new TaskExecution(task));
		} else {
			try {
				task.run(commonOutputStream);
			} catch (IOException e) {
			}
		}
	}
	
	/**
	 * Wait completion of all tasks.
	 */
	public void waitComplete() {
		waitComplete(null);
	}
	
	/**
	 * Wait completion of all tasks and then execute a clean-up action.
	 * @param onComplete specifies the clean-up action.
	 */
	public void waitComplete(Runnable onComplete) {
		if (executor != null) {
			try {
				executor.shutdown();
				executor.awaitTermination(1, TimeUnit.DAYS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (onComplete != null) onComplete.run();
	}
	
	private class TaskExecution implements Runnable {
		
		private Task task;
		
		public TaskExecution(Task t) {
			this.task = t;
		}
		
		@Override
		public void run() {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream(65536);
			try {
				if (task.run(buffer)) {
					if (commonOutputStream != null) {
						synchronized(commonOutputStream) {
							buffer.writeTo(commonOutputStream);
						}
					}
				}
			} catch (IOException e) {
			}
		}
	}
	
	public interface Task {
		/**
		 * An implementation 
		 * @param out is an output stream for writing a result.
		 * The written result is actually saved to a stream when this method returns true. 
		 * The written result is discarded if this method returns false. 
		 * @return
		 * @throws IOException
		 */
		public boolean run(OutputStream out) throws IOException;
	}
	
}
