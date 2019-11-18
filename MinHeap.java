import java.util.*;


public class MinHeap {
//    public int[] Heap;
    public Node[] Heap;
    public int size;
    public int maxsize;
    public int z=0;
    private static final int FRONT = 1;
  
    public MinHeap(int maxsize, String term)
    {
        this.maxsize = maxsize;
        this.size = 0;
        Heap = new Node[this.maxsize + 1];
        Heap[0] = new Node(Integer.MIN_VALUE,term);
       
    }
    public MinHeap(int maxsize, double score)
       {
           this.maxsize = maxsize;
           this.size = 0;
           Heap = new Node[this.maxsize + 1];
           Heap[0] = new Node(Integer.MIN_VALUE,score);
          
       }
  
  private int parent(int pos)
    {
        return pos / 2;
    }
  private int leftChild(int pos)
    {
        if (2 * pos <= size)
            return 2 * pos;
        else
            return -1;
        
    }
    
  private int rightChild(int pos)
    {
        if (2 * pos + 1 <= size)
            return 2 * pos + 1;
        else
            return -1;
    }
    
    public void removeZscore()
    {
        for(int i=0; i<size; i++)
        {
            if(Heap[i].score <= 0.0)
            {  Heap[i].score=Integer.MAX_VALUE;
                z++;
            }
        }
        
    }
    
    
    public void minHeap()
       {
           for (int pos = (size/2); pos >= 1; pos--)
           {   // System.out.println(" Pos :"+pos);
               minHeapifyTerms(pos);
           }
       }
     
       // Function to heapify the node at pos
          private void minHeapifyTerms(int pos)
          {
            
              // If the node is a non-leaf node and greater
              // than any of its child
              int l=leftChild(pos);
              int r=rightChild(pos);
              int smallest=pos;
              
              if ( l >0 && l <=size && Heap[l].docId < Heap[pos].docId)
                smallest = l ;
            else
                smallest = pos;
              
              if ( r >0 && r <= size && Heap[r].docId < Heap[smallest].docId)
                  smallest = r ;
            
              if (smallest != pos)
              {
                  swap(pos, smallest);
                  minHeapifyTerms(smallest);
              }
            
                    
          }
      
    public void minHeapResult()
      {
          for (int pos = (size/2); pos >= 1; pos--)
          {   // System.out.println(" Pos :"+pos);
              minHeapifyResults(pos);
          }
      }
    
      // Function to heapify the node at pos
         private void minHeapifyResults(int pos)
         {
            
             // If the node is a non-leaf node and greater
             // than any of its child
             int l=leftChild(pos);
             int r=rightChild(pos);
             int smallest=pos;
             
             if ( l >0 && l <=size && Heap[l].score < Heap[pos].score)
               smallest = l ;
           else
               smallest = pos;
             
             if ( r >0 && r <= size && Heap[r].score < Heap[smallest].score)
                 smallest = r ;
           
             if (smallest != pos)
             {
                 swap(pos, smallest);
                 minHeapifyTerms(smallest);
             }
                   
         }
     
  
    // Function that returns true if the passed
    // node is a leaf node
    private boolean isLeaf(int pos)
    {
        if (pos > (size / 2) && pos <= size) {
           // System.out.println(Heap[pos].term+" Is Leaf");
            return true;
        }
        return false;
    }
        
    public void insertTerms(int docId,String term)
      {
        if (size >= maxsize)
             return;
        
        Heap[++size] = new Node(docId,term);
        int current = size;
  
          while (Heap[current].docId < Heap[parent(current)].docId)
          {
             swap(current, parent(current));
             current = parent(current);
          }
      }
    
    public void insertResult(int docId, double score)
        {
          if (size >= maxsize)
               return;
          
          Heap[++size] = new Node(docId,score);
          int current = size;
    
            while (Heap[current].score < Heap[parent(current)].score)
            {
               swap(current, parent(current));
               current = parent(current);
            }
        }
    
  // Function to swap two nodes of the heap
    private void swap(int fpos, int spos)
    { //System.out.println("swapping  "+fpos+ " "+spos);
        Node tmp;
        tmp = Heap[fpos];
        Heap[fpos] = Heap[spos];
        Heap[spos] = tmp;
        
    
    }
  
    // Function to print the contents of the heap
    public void print()
    {  if(Heap.length > 3)
    { int i=0;
               for ( i = 1; i < size / 2; i++)
                 {
                    System.out.print(" PARENT : " + Heap[i].docId
                                     + " LEFT CHILD : " + Heap[2 * i].docId
                                     + " RIGHT CHILD :" + Heap[2 * i + 1].docId);
                }
        if(2*i + 1 <= size)
           System.out.print(" PARENT : " + Heap[i].docId
                           + " LEFT CHILD : " + Heap[2 * i].docId
                           + " RIGHT CHILD :" + Heap[2 * i + 1].docId);
        else
            System.out.print(" PARENT : " + Heap[i].docId
            + " LEFT CHILD : " + Heap[2 * i].docId
            + " RIGHT CHILD : 0 " );

           }
        else if(Heap.length ==3)
             {
                System.out.print(" PARENT : " + Heap[1] + " LEFT CHILD : " + Heap[2] + " RIGHT CHILD : 0");
             }
        else
            System.out.println(" PARENT : " + Heap[1]);
         
        System.out.println("\n");
            
    }
        
    
  
    // Function to build the min heap using
    // the minHeapify
    
    // Driver code
    public static void main(String[] args)
    {
        System.out.println("The Min Heap is of Terms ");
        MinHeap minHeap = new MinHeap(5,"terms");
        minHeap.insertTerms(5,"s5");
        minHeap.insertTerms(3,"s3");
        minHeap.insertTerms(4,"s4");
        minHeap.insertTerms(1,"s1");
        minHeap.insertTerms(2,"s2");
       
        minHeap.print();
        for(Node node : minHeap.Heap)
            System.out.println(node.docId + " "+ node.term);
  

         minHeap.Heap[1].term="99";
         minHeap.Heap[1].docId=99;
         
        minHeap.minHeap();
         minHeap.print();
        
        System.out.println("\n");
        System.out.println("The Min Heap is of Result");
        MinHeap minHeap1 = new MinHeap(6,"results");
        minHeap1.insertResult(5,5.89);
        minHeap1.insertResult(3,3.9);
        minHeap1.insertResult(6,6.2);
        minHeap1.insertResult(4,4.2);
         minHeap1.insertResult(2,2.2);
         minHeap1.insertResult(1,1.2);


        for(Node node : minHeap1.Heap)
            System.out.println(node.docId + " "+ node.score);

        minHeap1.Heap[1].score=99.99;
        minHeap1.Heap[1].docId=99;
        
       minHeap1.minHeapResult();
        
           System.out.println("\n");
       
        for(Node node : minHeap1.Heap)
        System.out.println(node.docId + " "+ node.score);
        
        
        minHeap1.print();
    }
    
    public int getIndexByTerm(String term) {
    	for(int i=1; i<size; i++)
    		if(Heap[i].term.equals(term))
    			return i;
    	
    	return -1;
    }
    
    public void delete(int position) {
    	Heap[position].docId = Integer.MAX_VALUE;
    	swap(position,size);
    	size--;
    	minHeap();
    }
}
