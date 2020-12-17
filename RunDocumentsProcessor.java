import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class RunDocumentsProcessor {
	public static void main(String[] args) {

		//testProcessDocuments("./src/sm_doc_set", 5);
		testProcessDocumentsRefactored("./src/big_doc_set", 5);
		
	}
	
	public static void testProcessDocuments(String directoryPath, int n) {
		DocumentsProcessor test = new DocumentsProcessor();
						
		Map<String, List<String>> testProcessDocuments = test.processDocuments(directoryPath, n);
		
		for (String document : testProcessDocuments.keySet()) {
			System.out.println(document);
						
			for (String s : testProcessDocuments.get(document)) {
				System.out.println(s);
			}						
		}
				
		List<Tuple<String, Integer>> fileIndex = test.storeNWordSequences(testProcessDocuments, "./src/N_Word_Sequences_File.txt");
				
		for (Tuple<String, Integer> file : fileIndex) {
			System.out.println(file.getRight());
		}
						
		TreeSet<Similarities> similarities = test.computeSimilarities("./src/N_Word_Sequences_File.txt", fileIndex);
		
		test.printSimilarities(similarities, 1);		
	}
	
	public static void testProcessDocumentsRefactored(String directoryPath, int n) {
		DocumentsProcessor test = new DocumentsProcessor();
		
		List<Tuple<String, Integer>> fileIndex = test.processAndStore(directoryPath, "./src/N_Word_Sequences_File.txt", n);
		
		for (Tuple<String, Integer> t : fileIndex) {
			System.out.println(t.getLeft() + ": " + t.getRight());
		}
		
		TreeSet<Similarities> similarities = test.computeSimilarities("./src/N_Word_Sequences_File.txt", fileIndex);
		
		test.printSimilarities(similarities, 0);

	}
	
	
	
}
