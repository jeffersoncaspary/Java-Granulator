package model;

import java.util.Random;

public class GranulateNoisiness {
	double[] density;
	double[] totalMassUpToCurrentIndex;
	int maxHertz;
	Random random;
	public GranulateNoisiness(double[][] data, int sampleRate, int maxHertz) {
		int granuleDuration = sampleRate/maxHertz;
		double sum = 0;
		density = new double[data[0].length/ granuleDuration];
		totalMassUpToCurrentIndex = new double[data[0].length/ granuleDuration];
		for (int i = 0; i < density.length; i ++) {
			density[i] = noisiness(data, granuleDuration * i, granuleDuration);
			sum += density[i];
		}
		//Normalize
		for (int i = 0; i < density.length; i ++) {
			density[i] /= sum;
			totalMassUpToCurrentIndex[i] = (i == 0 ? 0 : totalMassUpToCurrentIndex[i-1]) + density[i];
			//System.out.println(density[i]);
		}
		random = new Random();
		this.maxHertz = maxHertz;
	}
	private double noisiness(double[][] data, int offset, int granuleDuration) {
		// attempt 1 - average of a few points
		// TODO avoid high autocorrelation for very quiet parts of the sample
		double autoCorrelation = 0;
		int numSamples = 4;
		for (int j = 0; j < numSamples; j ++) {
			autoCorrelation += AutoCorrelation.NormalizedAutoCorrelation(data, offset, granuleDuration, j * 2);
		}
		autoCorrelation /= numSamples;
		System.out.println(autoCorrelation);
		if (autoCorrelation > .5) {
			return 4;
		} else if (autoCorrelation > .3) {
			return 3;
		} else if (autoCorrelation > .1) {
			return 1;
		} else
		    return 0;
	}
	/**
	 * 
	 * @param samples - wav data as doubles
	 * @param length - length of sample to be created in seconds
	 * @param sampleRate - hz of audio sample
	 * @param granuleHertz - how many granules to play per second
	 * @param fadeNum - number of data points in sample to fade by
	 * @return
	 */
	public double [][] GranulateDensityWise(double[][]samples, double length, int sampleRate, int granuleHertz, int fadeNum) {
		// iterate over number of granules
		int granuleDuration = sampleRate/granuleHertz;
		int numGranules = ((int) length * sampleRate) / (granuleDuration - fadeNum);
		double[][] out = new double[samples.length][numGranules * (granuleDuration - fadeNum) + fadeNum]; // could be different from sample
		for (int i = 0; i < numGranules; i++) {
			int maxStart = samples[0].length - granuleDuration;
			// since we can't go past the end of the sample, we must not choose
			// a start point greater than or equal to maxStart
			// thus we find the index of the random number multiplied by the maximum
			// value it can legally take
			double legalityMultiplier = totalMassUpToCurrentIndex[maxStart * maxHertz / sampleRate];
			int index = findIndex(random.nextDouble() * legalityMultiplier);
			System.out.println(index);
			int start = sampleRate /maxHertz * index;
			crossFade(samples, out, granuleDuration, start, fadeNum, i);
		}
		return out;
	}
	// written by chatgpt, lesgo
	private int findIndex(double target) {
		int left = 0;
	    int right = totalMassUpToCurrentIndex.length - 1;

	    while (left < right) {
	        int mid = left + (right - left) / 2;

	        // Check if the middle element is the target
	        if (totalMassUpToCurrentIndex[mid] == target) {
	            return mid;
	        }

	        // Check if the target is in the left half of the array
	        if (totalMassUpToCurrentIndex[mid] > target) {
	            right = mid - 1;
	        }
	        // Check if the target is in the right half of the array
	        else {
	            left = mid + 1;
	        }
	    }

	    // At this point, the target was not found in the array
	    // Return the index of the element closest to the target
	    // TODO check to see if this causes problems
	    if (Math.abs(totalMassUpToCurrentIndex[left] - target) < Math.abs(totalMassUpToCurrentIndex[right] - target)) {
	        return left;
	    } else {
	        return right;
	    }
	}
	private static double[][] crossFade(double[][] samples, double[][] out, int granuleDuration,
			int start, int fadeNum, int gran) {
		int numChannels = samples.length;
		for (int offset = 0; offset < granuleDuration; offset ++) {
			// real crossfade
			// the idea here is that the ending granule is multiplied by (cos(x) + 1)/2, incoming is -(cos(x) + 1)/2
			int sampleIndex = start + offset;
			// fade in
			if (offset <= fadeNum) {
				for (int j = 0; j < numChannels; j ++) {
					out[j][gran * (granuleDuration - fadeNum) + offset] += samples[j][sampleIndex] * (-Math.cos((double)offset/fadeNum * Math.PI)/2 + .5);
				}


				//System.out.println((-Math.cos((double)offset/fadeNum * Math.PI)/2 + .5));
			// fade out
			} else if (offset >= granuleDuration - fadeNum) {
				int endOffset = offset - (granuleDuration - fadeNum);
				for (int j = 0; j < numChannels; j ++) {
					out[j][gran * (granuleDuration - fadeNum) + offset] += samples[j][sampleIndex] * (Math.cos((double)endOffset/fadeNum * Math.PI)/2 + .5);
				}
			// play normally
			} else {
				// iterate through granules, but they are chosen from shuffled list
				for (int j = 0; j < numChannels; j ++) {
					out[j][gran * (granuleDuration - fadeNum) + offset] = samples[j][sampleIndex];
				}
			}

		}
		return out;
	}
}
