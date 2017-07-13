public class BTree<Key extends Comparable<Key>, Value> {
	private static final int M = 4;

	private Node root; // root of the B-tree
	private int height; // height of the B-tree
	private int n;// number of key-value pairs in B-tree

	private static final class Node {
		private int m; // number of children
		private Entry[] children = new Entry[M]; // the array of children

		// create a node with k children
		private Node(int k) {
			m = k;
		}
	}

	// internal nodes: only use key, next and pointer 
	// external nodes: only use key and value
	private static class Entry {
		private Comparable<?> key;
		private final Object val;
		private Node next; // helper field to iterate over array entries

		public Entry(Comparable<?> key, Object val, Node next) {
			this.key = key;
			this.val = val;
			this.next = next;
		}
	}

	// Initializes an empty B-tree.
	public BTree() {
		root = new Node(0);
	}
	//return the number of key-values pairs in this symbol table
	public int size() {
		return n;
	}
	//return the height of the B-tree
	public int height() {
		return height;
	}
	//get function, give the key return the value
	public Value get(Key key) {
		if (key == null)
			throw new IllegalArgumentException("argument to get() is null");
		return search(root, key, height);
	}

	@SuppressWarnings("unchecked")
	private Value search(Node x, Key key, int ht) {
		Entry[] children = x.children;

		// Search function based on compare the key equal or not 
		if (ht == 0) {
			for (int j = 0; j < x.m; j++) {
				if (eq(key, children[j].key))//If equal, return the values
					return (Value) children[j].val;
			}
		}

		// internal node use the recurrence traversal find the next
		else {
			for (int j = 0; j < x.m; j++) {
				if (j + 1 == x.m || less(key, children[j + 1].key))
					//If not equal, return the pointer to find the next
					return search(children[j].next, key, ht - 1);
			}
		}
		return null;
	}
	/**
	   * Inserts the key-value pair into the symbol table, overwriting the old value
	   * with the new value if the key is already in the symbol table.
	   * If the value is {@code null}, this effectively deletes the key from the symbol table.
	   */
	public void put(Key key, Value val) {
		if (key == null)
			throw new IllegalArgumentException("argument key to put() is null");
		Node u = insert(root, key, val, height); // right node after split
		n++;
		if (u == null)
			return;
		//need split root and recombine the root
		Node t = new Node(2);
		t.children[0] = new Entry(root.children[0].key, null, root);
		t.children[1] = new Entry(u.children[0].key, null, u);
		root = t;
		height++;
	}

	private Node insert(Node h, Key key, Value val, int ht) {
		int j;
		Entry t = new Entry(key, val, null);

		// external node also the leaf-node but in the lowest level and store the value
		if (ht == 0) {
			for (j = 0; j < h.m; j++) {
				if (less(key, h.children[j].key))
					break;
			}
		}

		// internal node store the next node address
		else {
			for (j = 0; j < h.m; j++) {
				if ((j + 1 == h.m) || less(key, h.children[j + 1].key)) {
					Node u = insert(h.children[j++].next, key, val, ht - 1);
					if (u == null)
						return null;
					t.key = u.children[0].key;
					t.next = u;
					break;
				}
			}
		}

		for (int i = h.m; i > j; i--)
			h.children[i] = h.children[i - 1];
		h.children[j] = t;
		h.m++;
		//determine current node more than 4 or not. If full, return split
		if (h.m < M){
			return null;
		}else{
			//split the node
			return split(h);
		}
	}

	// split root node in half
	private Node split(Node h) {
		Node t = new Node(M / 2);
		h.m = M / 2;
		for (int j = 0; j < M / 2; j++)
			t.children[j] = h.children[M / 2 + j];
		return t;
	}
	// comparison functions - make Comparable instead of Key to avoid casts
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private boolean less(Comparable k1, Comparable k2) {
		return k1.compareTo(k2) < 0;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private boolean eq(Comparable k1, Comparable k2) {
		return k1.compareTo(k2) == 0;
	}

}
