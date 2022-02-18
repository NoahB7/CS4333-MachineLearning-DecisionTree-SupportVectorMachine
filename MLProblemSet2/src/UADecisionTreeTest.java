import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class UADecisionTreeTest {
	/********************************
	Name: Noah Buchanan
	Username: ua100
	Problem Set: PS2
	Due Date: February 21,2021
	********************************/

	public int getBestDepth(String filename, String target, float min_impurity) throws IOException {

		BufferedReader br = new BufferedReader(new FileReader(filename));
		String[] split = br.readLine().split(",");
		int loop = split.length;
		if (split.length > 10) {
			loop = 10;
		}
		br.close();
		float[] sum = new float[loop];
		float[][] avgs = new float[100][loop];
		
		BufferedWriter bw = new BufferedWriter(new FileWriter("results2.txt"));


		for (int k = 0; k < 100; k++) {
			
			splitDataOf(filename);

			for (int i = 0; i < loop; i++) {

				float accuracy = getAccuracy(filename,target,i, min_impurity);
				bw.append(accuracy + ", ");
				sum[i]+=accuracy;
				avgs[k][i]=accuracy;
			}
			bw.newLine();
		}
		bw.close();
		float[] avg = new float[sum.length];
		for(int i = 0; i < sum.length; i++) {
			avg[i] = sum[i]/100;
		}
		float[] variances = new float[avg.length];
		for(int i = 0; i < avgs.length; i++) {
			for(int j = 0; j < avgs[0].length; j++) {
				variances[j] += (float) Math.pow(avgs[i][j]-avg[j], 2);
			}
		}
		for(int i = 0; i < variances.length; i++) {
			variances[i]/=100;
		}
		float max = avg[0];
		int bestDepth = 0;
		for(int i = 0; i < avg.length; i++) {
			System.out.println("depth " + i + ", average accuracy and variance: " + avg[i] + " " + variances[i]);
			if(avg[i] > max) {
				max = avg[i];
				bestDepth = i;
			}
		}
		return bestDepth;
	}

	public float getRandomAccuracy(String filename, String target, float min_impurity, int max_depth) throws IOException {

		// performing randomization, classification and accuracy 100 times
		float sum = 0;
		float[] avgs = new float[100];
		BufferedWriter bw = new BufferedWriter(new FileWriter("results1.txt"));
		for(int j = 0; j < 100; j++) {
			
			float accuracy = getAccuracy(filename,target,max_depth, min_impurity);
			sum += accuracy;
			avgs[j] = accuracy;
			bw.append(accuracy+"");
			bw.newLine();
		}
		bw.close();
		// calculating avg and variance
		float avg = (sum / 100);
		sum = 0;
		for (int i = 0; i < 100; i++) {
			sum += (float) Math.pow(avgs[i] - avg, 2);
		}
		float variance = (sum / 99);
		System.out.println("average accuracy and variance: " + avg + " " + variance);
		return avg;
	}

	private void splitDataOf(String filename) throws IOException {

		BufferedReader br = new BufferedReader(new FileReader(filename));
		BufferedWriter bw1 = new BufferedWriter(new FileWriter("80.txt"));
		BufferedWriter bw2 = new BufferedWriter(new FileWriter("20.txt"));
		String line = "";
		ArrayList<String> holder = new ArrayList<>();
		while ((line = br.readLine()) != null) {
			holder.add(line);
		}
		int size = holder.size() - 1;
		bw1.write(holder.get(0));
		bw1.newLine();
		holder.remove(0);

		for (int i = 0; i < size; i++) {

			int rand = (int) (Math.random() * (holder.size() - 1));

			if (i < (int) (size * .8)) {
				bw1.write(holder.get(rand));
				bw1.newLine();
			} else {
				bw2.write(holder.get(rand));
				bw2.newLine();
			}
			holder.remove(rand);

		}
		br.close();
		bw1.close();
		bw2.close();
	}
	
	private float getAccuracy(String filename, String target, int splits, float min_impurity) throws IOException{
		
		splitDataOf(filename);
		
		UADecisionTree a = new UADecisionTree();
		a.setTreeMaxDepth(splits);
		a.setMinimumImpurity(min_impurity);
		a.train(a.getTrainingMatrix("80.txt", target));

		BufferedReader br = new BufferedReader(new FileReader("20.txt"));
		String line = "";
		float count = 0;
		float total = 0;
		while ((line = br.readLine()) != null) {

			// parsing out the response from training file
			String[] split = line.split(",");
			line = "";
			for (int i = 0; i < split.length - 1; i++)
				line += split[i] + ",";
			line = line.substring(0, line.length() - 1);

			// classifying and keeping track of correct classifications if able
			if (a.classifyValue(line).equals(split[split.length - 1]))
				count++;

			if (!split[split.length - 1].equals("unclassifiable, not sufficient data"))
				total++;
		}
		br.close();
		return count/total;
	}

}