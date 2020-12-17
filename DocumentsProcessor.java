import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeSet;


//import sm_doc_set.Mobile;

/**
 * @author Steven Chang
 * @date February 3, 2020
 */
public class DocumentsProcessor implements IDocumentsProcessor{
	
	/**
	 * The processDocuments method takes in a directory of documents and an integer specifying the length of contiguous
	 * word sequences to be analyzed. It parses through each document and returns a hashmap where the keys are the document
	 * file names and the values are array lists that include all the n-word sequences that came out of each file
	 * 
     * @param directoryPath - user-defined directory path that holds all the documents
     * @param n - length of contiguous word sequences to be analyzed
     * @return Hashmap with keys as document names and values as array lists holding all n-word sequences that came out of
     * each file
     */
	@Override
	public Map<String, List<String>> processDocuments(String directoryPath, int n) {

		try {
			File directory = new File(directoryPath);
			File[] all_files = directory.listFiles();
			FileReader reader = null;
			BufferedReader br = null;
			DocumentIterator iter = null;
			
			HashMap<String, List<String>> processedDocuments = new HashMap<>();
						
			// Loop through each document in the directory
			List<String> sequences = null;
			for (int i = 0; i < all_files.length; i++) {				
					
				
				if (all_files[i].getName().contains(".txt")) { // Prevents .DS_Store from being processed
					reader = new FileReader(all_files[i]);
					br = new BufferedReader(reader);
					iter = new DocumentIterator(br, n);
					sequences = new ArrayList<String>();
				
					// Add all n-word sequences in one file to an ArrayList
					while (iter.hasNext()) {
						String element = iter.next();
						if (element.length() > 0) {
							sequences.add(element);	
						}
					}
				
					// Associate each ArrayList to the file where the words came from
					processedDocuments.put(all_files[i].getName(), sequences);
				
				}
				
			}
			
			// System.out.println("End reached");
			return processedDocuments;
			
		} catch (FileNotFoundException | NullPointerException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	
	
	
	/**
	 * This method takes in the hashmap generated from the previous method, reads through it key by key, and deposits all
	 * the n-words found in all the files into one single file, with each n-word separated by a space. An index is maintained
	 * to keep track of which word sequences came out of which file.
	 * 
     * @param docs - Hashmap of document names and n-words created from processDocuments
     * @param nwordFilePath - user-specified destination to store a central file of all n-words found
     * @return Array list of tuples that map each file name with its index (how many characters came out of that file)
     */
	@Override
	public List<Tuple<String, Integer>> storeNWordSequences(Map<String, List<String>> docs, String nwordFilePath) {
		// TODO Auto-generated method stub
		try {
			FileWriter writer = new FileWriter(nwordFilePath);
			List<String> nWordSequences = new ArrayList<String>();
			List<Tuple<String, Integer>> filesAndSizes = new ArrayList<Tuple<String, Integer>>();
			
			// Iterate through all documents (keys) in the TreeSet
			for (String documentKey : docs.keySet()) {
				
				// Collect the ArrayList holding all the n-word sequences in the document
				nWordSequences = docs.get(documentKey);
				
				// Iterate through the ArrayList and write each n-word sequence into one giant file, separated by spaces
				// Count how many bytes written; size of each file
				int fileSizeBytes = 0;
				for (String element : nWordSequences) {
					writer.write(element + " ");
					fileSizeBytes = fileSizeBytes + element.length() + 1;
				}
				
				// Associate each file with its corresponding size
				Tuple<String, Integer> association = new Tuple<String, Integer>(documentKey, fileSizeBytes);
				filesAndSizes.add(association);
				//System.out.println(fileSizeBytes);
			}
			writer.close();
			return filesAndSizes;
			
		} catch (IOException | NullPointerException e) {
			e.printStackTrace();
		}
				
		return null;
		
	}

	
	
	
	
	/**
	 * This method reads through all the n-word sequences from all files and, using the file indices as a guide, computes
	 * the number of shared n-word sequences between file pairs.
     * @param nwordFilePath - path to the central file that holds all the n-word sequences
     * @param fileindex - list of tuples that map each file name its index (the number of characters that came out of 
     * that file)
     * @return TreeSet of Similiarities objects, where each hold a file pair that share an n-word sequence, and a count of
     * how many n-word similarities are found between that file pair
     */
	@Override
	public TreeSet<Similarities> computeSimilarities(String nwordFilePath, List<Tuple<String, Integer>> fileindex) {
		// TODO Auto-generated method stub
		try {			
			
			HashMap<String, List<String>> mapping = new HashMap<>();
			TreeSet<Similarities> similarities = new TreeSet<>();
			
			RandomAccessFile r = new RandomAccessFile(nwordFilePath, "rw");
			
			Similarities temp;
			
			// Loop through each file : file-size (index) pair
			for (Tuple<String, Integer> file : fileindex) {
				
				String fileName = file.getLeft();
				// Create byte array with size equal to the size of the file
				byte[] byteArray = new byte[file.getRight()];
				
				// Populate byte array with the n-words from that file and convert byte array into String
				r.read(byteArray, 0, byteArray.length);
				String byteArrayToString = new String(byteArray);
				
				// Parse through the String and create a key for each n-word in n-word--file mapping
				StringTokenizer parseToken = new StringTokenizer(byteArrayToString, " ");
				while (parseToken.hasMoreTokens()) {
					String token = parseToken.nextToken().toLowerCase();
					
					// If key is not yet present in map, add key and file where key (n-word) is found to the map
					if (mapping.containsKey(token) == false) {
						
						// Create ArrayList to hold list of files that contain this particular n-word
						ArrayList<String> values = new ArrayList<String>();
						values.add(fileName);
						
						// Map that ArrayList to the corresponding n-word
						mapping.put(token, values);
					} else {
						
						// If key is already present, get the ArrayList mapped to that key, and add one more file (the current one) to it
						// If the same file contains the same n-word sequence more than once, it is only added once
						if (!mapping.get(token).contains(fileName)) {
							
							// When one file shares an n-word with one or more files, a Similarities object is created between this incoming
							// file and every other file that already has that n-word (already present in the ArrayList)
							for (int i = 0; i < mapping.get(token).size(); i++) {
								temp = new Similarities(fileName, mapping.get(token).get(i));
								if (similarities.contains(temp)) {
									// Increment count
									int currentCount = similarities.floor(temp).getCount();
									similarities.floor(temp).setCount(currentCount + 1);
								} else {
									similarities.add(temp);
									similarities.floor(temp).setCount(1);
								}
								
							}
							
							// Add this new incoming file to the ArrayList of files that have this particular n-word
							mapping.get(token).add(fileName);
							
						}
					}
				}
				
			}
			r.close();
			
			// Test
			for (String key : mapping.keySet()) {
				System.out.println(key + ": " + mapping.get(key));
			}
			// Test
			
			return similarities;
			
		} catch (IOException | NullPointerException e) {
			e.printStackTrace();
		}
		return null;
	}

	
	
	
	
	/**
	 * This method takes in the TreeSet of all the similarities file pairs, sorts them in descending order, and prints out
	 * similarities pairs with counts greater than or equal to a user-specified threshold.
     * @param sims - TreeSet holding all the similarities file pairs from the previous method
     * @param threshold - user-specified number of hits between document pairs that is to be printed out
     */
	@Override
	public void printSimilarities(TreeSet<Similarities> sims, int threshold) {
		// TODO Auto-generated method stub
		
		Comparator<Similarities> comp = new Comparator<Similarities>() {

			//Redefine the compare method here, so that when iterating through the document below, each .contains call
			//uses this new definition of compare to add words to res?
						
			@Override
			public int compare(Similarities o1, Similarities o2) {
				// TODO Auto-generated method stub
				if (o2.getCount() == o1.getCount()) {
					return o2.getFile1().compareTo(o1.getFile1());
				}
				return Integer.compare(o2.getCount(), o1.getCount());
			}
		};
		
		TreeSet<Similarities> sortedByCount = new TreeSet<>(comp);
		sortedByCount.addAll(sims);
		
		for (Similarities entry : sortedByCount) {
			if (entry.getCount() >= threshold) {
				System.out.println(entry.getFile1() + " & " + entry.getFile2() + " : " + entry.getCount() + " hits");
			}
		}
		
	}
	
	
	
	
	
	/**
	 * This is an alternative approach. This method reads the documents in the initial directory and creates the central
	 * repository for all n-word sequences directly, without creating an intermediary hashmap. This is done because for very
	 * large numbers of documents, there may not be enough memory to hold all n-word sequences in a hashmap.
     * @param directoryPath - user-defined directory path that holds all the documents
     * @param sequenceFile - path to the central file that holds all the n-word sequences
     * @param n - length of contiguous word sequences to be analyzed
     * @return Array list of tuples that map each file name with its index (how many characters came out of that file)
     */
	public List<Tuple<String, Integer>> processAndStore (String directoryPath, String sequenceFile, int n) {
		try {
			File directory = new File(directoryPath);
			File[] all_files = directory.listFiles();
			FileReader reader = null;
			BufferedReader br = null;
			DocumentIterator iter = null;
			FileWriter writer = new FileWriter(sequenceFile);
			List<Tuple<String, Integer>> fileIndex = new ArrayList<Tuple<String, Integer>>();
			
			//HashMap<String, List<String>> processedDocuments = new HashMap<>();
						
			// Loop through each document in the directory
			//List<String> sequences = null;
			for (int i = 0; i < all_files.length; i++) {				
									
				if (all_files[i].getName().contains(".txt")) { // Prevents .DS_Store from being processed
					reader = new FileReader(all_files[i]);
					br = new BufferedReader(reader);
					iter = new DocumentIterator(br, n);
					//sequences = new ArrayList<String>();
				
					// Add all n-word sequences in one file to an ArrayList
					int fileSizeBytes = 0;
					while (iter.hasNext()) {
						String element = iter.next();
						if (element.length() > 0) {
							//sequences.add(element);
							writer.write(element + " ");
							fileSizeBytes = fileSizeBytes + element.length() + 1;
						}
					}
					
					// Associate each file with its corresponding size
					Tuple<String, Integer> association = new Tuple<String, Integer>(all_files[i].getName(), fileSizeBytes);
					fileIndex.add(association);				
					
				}
				
			}
			
			System.out.println("End reached");
			writer.close();
			return fileIndex;
			
		} catch (IOException | NullPointerException e) {
			e.printStackTrace();
		}
		return null;
	}

}
