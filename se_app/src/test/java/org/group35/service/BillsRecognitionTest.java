package org.group35.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.group35.util.ImageUtils;
import org.group35.util.LogUtils;
import org.group35.model.Transaction;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

class BillsRecognitionTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void buildRequestBody() {
    }

    @Test
    void writeDataToJson() {
    }

    @Test
    void imageRecognitionAsyn() {
    }

    public static void main(String[] args) {
        try {
            // Load image and convert to base64 format (simulating the image recognition process)
            byte[] imageData = ImageUtils.loadCompressImage("C:\\Users\\29772\\OneDrive - Queen Mary, University of London\\Pictures\\Screenshots\\屏幕截图 2025-05-23 173712.png"); // Replace with actual image path
            String base64Image = ImageUtils.toBase64(imageData);

            // Simulate user categories


            // Call image recognition asynchronously
            CompletableFuture<Transaction> futureTransaction = BillsRecognition.imageRecognitionAsync(base64Image);

            // Wait for result
            futureTransaction.thenAccept(transaction -> {
                // Handle successful transaction
                LogUtils.debug("Transaction successful: " + transaction);
                // You can process the transaction here, like saving it to a file or database
            }).exceptionally(e -> {
                // Handle failure
                LogUtils.error("Error during transaction recognition: " + e.getMessage());
                return null;
            }).get(); // Blocking call to wait for completion

        } catch (IOException | InterruptedException | ExecutionException e) {
            LogUtils.error("Error during image recognition: " + e.getMessage());
            e.printStackTrace();
        }
    }
}