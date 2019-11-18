import java.util.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Bm25MaxScore extends InvertedIndexer {
	
	static final double K = 1.2;
	static final double B = 0.75;

	public static void main(String args[]) throws Exception {
		final long startTime = System.currentTimeMillis();
		corpusParser(args[0]);
		freq_term_doc();
		
		String[] query = (args[2].split("\\s+"));

		for (int i = 0; i < query.length; i++) {
			query[i] = query[i].toLowerCase();
		}
		
		String queryId = "Query1";
		String runId = "RUN_1";
		int rank=1;
		//Sample record in trec_top file : Q2 0 DOC_WIKI_SAN_JOSE_02 4 0.028 RUN_1
		HashMap<Integer, Double> result = rankBM25DocumentAtATimeWithHeaps(query, Integer.parseInt(args[1]));
		for(Map.Entry<Integer, Double> entry : result.entrySet()) {
			System.out.println(queryId+ " 0 D_" +entry.getKey()+ " " +(rank++)+ " " +entry.getValue()+ " " + runId);
		}
		final long endTime = System.currentTimeMillis();
		System.out.println("Total execution time: " + (endTime - startTime));
	}

	/**
	 * Function to rank the documents based on MaxScore heuristic applied on BM25 scoring algorithm 
	 * @param queryTerms: String array which contains all the terms in the query
	 * @param k : retrieve top k results 
	 * @return List of top k documents and their scores ranked in descending order
	 */
	public static HashMap<Integer, Double> rankBM25DocumentAtATimeWithHeaps(String[] queryTerms, int k) {
		HashMap<String, Double> maxScoreMap = new HashMap<>();
		ArrayList<Node> maxScoreList = new ArrayList<>();
		ArrayList<String> deletedTerms = new ArrayList<>();
		
		int maxScoreIndex = 0;
		double currentMaxScore = 0;
		int d = 0;
		double score = 0;
		boolean flag = false;
		
		//calculating maxScore of each term as (k+1).log(N/Nt)
		//if the term is not present in the corpus, maxScore=0
		for (String term : queryTerms) {
			int Nt = numofDocsTermOccursIn(term);
			if (Nt > 0) {
				maxScoreMap.put(term, (K+1) * Math.log((float) docList.size() / dictionary.get(term).size()) / Math.log(2));
			} else {
				maxScoreMap.put(term, 0.0);
			}
		}
		
		//sort maxScore in ascending order
		maxScoreMap = maxScoreMap.entrySet().stream().sorted(Map.Entry.comparingByValue()).collect(Collectors
		.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
		
		for (Map.Entry<String, Double> entry : maxScoreMap.entrySet()) {
			maxScoreList.add(new Node(entry.getValue(), entry.getKey()));
		}
		
		currentMaxScore = maxScoreList.get(0).score;
		
		//creating a min-heap for the top k search results
		//the minheap will contain the bm25 score calculated for each document
		MinHeap resultHeap = new MinHeap(k, "results");
		for (int i = 0; i < k; i++) { 
			resultHeap.insertResult(0, 0.0);
		}

		//creating a min-heap for the n query terms
		MinHeap termHeap = new MinHeap(queryTerms.length, "terms");
		for (int i = 0; i < queryTerms.length; i++) {
			int nextDocid = nextDoc(queryTerms[i], Integer.MIN_VALUE);
			termHeap.insertTerms(nextDocid, queryTerms[i]);
		}
		
		//sort the terms heap in increasing order of nextDoc value
		termHeap.minHeap();

		while (termHeap.Heap[1].docId < Integer.MAX_VALUE) {
			
			//if the top element of the result heap > lowest value in the max score array
			//then delete term from the results heap
			if(resultHeap.Heap[1].score > currentMaxScore) {
				String deleteTerm = maxScoreList.get(maxScoreIndex).term;
				deletedTerms.add(deleteTerm);
				termHeap.delete(termHeap.getIndexByTerm(deleteTerm));
				if(termHeap.Heap[1].docId == Integer.MAX_VALUE)
					break;
				maxScoreIndex ++;
				currentMaxScore += maxScoreList.get(maxScoreIndex).score;
			}
			
			d = termHeap.Heap[1].docId;
			flag = false;
			score = 0;
			
			//calculate score for terms based on the number of occurrences in the current document
			while (termHeap.Heap[1].docId == d) {
				flag = true;
					String term = termHeap.Heap[1].term;
					score += calculateBM25Score(term, d);
					termHeap.Heap[1].docId = nextDoc(term, d);
					// restore heap property for terms
					termHeap.minHeap(); 
			}
			
			//adding score for deleted terms
			if(flag) {
				for(String term : deletedTerms) {
					if(nextDoc(term,d-1) == d) {
						score += calculateBM25Score(term, d);
					}
				}
			}
			
			//store the document score in the result heap
			if (score > resultHeap.Heap[1].score) {
				resultHeap.Heap[1].docId = d;
				resultHeap.Heap[1].score = score;
				// restore heap property for results
				resultHeap.minHeapResult(); 
			}
		}

		//remove scores <=0 from result heap
		HashMap<Integer, Double> resultfinal = new HashMap<>();
		for (Node node : resultHeap.Heap) {
			if (node.score > 0.0) {
				resultfinal.put(node.docId, node.score);
			}
		}

		//sort the document results based on the bm25 score
		 resultfinal = resultfinal.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).collect(Collectors
				.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));

		return resultfinal;

	}

	/**
	 * Function to calculate the TF-IDF score according to BM25 
	 * @param term for which the TF-IDF score is to be calculated
	 * @param docId is the document in which the term occurs
	 * 
	 */
	public static double calculateBM25Score(String term, int docId) {
		double tf = 0, idf = 0;

		//calculate TF
		if (term_freq.get(term) == null || term_freq.get(term).get(docId) == null)
			return 0.0;
		int frequency = term_freq.get(term).get(docId);
		if (frequency <= 0)
			return 0.0;
		tf = frequency * (K + 1) / (frequency + K * (1 - B + B * docList.get(docId) / avgDocLength));

		//calculate IDF
		int Nt = numofDocsTermOccursIn(term);
		if (Nt == 0)
			return 0.0;
		idf = Math.log((float) docList.size() / dictionary.get(term).size()) / Math.log(2);

		return tf * idf;
	}
	
	/**
	 * 
	 * Function to find the number of documents in which the term occurs
	 * 
	 */
	public static int numofDocsTermOccursIn(String term) {
		if (dictionary.get(term) != null)
			return dictionary.get(term).size();
		else
			return 0;
	}
	
}
