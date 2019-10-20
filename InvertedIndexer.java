import java.io.*;
import java.util.*;

public class InvertedIndexer {
	
	//main dictionary. will store term(as key) and a value which will is a map whose 
	//key:document ID and value:list of postings of the term in that documentID 
	static TreeMap<String, TreeMap<Integer, ArrayList<Integer>>> dictionary = new TreeMap<>();
	//list containing all document IDs
	static ArrayList<Integer> docList = new ArrayList<>();
	
	public static void corpusParser(String fileName) throws Exception {
		File file = new File(fileName);
		BufferedReader br = new BufferedReader(new FileReader(file));
		
		//variable to store docID, which will later be used as total number of docs in VSM
		int docID=1;
		
		//position of term in current document
		int termPos=1;
			
		//read the file line by line
		String st;
		docList.add(1);
		while((st = br.readLine()) != null) {
			//if line is just '\n' means we have encountered '\n\n' 
			//so increment docID and reset term position
			if(st.isEmpty()) {
				docID++;
				docList.add(docID);
				termPos=1;
				
			}
			else {
				for(String terms : st.split("\\s+")) {
					//list of positions in current doc for current term
					ArrayList<Integer> posting = new ArrayList<>();
					//map of current docID to posting positions for current term
					TreeMap<Integer , ArrayList<Integer>> postingList = new TreeMap<>();
					
					//can also use terms = terms.replaceAll("\\p{Punct}", "").toLowerCase(); incase we want to get rid of punctuations as well
					//but that wasn't asked in the question
					terms = terms.replaceAll("\\p{Punct}", "").toLowerCase();
					
					//if term is not in dictionary
					if(!dictionary.containsKey(terms)) {
						posting.add(termPos);
						postingList.put(docID, posting);
						dictionary.put(terms, postingList);
					}
					
					//if term is already in dictionary
					else {
						postingList = dictionary.get(terms);
						//if the term comes first time in current document
						if(postingList.containsKey(docID)) {
							posting = postingList.get(docID);
							posting.add(termPos);
							dictionary.put(terms, postingList);
						}
						//if the term is already encountered in current document
						else {
							posting.add(termPos);
							postingList.put(docID, posting);
							dictionary.put(terms, postingList);
						}
					}
					termPos++;
				}
			}	
		}
		br.close();
	}
}
