package com.example.pecodetesttask;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.widget.Toast;

import com.example.pecodetesttask.databinding.ActivityMainBinding;
import com.example.pecodetesttask.image_editor.ImageEditorActivity;
import com.google.common.util.concurrent.ListenableFuture;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivityWithoutSystemUI {

    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private ActivityMainBinding binding;
    private boolean isCapturing = false;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startCamera();
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.on_camera_disalowed_toast),
                        Toast.LENGTH_LONG
                    ).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.cameraCaptureButton.setOnClickListener(view -> capture());
        cameraExecutor = Executors.newSingleThreadExecutor();
        requestPermissions();
    }

    private void capture() {
        if (imageCapture == null || isCapturing) {
            return;
        }

        isCapturing = true;
        imageCapture.takePicture(
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy image) {
                        super.onCaptureSuccess(image);
                        isCapturing = false;
                        openEditor(image);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        super.onError(exception);
                        isCapturing = false;
                    }
                }
        );
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(
            () -> {
                ProcessCameraProvider cameraProvider;
                try {
                    cameraProvider = cameraProviderFuture.get();
                    Preview preview = new Preview.Builder().build();
                    imageCapture = new ImageCapture.Builder().build();
                    preview.setSurfaceProvider(binding.viewFinder.getSurfaceProvider());
                    CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                    cameraProvider.unbindAll();
                    cameraProvider.bindToLifecycle(
                            this,
                            cameraSelector,
                            preview,
                            imageCapture
                    );
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            },
            ContextCompat.getMainExecutor(this)
        );
    }

    @OptIn(markerClass = androidx.camera.core.ExperimentalGetImage.class)
    private void openEditor(@NonNull ImageProxy image) {
        Intent openEditorIntent = new Intent(this, ImageEditorActivity.class);
        ImageEditorActivity.imageBytes = imageToByteArray(image);
        startActivity(openEditorIntent);
    }

    private void requestPermissions() {
        int permissionCheck =
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        } else {
            startCamera();
        }
    }

    @OptIn(markerClass = androidx.camera.core.ExperimentalGetImage.class)
    private byte[] imageToByteArray(@NonNull ImageProxy imageProxy) {
        Image image = imageProxy.getImage();
        if (image == null) {
            return null;
        }
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer imageYPlane = planes[0].getBuffer();
        byte[] bytes = new byte[imageYPlane.remaining()];
        imageYPlane.get(bytes);
        return bytes;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }

}