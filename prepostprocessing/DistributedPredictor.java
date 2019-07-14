import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class DistributedPredictor {

	public DistributedPredictor(){

	}

	public static int[] maxKIndex(double[] array, int top_k) {
		double[] max = new double[top_k];
		int[] maxIndex = new int[top_k];
		Arrays.fill(max, Double.NEGATIVE_INFINITY);
		Arrays.fill(maxIndex, -1);

		top: for(int i = 0; i < array.length; i++) {
			for(int j = 0; j < top_k; j++) {
				if(array[i] > max[j]) {
					for(int x = top_k - 1; x > j; x--) {
						maxIndex[x] = maxIndex[x-1]; max[x] = max[x-1];
					}
					maxIndex[j] = i; max[j] = array[i];
					continue top;
				}
			}
		}
		return maxIndex;
	}
	
	static int[] removeAt(int k, int[] arr) {
		final int L = arr.length;
		int[] ret = new int[L - 1];
		System.arraycopy(arr, 0, ret, 0, k);
		System.arraycopy(arr, k + 1, ret, k, L - k - 1);
		return ret;
	}

	// this function will read top-5 scores from individual output files stored in the inputDirectory
	// and generate the final top 5 labels in the outputFile
	public void readFiles(String inputDirectory, int top_k, String outputFile, int testSamples){

		int five = 5;

		String [] fileNames = new File(inputDirectory).list();
		String [] partialOutputFiles = new String[fileNames.length];
		FileReader [] fileReaders = new FileReader [fileNames.length];
		BufferedReader [] br = new BufferedReader [fileNames.length];

		String [] outputStrings = new String [testSamples];

		PrintWriter output = null;
		String [] topLKabels = new String[top_k];
		try{

			for(int fileIndex = 0 ; fileIndex < fileNames.length; fileIndex++){

				partialOutputFiles[fileIndex] = new String(inputDirectory+"/"+fileNames[fileIndex]);
				fileReaders[fileIndex] = new FileReader(partialOutputFiles[fileIndex]);
				br[fileIndex] = new BufferedReader(fileReaders[fileIndex]);
			}

			output = new PrintWriter(new BufferedWriter(new FileWriter(outputFile, true)));

			String [] scores = new String [fileNames.length];

			String [] classLabels = new String[five*fileNames.length];
			double [] scoreDouble = new double[five*fileNames.length];

			int testIndex =0;
			while((scores[0] = br[0].readLine()) != null) {

				String [] firstScore = scores[0].split(" ");
				for(int j=0; j < five; j++){
					scoreDouble[j] = Double.parseDouble(firstScore[j].split(":")[1]);
					classLabels[j] = firstScore[j].split(":")[0];
				}

				for(int i=1; i < fileNames.length;i++){

					//				System.out.println("file number is " + i);

					scores[i] = br[i].readLine();

					String [] localScores = scores[i].split(" ");  
					for(int j=0; j < five; j++){
						scoreDouble[i*five+j] = Double.parseDouble(localScores[j].split(":")[1]);
						classLabels[i*five+j] = localScores[j].split(":")[0];
					}					
				}

				int [] topKIndices = maxKIndex(scoreDouble, top_k);
				StringBuffer sb = new StringBuffer(); 
				for(int i=0;i<top_k; i++){
					topLKabels[i] = classLabels[topKIndices[i]];
					sb.append(topLKabels[i]);sb.append(" ");
				}
				outputStrings[testIndex] = new String(sb);
				testIndex++;
				if(testIndex%10000 == 0)
					System.out.println(testIndex);
			}	

			for(int i=0;i<testSamples; i++){
				output.println(outputStrings[i]);
			}
		}
		catch (IOException ex) {

			System.out.println(ex);
		}
		finally{
			try{
				for(int fileIndex = 0 ; fileIndex < fileNames.length; fileIndex++){

					if(fileReaders[fileIndex]!=null){
						fileReaders[fileIndex].close();
					}
				}
				if (output!=null){
					output.close();
				}

			}catch(IOException ex){
				System.out.println(ex);
			}
		}
	}

	public Map getInvProp(String inputFile){

		Map classPropMap = new TreeMap();
		Integer classIndex = 0;
		FileReader fr1=null;

		try{
			fr1 = new FileReader(inputFile); 
			BufferedReader br1 = new BufferedReader(fr1);

			String line;

			while((line = br1.readLine()) != null) {
				classIndex++;

				String propensity = line;

				classPropMap.put(classIndex.toString(), propensity);			
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
		return classPropMap;
	}

	// this function will read top-5 scores from individual output files stored in the inputDirectory
    // and generate the final top 5 labels:scores (w^T*x) for the top-5 labels in the outputFile
	public void readFilesForPropensity(String inputDirectory, int top_k, String outputFile, int testSamples, Map invProp){

		int five = 5;

		String [] fileNames = new File(inputDirectory).list();
		String [] partialOutputFiles = new String[fileNames.length];
		FileReader [] fileReaders = new FileReader [fileNames.length];
		BufferedReader [] br = new BufferedReader [fileNames.length];

		String [] outputStrings = new String [testSamples];

		PrintWriter output = null;
		String [] topKLabels = new String[top_k];
		try{

			for(int fileIndex = 0 ; fileIndex < fileNames.length; fileIndex++){

				partialOutputFiles[fileIndex] = new String(inputDirectory+"/"+fileNames[fileIndex]);
				fileReaders[fileIndex] = new FileReader(partialOutputFiles[fileIndex]);
				br[fileIndex] = new BufferedReader(fileReaders[fileIndex]);
			}

			output = new PrintWriter(new BufferedWriter(new FileWriter(outputFile, true)));

			String [] scores = new String [fileNames.length];

			String [] classLabels = new String[five*fileNames.length];
			double [] scoreDouble = new double[five*fileNames.length];

			int testIndex =0;
			while((scores[0] = br[0].readLine()) != null) {

				String [] firstScore = scores[0].split(" ");
				for(int j=0; j < five; j++){
					scoreDouble[j] = Double.parseDouble(firstScore[j].split(":")[1]);
					classLabels[j] = firstScore[j].split(":")[0];
				}

				for(int i=1; i < fileNames.length;i++){

					//				System.out.println("file number is " + i);

					scores[i] = br[i].readLine();

					String [] localScores = scores[i].split(" ");  
					for(int j=0; j < five; j++){
						scoreDouble[i*five+j] = Double.parseDouble(localScores[j].split(":")[1]);
						classLabels[i*five+j] = localScores[j].split(":")[0];
					}					
				}
				// just pick top 20 among which to pick the final top_k
				int top_kk = 5;
				int [] topKIndices = maxKIndex(scoreDouble, top_kk);

				String firstPredLabel = "";
				int bestIndex = 0;
				StringBuffer sb = new StringBuffer(); 
				for(int i=0;i<1; i++){

					topKLabels[i] = classLabels[topKIndices[bestIndex]];
					Double maxProp = Double.parseDouble( (String) invProp.get(topKLabels[i]) );
					for(int j=1 ; j < top_kk; j++){
						Double newProp = Double.parseDouble( (String) invProp.get(classLabels[topKIndices[j]]) );
						if(scoreDouble[topKIndices[j]] > 0 && (newProp > maxProp)){
							bestIndex = j;
							topKLabels[i] = classLabels[topKIndices[bestIndex]];
							maxProp = newProp;
						}
						else{
							continue;
						}
					}				
					firstPredLabel = topKLabels[i];
					sb.append(topKLabels[i]);sb.append(" ");
				}
				
				int [] topKmin2Indices = new int[top_kk-2];
				if(top_k >= 3){
					int [] topKmin1Indices = removeAt(bestIndex, topKIndices);				
					for(int i=0;i<1; i++){
						bestIndex = 0;
						topKLabels[i] = classLabels[topKmin1Indices[bestIndex]];
						Double maxProp = Double.parseDouble( (String) invProp.get(topKLabels[i]) );
						for(int j=1 ; j < top_kk-1; j++){
							Double newProp = Double.parseDouble( (String) invProp.get(classLabels[topKmin1Indices[j]]) );
							if(scoreDouble[topKmin1Indices[j]] > 0 && (newProp > maxProp)){
								bestIndex = j;
								topKLabels[i] = classLabels[topKmin1Indices[bestIndex]];
								maxProp = newProp;
							}
							else{
								continue;
							}
						}				
						sb.append(topKLabels[i]);sb.append(" ");
					}

					topKmin2Indices = removeAt(bestIndex, topKmin1Indices);	
					for(int i=0;i<1; i++){
						bestIndex = 0;
						topKLabels[i] = classLabels[topKmin2Indices[bestIndex]];
						Double maxProp = Double.parseDouble( (String) invProp.get(topKLabels[i]) );
						for(int j=1 ; j < top_kk-2; j++){
							Double newProp = Double.parseDouble( (String) invProp.get(classLabels[topKmin2Indices[j]]) );
							if(scoreDouble[topKmin2Indices[j]] > 0 && (newProp > maxProp)){
								bestIndex = j;
								topKLabels[i] = classLabels[topKmin2Indices[bestIndex]];
								maxProp = newProp;
							}
							else{
								continue;
							}
						}				
						sb.append(topKLabels[i]);sb.append(" ");
					}
				}

				if(top_k == 5){
					int [] topKmin3Indices = removeAt(bestIndex, topKmin2Indices);				
					for(int i=0;i<1; i++){
						bestIndex = 0;
						topKLabels[i] = classLabels[topKmin3Indices[bestIndex]];
						Double maxProp = Double.parseDouble( (String) invProp.get(topKLabels[i]) );
						for(int j=1 ; j < top_kk-3; j++){
							Double newProp = Double.parseDouble( (String) invProp.get(classLabels[topKmin3Indices[j]]) );
							if(scoreDouble[topKmin3Indices[j]] > 0 && (newProp > maxProp)){
								bestIndex = j;
								topKLabels[i] = classLabels[topKmin3Indices[bestIndex]];
								maxProp = newProp;
							}
							else{
								continue;
							}
						}				
						sb.append(topKLabels[i]);sb.append(" ");
					}

					int [] topKmin4Indices = removeAt(bestIndex, topKmin3Indices);	
					for(int i=0;i<1; i++){
						bestIndex = 0;
						topKLabels[i] = classLabels[topKmin4Indices[bestIndex]];
						Double maxProp = Double.parseDouble( (String) invProp.get(topKLabels[i]) );
						for(int j=1 ; j < top_kk-4; j++){
							Double newProp = Double.parseDouble( (String) invProp.get(classLabels[topKmin4Indices[j]]) );
							if(scoreDouble[topKmin4Indices[j]] > 0 && (newProp > maxProp)){
								bestIndex = j;
								topKLabels[i] = classLabels[topKmin4Indices[bestIndex]];
								maxProp = newProp;
							}
							else{
								continue;
							}
						}				
						sb.append(topKLabels[i]);sb.append(" ");
					}
				}

				outputStrings[testIndex] = new String(sb);
				testIndex++;
				if(testIndex%10000 == 0)
					System.out.println(testIndex);
			}	

			for(int i=0;i<testSamples; i++){
				output.println(outputStrings[i]);
			}
		}
		catch (IOException ex) {

			System.out.println(ex);
		}
		finally{
			try{
				for(int fileIndex = 0 ; fileIndex < fileNames.length; fileIndex++){

					if(fileReaders[fileIndex]!=null){
						fileReaders[fileIndex].close();
					}
				}
				if (output!=null){
					output.close();
				}

			}catch(IOException ex){
				System.out.println(ex);
			}
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		DistributedPredictor dp = new DistributedPredictor();

		int testSamples = 3803; //number of test samples

		String inputDirectory = args[0];

		String outputFile1 =     args[1]; // final output file for top1 prediction
		String outputFile3 =     args[2]; // final output file for top3 prediction
		String outputFile5 =     args[3]; // final output file for top5 prediction

		String inv_propFile    =   args[4]; // input file containing inverse propensity values
		Map invProp = dp.getInvProp(inv_propFile);

		String outputFileProp1 =   args[5]; // final output file for top1 weighted prediction
		String outputFileProp3 =   args[6]; // final output file for top3 weighted prediction
		String outputFileProp5 =   args[7]; // final output file for top5 weighted prediction


		int top_k ;

		top_k=1; dp.readFiles(inputDirectory, top_k, outputFile1, testSamples);		
		top_k=3; dp.readFiles(inputDirectory, top_k, outputFile3, testSamples);
		top_k=5; dp.readFiles(inputDirectory, top_k, outputFile5, testSamples); 

		top_k=1; dp.readFilesForPropensity(inputDirectory, top_k, outputFileProp1, testSamples, invProp);
		top_k=3; dp.readFilesForPropensity(inputDirectory, top_k, outputFileProp3, testSamples, invProp);
		top_k=5; dp.readFilesForPropensity(inputDirectory, top_k, outputFileProp5, testSamples, invProp); 

	}

}

