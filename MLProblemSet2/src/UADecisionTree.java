import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class UADecisionTree {
	
	/********************************
	Name: Noah Buchanan
	Username: ua100
	Problem Set: PS2
	Due Date: February 21,2021
	********************************/

	public class Node {
		HashMap<String, Node> Children = new HashMap<String, Node>();
		boolean leaf = false;
		String response = "Default";
		int feature = 0;
	}

	private Node Head = new Node();
	private ArrayList<ArrayList<String>> readin = new ArrayList<>();
	private ArrayList<String> columns = new ArrayList<>();
	private String[][] data;
	private int max_depth;
	private float min_impurity;


	// still havent accounted for non-categorical data
	public String[][] getTrainingMatrix(String filename, String target) throws IOException {

		BufferedReader br = new BufferedReader(new FileReader(filename));
		String[] split = br.readLine().split(",");
		int coi = 1;
		boolean ontarget = false;
		for (int i = 0; i < split.length; i++) {
			columns.add(split[i]);
			if (columns.get(i).equals(target))
				coi = i;
		}
		if (columns.get(columns.size() - 1).equals(target))
			ontarget = true;
		String line = "";
		int row = 0;
		while ((line = br.readLine()) != null) {
			readin.add(new ArrayList<>());
			split = line.split(",");
			for (int i = 0; i < split.length; i++) {

				if (!ontarget) {
					if (i == coi) {
						readin.get(row).add(split[split.length - 1]);
					} else if (i == columns.size() - 1) {
						readin.get(row).add(split[coi]);
					} else {
						readin.get(row).add(split[i]);
					}
				} else {
					readin.get(row).add(split[i]);

				}
			}
			row++;
		}
		br.close();
		String[][] hold = new String[readin.size()][readin.get(0).size()];
		for (int i = 0; i < readin.size(); i++) {
			for (int j = 0; j < readin.get(0).size(); j++) {
				hold[i][j] = readin.get(i).get(j);
			}
		}
		data = hold;
		data = removeNonCategorical(data);
		
		return data;
	}
	
	private String[][] removeNonCategorical(String[][] matrix) {
		
		ArrayList<Integer> remove = new ArrayList<Integer>();
		for(int i = 0; i < matrix[0].length; i++) {
			if(distincts(matrix,i).size() > 20) {
				remove.add(i);
			}
		}
		if(remove.size() > 0) {
			ArrayList<ArrayList<String>> hold = new ArrayList<>();
			int count = 0;
			for(int i = 0; i < matrix.length; i++) {
				hold.add(new ArrayList<>());
				for(int j = 0; j < matrix[0].length; j++) {
					if(j==remove.get(count)) {
						count++;
					} else {
						hold.get(i).add(matrix[i][j]);
					}
				}
				count = 0;
			}
			matrix = new String[hold.size()][hold.get(0).size()];
			for(int i = 0; i < hold.size(); i++) {
				for(int j = 0; j < hold.get(0).size(); j++) {
					matrix[i][j] = hold.get(i).get(j);
				}
			}
		}
		
		return matrix;
	}

	public void setTreeMaxDepth(int max) {
		max_depth = max;
	}

	public void setMinimumImpurity(float min) {
		min_impurity = min;
	}

	private float entropy(String[][] matrix) {

		float entropy = 0;
		HashMap<String, Integer> distincts = distincts(matrix, matrix[0].length - 1);
		float[] distinctsCount = new float[distincts.size()];
		for (int i = 0; i < matrix.length; i++) {

			distinctsCount[distincts.get(matrix[i][matrix[0].length - 1])]++;
		}
		for (int i = 0; i < distinctsCount.length; i++) {

			entropy += (float) (distinctsCount[i] / matrix.length)
					* (float) (Math.log10(distinctsCount[i] / matrix.length) / Math.log10(2));
		}

		return -entropy;
	}

	private float condEntropy(String[][] matrix, int col) {

		float condEntropy = 0;
		HashMap<String, Integer> distinctsCol = distincts(matrix, col);
		int[] distinctsColCount = new int[distinctsCol.size()];
		HashMap<String, Integer> distinctsTarget = distincts(matrix, matrix[0].length - 1);
		float[] distinctsTargetCount = new float[distinctsTarget.size()];

		for (int i = 0; i < matrix.length; i++) {
			distinctsColCount[distinctsCol.get(matrix[i][col])]++;
		}

		for (int i = 0; i < distinctsColCount.length; i++) {
			float condProb = 0;
			String x = (String) distinctsCol.keySet().toArray()[i];
			String[][] filtered = filter(matrix, col, x);
			float prob = (float) distinctsColCount[distinctsCol.get(x)] / (float) matrix.length;
			for (int j = 0; j < filtered.length; j++) {

				distinctsTargetCount[distinctsTarget.get(filtered[j][filtered[0].length - 1])]++;
			}
			for (int j = 0; j < distinctsTargetCount.length; j++) {
				condProb += (float) (((float) distinctsTargetCount[j] / (float) filtered.length + 0.00001)
						* (float) ((float) Math
								.log10(((float) distinctsTargetCount[j] / (float) filtered.length) + 0.00001)
								/ (float) Math.log10(2)));
			}
			for (int j = 0; j < distinctsTargetCount.length; j++) {
				distinctsTargetCount[j] = 0;
			}
			condEntropy += prob * condProb;

		}
		return -condEntropy;
	}

	private float IG(String[][] matrix, int col) {
		return entropy(matrix) - condEntropy(matrix, col);
	}

	private HashMap<String, Integer> distincts(String[][] matrix, int col) {

		int count = 0;
		HashMap<String, Integer> distincts = new HashMap<>();
		for (int i = 0; i < matrix.length; i++) {
			if (distincts.get(matrix[i][col]) == null) {
				distincts.put(matrix[i][col], count++);
			}
		}

		return distincts;
	}
	
	public void train(String[][] matrix) {
		training(matrix,0,Head);
	}

	private void training(String[][] matrix, int currentdepth, Node parent) {
		
		//continue building tree unless it is pure enough to classify already, it is homogenous, or max depth has been reached
		if ((entropy(matrix) <= min_impurity) || (currentdepth >= max_depth) || homogeneous(matrix)) {
			//stop
			parent.leaf = true;
			parent.response = classifyResponse(matrix);
			return;
		} else {
			
			//find highest IG
			float max = Integer.MIN_VALUE;
			int featureToSplit = 0;
			for (int i = 0; i < matrix[0].length - 1; i++) {
				if (IG(matrix, i) > max) {
					max = IG(matrix, i);
					featureToSplit = i;
				}
			}
			parent.feature = featureToSplit;
			
			//recurse further
			HashMap<String, Integer> distincts = distincts(matrix, featureToSplit);
			int iterations = 0;
			for (String x : distincts.keySet()) {
				Node node = new Node();
				parent.Children.put(x, node);
				training(filter(matrix, featureToSplit, x), ++currentdepth-iterations, node);
				iterations++;
			}
			return;
		}
	}

	private String[][] filter(String[][] matrix, int col, String value) {

		ArrayList<ArrayList<String>> hold = new ArrayList<>();
		int row = 0;
		for (int i = 0; i < matrix.length; i++) {

			for (int j = 0; j < matrix[0].length; j++) {

				if (matrix[i][j].equals(value) && j == col) {
					hold.add(new ArrayList<>());

					for (int k = 0; k < matrix[0].length; k++) {
						if (k != col) {
							hold.get(row).add(matrix[i][k]);
						}
					}
					row++;
				}
			}
		}
		matrix = new String[hold.size()][hold.get(0).size()];

		for (int i = 0; i < hold.size(); i++) {

			for (int j = 0; j < hold.get(0).size(); j++) {

				matrix[i][j] = hold.get(i).get(j);
			}
		}

		return matrix;

	}

	private boolean homogeneous(String[][] matrix) {

		boolean homog = true;
		String comp = matrix[0][matrix[0].length - 1];
		for (int i = 0; i < matrix.length; i++) {
			if (!comp.equals(matrix[i][matrix[0].length - 1])) {
				homog = false;
			}
		}
		return homog;
	}
	
	public String classifyValue(String input) {
		return classifying(input,Head, 0);
	}

	private String classifying(String input, Node node, int count) {

		String[] split = input.split(",");
		if (node!=null) {
			if(node.leaf)
				return node.response;
		} else if(node==null)
			return "unclassifiable, not sufficient data";
		
		//I couldnt find an easy fix around finding a way to dynamically assign the correct index to access later for classifying 
		//so I just follow the same steps of filtering that I did while building the tree but for the input I want to classify now
		input = "";
		for(int i = 0; i < split.length; i++) {
			if(i!= node.feature)
				input += split[i] + ",";
		}
		
		return classifying(input, node.Children.get(split[node.feature]), ++count);
		
	}

	private String classifyResponse(String[][] matrix) {
		//1) get distinct responses
		HashMap<String, Integer> distinctResponses = distincts(matrix, matrix[0].length - 1);
		//2) get the count of each distinct response
		int[] distinctCount = new int[distinctResponses.size()];
		int max = Integer.MIN_VALUE;
		String response = "";
		for (String x : distinctResponses.keySet()) {
			for(int i = 0; i < matrix.length; i++) {
				if(matrix[i][matrix[0].length-1].equals(x)) {
					distinctCount[distinctResponses.get(x)]++;
				}
			}
			
		}
		//3) determine which distinct response had more occurences
		int count = 0;
		for (String x : distinctResponses.keySet()) {
			if (distinctCount[distinctResponses.get(x)] > max) {
				max = distinctCount[count];
				response = x;
			}
			count++;
		}
		return response;
	}
}