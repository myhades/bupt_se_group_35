package org.group35.util;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Utility for controlling the webcam: live preview and snapshot capture,
 * with error handling and controlled logging via LogUtils.
 */
public class CameraUtils {
    static {
        // Suppress OpenCV native logs by temporarily redirecting stderr
        PrintStream originalErr = System.err;
        try {
            System.setErr(new PrintStream(new OutputStream() {
                @Override
                public void write(int b) { /* ignore */ }
            }));
            OpenCV.loadLocally();
        } catch (Exception e) {
            LogUtils.error("Failed to load OpenCV native library");
            throw new RuntimeException(e);
        } finally {
            System.setErr(originalErr);
        }
    }

    private final VideoCapture capture = new VideoCapture();
    private ScheduledExecutorService executor;
    private volatile boolean cameraActive = false;

    /**
     * Starts camera preview in the given ImageView at ~30 FPS.
     */
    public synchronized void startCamera(ImageView view) {
        if (cameraActive) {
            LogUtils.warn("Camera already active, startCamera() ignored");
            return;
        }
        try {
            if (!capture.open(0)) {
                LogUtils.error("Cannot open camera device (it may be in use)");
                return;
            }
        } catch (Exception e) {
            LogUtils.error("Exception opening camera device");
            return;
        }
        cameraActive = true;
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            try {
                Mat frame = new Mat();
                if (capture.read(frame) && !frame.empty()) {
                    Image img = matToImage(frame);
                    Platform.runLater(() -> view.setImage(img));
                }
            } catch (Exception e) {
                LogUtils.error("Error reading frame from camera");
                stopCamera();
            }
        }, 0, 33, TimeUnit.MILLISECONDS);
        LogUtils.info("Camera preview has started");
    }

    /**
     * Stops camera preview and releases resources.
     */
    public synchronized void stopCamera() {
        if (!cameraActive) return;
        cameraActive = false;
        try {
            if (executor != null && !executor.isShutdown()) {
                executor.shutdownNow();
            }
            capture.release();
            LogUtils.debug("Camera preview stopped and device released");
        } catch (Exception e) {
            LogUtils.error("Exception occurred when shutting down camera");
        }
    }

    /**
     * Captures a snapshot and returns it as a JavaFX Image.
     * @return Image of the captured frame, or null if failed
     */
    public Image captureSnapshot() {
        try {
            Mat frame = new Mat();
            if (capture.read(frame) && !frame.empty()) {
                Image img = matToImage(frame);
                LogUtils.info("Snapshot captured");
                return img;
            } else {
                LogUtils.error("Failed to capture snapshot: no frame data");
            }
        } catch (Exception e) {
            LogUtils.error("Exception occurred when capturing snapshot");
        }
        return null;
    }

    /**
     * Cleanly shutdown the camera (alias for stopCamera()).
     */
    public void shutdown() {
        stopCamera();
    }

    /**
     * Convert OpenCV Mat to JavaFX Image.
     */
    private Image matToImage(Mat frame) {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", frame, buffer);
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }
}