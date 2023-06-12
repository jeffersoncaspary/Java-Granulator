package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Granulate {

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		System.out.println("enter name of file");
		String name = scanner.nextLine();
		System.out.println("enter number of granules per second");
		int granuleHertz = scanner.nextInt();
		System.out.println("enter length of crossfade in data points");
		int fadeNum = scanner.nextInt();
		// TODO desired output length
		scanner.close();
		Wave sample = new Wave(name);
		double [][] samples = sample.getData();
		
        
		double [][] out = GranulateUniformlyRandom(samples, sample.getSampleRate(), granuleHertz, sample.numChannels(), fadeNum);
        Wave.writeWavFile("out/out1.wav", out, sample.getSampleRate(), sample.numChannels());
        out = GranulateExponentiallyLoudness(samples, sample.getSampleRate(), granuleHertz, sample.numChannels(), fadeNum);
        Wave.writeWavFile("out/out2.wav", out, sample.getSampleRate(), sample.numChannels());
        
	}
	// TODO just add a Wave object
	// TODO adjustable length, longer random sequences
	// assumes 2 channels
	public static  double[][] GranulateUniformlyRandom(double[][] samples, int sampleRate, int hertz, int numChannels, int fadeNum) {
		assert hertz > 5;
		assert hertz < 200;
		int numSamples = samples[0].length;
		int granuleDuration = sampleRate / hertz; // duration in number of samples - time duration is the inverse of hertz
        int numGranules = numSamples/granuleDuration; // TODO what if not a divisor of sample rate?
        Random random = new Random();
		List<Integer> startsList = new ArrayList<>();
		for (int i = 0; i < numSamples/granuleDuration; i ++) {
			startsList.add( random.nextInt(numSamples/granuleDuration) * granuleDuration);
		}
		//Collections.shuffle(startsList);
		System.out.println(startsList.toString());
		// now we make a new samples with the granules in new places
		// TODO crossfade
		// we crossfade by starting next granule sooner by fadenumber
		double[][] out = crossFade(samples, numChannels, granuleDuration, numGranules, startsList, fadeNum);
		return out;
	}
	private static double[][] crossFade(double[][] samples, int numChannels, int granuleDuration, int numGranules,
			List<Integer> startsList, int fadeNum) {
		double[][] out = new double[numChannels][numGranules * (granuleDuration - fadeNum) + fadeNum]; // could be different from sample
		for (int gran = 0; gran < numGranules; gran ++) {
			System.out.println(gran + " " + numGranules);
			for (int offset = 0; offset < granuleDuration; offset ++) {
				// real crossfade
				// the idea here is that the ending granule is multiplied by (cos(x) + 1)/2, incoming is -(cos(x) + 1)/2
				if (offset <= fadeNum) {
					for (int j = 0; j < numChannels; j ++) {
						out[j][gran * (granuleDuration - fadeNum) + offset] += samples[j][startsList.get(gran) + offset] * (-Math.cos((double)offset/fadeNum * Math.PI)/2 + .5);
					}
					
				
					//System.out.println((-Math.cos((double)offset/fadeNum * Math.PI)/2 + .5));
				} else if (offset >= granuleDuration - fadeNum) {
					int endOffset = offset - (granuleDuration - fadeNum);
					for (int j = 0; j < numChannels; j ++) {
						out[j][gran * (granuleDuration - fadeNum) + offset] += samples[j][startsList.get(gran) + offset] * (Math.cos((double)endOffset/fadeNum * Math.PI)/2 + .5);
					}
				} else {
					// iterate through granules, but they are chosen from shuffled list
					for (int j = 0; j < numChannels; j ++) {
						out[j][gran * (granuleDuration - fadeNum) + offset] = samples[j][startsList.get(gran) + offset];
					}
				}
			}
		}
		return out;
	}
	public static double[][] GranulateExponentiallyLoudness(double[][] samples, int sampleRate, int hertz, int numChannels, int fadeNum) {
		assert hertz > 5;
		assert hertz < 200;
		int numSamples = samples[0].length;
		int granuleDuration = sampleRate / hertz; // duration in number of samples - time duration is the inverse of hertz
        int numGranules = numSamples/granuleDuration; // TODO what if not a divisor of sample rate?
        // sort by loudness (RMS)
        
        
		List<Granule> granList = new ArrayList<>();
		for (int i = 0; i < numSamples/granuleDuration; i ++) {
			granList.add(new Granule(i * granuleDuration, RMS(samples, i * granuleDuration, granuleDuration)));
		}
		Collections.sort(granList);
		// randomize, louder sound granules have priority
		Random rand = new Random();
		List<Integer> startsList = new ArrayList<>();
		for (int i = 0; i < granList.size(); i ++) {
			int nextIndex = (int) (rand.nextExponential() * 5);
			startsList.add(granList.get(nextIndex).start);
		}
		System.out.println(granList.toString());
		double[][] out = crossFade(samples, numChannels, granuleDuration, numGranules, startsList, fadeNum);
		return out;
	}
	private static double RMS(double[][] samples, int offset, int granuleDuration) {
		//TODO
		double sum = 0;
		for (int i = 0; i < samples.length; i ++) {
			for (int j = offset; j < offset + granuleDuration; j ++) {
				sum += Math.pow(samples[i][j], 2);
			}
		}
		sum /= samples.length * granuleDuration;
		sum = Math.sqrt(sum);
		System.out.print(sum + " ");
		return sum;
		
	}

}
class Granule implements Comparable {
    double property;
    int start;
    
	public Granule(int i, double rms) {
		start = i;
		property = rms;
	}

	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		if (property < ((Granule) o).property) {
			return 1;
		} else if (property > ((Granule) o).property) {
			return -1;
		}
		return 0;
	}
	public String toString() {
		return start + "";
	}
	
}
