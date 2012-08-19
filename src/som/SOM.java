package som;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SOM {

	private List<Integer> indices = new ArrayList<Integer>(4000);

	private int numInput;
	private int numOutput;
	private double[][] weights;
	private double startLearningRate = 0.1;
	private int numIteration;
	private int countIteration;

	private double[][] dwt = new double[50 * 13 + 1][50 * 13 + 1];
	private int windowOffset = 50 / 10;

	private double startRadius;

	public SOM(int numInput, int numOutput) {
		this.numInput = numInput;
		this.numOutput = numOutput;
		this.startRadius = numOutput / 2;
		this.weights = new double[numOutput][numInput];
		for (int i = 0; i < numOutput; i++) {
			for (int j = 0; j < numInput; j++) {
				weights[i][j] = 0;
			}
		}
	}

	public void train(TrainingSet ts, int numIteration) {
		initWeights();
		this.numIteration = numIteration;
		countIteration = 0;
		boolean ok = true;
		while (countIteration <= numIteration && ok) {
			countIteration++;
			ok = epoch(ts);
		}
	}

	public int findWinner(Input input) {
		double[] values = input.getValues();
		if (values.length != numInput) {
			return -1;
		} else {
			int winner = 0;
			double min = Double.MAX_VALUE;
			double currentDist = 0;
			for (int i = 0; i < numOutput; i++) {
				System.out.println("dist to " + i + " ");
				currentDist = getDistance(weights[i], values);
				System.out.println(currentDist);
				if (currentDist < min) {
					min = currentDist;
					winner = i;
				}
			}
			return winner;
		}
	}

	private boolean epoch(TrainingSet ts) {
		Input input = ts.getRandomInput();
		input.normalize();
		if (input.getValues().length != numInput) {
			return false;
		} else {
			int winner = findWinner(input);
			indices.add(winner);
			System.out.println("winner " + winner);
			adjustWeights(input, winner);
			return true;
		}
	}

	private void initWeights() {
		for (int i = 0; i < numOutput; i++) {
			for (int j = 0; j < numInput; j++) {
				weights[i][j] = Math.random() / 10000; // small random number
			}
		}
	}

	private void adjustWeights(Input input, int winner) {
		double[] values = input.getValues();
		// System.out.println("learning rage " + getLearningRate());
		// System.out.println("neighbourhood rage " + getNeighbourhoodRadius());
		for (int i = 0; i < numOutput; i++) {
			double radius = getNeighbourhoodRadius();
			int distance = Math.abs(winner - i);
			if (distance > Math.round(radius)) {
				continue;
			}
			// System.out.println("distance factor " + winner + " to " + i + " "
			// + getDistanceFactor(distance, radius));
			for (int j = 0; j < numInput; j++) {
				weights[i][j] += getDistanceFactor(distance, radius) * getLearningRate() * (values[j] - weights[i][j]);
			}
		}
	}

	private double getLearningRate() {
		return startLearningRate * Math.exp(-((double) countIteration) / numIteration);
	}

	private double getDistanceFactor(double distance, double neighbourhoodRadius) {
		return Math.exp(-distance * distance / (2 * neighbourhoodRadius * neighbourhoodRadius));
	}

	private double getNeighbourhoodRadius() {
		return startRadius * Math.exp(-((double) countIteration) / numIteration);
	}

	// dynamic time warping
	private double getDistance(double[] a, double[] b) {
		for (int i = 0; i <= a.length; i++) {
			for (int j = 0; j <= b.length; j++) {
				dwt[i][j] = Double.MAX_VALUE;
			}
		}
		dwt[0][0] = 0d;
		int start;
		int end;
		for (int i = 1; i <= a.length; i++) {
			start = Math.max(1, i - windowOffset);
			end = Math.min(b.length, i + windowOffset);
			for (int j = start; j <= end; j++) {
				dwt[i][j] = Math.abs(a[i - 1] - b[i - 1]) + Math.min(dwt[i - 1][j - 1], Math.min(dwt[i - 1][j], dwt[i][j - 1]));
			}
		}

		return dwt[a.length][b.length];
	}

	public List<Integer> getList() {
		Collections.reverse(indices);
		return indices;
	}

	public void printMe() {
		Collections.reverse(indices);
		System.out.print("Output ");
		System.out.println(indices);
	}

	public int getNumInput() {
		return numInput;
	}

	public void setNumInput(int numInput) {
		this.numInput = numInput;
	}

	public int getNumOutput() {
		return numOutput;
	}

	public void setNumOutput(int numOutput) {
		this.numOutput = numOutput;
	}

}