package tests;

import model.Wave;
import model.LoudnessDensity;

public class LoudnessDensityTest {
    public static void main(String[] args) {
    	Wave wave = new Wave("in/clink2.wav");
    	LoudnessDensity test = new LoudnessDensity(wave.getData(), wave.getSampleRate(), 1050);
    	Wave.writeWavFile("test1.wav", test.GranulateDensityWise(wave.getData(), 2, wave.getSampleRate(), 525, 20), wave.getSampleRate(), wave.numChannels());
    }
}
