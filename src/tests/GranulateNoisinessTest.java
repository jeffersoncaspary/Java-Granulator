package tests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import model.GranulateNoisiness;
import model.LoudnessDensity;
import model.Wave;

class GranulateNoisinessTest {

	@Test
	void test() {
		Wave wave = new Wave("in/DPS_100TechnoKicks_Clean_12.wav");
    	GranulateNoisiness test = new GranulateNoisiness(wave.getData(), wave.getSampleRate(), 1050);
    	Wave.writeWavFile("test1.wav", test.GranulateDensityWise(wave.getData(), 2, wave.getSampleRate(), 30, 80), wave.getSampleRate(), wave.numChannels());
	}

}
