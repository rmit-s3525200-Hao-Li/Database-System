import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Heapfile {
	public static void main(String[] args) {
		// Main function and calculate the function running time
		Heapfile h = new Heapfile();
		long startTime = System.currentTimeMillis();
		h.test();
		long endTime = System.currentTimeMillis();
		System.out.println("Load Time:" + (endTime - startTime) + "ms");
	}

	public void test() {
		// Define the original data file
		readLineByLine("Test.csv");
	}

	public void readLineByLine(String fileName) {
		// Set the page size
		int Page_size = 4096;
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		try {
			// read the file into heap by fileReader and bufferedReader
			fileReader = new FileReader(fileName);
			bufferedReader = new BufferedReader(fileReader);
			// Define the buffer
			String currentLine = null;
			byte[] data = new byte[Page_size];
			// Define the line size in page
			int lineSize = 120;
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			int linesForPage = 0;
			int lines = 0;
			// Use Hash map to generate the index relation
			HashMap<Integer, ArrayList<Integer>> map = new HashMap<Integer, ArrayList<Integer>>();
			bufferedReader.readLine();
			int pageNumber = 0;
			int totalOffset = 0;
			int pageOffset = 0;
			if (Page_size == 4096) {
				pageOffset = 16;
			} else {
				pageOffset = 32;
			}
			// Use loop to create the key and store the offset follow by key
			while ((currentLine = bufferedReader.readLine()) != null) {
				// Skip first line in original data file(header)
				lines++;
				totalOffset = pageOffset * pageNumber;
				// Identify the key as "hourly_count"
				int index = currentLine.lastIndexOf(',');
				int key = Integer.parseInt(currentLine.substring(index + 1));
				// System.out.println(key);
				// Read first line in order to initial the hashmap.
				if (lines == 1) {
					ArrayList<Integer> values = new ArrayList<Integer>();
					values.add((lines - 1) * lineSize + totalOffset);
					map.put(key, values);
					// Compare the temkey with key which already exist in
					// hashmap
				} else {
					int tempkey = -1;
					Iterator<Integer> iterator = map.keySet().iterator();
					while (iterator.hasNext()) {
						// If the key alreadly exist in hashmap, then add the
						// value behind that key
						if (key == (int) iterator.next()) {
							tempkey = key;
							map.get(key).add((lines - 1) * lineSize + totalOffset);
							break;
						}
					}
					// Otherwise create a new arraylist to store the values
					if (tempkey == -1) {
						ArrayList<Integer> values = new ArrayList<Integer>();
						values.add((lines - 1) * lineSize + totalOffset);
						map.put(key, values);
					}
				}
				// Identify the data
				data = currentLine.getBytes();
				// Define the buffer size is page_size - 2, 2 bytes for "Number
				// of record"
				if (data.length + bos.size() < Page_size - 2) {
					linesForPage++;
					bos.write(data);
					// If finish insert record still have space then insert " "
					do {
						bos.write(" ".getBytes());
					} while (bos.size() < linesForPage * lineSize);
				} else {

					do {
						// After print the number of record still have space,
						// use " " fill the space
						bos.write(" ".getBytes());
					} while (bos.size() < Page_size - 2);
					// Write the number of record at the end of page
					bos.write(Integer.toString(linesForPage).getBytes());
					System.out.println(bos.size());
					this.writeContent("test", bos.toByteArray());
					// Buffer close
					bos.close();
					// Start with new Buffer
					bos = new ByteArrayOutputStream();
					// System.out.println(bos.size());
					bos.write(data);
					linesForPage = 1;
					do {
						bos.write(" ".getBytes());
					} while (bos.size() < lineSize);

					pageNumber++;
				}
			}
			System.out.println("Total page number: " + pageNumber);
			// Close the resource
			bufferedReader.close();
			fileReader.close();
			writeIndex("Index", map);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Write the page from heap to disc
	private void writeContent(String filename, byte[] data) {
		// TODO Auto-generated method stub
		FileOutputStream fileOuputStream = null;
		try {
			fileOuputStream = new FileOutputStream(filename, true);
			fileOuputStream.write(data);
			fileOuputStream.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fileOuputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// Write the index out
	private void writeIndex(String filename, HashMap<Integer, ArrayList<Integer>> map) throws IOException {
		// TODO Auto-generated method stub
		FileWriter mapWriter = null;
		BufferedWriter writer = null;
		try {
			mapWriter = new FileWriter(new File("Index"));
			writer = new BufferedWriter(mapWriter);
			// Use iterator to write the key, value to Index file
			Iterator<Integer> iterator = map.keySet().iterator();
			while (iterator.hasNext()) {
				int key = (int) iterator.next();
				writer.write(key + " " + map.get(key));
				writer.newLine();
			}
			writer.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		writer.close();
		mapWriter.close();
	}
}
