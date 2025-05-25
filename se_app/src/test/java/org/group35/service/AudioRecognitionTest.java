package org.group35.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class AudioRecognitionTest {

    /**
     * Use reflection to invoke the private static buildAudioPrompt() and verify its return value.
     */
    @Test
    void testBuildAudioPrompt() throws Exception {
        // Access the private method via reflection
        var method = AudioRecognition.class.getDeclaredMethod("buildAudioPrompt");
        method.setAccessible(true);

        String prompt = (String) method.invoke(null);
        assertNotNull(prompt, "Prompt should not be null");
        assertTrue(prompt.contains("This audio is about transactions"),
                "Prompt text should match the expected content");
    }

    /**
     * Test getMimeType(File) for different file extensions.
     */
    @Test
    void testGetMimeTypeExtensions() throws Exception {
        var method = AudioRecognition.class.getDeclaredMethod("getMimeType", File.class);
        method.setAccessible(true);

        // WAV file
        File wav = new File("test.wav");
        assertEquals("audio/wav", method.invoke(null, wav));

        // MP3 file (case-insensitive extension)
        File mp3 = new File("song.MP3");
        assertEquals("audio/mpeg", method.invoke(null, mp3));

        // M4A file
        File m4a = new File("voice.m4a");
        assertEquals("audio/mp4", method.invoke(null, m4a));

        // Unknown extension should return null
        File txt = new File("notes.txt");
        assertNull(method.invoke(null, txt), "Unknown extension should return null");
    }

    /**
     * Create a temporary WAV file and verify loadYourWavBytes reads the exact bytes.
     */
    @Test
    void testLoadYourWavBytesSuccess(@TempDir Path tempDir) throws Exception {
        // Create a minimal WAV-like file in the temporary directory
        Path wavFile = tempDir.resolve("sample.wav");
        byte[] data = new byte[]{0x52, 0x49, 0x46, 0x46}; // "RIFF" header bytes
        Files.write(wavFile, data);

        // Read back the bytes and verify
        byte[] loaded = AudioRecognition.loadYourWavBytes(wavFile.toString());
        assertArrayEquals(data, loaded, "Should read back the exact bytes");
    }

    /**
     * Verify that loadYourWavBytes(null) throws NullPointerException.
     */
    @Test
    void testLoadYourWavBytesNullPath() {
        assertThrows(NullPointerException.class, () -> {
            AudioRecognition.loadYourWavBytes(null);
        });
    }

    /**
     * Verify that loadYourWavBytes for a non-existent file throws IOException.
     */
    @Test
    void testLoadYourWavBytesNotExist() {
        String fakePath = "nonexistent_directory/no_file.wav";
        IOException ex = assertThrows(IOException.class, () -> {
            AudioRecognition.loadYourWavBytes(fakePath);
        });
        assertTrue(ex.getMessage().contains("WAV file not found"));
    }

    /**
     * Verify that loadYourWavBytes for an unreadable file throws IOException.
     */
    @Test
    void testLoadYourWavBytesNotReadable(@TempDir Path tempDir) throws Exception {
        Path wavFile = tempDir.resolve("unreadable.wav");
        Files.write(wavFile, new byte[]{0, 1, 2});
        // Make the file not readable
        wavFile.toFile().setReadable(false);

        IOException ex = assertThrows(IOException.class, () -> {
            AudioRecognition.loadYourWavBytes(wavFile.toString());
        });
        assertTrue(ex.getMessage().contains("not readable"));
    }

    /**
     * Simple async failure test for transcribeAsync:
     * Passing empty byte[] should cause the future to complete exceptionally.
     * (May rely on network/API error path.)
     */
    @Test
    void testTranscribeAsyncFailure() throws Exception {
        // Pass an empty byte array to trigger failure
        CompletableFuture<String> future = AudioRecognition.transcribeAsync(new byte[0]);

        try {
            // Wait up to 3 seconds for completion
            future.get(3, TimeUnit.SECONDS);
            fail("Expected the transcription future to complete exceptionally");
        } catch (ExecutionException ee) {
            // OK: onFailure should have been invoked
            assertNotNull(ee.getCause(), "Failure cause should be provided");
        } catch (TimeoutException te) {
            // If it times out, treat as exceptional completion
            assertTrue(future.isCompletedExceptionally(),
                    "Future should be completed exceptionally on timeout");
        }
    }
}
