package ncdsearch.ncd.folca;


public class Queue {
	
	public static int DUMMY = Integer.MAX_VALUE;

	private int[] queue;
	private int length;

	public Queue() {
		queue = new int[] { DUMMY, DUMMY, 0, 0, 0 };
		length = 2;
	}
	
	public void enque(int b) {
		queue[length++] = b;
	}
	
	public int size() {
		return length;
	}
	
	public int get(int index) {
		assert index < length;
		return queue[index];
	}
	
	public void deque2() {
		assert length >= 2;
//		length = 2;
		queue[0] = queue[2];
		queue[1] = queue[3];
		queue[2] = queue[4];
		length -= 2;
	}
	
	public void deque3() {
		assert length >= 3;
//		length = 2;
		queue[0] = queue[3];
		queue[1] = queue[4];
		length -= 3;
	}
	
	public boolean isMinimal(int index) {
		return queue[index] > queue[index+1] && queue[index+1] < queue[index+2];
	}

	public boolean isMaximal(int index) {
		return queue[index] < queue[index+1] && queue[index+1] > queue[index+2];
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("[");
		for (int i=0; i<length; i++) {
			if (i>0) buf.append(", ");
			buf.append(queue[i]);
		}
		return buf.toString();
	}

}

