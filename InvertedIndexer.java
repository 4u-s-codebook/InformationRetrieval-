import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
 
 
public class InvertedIndexer {
    
        //main dictionary. will store term(as key) and a value which will is a map whose
        //key:document ID and value:list of postings of the term in that documentID
        static TreeMap<String, TreeMap<Integer, ArrayList<Integer>>> dictionary = new TreeMap<>();
        //list containing all document IDs
        static HashMap<Integer,Integer> docList = new HashMap<>();
        //average length of a document in the corpus
        static double avgDocLength = 0;
    
     static HashMap<String, HashMap<Integer, Integer>> term_freq = new HashMap<>();
        
    //cache variable for implementing nextDoc and prevDoc using galloping search
    static HashMap<String,Integer> nextCache = new HashMap<>();
    static HashMap<String,Integer> prevCache = new HashMap<>();
    
        public static void main(String args[]) throws Exception {
                corpusParser(args[0]);
                System.out.println(dictionary);
                
                System.out.println("DOCLIST");
                System.out.println(docList);
                
                System.out.println("AVG DOC LENGTH");
                System.out.println(avgDocLength);
 
                freq_term_doc();
                System.out.println("\n \n Printing Term Frequency");
                System.out.println(term_freq);
            
            
             System.out.println("should->"+term_freq.get("should"));
        }
        
        public static void corpusParser(String fileName) throws Exception {
            File file = new File(fileName);
            BufferedReader br = new BufferedReader(new FileReader(file));
            
            //variable to store docID, which will later be used as total number of docs in VSM
            int docID=1;
            
            //position of term in current document
            int termPos=1;
            
            //read the file line by line
            String st;
            docList.put(1,0);
            while((st = br.readLine()) != null) {
                //if line is just '\n' means we have encountered '\n\n'
                //so increment docID and reset term position
                if(st.isEmpty()) {
                    docList.put(docID,termPos-1);
                    docID++;
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
            docList.put(docID,termPos-1);
            double totalDocLength = 0;
            for(int docLength : docList.values()){
                totalDocLength += docLength;
            }
            avgDocLength = totalDocLength/docList.size();
        }
        
       
        
        public static void freq_term_doc() {
                for(Map.Entry<String, TreeMap<Integer,ArrayList<Integer>>> entry : dictionary.entrySet()) {
                        TreeMap<Integer, ArrayList<Integer>> dict = new TreeMap<>(entry.getValue());
                        HashMap<Integer, Integer> tm = new HashMap<>();
                        for(Map.Entry<Integer, ArrayList<Integer>> dictEntry : dict.entrySet()) {
                                
                                tm.put(dictEntry.getKey(), dictEntry.getValue().size());  //doc number, freq
 
 
                        }
        term_freq.put(entry.getKey(), tm);
       
 
                }
        }
    
    
    /**  FIRSTDOC:
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

    /**  LASTDOC
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
    
    /**  GALLOPPING SEARCH: PREVDOC
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
    

    /**  GALLOPPING SEARCH: NEXTDOC
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
}
