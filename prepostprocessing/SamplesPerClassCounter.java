import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;


public class SamplesPerClassCounter {

	public SamplesPerClassCounter(){
		
	}
	
	
	public Map countSamplesForClasses(String inputFile){
		
		Map classCountMap = new TreeMap();
		int numberOfSamples = 0;
		FileReader fr1=null;
		
		try{
			fr1 = new FileReader(inputFile); 
			BufferedReader br1 = new BufferedReader(fr1);
			
			String trainingExample;

			while((trainingExample = br1.readLine()) != null) {
				numberOfSamples++;
				String classLabelsString = trainingExample.split(" ")[0];
				String [] classLabels = classLabelsString.split(",");
				for(int i=0; i < classLabels.length; i++){
					String classLabel = classLabels[i];
					if(!classCountMap.containsKey(classLabel)){
						classCountMap.put(classLabel, 1);	
					}
					else{
						int count = (Integer)classCountMap.get(classLabel);
						count++;
						classCountMap.put(classLabel, count);
					}
				}
				
			}	
			System.out.println("number of labels = " + classCountMap.size());
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
		return classCountMap;
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		SamplesPerClassCounter spcc = new SamplesPerClassCounter();

		String inputFile = args[0];
		
		Map classSampleCountMap = new TreeMap();
		classSampleCountMap = spcc.countSamplesForClasses(inputFile);
	}

}

