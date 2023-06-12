package tests;
import model.Wave;
import model.AutoCorrelation;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AutoCorrelationTest {

	@Test
	void testWithNotPureNoise() {
		Wave wave = new Wave("in/clink2.wav");
		assertEquals(1, AutoCorrelation.NormalizedAutoCorrelation(wave.getData(), 200, 42, 0));
		for (int i = 1; i < 42; i ++) {
			System.out.println(AutoCorrelation.NormalizedAutoCorrelation(wave.getData(), 200, 42, i));
		}
	}
	@Test
	void testWithPureNoise() {
		Wave wave = new Wave("in/TEST_White noise (ID 1037)_BSB.wav");
		assertEquals(1, AutoCorrelation.NormalizedAutoCorrelation(wave.getData(), 200, 42, 0));
		for (int i = 1; i < 42; i ++) {
			System.out.println(AutoCorrelation.NormalizedAutoCorrelation(wave.getData(), 200, 42, i));
		}
	}

}
