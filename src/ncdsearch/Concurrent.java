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
	private boolean closeOnComplete;

	public Concurrent(OutputStream out) {
		this(Integer.parseInt(System.getProperty("sarf.threads", "0")), out);		
	}
	
	/**
	 * Close a given output stream after completion of all tasks and "onComplete" task. 
	 * @param value
	 */
	public void setAutoCloseOnComplete(boolean value) {
		this.closeOnComplete = value;
	}
	

	public Concurrent(int thread, OutputStream out) {
		if (thread <= 0) {
			thread = Runtime.getRuntime().availableProcessors();	
			if (thread > 4) {
				thread = thread - 2; // don't occupy all the processors
			}
		}
		executor = Executors.newFixedThreadPool(thread);
		this.commonOutputStream = out;
	}
		
	public void execute(Task task) {
		executor.execute(new TaskExecution(task));
	}
	
	public void waitComplete() {
		waitComplete(null);
	}
	
	/**
	 * Wait completion of all tasks and then execute a clean up action.
	 * @param onComplete
	 */
	public void waitComplete(Runnable onComplete) {
		try {
			executor.shutdown();
			executor.awaitTermination(1, TimeUnit.DAYS);
			if (onComplete != null) onComplete.run();
			if (closeOnComplete) {
				try {
					commonOutputStream.close();
				} catch (IOException e) {
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
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
