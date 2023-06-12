package model;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class Wave {
	 int sampleRate;
	 int numChannels;
	 int bytesPerSample;
	 
	 double[][] data;
     public Wave() {
    	 
     }
     public Wave(String name) {
		data = readWavFile(name);
	}
	public static void printStart(double[] samples) {
    	 for (int i = 0; i < 5; i ++) {
    		 System.out.println(samples[i]);
    	 }
     }
     public double[][] readWavFile(String filename) {
         try (FileInputStream fis = new FileInputStream(filename)) {
             byte[] header = new byte[36]; // WAV file header is 36 bytes
             fis.read(header);

             int headerSize = byteArrayToInt(header, 16); // Extract header size from header
             System.out.println("Header size: " + headerSize);
             sampleRate = byteArrayToInt(header, 24); // Extract sample rate from header
             numChannels = byteArrayToShort(header, 22); // Extract number of channels from header
             int bitDepth = byteArrayToShort(header, 34); // Extract bit depth from header
             // TODO support non16bit audio
             bytesPerSample = bitDepth / 8;
             
             /*String chunkIdString = new String(header, StandardCharsets.UTF_8).substring(36, 40);
             System.out.println(chunkIdString);
             int chunkSize = byteArrayToInt(header,40);
             System.out.println(chunkSize);
             if (!chunkIdString.equals("data")) {
                 fis.skip(chunkSize);
             }*/
          // Skip over chunks until data section is reached
             int chunkSize = 0;
             String chunkIdString;
             boolean dataSectionFound = false;
             while (!dataSectionFound) {
                 byte[] chunkId = new byte[4];
                 fis.read(chunkId);

                 // Check if chunk ID corresponds to data section
                 chunkIdString = new String(chunkId, StandardCharsets.UTF_8);
                 System.out.println(chunkIdString);
                 if (chunkIdString.equals("data")) {
                     dataSectionFound = true;
                     byte[] chunkSizeBytes = new byte[4];
                     fis.read(chunkSizeBytes);
                     chunkSize = byteArrayToInt(chunkSizeBytes, 0);
                 } else {
                     // Read and skip the chunk size
                     byte[] chunkSizeBytes = new byte[4];
                     fis.read(chunkSizeBytes);
                     chunkSize = byteArrayToInt(chunkSizeBytes, 0);
                     System.out.println(chunkSize);
                     fis.skip(chunkSize);
                 }
                 System.out.println(chunkSize);
             }
             int numSamples = (int) (chunkSize / (bytesPerSample * numChannels));
             double[][] audioData = new double[numChannels][numSamples];
             byte[] bytes = new byte[bytesPerSample];
             
             for (int i = 0; i < numSamples; i++) {
                 for (int j = 0; j < numChannels; j++) {
                     int value = 0;
                     
                     fis.read(bytes);
                     value = byteArrayToShort(bytes, 0);
          
                     //System.out.print((j == 0 ? value : 0) + " ");
                     audioData[j][i] = value / Math.pow(2, bitDepth - 1);
                     //System.out.print(audioData[j][i]  + " ");
                     //if ( audioData[j][i] > 1) {
                    //	 System.out.println( "wtf");
                     //}
                     assert audioData[j][i] <= 1;
                     assert audioData[j][i] >= -1;
                 }
             }
             System.out.println("loaded");
             return audioData;
         } catch (IOException e) {
             e.printStackTrace();
             return null;
         }
     }
     /**
      * Wav is little endian, but java is big ending: 0xABCD --> 0xDCBA
      * @param bytes
      * @param offset
      * @return
      */
     public static int byteArrayToInt(byte[] bytes, int offset) {
         return (bytes[offset] & 0xFF) |
                 ((bytes[offset + 1] & 0xFF) << 8) |
                 ((bytes[offset + 2] & 0xFF) << 16) |
                 ((bytes[offset + 3] & 0xFF) << 24);
     }

     private static short byteArrayToShort(byte[] bytes, int offset) {
         return (short) ((bytes[offset] & 0xFF) |
                 ((bytes[offset + 1] & 0xFF) << 8));
     }
	public static void writeWavFile(String filename, double[] samplesLeft, double[] samplesRight, int sampleRate) {
    	 assert samplesLeft.length == samplesRight.length;
         try (FileOutputStream fos = new FileOutputStream(filename)) {
        	 int numBytes = 2;
        	 int numChannels = 2;
             // Write WAV file header
        	 //RIFF Header
             writeString(fos, "RIFF"); // Chunk ID
             writeInt(fos, 36 + samplesLeft.length * 2); // Chunk Size
             writeString(fos, "WAVE"); // Format
             // Format chunk
             writeString(fos, "fmt "); // Subchunk1 ID
             writeInt(fos, 16); // Subchunk1 Size
             writeShort(fos, 1); // Audio Format (1 = PCM)
             writeShort(fos, numChannels); // Number of Channels
             writeInt(fos, sampleRate); // Sample Rate
             writeInt(fos, sampleRate * numBytes); // Byte Rate
             writeShort(fos, numBytes * numChannels); // Block Align
             writeShort(fos, 8*numBytes); // Bits per Sample
             writeString(fos, "data"); // Subchunk2 ID
             writeInt(fos, samplesLeft.length * numChannels * numBytes); // Subchunk2 Size

             // Write audio data (double to PCM)
             for (int i = 0; i < samplesLeft.length; i ++) {
                 writeShort(fos, (int) (samplesLeft[i] * Short.MAX_VALUE));
                 writeShort(fos, (int) (samplesRight[i] * Short.MAX_VALUE));
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
	public static void writeWavFile(String filename, double[][] samples, int sampleRate, int numChannels) {
   	 assert samples[0].length == samples[0].length;
        try (FileOutputStream fos = new FileOutputStream(filename)) {
       	 int numBytes = 2;
            // Write WAV file header
       	 //RIFF Header
            writeString(fos, "RIFF"); // Chunk ID
            writeInt(fos, 36 + samples[0].length * 2); // Chunk Size
            writeString(fos, "WAVE"); // Format
            // Format chunk
            writeString(fos, "fmt "); // Subchunk1 ID
            writeInt(fos, 16); // Subchunk1 Size
            writeShort(fos, 1); // Audio Format (1 = PCM)
            writeShort(fos, numChannels); // Number of Channels
            writeInt(fos, sampleRate); // Sample Rate
            writeInt(fos, sampleRate * numBytes); // Byte Rate
            writeShort(fos, numBytes * numChannels); // Block Align
            writeShort(fos, 8*numBytes); // Bits per Sample
            writeString(fos, "data"); // Subchunk2 ID
            writeInt(fos, samples[0].length * numChannels * numBytes); // Subchunk2 Size

            // Write audio data (double to PCM)
            for (int i = 0; i < samples[0].length; i ++) {
            	for (int j = 0; j < numChannels; j ++) {
            		writeShort(fos, (int) (samples[j][i] * Short.MAX_VALUE));
            	}
                
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
     private static void writeInt(FileOutputStream fos, int value) throws IOException {
         fos.write(value);
         fos.write(value >> 8);
         fos.write(value >> 16);
         fos.write(value >> 24);
     }

     private static void writeShort(FileOutputStream fos, int value) throws IOException {
         fos.write(value);
         fos.write(value >> 8);
     }

     private static void writeString(FileOutputStream fos, String value) throws IOException {
         for (int i = 0; i < value.length(); i++) {
             fos.write(value.charAt(i));
         }
     }
	public double[][] getData() {
		return data;
	}
	public int getSampleRate() {
		return sampleRate;
	}
	public int numChannels() {
		return numChannels;
	}
     
}
