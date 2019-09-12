
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PropensityComputer {
	
	public PropensityComputer(){
		
	}

	public void computePropensity(Map classSampleMap, String propFile, double A, double B, double C){
		
		try{
			
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(propFile, true)));
			String instance;
		
			Set classSet = classSampleMap.keySet();
			
			Iterator iter = classSet.iterator();
			List countList = new ArrayList();
			while(iter.hasNext()){
				String label = (String)iter.next();
				Integer count = (Integer)classSampleMap.get(label);
				double invProp = 1 + C * Math.exp(-A* (Math.log(B + count)));
				out.println( label + " " +   new DecimalFormat("#.####").format(invProp));
				
			}
			double missingProp = 1 + C * Math.exp(-A* (Math.log(B + 0)));
			System.out.println("for missing label, prop = " + missingProp);
			
			if (out!=null){
				out.close();
			}	
		}
		catch (IOException ex) {

	           System.out.println(ex);
	        }
		
		
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
	//	Wikipedia-LSHTC/500K: A=0.5,  B=0.4 
	//	Amazon-670K/3M:       A=0.6,  B=2.6 
	//	Other:                A=0.55, B=1.5
		
		PropensityComputer pc = new PropensityComputer();
		
		double A = 0.55 ; double B = 1.5; 
		String inputFile = args[0];
		String propFile =  args[1];

		long totalInstances = 15511; //eurlex
		
		SamplesPerClassCounter spcc = new SamplesPerClassCounter();
		Map classSampleMap = spcc.countSamplesForClasses(inputFile);
		
		
		double C = (Math.log(totalInstances)-1) * Math.pow((B+1), A); 
		System.out.println("C = " + C);
		pc.computePropensity(classSampleMap, propFile, A, B, C);
		
	}

}
