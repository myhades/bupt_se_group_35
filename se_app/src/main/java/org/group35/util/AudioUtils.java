package org.group35.util;

import javax.sound.sampled.*;
import java.io.*;
import java.util.Base64;

/**
 * Utility for audio recording from microphone and packaging into WAV for API upload.
 */
public class AudioUtils {
    private TargetDataLine line;
    private AudioFormat format;
    private ByteArrayOutputStream pcmOutput;
    private Thread recordThread;

    /** Defines a standard PCM WAV format: 16kHz, 16-bit, mono, little-endian. */
    private AudioFormat getAudioFormat() {
        float sampleRate = 16000f;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = false;
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }

    /**
     * Start recording audio from the default microphone.
     * @throws LineUnavailableException if the mic is busy or unsupported
     */
    public void startRecording() throws LineUnavailableException {
        format = getAudioFormat();
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        line = (TargetDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start();

        pcmOutput = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];

        recordThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                int count = line.read(buffer, 0, buffer.length);
                if (count > 0) pcmOutput.write(buffer, 0, count);
            }
        }, "Audio Recorder Thread");
        recordThread.start();
    }

    /**
     * Stop recording and return raw WAV bytes (with proper header).
     * Does not release resources; call shutdown() afterward.
     * @return byte[] containing a valid WAV file
     * @throws IOException if writing fails
     */
    public byte[] stopRecording() throws IOException {
        if (line != null) {
            line.stop();
            line.close();
        }
        if (recordThread != null) {
            recordThread.interrupt();
        }
        byte[] pcmData = pcmOutput.toByteArray();
        return convertPcmToWav(pcmData, format);
    }

    /**
     * Stop recording and return Base64-encoded WAV data.
     * Does not release resources; call shutdown() afterward.
     * @return Base64 string of WAV
     * @throws IOException if conversion fails
     */
    public String stopRecordingAsBase64() throws IOException {
        byte[] wav = stopRecording();
        return Base64.getEncoder().encodeToString(wav);
    }

    /**
     * Shutdown and release all resources: line, thread, buffer.
     * Safe to call multiple times.
     */
    public void shutdown() {
        // Stop recording if still active
        if (recordThread != null && recordThread.isAlive()) {
            recordThread.interrupt();
            recordThread = null;
        }
        if (line != null && line.isOpen()) {
            line.stop();
            line.close();
            line = null;
        }
        if (pcmOutput != null) {
            try {
                pcmOutput.close();
            } catch (IOException e) {
                LogUtils.error("Error closing PCM buffer: " + e.getMessage());
            }
            pcmOutput = null;
        }
        LogUtils.debug("AudioUtils resources have been released.");
    }

    /** Convert raw PCM bytes into a WAV byte array with header. */
    private byte[] convertPcmToWav(byte[] pcmData, AudioFormat fmt) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeWavHeader(out, pcmData.length, fmt);
        out.write(pcmData);
        return out.toByteArray();
    }

    /** Write a 44-byte WAV RIFF header to the stream. */
    private void writeWavHeader(OutputStream os, int pcmLen, AudioFormat fmt) throws IOException {
        DataOutputStream dos = new DataOutputStream(os);
        int channels = fmt.getChannels();
        int sampleRate = (int) fmt.getSampleRate();
        int byteRate = sampleRate * channels * fmt.getSampleSizeInBits() / 8;
        int blockAlign = channels * fmt.getSampleSizeInBits() / 8;

        dos.writeBytes("RIFF");
        dos.writeInt(Integer.reverseBytes(36 + pcmLen));
        dos.writeBytes("WAVE");
        dos.writeBytes("fmt ");
        dos.writeInt(Integer.reverseBytes(16));
        dos.writeShort(Short.reverseBytes((short) 1));
        dos.writeShort(Short.reverseBytes((short) channels));
        dos.writeInt(Integer.reverseBytes(sampleRate));
        dos.writeInt(Integer.reverseBytes(byteRate));
        dos.writeShort(Short.reverseBytes((short) blockAlign));
        dos.writeShort(Short.reverseBytes((short) fmt.getSampleSizeInBits()));
        dos.writeBytes("data");
        dos.writeInt(Integer.reverseBytes(pcmLen));
    }
}
