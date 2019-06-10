package com.example.healthy_and_unhealthy_food_recognition;

import android.graphics.Bitmap;

import java.util.List;

public interface Classifier {

    class Recognition {
        /**
         * Declare object I
         */
        public final String id;

        /**
         * Declare object name
         */
        private final String name;

        /**
         * Declare confidence
         */
        public final Float confidence;

        public Recognition(
                final String id, final String name, final Float confidence) {
            this.id = id;
            this.name = name;
            this.confidence = confidence;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public Float getConfidence() {
            return confidence;
        }

        @Override
        public String toString() {
            String resultString = "";
            if (id != null) {
                resultString += "[" + id + "] ";
            }

            if (name != null) {
                resultString +=  name + " ";
            }

            if (confidence != null) {
                resultString += String.format("(%.1f%%) ", confidence * 100.0f);
            }

            return resultString.trim();
        }
    }


    List<Recognition> recognizeImage(Bitmap bitmap);

    void close();
}
