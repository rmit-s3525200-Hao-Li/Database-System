import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Scanner;

public class Search {
	BTree<Integer, ArrayList<Integer>> bTree;
	private PrintWriter pw;

	public Search() {
		// Build B-tree based on Index file
		bTree = readIndexAndBuildBTree("Index");
	}

	public ArrayList<String> search(int index) {
		// Get the offset from index file
		ArrayList<Integer> offset = bTree.get(index);
		ArrayList<String> content = new ArrayList<String>();
		try {
			pw = new PrintWriter(new FileWriter("output"), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < offset.size(); i++) {
			//Get the data from test file with offset from Index file
			content.add(seekContent("test", offset.get(i)));
		}
		return content;
	}

	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		int input = 0;
		boolean b = true;
		System.out.println("Please enter the number:");
		//User I/O control, allowed number only
		while (b) {
			try {
				input = in.nextInt();// If input are not integer,throw exception
				b = false;// If input is integer, end the loop
			} catch (Exception e) {
				System.out.println("Error, please enter a number again:");
				in.nextLine();
			}
		}
		//Search function running time
		long startTime = System.currentTimeMillis();
		Search search = new Search();
		//If the user input a number does not exist in Index file then throw the exception
		try{
		search.search(input);
		long endTime = System.currentTimeMillis();
		System.out.println("Select all data from data file where hourly_count = "+input);
		System.out.println("Search Time:" + (endTime - startTime) + "ms");
		}catch(Exception e){
			System.out.println("Index does not exist");
		}
		in.close();
	}
	
	//Construct B-tree in memory
	public BTree<Integer, ArrayList<Integer>> readIndexAndBuildBTree(String filename) {
		//Initial B-tree
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		BTree<Integer, ArrayList<Integer>> bTree = new BTree<Integer, ArrayList<Integer>>();
		// Read data into memory from lexicon file.
		try {
			fileReader = new FileReader(filename);
			bufferedReader = new BufferedReader(fileReader);
			// Read the data from file split by space
			String currentLine = null;
			String data[] = null;
			while ((currentLine = bufferedReader.readLine()) != null) {
				data = currentLine.split(" ");
				//Key is the data from start and end with space
				Integer key = Integer.parseInt(data[0]);
				char[][] charArray = new char[data.length - 1][];
				for (int i = 1; i < data.length; i++) {
					charArray[i - 1] = data[i].toCharArray();
				}
				//Read data from Index file and deal with the String, Byte and char
				ArrayList<Integer> value = new ArrayList<Integer>();
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < charArray.length; i++) {
					for (int j = 0; j < charArray[i].length; j++) {
						if (charArray[i][j] == ']' || charArray[i][j] == ',' || charArray[i][j] == '[') {
							continue;
						} else {
							sb.append(charArray[i][j]);
						}
					}
					if (i != charArray.length - 1) {
						sb.append(" ");
					}
				}
				String[] values = sb.toString().split(" ");
				sb.delete(0, sb.length());
				for (int i = 0; i < values.length; i++) {
					value.add(Integer.parseInt(values[i]));
				}
				// Put the key value into B-tree
				bTree.put(key, value);
			}
			// Finish read close bufferedReader and fileReader
			bufferedReader.close();
			fileReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bTree;
	}

	// Use seek() function based on RandomAccessFile class to find the data by offset.
	private String seekContent(String filename, int offset) {
		RandomAccessFile sourceFile = null;
		//Allocate 120 bytes to a line of data
		byte[] data = new byte[120];
		try {
			//RandomAccessFile read only
			sourceFile = new RandomAccessFile(new File(filename), "r");
			//Seek the offset to find data
			sourceFile.seek(offset);
			//Read the data
			sourceFile.read(data);
			//Write the data into output file
			pw.println(new String(data));
			System.out.println(new String(data));
			sourceFile.close();
		} catch (IOException e) {
			System.out.println("Read Doc failed");
			System.out.println(e.toString());
		}
		return new String(data);

	}

}
