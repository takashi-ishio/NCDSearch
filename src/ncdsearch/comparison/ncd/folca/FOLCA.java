package ncdsearch.comparison.ncd.folca;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * An implementation of FOLCA algorithm.
 * The algorithm is explained in [1] and [2].
 * Note that this implementation focuses on normalized compression distance;
 * the code does not output a compression form of data.
 * 
 * [1] S. Maruyama et al. Fully Online Grammar Compression, 
 * SPIRE 2013. DOI:10.1007/978-3-319-02432-5_25
 * 
 * [2] S. Maruyama et al. An Online Algorithm for 
 * Lightweight Grammar-Based Compression.  
 * Algorithms, vol.5, pp.214--235, 2012. 
 * DOI:10.3390/a5020214
 *
 * @author ishio
 */
public class FOLCA {

	private ArrayList<Queue> queues;
	private Dictionary d;
	private int count;
	
	public FOLCA() {
		queues = new ArrayList<>();
		queues.add(new Queue());
		d = new Dictionary();
		count = 0;
	}
	
	public void finish() {
		if (count > 0) {
			for (int i=0; i<queues.size(); i++) {
				postprocess(i);
			}
			d.setRoot(queues.get(queues.size()-1).get(2));
		}
	}
	
	public void process(byte[] buf) {
		for (int i=0; i<buf.length; i++) {
			process(buf[i]);
		}
	}
	
	public void process(byte[] buf, int start, int length) {
		for (int i=0; i<length; i++) {
			process(buf[start + i]);
		}
	}

	private void process(byte b) {
		processSymbols(0, b);
		count++;
	}
	
	private void processSymbols(int queue, int b) {
		Queue q = getQueue(queue);
		q.enque(b);
		if (q.size() == 4) {
			if (!isLandmark(q, 1)) {
				// Replace two symbols
				int y = d.getSymbol(q.get(2), q.get(3));
				processSymbols(queue+1, y);
				q.deque2();
			}
		} else if (q.size() == 5) {
			int y = d.getSymbol(q.get(3), q.get(4));
			int z = d.getSymbol(q.get(2), y);
			processSymbols(queue+1, z);
			q.deque3();
		}
	}

	private boolean isLandmark(Queue q, int index) {
		assert q.size()>= index+2;
		if ((q.get(index) == q.get(index+1))) return true;
		else if ((q.get(index+1) == q.get(index+2))) return false;
		else if (q.isMinimal(index) || q.isMaximal(index)) return true;
		else if (q.isMinimal(index+1) || q.isMaximal(index+1)) return false;
		else return true;
	}
	

	private void postprocess(int queue) {
		Queue q = getQueue(queue);
		assert q.size() >= 2 || q.size() <= 4;
		if (q.size() == 4) {
			// Replace two symbols
			int y = d.getSymbol(q.get(2), q.get(3));
			processSymbols(queue+1, y);
			q.deque2();
		} else if (q.size() == 3) {
			if (queue < queues.size()-1) {
				processSymbols(queue+1, q.get(2));
			}
		} 
	}

	private Queue getQueue(int q) {
		assert q <= queues.size();
		if (queues.size() == q) {
			queues.add(new Queue());
		}
		return queues.get(q);
	}
	
	public void dump() {
		System.out.println("d: " + d.toString());
		for (int i=0; i<queues.size(); i++) {
			System.out.println("q" + i + ": " + queues.get(i).toString());
		}
		System.out.println();
	}

	public byte[] decode() {
		return d.decode();
	}
	
	public int getDictionarySize() {
		return d.size();
	}
	
	public HashSet<String> getStringSet() {
		return d.getStrings();
	}

}

