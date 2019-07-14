This package consists of the source codes for the following paper

R. Babbar, B. Schölkopf. ProXML - Data Scarcity, Robustness and Extreme multi-label classification, MLJ and ECML, 2019

It is built on top of Liblinear solver and adds a proximal gradient based solver which can be invoked using "-s 8" option. Its overall usage is quite similar to DiSMEC. However, it is a lot slower since it tries to solve the problem exactly compared to the Liblinear co-ordinate descent method ("-s 5" option).

===================
CONTENTS
===================
There are following directories
1) ./proxml contains the ProXML code (along with original DiSMEC code) 

2) ./eurlex consists of data for EURLex-4k downloaded from XMC repository

3) ./prepostprocessing consists of Java code for (a) pre-processing data to get into tf-idf format and remapping labels and features, and (b) Evaluation of propensity scored precision@k and nDCG@k corresponding to the prediction results.


=========Data Pre-processing (in Java)============
0) Download the eurlex dataset from XMC repository, and remove the first line from the train and test files downloaded, call them train.txt and test.txt

1) Change feature ID's so that they start from 1..to..number_of_features, using the code provided in FeatureRemapper.java using the following command
javac FeatureRemapper.java
java FeatureRemapper ../eurlex/train.txt ../eurlex/train-remapped.txt ../eurlex/test.txt ../eurlex/test-remapped.txt

2) Convert to tf-idf format using the code in file TfIdfCalculator.java
javac TfIdfCalculator.java
java TfIdfCalculator ../eurlex/train-remapped.txt ../eurlex/train-remapped-tfidf.txt ../eurlex/test-remapped.txt ../eurlex/test-remapped-tfidf.txt

3) Change labels ID's so that they also start from 1..to..number_of_labels, using the code provided in LabelRelabeler.java 
javac LabelRelabeler.java 
java LabelRelabeler ../eurlex/train-remapped-tfidf.txt ../eurlex/train-remapped-tfidf-relabeled.txt ../eurlex/test-remapped-tfidf.txt ../eurlex/test-remapped-tfidf-relabeled.txt ../eurlex/label-mappings.txt

=================================================

======Building ProXML=============

Just run make command in the ../proxml/ directory. This will build the train and predict executable

=================================

=======Training model with ProXML============

// make the directory to write the model files
mkdir ../eurlex/models 
../proxml/train -s 8 -B 1 -i 1 ../eurlex/train-remapped-tfidf-relabeled.txt ../eurlex/models/1.model
../proxml/train -s 8 -B 1 -i 2 ../eurlex/train-remapped-tfidf-relabeled.txt ../eurlex/models/2.model
../proxml/train -s 8 -B 1 -i 3 ../eurlex/train-remapped-tfidf-relabeled.txt ../eurlex/models/3.model
../proxml/train -s 8 -B 1 -i 4 ../eurlex/train-remapped-tfidf-relabeled.txt ../eurlex/models/4.model

===================

Predicting with ProXML in parallel (in C++)

Since the base Liblinear code does not understand the comma separated labels. We need to zero out labels in the test file, and put that in a separate file (called GS.txt) consisting of only the labels. 
javac LabelExtractor.java
java LabelExtractor ../eurlex/test-remapped-tfidf-relabeled.txt ../eurlex/test-remapped-tfidf-relabeled-zeroed.txt ../eurlex/GS.txt

 
mkdir ../eurlex/output // make the directory to write output files
../proxml/predict ../eurlex/test-remapped-tfidf-relabeled-zeroed.txt ../eurlex/models/1.model ../eurlex/output/1.out
../proxml/predict ../eurlex/test-remapped-tfidf-relabeled-zeroed.txt ../eurlex/models/2.model ../eurlex/output/2.out
../proxml/predict ../eurlex/test-remapped-tfidf-relabeled-zeroed.txt ../eurlex/models/3.model ../eurlex/output/3.out
../proxml/predict ../eurlex/test-remapped-tfidf-relabeled-zeroed.txt ../eurlex/models/4.model ../eurlex/output/4.out

===================

Performance evaluation (in Java)

Computation of Precision@k and nDCG@k for k=1,3,5
Now, we need to get final top-1, top-3 and top-5 from the output of individual models. This is done by the following :

****** IMPORTANT : Change the number of test points in DistributedPredictor.java (at line number 138) based on number of test points in the datasets ******

mkdir ../eurlex/final-output
javac DistributedPredictor.java
java DistributedPredictor ../eurlex/output/ ../eurlex/final-output/top1.out ../eurlex/final-output/top3.out ../eurlex/final-output/top5.out ../eurlex/final-output/top1-prop.out ../eurlex/final-output/top3-prop.out ../eurlex/final-output/top5-prop.out

javac MultiLabelMetrics.java
java MultiLabelMetrics ../eurlex/GS.txt ../eurlex/final-output/top1.out ../eurlex/final-output/top3.out ../eurlex/final-output/top5.out ../eurlex/inv_prop.txt ../eurlex/final-output/top1-prop.out ../eurlex/final-output/top3-prop.out ../eurlex/final-output/top5-prop.out

The expected output should be something like this:

 precision at 1 is 83.43413094925059
 precision at 3 is 70.95275659567008
 precision at 5 is 58.96397580857218

 ndcg at 1 is 83.43413094925059
 ndcg at 3 is 74.22166365735946
 ndcg at 5 is 68.17271200822218

 weighted prec at 1 is 45.04358010111173
 weighted prec at 3 is 48.347476078938115
 weighted prec at 5 is 50.80574116439192

 weighted prec at 1 is 45.04358010111173
 weighted prec at 3 is 48.347476078938115
 weighted prec at 5 is 50.80574116439192

===================

Other Datasets from XMC repository:
If you would like to build for another dataset, please change the number of labels and replace with appropriate number at line number 2628, and then run make

long long nr_class = 3786; //3786 for eurlex

If you would like to train faster on EURLex dataset and have more cores, change the batch size to 100 (1000 by default) by chaning the line number 2754, and then run make

======================
References
[1] R.-E. Fan, K.-W. Chang, C.-J. Hsieh, X.-R. Wang, and C.-J. Lin. LIBLINEAR: A library for large linear classification Journal of Machine Learning Research 9(2008), 1871-1874.
[2] R. Babbar, B. Schölkopf. DiSMEC: Distributed Sparse Machines for Extreme Multi-label Classification, WSDM 2017
[3] R. Babbar, B. Schölkopf. Data scarcity, robustness and extreme multi-label classification, MLJ and ECML 2019
