public class Node{
    int docId;
    String term;
    double score;
    
    Node(int docId,String term){
        this.term=term;
        this.docId=docId;
    }
    
    Node(int docId,double score){
        this.score=score;
        this.docId=docId;
    }
    
    Node(double score, String term){
    	this.score = score;
    	this.term = term;
    }
}
