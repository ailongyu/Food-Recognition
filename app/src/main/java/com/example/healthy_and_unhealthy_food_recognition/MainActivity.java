package com.example.healthy_and_unhealthy_food_recognition;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

//import android.util.Log;

public class MainActivity extends AppCompatActivity {
    private static final String MODEL_PATH = "food_mobilenetV2_model_best.tflite";
    private static final String LABEL_PATH = "labels.txt";
    private static final int INPUT_SIZE = 224;
    public final static String[] HEALTHY  = {"Granny_Smith", "foie_gras", "club_sandwich", "cheese_plate", "strawberry", "garlic_bread", "gnocchi", "bell_pepper", "pomegranate", "tuna_tartare", "spinach", "seaweed_salad", "french_toast", "chicken_curry", "bagel_ beigel", "gyoza", "lemon", "lobster_roll_sandwich", "huevos_rancheros", "breakfast_burrito", "grilled_cheese_sandwich", "onion", "falafel", "potato", "greek_salad", "beef_tartare", "guacamole", "cucumber", "lobster_bisque", "beet_salad", "head_cabbage", "dough", "edamame", "ceviche", "hot_and_sour_soup", "sashimi", "green_pepper", "clam_chowder", "miso_soup", "escargots", "bruschetta", "rape", "watermelon", "cabbage", "scallops", "frozen_yogurt", "chicken_quesadilla", "mussels", "beef_carpaccio", "eggs_benedict", "eggplant", "omelette", "cheeseburger", "corn", "sushi", "pizza_pizza pie", "bibimbap", "broccoli", "red_pepper", "hummus", "grilled_salmon", "celery", "banana", "taro", "macaroni_and_cheese", "ramen", "chinese_cabbage", "pineapple", "croque_madame", "deviled_eggs", "dumplings", "fried_rice", "orange", "carrot", "pho", "caprese_salad", "oysters", "peach", "caesar_salad"};
    public final static String[] UNHEALTHY = {"cup_cakes", "ice_cream", "samosa", "donuts", "filet_mignon", "shrimp_and_grits", "steak", "cheesecake", "red_velvet_cake", "waffles", "churros", "spaghetti_bolognese", "poutine", "fried_calamari", "ravioli", "risotto", "crab_cakes", "strawberry_shortcake", "spring_rolls", "paella", "hot_dog", "pulled_pork_sandwich", "panna_cotta", "fish_and_chips", "pad_thai", "tiramisu", "takoyaki", "macarons", "apple_pie", "spaghetti_carbonara", "chocolate_mousse", "beignets", "pork_chop", "chicken_wings", "chocolate_cake", "tacos", "hamburger", "baby_back_ribs", "pancakes", "prime_rib", "pizza", "nachos", "bread_pudding", "lasagna", "peking_duck", "french_fries", "pretzel", "french_onion_soup", "baklava", "creme_brulee", "carrot_cake", "onion_rings" } ;
    private Classifier classifier;
    private Executor executor = Executors.newSingleThreadExecutor();
    private TextView textViewResult;
    private ImageView imageViewResult;
    private CameraView cameraView;
    private Button btnAlbum;
    private Button btnCamera;
    private ImageView albumViewresult;
    private String result0 = "";
    private static final int CODE_PICK = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraView = findViewById(R.id.cameraView);
        imageViewResult = findViewById(R.id.imageViewResult);
        textViewResult = findViewById(R.id.textViewResult);
        albumViewresult = findViewById(R.id.albumViewResult);
        textViewResult.setMovementMethod(new ScrollingMovementMethod());
        btnAlbum = findViewById(R.id.btnAlbum);
        btnCamera = findViewById(R.id.btnCamera);
        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            /**
             *declare app life cycle activities
             */
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {
                String foodresult;
                Bitmap bitmap = cameraKitImage.getBitmap();

                bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);

                imageViewResult.setImageBitmap(bitmap);

                final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);
                foodresult = detect(results.get(0).getName());
                String results1 = results.get(0).toString().concat(foodresult);
                textViewResult.setText(results1);

            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });

        btnAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, CODE_PICK);
            }
        });
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.captureImage();
                albumViewresult.setVisibility(View.INVISIBLE);
                cameraView.setVisibility(View.VISIBLE);

            }
        });
        initTensorFlowAndLoadModel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        cameraView.stop();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                classifier.close();
            }
        });
    }
    /**
     * Declare detect method to check if the food is healthy or not
     */
    public String detect(String foodname) {
        String result = new String();
        if(isInArray(HEALTHY,foodname))
            result = "Healthy Food, Congratulations！！！！！!";
        else if(isInArray(UNHEALTHY, foodname))
            result = "Unhealthy Food, Be Careful！！！！！！";
        return result;
    }
    /**
     * Declare isInArray method so that it can check if it is in the foood list
     */
    private static final boolean isInArray(final String [] foodlist, String foodname) {
        boolean temp = false;
        final int size = foodlist.length;
        for(int i = 0; i < size && !temp; i++) {
            temp = foodname.equalsIgnoreCase(foodlist[i]);
        }
        return temp;
    }
    /**
     * initialize TensorFlow model
     */
    private void initTensorFlowAndLoadModel() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    classifier = TensorFlowImageClassifier.create(
                            getAssets(),
                            MODEL_PATH,
                            LABEL_PATH,
                            INPUT_SIZE
                            );
                    makeButtonVisible();
                } catch (final Exception e) {
                    throw new RuntimeException("initializing TensorFlow Error!", e);
                }
            }
        });
    }
    /**
     *Loads a Bitmap from outer URI
     */
    private Bitmap loadBitmapFromUri(Uri uri) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, options);
            int propersize = 1;
            while (options.outWidth / (2 * propersize) >= imageViewResult.getWidth() &&
                    options.outHeight / (2 * propersize) >= imageViewResult.getHeight()) {
                propersize *= 2;
            }
            options = new BitmapFactory.Options();
            options.inSampleSize = propersize;
            return BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, options);
        } catch (IOException e) {

        }
        return null;
    }
    /**
     *accept intent of pictures from album and give result
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        String foodresult;
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == CODE_PICK && resultCode == RESULT_OK) {
            Bitmap bitmap = loadBitmapFromUri(intent.getData());
            bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);
            if (bitmap != null) {
                albumViewresult.setImageBitmap(bitmap);
                imageViewResult.setImageBitmap(bitmap);
                cameraView.setVisibility(View.GONE);
                albumViewresult.setVisibility(View.VISIBLE);
                imageViewResult.setVisibility(View.VISIBLE);
                final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);
                float probility = results.get(0).getConfidence();
                int i = 0;
                if (probility < 0.85) {
                    for (Classifier.Recognition temp: results ) {
                        i = i + 1;
                        String id;
                        id = "[" + i + "]";
                        String name = temp.getName();
                        String cf = String.format("(%.1f%%) ", temp.confidence * 100.0f);
                        String tempresult;
                        tempresult = id + " " +  name + cf;
                        result0 += tempresult + "\n";
                    }
                    String string1 = result0 + detect(results.get(0).getName());
                    String.format("%-10s", string1);
                    textViewResult.setText(string1);
                    result0 = "";

                } else {
                    foodresult = detect(results.get(0).getName());
                    String name = results.get(0).getName();
                    String cf = String.format("(%.1f%%) ", results.get(0).confidence * 100.0f);
                    String tempresult = name + cf;
                    String results1 = tempresult.concat("\n"+foodresult);
                    String.format("%-10s", results1);
                    textViewResult.setText(results1);
                }
            }
        }}

    /**
     *make camera button visible
     */
    private void makeButtonVisible () {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnCamera.setVisibility(View.VISIBLE);
            }
        });
    }
}
