# InformationRetrieval-

## 1. Information retrieval using Vector Space Model

    1. This project builds an Inverted Index capable of performing boolean queries for information retrieval.  
    2. The code ranks result according to the vector space model.   
    3. It computes VSM scores use all the terms from the query. i.e., for the below these would be : good, dog, bad, cat.  


    An example of running the program might be:
    java PositiveRank my_corpus.txt 5 "_OR _AND good dog _AND bad cat"

     The output should is a line with DocId Score on it, followed by a sequence of num_result lines with this information for the top num_results many documents.
     For example,

    DocId Score  
      7   .65   
      2   .51  
      3   .23   
     11  .0012  



## 2. Information retrieval using BM25 Algorithm along with Max Score Heuristic 
     This program ranks the results using Bm25 Algorithm and Max Score Huristic.
     
      An example of running the program might be:
      java Bm25MaxScore my_corpus.txt 5 "good php python cat"
      
      
      Files used: Bm25MaxScore.java , InvertedIndexer.java , MinHeap.java , Node.java

