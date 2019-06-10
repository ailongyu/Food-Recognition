package com.example.healthy_and_unhealthy_food_recognition;

import android.annotation.SuppressLint;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;


public class TensorFlowImageClassifier implements Classifier {
    private static final int Results_number = 4;
    private static final int Batchsize = 1;
    private static final int Pixelsize = 3;
    private static final float Threshold = 0.2f;
    private static final int IMAGE_MEAN = 128;
    private static final float IMAGE_STD = 128.0f;
    private Interpreter interpreter;
    private int inputSize;
    private List<String> labelList;

    private TensorFlowImageClassifier() {

    }
    /**
     *declare a tensorflow classifier
     */
    static Classifier create(AssetManager assetManager,
                             String model_Path,
                             String label_Path,
                             int inputSize
                                ) throws IOException {

        TensorFlowImageClassifier classifier = new TensorFlowImageClassifier();
        classifier.interpreter = new Interpreter(classifier.loadModelFile(assetManager, model_Path), new Interpreter.Options());
        classifier.labelList = classifier.loadLabelList(assetManager, label_Path);
        classifier.inputSize = inputSize;

        return classifier;
    }
    /**
     *declare  method  to predict result
     */
    @Override
    public List<Recognition> recognizeImage(Bitmap bitmap) {
        ByteBuffer byteBuffer = convertToByteBuffer(bitmap);
            float [][] result = new float[1][labelList.size()];
            interpreter.run(byteBuffer, result);
            return getSortedResultFloat(result);
        }

    /**
     *declare close method
     */
    @Override
    public void close() {
        interpreter.close();
        interpreter = null;
    }
    /**
     *declare a method to load model file
     */
    private MappedByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = assetManager.openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long so = fileDescriptor.getStartOffset();
        long d_length = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, so, d_length);
    }
    /**
     *declare a method a load lable list
     */
    private List<String> loadLabelList(AssetManager assetManager, String labelPath) throws IOException {
        List<String> labelList = new ArrayList<>();
        BufferedReader buffereader = new BufferedReader(new InputStreamReader(assetManager.open(labelPath)));
        String labelobject = buffereader.readLine();
        while (labelobject != null) {
            labelList.add(labelobject);
        }
        buffereader.close();
        return labelList;
    }

    private ByteBuffer convertToByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * Batchsize * inputSize * inputSize * Pixelsize);
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[inputSize * inputSize];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        int pixel = 0;
        for (int i = 0; i < inputSize; ++i) {
            for (int j = 0; j < inputSize; ++j) {
                final int val = intValues[pixel++];
                 byteBuffer.putFloat((((val >> 16) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                 byteBuffer.putFloat((((val >> 8) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                 byteBuffer.putFloat((((val) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                }

            }
        return byteBuffer;
    }

    @SuppressLint("DefaultLocale")
    private List<Recognition> getSortedResultFloat(float[][] labelProbArray) {
        PriorityQueue<Recognition> Pq =
                new PriorityQueue<>(
                        Results_number,
                        new Comparator<Recognition>() {
                            @Override
                            public int compare(Recognition lhs, Recognition rhs) {
                                return Float.compare(rhs.getConfidence(), lhs.getConfidence());
                            }
                        });

        for (int i = 0; i < labelList.size(); ++i) {
            float confidence = labelProbArray[0][i];
            if (confidence > Threshold) {
                Pq.add(new Recognition("" + i,
                        labelList.size() > i ? labelList.get(i) : "unknown",
                        confidence));
            }
        }

        final ArrayList<Recognition> recognitions = new ArrayList<>();
        int recognitionsSize = Math.min(Pq.size(), Results_number);
        for (int i = 0; i < recognitionsSize; ++i) {
            recognitions.add(Pq.poll());
        }

        return recognitions;
    }

}
