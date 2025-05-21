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
import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Utility for controlling the webcam: live preview and snapshot capture.
 */
public class CameraUtils {
    static {
        // Load OpenCV library locally to avoid SharedLoader issues on Java >= 12
        try {
            OpenCV.loadLocally();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load OpenCV native library", e);
        }
    }

    private final VideoCapture capture = new VideoCapture();
    private ScheduledExecutorService executor;
    private boolean cameraActive = false;

    /**
     * Starts camera preview in the given ImageView at ~30 FPS.
     */
    public void startCamera(ImageView view) {
        if (cameraActive) return;
        capture.open(0);
        if (!capture.isOpened()) {
            System.err.println("Failed to open camera");
            return;
        }
        cameraActive = true;
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            Mat frame = new Mat();
            if (capture.read(frame)) {
                Image img = matToImage(frame);
                Platform.runLater(() -> view.setImage(img));
            }
        }, 0, 33, TimeUnit.MILLISECONDS);
    }

    /**
     * Stops camera preview and releases resources.
     */
    public void stopCamera() {
        if (!cameraActive) return;
        cameraActive = false;
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
        capture.release();
    }

    /**
     * Captures a single frame and writes it to the given file path.
     * @return File object if successful, null otherwise
     */
    public File capturePhoto(Path outputPath) {
        Mat frame = new Mat();
        if (capture.read(frame)) {
            Imgcodecs.imwrite(outputPath.toString(), frame);
            return outputPath.toFile();
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
