import java.util.ArrayList;
import java.util.HashMap;

public class UtilityFunctions {

	//this filter method is the one that actually splits the tree
	public static ArrayList<ArrayList<String>> filterRemove(ArrayList<ArrayList<String>> matrix, int col, String value) {

		ArrayList<ArrayList<String>> hold = new ArrayList<>();
		int row = 0;
		for (int i = 0; i < matrix.size(); i++) {

			for (int j = 0; j < matrix.get(0).size(); j++) {

				if (matrix.get(i).get(j).equals(value)) {

					hold.add(new ArrayList<>());
					for (int k = 0; k < matrix.get(0).size(); k++) {
						if (k != col) {
							hold.get(row).add(matrix.get(i).get(k));
						}
					}
					row++;
				}
			}
		}
		return hold;

	}

	//distinct values in a list
	public static HashMap<String, Integer> distincts(ArrayList<ArrayList<String>> matrix, int col) {

		int count = 0;
		HashMap<String, Integer> distincts = new HashMap<>();
		for (int i = 0; i < matrix.size(); i++) {
			if (distincts.get(matrix.get(i).get(col)) == null) {
				distincts.put(matrix.get(i).get(col), count++);
			}
		}

		return distincts;
	}

	//condEntropy (response|attribute)
	public static float condEntropy(ArrayList<ArrayList<String>> matrix, int col) {

		float condEntropy = 0;
		HashMap<String, Integer> distinctsCol = distincts(matrix, col);
		int[] distinctsColCount = new int[distinctsCol.size()];
		HashMap<String, Integer> distinctsTarget = distincts(matrix, matrix.get(0).size() - 1);
		float[] distinctsTargetCount = new float[distinctsTarget.size()];

		for (int i = 0; i < matrix.size(); i++) {
			distinctsColCount[distinctsCol.get(matrix.get(i).get(col))]++;
		}

		for (int i = 0; i < distinctsColCount.length; i++) {
			float condProb = 0;
			String x = (String) distinctsCol.keySet().toArray()[i];
			ArrayList<ArrayList<String>> filtered = filter(matrix, col, x);
			float prob = (float) distinctsColCount[distinctsCol.get(filtered.get(0).get(col))] / (float) matrix.size();
			for (int j = 0; j < filtered.size(); j++) {

				distinctsTargetCount[distinctsTarget.get(filtered.get(j).get(matrix.get(0).size() - 1))]++;
			}
			for (int j = 0; j < distinctsTargetCount.length; j++) {
				condProb += (float) (((float) distinctsTargetCount[j] / (float) filtered.size() + 0.00001)
						* (float) ((float) Math
								.log10(((float) distinctsTargetCount[j] / (float) filtered.size()) + 0.00001)
								/ (float) Math.log10(2)));
			}
			for (int j = 0; j < distinctsTargetCount.length; j++) {
				distinctsTargetCount[j] = 0;
			}
			condEntropy += prob * condProb;

		}

		return -condEntropy;
	}

	//Entropy (response only)
	public static float entropy(ArrayList<ArrayList<String>> matrix) {

		float entropy = 0;
		HashMap<String, Integer> distincts = distincts(matrix, matrix.get(0).size() - 1);
		float[] distinctsCount = new float[distincts.size()];
		for (int i = 0; i < matrix.size(); i++) {

			distinctsCount[distincts.get(matrix.get(i).get(matrix.get(0).size() - 1))]++;
		}
		for (int i = 0; i < distinctsCount.length; i++) {

			entropy += (float) (distinctsCount[i] / matrix.size())
					* (float) (Math.log10(distinctsCount[i] / matrix.size()) / Math.log10(2));
		}

		return -entropy;
	}

	//this is used in calculating entropy
	public static ArrayList<ArrayList<String>> filter(ArrayList<ArrayList<String>> matrix, int col, String value) {

		ArrayList<ArrayList<String>> hold = new ArrayList<>();
		int row = 0;
		for (int i = 0; i < matrix.size(); i++) {

			for (int j = 0; j < matrix.get(0).size(); j++) {

				if (matrix.get(i).get(j).equals(value)) {

					hold.add(new ArrayList<>());
					for (int k = 0; k < matrix.get(0).size(); k++) {
						hold.get(row).add(matrix.get(i).get(k));
					}
					row++;
				}
			}
		}
		return hold;

	}
}
