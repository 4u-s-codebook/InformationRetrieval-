import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

//tree Node for Polish expression to infix expression
class Node {
	  String value;
	  Node left, right;
	  Node(String item) {
		  value = item;
		  left = right = null;
	  }
}

public class PositiveRank extends InvertedIndexer{
	
	//cache variable for implementing nextDoc and prevDoc using galloping search 
	static HashMap<String,Integer>  nextCache = new HashMap<>();
	static HashMap<String,Integer> prevCache = new HashMap<>();
	
	//list of boolean operators
	static ArrayList<String> operatorList = new ArrayList<String>(Arrays.asList("_AND","_OR"));
	
	
	public static Node ParsePolishExpr(String[] str) {
		Stack<Node> st = new Stack<>();
		Node t, t1, t2;
	    for (int i = str.length - 1; i >= 0; i--) {
	    	String tokens = str[i];
	    	// if operand push into the stack, 
			if (!operatorList.contains(tokens)) {
				t = new Node(tokens);
			    st.push(t);
			} 
			//if operator, pop 2 items and create a node
			//with these items as left and right and insert back
			else {
				t = new Node(tokens);
			    if (!st.isEmpty()) {
			    	t1 = st.pop(); 
			         t.left=t1;
			         if (!st.isEmpty()) { 
			        	 t2=st.pop(); 
			        	 t.right=t2;
			        }
			    }
			 st.push(t);
			 }
		}
		t = st.peek();
		//pop root node(operator)
		st.pop();
		return t;
	}

	public static int docRight(Node root, int u) {
		int l,r;
		if (root != null) {
			//if at leaves, call nextDoc with "term" and u as argument
			if(root.left==null || root.right==null)	
		    	return nextDoc(root.value,u);
		    else { 
		    	//recursively traverse till leaves(which are terms in query)
		    	l = docRight(root.left,u);
		        r = docRight(root.right,u);
		        if(root.value.equals("_AND")){ 
		        	return  java.lang.Math.max(l,r);
		        }
		        else if(root.value.equals("_OR")){ 
		        	return  java.lang.Math.min(l,r);
		        }
		    }
		 }   
		return -1;  
	}

	public static int docLeft(Node root, int u) {
		int l,r;
		if (root != null) {
			//if at leaves, call nextDoc with "term" and u as argument
			if(root.left==null || root.right==null) 
				return prevDoc(root.value,u);
		    else { 
		    	//recursively traverse till leaves(which are terms in query)
		    	l = docLeft(root.left,u);
		        r = docLeft(root.right,u);
		        if(root.value.equals("_AND")){  
		        	return  java.lang.Math.min(l,r);
		        }
		        else if(root.value.equals("_OR")) { 
		        	return  java.lang.Math.max(l,r);
		        }
		    }
		}   
		return -1;  
	}
		//returns candidate solution cover for given query
		public static int nextSolution(Node query, int position){
			int v,u;
			v=docRight(query,position);
			if(v==Integer.MAX_VALUE)
				return Integer.MAX_VALUE;
			u=docLeft(query, v+1);
			if(u==v)
				return u;
			else
				return nextSolution(query, v);
		}

		/**  
		* Get the document id of the first document that contains the term
		*
		*/
		public static int firstDoc(String term){

		//List of documents in which the term occurs
		ArrayList<Integer> docList = new ArrayList<>(dictionary.get(term).keySet());

		//Term doesn't exist in the dictionary
		if(docList.size() == 0)
		return -1;

		return docList.get(0);
		}

		/**  
		* Get the document id of the last document that contains the term
		*
		*/
		public static int lastDoc(String term){

		//List of documents in which the term occurs
		ArrayList<Integer> docList = new ArrayList<>(dictionary.get(term).keySet());

		//Term doesn't exist in the dictionary
		if(docList.size() == 0)
		return -1;

		return docList.get(docList.size()-1);
		}
		
		/**  
		 * Get the document id of the last document 
		 * before current that contains the term
		 * using galloping search
		 * 
		 */
		public static int prevDoc(String term, int current)
		{
			//List of documents in which the term occurs
			ArrayList<Integer> docList;	

			if(dictionary.containsKey(term)) {
				docList = new ArrayList<>(dictionary.get(term).keySet());
			}else {
				return Integer.MIN_VALUE;
			}

			int listSize = docList.size();
			int cachedId, low=0, high=0, jump;
			
			//check for boundary conditions
			
			// current <= first document id in the list
			// then return -infinity to signify no previous doc exists
			if(listSize == 0 || docList.get(0) >= current){
				return Integer.MIN_VALUE; 
			}
			
			//current > last document id in the list
			//then return the highest(last) document id from the list 
			if(docList.get(listSize - 1) < current) {
				prevCache.put(term,listSize-1);
				return lastDoc(term);
			}
			
			//check if previous cached index for the term exists 
			if(prevCache.containsKey(term)) {
				cachedId = prevCache.get(term);
			}else {
				cachedId = -1;
			}
			
			// previous cached index >= current
			// then set high to start after the cached index else set it to end of list
			if(cachedId>0 && docList.get(cachedId) >= current) {
				high = cachedId+1;
			}else {
				high = listSize-1 ;
			}
			
			//Initialize jump to one at the beginning of galloping search
			jump = 1;
			low = high - jump;
			
			// increase jumps exponentially till
			// either current is lesser than low or the list ends
			while(low >= 0 && docList.get(low)>=current) {
				high = low;
				jump = 2 * jump;
				low = high - jump;
			}
			
			if(low<0) {
				low = 0;
			}
			
			//binarySearch for current on the last 2 jumps 
			cachedId = binarySearch(docList,low,high,current,false);
			
			//cache the previous document id retrieved
			prevCache.put(term, cachedId);
			
			return docList.get(cachedId);
		}
		

		/**  
		 * Get the document id of the first document 
		 * after current that contains the term
		 * using galloping search
		 * 
		 */
		public static int nextDoc(String term, int current) {
			ArrayList<Integer> docList;	
			//List of documents that contain the term
			if(dictionary.containsKey(term)) {
				docList = new ArrayList<>(dictionary.get(term).keySet());
			}else {
				return Integer.MAX_VALUE;
			}
		
			int listSize = docList.size();
			int cachedId, low=0, high=0, jump;
			
			//check border conditions
			
			//if current >= last document id in the list
			//then return +infinity to signify no next doc exists
			if(listSize == 0 || docList.get(listSize-1) <= current){
				return Integer.MAX_VALUE;
			}
			
			//if current < first document id in the list
			//then return the first document id from the list
			if(docList.get(0) > current) {
				nextCache.put(term,0);
				return firstDoc(term);
			}
			
			//check if the next cached index exists
			if(nextCache.containsKey(term)) {
				cachedId = nextCache.get(term);
			}else {
				cachedId = -1;
			}
			
			//next cached index <= current
			//set low to start from the previous index else set it to the start of the list
			if(cachedId>0 && docList.get(cachedId)<=current) {
				low = cachedId-1;
			}else {
				low = 0;
			}
			
			//initialize jump to 1 at the beginning of the galloping search
			jump = 1;
			high = low + jump;

			//search for current with exponential jumps
			//till either current value is greater than high or high exceeds list size 
			while(high<listSize && docList.get(high)<=current) {
				low = high;
				jump = 2 * jump;
				high = low + jump;
			}
			
			if(high>listSize) {
				high = listSize;
			}
			
			//binary search for current in the last 2 jumps
			cachedId = binarySearch(docList,low,high,current,true);
			
			//cache the next_document id retrieved
			nextCache.put(term, cachedId);
			
			return docList.get(cachedId);
		}
		

		/**  
		 * Function to search for an element in a list using binary search
		 * @param isNext Signifies if search is for previous or next element
		 * 
		 */
		public static int binarySearch(ArrayList<Integer> docList, int low, int high, int current, boolean isNext) {
			int mid = 0;
			
			//Loop till high and low don't cross each other
			while(high-low > 1) {
				
				//calculate the mid index of the search window
				mid = (low+high)/2;
				
				//searching for the next element
				if(isNext) {
					if(docList.get(mid) <= current) {
						low = mid;
					}else {
						high = mid;
					}
				}
				//searching for the previous element
				else {
					if(docList.get(mid) < current) {
						low = mid;
					}else {
						high = mid;
					}
				}
			}
			
			//return low value for previous and high value for next
			if(isNext) {
				return high;
			}else {
				return low;
			}
		}
		
		/**  
		 * Function to implement the ranking of the documents according to the vector space model
		 * 
		 */
		public static void rankCosine(ArrayList<String> query, ArrayList<Integer> docList, int displayCount) throws Exception
		{
			int l_count = 1;
			//Generate the query vector
			double[] queryVector = createQueryVector(query);
			
			//Generate the document vectors according to the document subset
			HashMap<Integer, double[]> documentVectors = createDocumentVector(docList);
			
			//Calculate the dot product values between the query vector and the document vectors
			HashMap<Integer,Double> cosineValues = calculateCosine(queryVector, documentVectors);
			
			//Rank in descending order according to cosine values
			HashMap<Integer,Double> sorted = cosineValues
												.entrySet() 
												.stream() 
												.sorted(Collections.reverseOrder(Map.Entry.comparingByValue())) 
												.collect( Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
			
			//Display ranked values according to the user input
			System.out.println("Doc Id \t Score");
			for(Entry<Integer, Double> record : sorted.entrySet()) {
				if (l_count <= displayCount)
					System.out.println(record.getKey()+" \t "+record.getValue());
				else
					break;
				l_count++;
	        }
			
		}
		

		/**  
		 * Function to generate a query vector from the query string using the TF-IDF weights
		 * 
		 */
		public static double[] createQueryVector(ArrayList<String> query)
		{
			int totalDocuments = docList.size();
			int index = 0;
			double tf = 0 , idf = 0, weight = 0, weightSum = 0;
			
			double[] queryVector = new double[dictionary.size()];
			Map<String, Long> termFreq= query.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

			//Iterate over all terms in the dictionary to find if it is in the query and assign weights
			
			for(String term: dictionary.keySet()) {

				if(query.contains(term)) {
					
					//calculate TF
					Long frequency = termFreq.get(term);
					if(frequency != null) {
						tf = Math.log(frequency)/Math.log(2) + 1;
					}
					
					//calculate IDF
					idf = Math.log((float)totalDocuments / dictionary.get(term).size())/Math.log(2);
					//initialize vector with term weights
					weight = tf * idf;
					queryVector[index] = weight;
					weightSum  += weight * weight;
				}
				
				index++;			
			}
			
			//normalize query vector
			if(weightSum==0)
				return queryVector;
			weightSum = Math.sqrt(weightSum);
			index = 0;
			for(double element : queryVector){
				queryVector[index] =  element/weightSum;
				index++;
			}
			return queryVector;
		}
		
		/**  
		 * Function to generate document vectors using the TF-IDF weights from the document subset after boolean retrieval method is applied
		 * 
		 */
		public static HashMap<Integer, double[]> createDocumentVector(ArrayList<Integer> documentList)
		{
			int totalDocuments = docList.size();
			int index = 0 ;
			double tf = 0 , idf = 0, weight = 0, weightSum = 0;
			
			HashMap<Integer, double[]> documentVector = new HashMap<>(); 
			HashMap<Integer, Double> vectorMagnitude = new HashMap<>();

			//Populate the document vector with relevant document ids
			documentList.forEach(documentId -> {
				documentVector.put(documentId, new double[dictionary.size()]);
			});
			
			//Iterate over all terms to assign weights to the document vectors
			
	        for ( TreeMap<Integer, ArrayList<Integer>> postingList : dictionary.values())  {
	        	
	        	//check if document is part of the relevant document list
	        	for(Integer document : documentList){
	        		
					//document contains term
					if(postingList.containsKey(document)) {
						
						//calculate TF
						tf = Math.log(postingList.get(document).size())/Math.log(2) + 1;
						
						//calculate IDF
						idf = Math.log((float)totalDocuments / postingList.size())/Math.log(2);
						
						weight = tf * idf;
						
						documentVector.get(document)[index] = weight;
						
						//calculating the magnitude of the vectors as root(x(t)*x(t))
						if(vectorMagnitude.containsKey(document)) {
							vectorMagnitude.put(document, vectorMagnitude.get(document) + weight*weight);
						}else {
							vectorMagnitude.put(document, weight*weight);
						}
					}
				}
	        	index++;
			}
	        
			//normalize document vector
			for(Entry<Integer, double[]> record : documentVector.entrySet()) {
				index = 0;
				weightSum = vectorMagnitude.get(record.getKey());
				if(weightSum==0)
					return documentVector;
				double[] vector = record.getValue();
				for(double element : vector){
					vector[index] =  element/Math.sqrt(weightSum);
					index++;
				}
				documentVector.put(record.getKey(), vector);
			}
	
			return documentVector;
		}
		

		/**  
		 * Function to calculate the cosine values between the query vector and the document vectors
		 * 
		 */
		public static HashMap<Integer, Double> calculateCosine(double[] queryVector, HashMap<Integer,double[]> documentVectors) throws Exception {
		
			HashMap<Integer,Double> cosineValues = new HashMap<>();
			double value = 0;
			
			//Iterate over all document vectors 
			for(Entry<Integer, double[]> record : documentVectors.entrySet()) {
				double[] documentVector = record.getValue(); 
				
				if(queryVector.length != documentVector.length)
					throw new Exception("Query vector and document vector differ in length");
				
				value=0;
				
				//Calculate cosine score as matrix multiplication between the two vectors
				for( int i=0; i<queryVector.length; i++)
					value += queryVector[i]*documentVector[i];
			
				cosineValues.put(record.getKey(), value);
			}
			
			return cosineValues;
		}
		
		public static void main(String args[]) throws Exception
		{
			corpusParser(args[0]);
			ArrayList<String> queryTerms = new ArrayList<>();
			String[] query = (args[2].split("\\s+"));
			for(int i=0; i<query.length; i++)
				if(!(query[i].equals("_AND")|| query[i].equals("_OR"))) {
					query[i] = query[i].toLowerCase();
					queryTerms.add(query[i]);
					
				}
			
			Node root = ParsePolishExpr(query);
			ArrayList<Integer> candidateSolution = new ArrayList<>(); 
			int u = Integer.MIN_VALUE;
			while(u<Integer.MAX_VALUE) {
				u = nextSolution(root, u);
				if(u<Integer.MAX_VALUE)
					candidateSolution.add(u);
			}
			rankCosine(queryTerms, candidateSolution, Integer.parseInt(args[1]));
			
		}
	}
