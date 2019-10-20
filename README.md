# InformationRetrieval-
CS267 Fall 2019 :Topics in Database Systems | Information Retrieval by Prof. Chris Pollet.


The goal of this project is to build a Inverted Index capable of performing boolean queries.
The code ranks result according to the vector space model. 


An example of running the program might be:

python PositiveRank.py my_corpus.txt 5 "_OR _AND good dog _AND bad cat"

To compute VSM scores use all the terms from the query. I.e., for the above these would be good, dog, bad, cat.
The output should be a line with DocId Score on it, followed by a sequence of num_result lines with this information for the top num_results many documents.
For example,

DocId Score
7 .65
2 .51
3 .23
11 .0012

