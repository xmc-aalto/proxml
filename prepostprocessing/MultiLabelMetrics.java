import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class MultiLabelMetrics {
	
	public MultiLabelMetrics(){
		
	}

	public void computeNDCGAtK(String GSFile, String predictionFile, int k){
		
		FileReader fr1 = null, fr2 = null;
		int total = 0;
		double globalScore =0.0;
		
		try{
			fr1 = new FileReader(GSFile); 
			fr2 = new FileReader(predictionFile);
			BufferedReader br1 = new BufferedReader(fr1);
			BufferedReader br2 = new BufferedReader(fr2);
			String trueLabelsString, predictionLabels;
			
			while(((trueLabelsString = br1.readLine()) != null) && ((predictionLabels = br2.readLine()) != null) ) {
				double localScore = 0.0;
				String [] trueLabels = trueLabelsString.split(",");
				int numtrueLabels = trueLabels.length;
				
				int maxIndex = Math.min(k, numtrueLabels);
				
				String [] predictedLabels = predictionLabels.split(" ");
				
				for(int j=0; j< predictedLabels.length; j++){
					String predictedLabel = predictedLabels[j].trim();
					for(int i=0; i < trueLabels.length; i++){
						String trueLabel = trueLabels[i].trim();
						if(predictedLabel.equalsIgnoreCase(trueLabel)){
							localScore = localScore + (Math.log(2))/(Math.log(1+j+1));
							break;
						}
					}
				}
				double deno = 0.0;
				for(int i=0; i < maxIndex; i++){
					deno = deno + (Math.log(2))/(Math.log(i+1+1));
				}
				localScore  = localScore/deno;
				globalScore = globalScore + localScore;
				total++;
			}
			System.out.println(" ndcg at " + k + " is " + (globalScore*100.0)/(total*1.0));
		}
		catch (IOException ex) {

	           System.out.println(ex);
	        }
		finally{
			try{
				if (fr1!=null){
					fr1.close();
				}
				if(fr2!=null)
				{
					fr2.close();
				}
			}catch(IOException ex){
				System.out.println(ex);
			}
		}
		
	}

	public void computePrecAtK(String GSFile, String predictionFile, int k){
		
		FileReader fr1 = null, fr2 = null;
		int correctCount = 0, total = 0;
		
		try{
			fr1 = new FileReader(GSFile); 
			fr2 = new FileReader(predictionFile);
			BufferedReader br1 = new BufferedReader(fr1);
			BufferedReader br2 = new BufferedReader(fr2);
			String trueLabelsString, predictionLabels;
			
			while(((trueLabelsString = br1.readLine()) != null) && ((predictionLabels = br2.readLine()) != null) ) {
				String [] trueLabels = trueLabelsString.split(",");
				String [] predictedLabels = predictionLabels.split(" ");
				
				for(int j=0; j< predictedLabels.length; j++){
					String predictedLabel = predictedLabels[j].trim();
					for(int i=0; i < trueLabels.length; i++){
						String trueLabel = trueLabels[i].trim();
						if(predictedLabel.equalsIgnoreCase(trueLabel)){
							correctCount++;
							break;
						}
					}
				}
				
				total++;
			}
			System.out.println(" precision at " + k +" is " + (correctCount*100.0)/(total*k*1.0));
		}
		catch (IOException ex) {

	           System.out.println(ex);
	        }
		finally{
			try{
				if (fr1!=null){
					fr1.close();
				}
				if(fr2!=null)
				{
					fr2.close();
				}
			}catch(IOException ex){
				System.out.println(ex);
			}
		}
		
	}

	public void computePropScorednDCGAtK(String GSFile, String predictionFile, int k, Map classWeightMap, Double missingWeight){
		
		FileReader fr1 = null, fr2 = null;
		
		try{
			fr1 = new FileReader(GSFile); 
			fr2 = new FileReader(predictionFile);
			BufferedReader br1 = new BufferedReader(fr1);
			BufferedReader br2 = new BufferedReader(fr2);
			String trueLabelsString, predictionLabels;
			
			double globalPredScore = 0.0; double globalTrueScore = 0.0;
			
			while(((trueLabelsString = br1.readLine()) != null) && ((predictionLabels = br2.readLine()) != null) ) {
				double localPredScore = 0.0; double localTrueScore = 0.0;
				String [] trueLabels = trueLabelsString.split(",");
				int length = trueLabels.length;
				
				Double [] trueLabelWeights = new Double[length];
				for (int i =0; i < length; i++){
					if(classWeightMap.get(trueLabels[i].trim()) != null){
						trueLabelWeights[i] = Double.parseDouble((String) classWeightMap.get(trueLabels[i].trim()) );
					}
					else{
						trueLabelWeights[i] = missingWeight; 
					}
				}
				// sort in ascending order
				Arrays.sort(trueLabelWeights);
				for(int i=0; i < k; i ++){
					// the number of true labels may be less than k
					if( i < length){
						// start from behind to get descending order
				//		System.out.println( trueLabelWeights[length-1-i]/k );
						localTrueScore = localTrueScore + trueLabelWeights[length-1-i]*((Math.log(2))/(Math.log(1+i+1)));
					}
					else{
						continue;
					}
				}
				int numtrueLabels = trueLabels.length;
				
				int maxIndex = Math.min(k, numtrueLabels);
				double deno = 0.0;
				for(int i=0; i < maxIndex; i++){
					deno = deno + (Math.log(2))/(Math.log(i+1+1));
				}
				
				localTrueScore = localTrueScore/deno;
				globalTrueScore = globalTrueScore +  localTrueScore;
				
				String [] predictedLabels = predictionLabels.split(" ");
				
				for(int j=0; j< predictedLabels.length; j++){
					String predictedLabel = predictedLabels[j].trim();
					for(int i=0; i < trueLabels.length; i++){
						String trueLabel = trueLabels[i].trim();
						if(predictedLabel.equalsIgnoreCase(trueLabel)){
							double weight = Double.parseDouble((String) classWeightMap.get(predictedLabel) );
							localPredScore = localPredScore + weight*((Math.log(2))/(Math.log(1+j+1)));
							break;
						}
					}
				}
				localPredScore  = localPredScore/deno;
				globalPredScore = globalPredScore + localPredScore;
			}
			System.out.println(" Propensity weighted nDCG at " + k +" is " + (globalPredScore*100.0)/(globalTrueScore));
		}
		catch (IOException ex) {

	           System.out.println(ex);
	        }
		finally{
			try{
				if (fr1!=null){
					fr1.close();
				}
				if(fr2!=null)
				{
					fr2.close();
				}
			}catch(IOException ex){
				System.out.println(ex);
			}
		}
		
	}
	
	public void computePropScoredPrecAtK(String GSFile, String predictionFile, int k, Map classWeightMap, double missingWeight){
		
		FileReader fr1 = null, fr2 = null;
		double predscore = 0, total = 0;
		double truescore =0;
		
		try{
			fr1 = new FileReader(GSFile); 
			fr2 = new FileReader(predictionFile);
			BufferedReader br1 = new BufferedReader(fr1);
			BufferedReader br2 = new BufferedReader(fr2);
			String trueLabelsString, predictionLabels;
			
			while(((trueLabelsString = br1.readLine()) != null) && ((predictionLabels = br2.readLine()) != null) ) {
				
				String [] trueLabels = trueLabelsString.split(",");
				int trueLength = trueLabels.length;
				
				Double [] trueLabelWeights = new Double[trueLength];
				for (int i =0; i < trueLength; i++){
					if(classWeightMap.get(trueLabels[i].trim()) != null){
						trueLabelWeights[i] = Double.parseDouble((String) classWeightMap.get(trueLabels[i].trim()) );
					}
					else{
						trueLabelWeights[i] = missingWeight; 
					}
				}
				// sort in ascending order
				Arrays.sort(trueLabelWeights);
				for(int i=0; i < k; i ++){
					// the number of true labels may be less than k
					if(i < trueLength){
						// start from behind to get descending order
				//		System.out.println( trueLabelWeights[length-1-i]/k );
						truescore = truescore + trueLabelWeights[trueLength-1-i]/k;
					}
					else{
						continue;
					}
				}
				
				String [] predictedLabels = predictionLabels.split(" ");
				
				for(int j=0; j< predictedLabels.length; j++){
					String predictedLabel = predictedLabels[j].trim();
					for(int i=0; i < trueLabels.length; i++){
						String trueLabel = trueLabels[i].trim();
						if(predictedLabel.equalsIgnoreCase(trueLabel)){
							double weight = Double.parseDouble((String) classWeightMap.get(predictedLabel) );
							predscore = predscore + weight/k;
							break;
						}
					}
				}
				total++;
			}
			System.out.println(" weighted prec at " + k +" is " + (predscore*100.0)/(truescore));
		}
		catch (IOException ex) {

	           System.out.println(ex);
	        }
		finally{
			try{
				if (fr1!=null){
					fr1.close();
				}
				if(fr2!=null)
				{
					fr2.close();
				}
			}catch(IOException ex){
				System.out.println(ex);
			}
		}
		
	}
	
	public void computeRelativeCoverage(String GSFile, String predictionFile, int k, Map classWeightMap, double missingWeight){
		
		FileReader fr1 = null, fr2 = null;
		double predscore = 0, total = 0;
		double truescore =0;
		Set trueLabelSet, predictedLabelSet;
		trueLabelSet = new HashSet();
		predictedLabelSet = new HashSet();
		
		
		
		try{
			fr1 = new FileReader(GSFile); 
			fr2 = new FileReader(predictionFile);
			BufferedReader br1 = new BufferedReader(fr1);
			BufferedReader br2 = new BufferedReader(fr2);
			String trueLabelsString, predictionLabels;
			
			while(((trueLabelsString = br1.readLine()) != null) && ((predictionLabels = br2.readLine()) != null) ) {
				
				List<LabelScoreTuple<String, Double>> tuples = new ArrayList<LabelScoreTuple<String, Double>>();
				
				String [] trueLabels = trueLabelsString.split(",");
				int trueLength = trueLabels.length;
				
				Double [] trueLabelWeights = new Double[trueLength];
				for (int i =0; i < trueLength; i++){
					if(classWeightMap.get(trueLabels[i].trim()) != null){
						trueLabelWeights[i] = Double.parseDouble((String) classWeightMap.get(trueLabels[i].trim()) );
					}
					else{
						//System.out.println("found missing label");
						trueLabelWeights[i] = missingWeight; 
					}
					 tuples.add(new LabelScoreTuple<String, Double>(trueLabels[i].trim(), new Double(trueLabelWeights[i])));
				}
				

			    Comparator<LabelScoreTuple<String, Double>> comparator = new Comparator<LabelScoreTuple<String, Double>>()
			    {

			        public int compare(LabelScoreTuple<String, Double> tupleA,
			        		LabelScoreTuple<String, Double> tupleB)
			        {
			            return tupleA.getScore().compareTo(tupleB.getScore());
			        }

			    };

			    Collections.sort(tuples, comparator);

			    
				for(int i=0; i < k; i ++){
					// the number of true labels may be less than k
					if(i < trueLength){
						// start from behind to get descending order
						trueLabelSet.add(tuples.get(trueLength-1-i).getLabelName());
					}
					else{
						continue;
					}
				}
				
				String [] predictedLabels = predictionLabels.split(" ");
				
				for(int j=0; j< predictedLabels.length; j++){
					String predictedLabel = predictedLabels[j].trim();
					for(int i=0; i < trueLabels.length; i++){
						String trueLabel = trueLabels[i].trim();
						if(predictedLabel.equalsIgnoreCase(trueLabel)){
							predictedLabelSet.add(predictedLabel);
							break;
						}
					}
				}
			}
			System.out.println(" coverage at " + k +" is " + (predictedLabelSet.size()*100.0)/(trueLabelSet.size()));
		}
		catch (IOException ex) {

	           System.out.println(ex);
	        }
		finally{
			try{
				if (fr1!=null){
					fr1.close();
				}
				if(fr2!=null)
				{
					fr2.close();
				}
			}catch(IOException ex){
				System.out.println(ex);
			}
		}
		
	}
	
	public Map giveInverseProp(String inputFile){
		
		Map classWeightMap = new TreeMap();
		
		FileReader fr1=null;
		
		try{
			fr1 = new FileReader(inputFile); 
			BufferedReader br1 = new BufferedReader(fr1);
			String line, label, weight;
			
			while((line = br1.readLine()) != null) {
				label = line.split(" ")[0];
				weight = line.split(" ")[1];
				classWeightMap.put(label, weight);
				
			}
			
		}
		catch (IOException ex) {

	           System.out.println(ex);
	        }
		finally{
			try{
				if (fr1!=null){
					fr1.close();
				}
			}catch(IOException ex){
				System.out.println(ex);
			}
		}
		return classWeightMap;
	}
	
	
	public static void main(String[] args) {

		MultiLabelMetrics mlm = new MultiLabelMetrics(); 
		
		String GSFile =         args[0]; // comma separated gold standard file
		
		String predictionFile1 =    args[1]; // top1 output
		String predictionFile3 =    args[2]; // space separated top3 output
		String predictionFile5 =    args[3]; // space separated top5 output


		String inv_propFile    =   args[4]; // input file containing inverse propensity values
		String outputFileProp1 =   args[5]; // space separated top1 output for propensity metric
		String outputFileProp3 =   args[6]; // space separated top3 output for propensity metric
		String outputFileProp5 =   args[7]; // space separated top5 output for propensity metric

		Map classweightMap = mlm.giveInverseProp(inv_propFile);
		
	      // mediamill = 13.3, smalldelicious = 12.2
              // eurlex = 12.4
             // missing labels wiki10=12.3, wiki325=26.0, amazon670=15.7, amazon13k-no missing, amazon14k=22.4
            // delicious = 15.8, wiki500 = 26.1
		double missingWeight = 12.4;
		
		
		int k;
		
		k=1;mlm.computePrecAtK(GSFile, predictionFile1, k); 	
		k=3;mlm.computePrecAtK(GSFile, predictionFile3, k); 
		k=5;mlm.computePrecAtK(GSFile, predictionFile5, k); 

		System.out.println();

		k=1;mlm.computeNDCGAtK(GSFile, predictionFile1, k); 	
		k=3;mlm.computeNDCGAtK(GSFile, predictionFile3, k); 
		k=5;mlm.computeNDCGAtK(GSFile, predictionFile5, k); 

		System.out.println();

                k=1;mlm.computePropScoredPrecAtK(GSFile, outputFileProp1, k, classweightMap, missingWeight); 	
		k=3;mlm.computePropScoredPrecAtK(GSFile, outputFileProp3, k, classweightMap, missingWeight); 	
		k=5;mlm.computePropScoredPrecAtK(GSFile, outputFileProp5, k, classweightMap, missingWeight); 	

                System.out.println();

                k=1;mlm.computePropScoredPrecAtK(GSFile, outputFileProp1, k, classweightMap, missingWeight); 	
		k=3;mlm.computePropScoredPrecAtK(GSFile, outputFileProp3, k, classweightMap, missingWeight); 
		k=5;mlm.computePropScoredPrecAtK(GSFile, outputFileProp5, k, classweightMap, missingWeight); 


		
	}
}
