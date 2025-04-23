// src/main/java/org/group35/util/CameraUtils.java
package org.group35.util;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class CameraUtils {

    private Webcam webcam;
    private ScheduledExecutorService executor;
    private final List<Consumer<BufferedImage>>  rawFrameListeners = new CopyOnWriteArrayList<>();
    private final List<Consumer<WritableImage>>  fxFrameListeners  = new CopyOnWriteArrayList<>();
    private final List<Consumer<Exception>>      errorListeners    = new CopyOnWriteArrayList<>();
    private final AtomicBoolean                  previewing        = new AtomicBoolean(false);
    private volatile int                         fps               = 30;

    public CameraUtils() {
        // ensure cleanup if the JVM shuts down
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    /** Initialize the default webcam at VGA resolution (or throw if none). */
    public void initCamera() {
        initCamera(Webcam.getDefault(), WebcamResolution.VGA);
    }

    /** Initialize a specific webcam & resolution. */
    public void initCamera(Webcam cam, WebcamResolution resolution) {
        if (cam == null) {
            throw new IllegalStateException("No webcam available");
        }
        this.webcam = cam;
        webcam.setViewSize(resolution.getSize());
        webcam.open();
    }

    /** Switch among available cameras by index (must be stopped first). */
    public void selectCamera(int index, WebcamResolution res) {
        if (previewing.get()) {
            throw new IllegalStateException("Stop preview before switching cameras");
        }
        Webcam[] cams = Webcam.getWebcams().toArray(new Webcam[0]);
        initCamera(cams[index], res);
    }

    public boolean isOpen()       { return webcam != null && webcam.isOpen(); }
    public boolean isPreviewing() { return previewing.get(); }

    /** Adjust resolution on the fly. */
    public void setResolution(WebcamResolution res) {
        if (isOpen()) {
            webcam.setViewSize(res.getSize());
        }
    }

    /** Adjust target framerate (restarts preview if already running). */
    public void setFPS(int fps) {
        this.fps = fps;
        if (previewing.get()) restartPreview();
    }

    // ─── Listener registration ──────────────────────────────────────

    public void addRawFrameListener(Consumer<BufferedImage> l)   { rawFrameListeners.add(l); }
    public void removeRawFrameListener(Consumer<BufferedImage> l){ rawFrameListeners.remove(l); }

    public void addFXFrameListener(Consumer<WritableImage> l)    { fxFrameListeners.add(l); }
    public void removeFXFrameListener(Consumer<WritableImage> l) { fxFrameListeners.remove(l); }

    public void addErrorListener(Consumer<Exception> l)          { errorListeners.add(l); }
    public void removeErrorListener(Consumer<Exception> l)       { errorListeners.remove(l); }

    // ─── Preview lifecycle ─────────────────────────────────────────

    /** Start pushing frames to all registered listeners. */
    public void startPreview() {
        if (!isOpen()) {
            throw new IllegalStateException("Camera not initialized");
        }
        if (previewing.getAndSet(true)) {
            return; // already running
        }

        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "CameraPreviewThread");
            t.setDaemon(true);
            return t;
        });
        long periodMs = 1000L / fps;

        executor.scheduleAtFixedRate(() -> {
            if (!previewing.get()) return;
            try {
                BufferedImage raw = webcam.getImage();
                if (raw == null) return;

                // notify raw listeners
                rawFrameListeners.forEach(l -> l.accept(raw));

                // convert once, then notify FX listeners on the FX thread
                if (!fxFrameListeners.isEmpty()) {
                    WritableImage fxImg = SwingFXUtils.toFXImage(raw, null);
                    Platform.runLater(() -> fxFrameListeners.forEach(l -> l.accept(fxImg)));
                }

            } catch (Exception e) {
                errorListeners.forEach(l -> l.accept(e));
            }
        }, 0, periodMs, TimeUnit.MILLISECONDS);
    }

    /** Temporarily pause pushing frames (camera stays open). */
    public void pausePreview() {
        previewing.set(false);
    }

    /** Resume pushing frames again. */
    public void resumePreview() {
        if (!isOpen()) throw new IllegalStateException("Camera not initialized");
        previewing.set(true);
    }

    /** Fully stop and tear down the preview executor (camera stays open). */
    public void stopPreview() {
        previewing.set(false);
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }

    /** Convenience to stop & immediately restart preview (e.g. after FPS change). */
    public void restartPreview() {
        stopPreview();
        startPreview();
    }

    /** Capture exactly one frame asynchronously on its own thread. */
    public void captureFrame(Consumer<BufferedImage> callback) {
        Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "CameraCaptureThread");
            t.setDaemon(true);
            return t;
        }).submit(() -> {
            try {
                if (!isOpen()) throw new IllegalStateException("Camera not initialized");
                BufferedImage raw = webcam.getImage();
                callback.accept(raw);
            } catch (Exception e) {
                errorListeners.forEach(l -> l.accept(e));
            }
        });
    }

    /** Clean up everything: stop preview, close camera, shut down executors. */
    public void shutdown() {
        stopPreview();
        if (isOpen()) {
            webcam.close();
            webcam = null;
            LogUtils.info("Webcam closed in shutdown");
        }
    }
}
