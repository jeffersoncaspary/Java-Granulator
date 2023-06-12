package model;

import java.io.FileOutputStream;
import java.io.IOException;

public class SinWaveGenerator {

    public static void main(String[] args) {
        int sampleRate = 44100; // Sample rate in Hz
        double frequency = 440.0; // Frequency of the sine wave in Hz
        double duration = 5.0; // Duration of the audio in seconds

        int numSamples = (int) (sampleRate * duration);
        double[] samples = new double[numSamples];

        for (int i = 0; i < numSamples; i++) {
            double time = i / (double) sampleRate;
            samples[i] = Math.sin(2 * Math.PI * frequency * time);
        }

        writeWavFile("sine_wave.wav", samples, sampleRate);
    }

    private static void writeWavFile(String filename, double[] samples, int sampleRate) {
        try (FileOutputStream fos = new FileOutputStream(filename)) {
            // Write WAV file header
            writeString(fos, "RIFF"); // Chunk ID
            writeInt(fos, 36 + samples.length * 2); // Chunk Size
            writeString(fos, "WAVE"); // Format
            writeString(fos, "fmt "); // Subchunk1 ID
            writeInt(fos, 16); // Subchunk1 Size
            writeShort(fos, 1); // Audio Format (1 = PCM)
            writeShort(fos, 1); // Number of Channels
            writeInt(fos, sampleRate); // Sample Rate
            writeInt(fos, sampleRate * 2); // Byte Rate
            writeShort(fos, 2); // Block Align
            writeShort(fos, 16); // Bits per Sample
            writeString(fos, "data"); // Subchunk2 ID
            writeInt(fos, samples.length * 2); // Subchunk2 Size

            // Write audio data (double to PCM)
            for (double sample : samples) {
                writeShort(fos, (int) (sample * Short.MAX_VALUE));
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
}