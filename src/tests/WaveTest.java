package tests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import model.Wave;

class WaveTest {

	@Test
	void testByteArrayToInt() {
		// little endian 255
		byte[] bytes = {(byte) 255, 0, 0, 0};
		assertEquals(Wave.byteArrayToInt(bytes, 0), 255);
		byte[] bytes1 = {(byte) 1, (byte) 255, (byte) 255, (byte) 255};
		assertEquals(-255, Wave.byteArrayToInt(bytes1, 0));
		assertEquals(.5, 1.0/2);
	}
	
	@Test
	void testWriteWav() {
		double[][] data = new double[2][1];
		data[0][0] = .5;
		data[1][0] = -.5;
		Wave.writeWavFile("test.wav", data[0], data[1], 44100);
	}
	@Test
	void testReadWav() {
		double[][] data = new Wave("test.wav").getData();
		assertEquals(.5, data[0][0], .01);
		assertEquals(-.5, data[1][0], .01);
		
	}

}
