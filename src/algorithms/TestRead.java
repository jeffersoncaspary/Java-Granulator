package algorithms;

import model.Wave;

public class TestRead {

	public static void main(String[] args) {
		double[][] samples = new Wave("sine_wave.wav").getData();
		Wave.printStart(samples[0]);
		//Wave.printStart(samples[1]);

	}

}
