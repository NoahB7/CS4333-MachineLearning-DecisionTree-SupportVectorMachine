
public class PS2Runtime {
	/********************************
	Name: Noah Buchanan
	Username: ua100
	Problem Set: PS2
	Due Date: February 21,2021
	********************************/
	
	public static void main(String[] args) {
		
		try {
			
		System.out.println("Classifying value: \"1,1,1,1,0,1\"");
		
		UADecisionTree dt = new UADecisionTree();
		
		dt.setTreeMaxDepth(6);
		dt.setMinimumImpurity((float) 0.05);
		dt.train(dt.getTrainingMatrix("train.txt", "SURVIVED"));
		System.out.println("input classified as: " + dt.classifyValue("1,1,1,1,0,1") + "\n");
		
			
		
		UADecisionTreeTest dtt = new UADecisionTreeTest();
		
		System.out.println("finding best depth\n----------------------------------------");
		int bestDepth = dtt.getBestDepth("train.txt", "SURVIVED", (float)0.05);
		System.out.println("best depth: " + bestDepth + "\n");
		

		System.out.println("data split randomly 100 times\n------------------------------------");
		dtt.getRandomAccuracy("train.txt", "SURVIVED", (float)0.2, 6);
		
		
		
		
		
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}
