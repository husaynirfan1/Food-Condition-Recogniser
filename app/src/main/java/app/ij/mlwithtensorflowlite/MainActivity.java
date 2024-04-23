package app.ij.mlwithtensorflowlite;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import app.ij.mlwithtensorflowlite.ml.AppleGoodBad;
import app.ij.mlwithtensorflowlite.ml.BananaGoodBad;
import app.ij.mlwithtensorflowlite.ml.Fruitsonion;
import app.ij.mlwithtensorflowlite.ml.ModelUnquant;
import app.ij.mlwithtensorflowlite.ml.OnionGoodBad;
import app.ij.mlwithtensorflowlite.ml.OrangeGoodBad;


public class MainActivity extends AppCompatActivity {

    public MaterialButton camButton, galleryButton, clasifyButton;
    public ImageView imageFrame;
    int imageSize = 224;
    int SELECT_PICTURE = 211;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preferences = getSharedPreferences(
                "app.ij.mlwithtensorflowlite", Context.MODE_PRIVATE);
        editor = preferences.edit();
        imageFrame = findViewById(R.id.imageFrame);
        camButton = findViewById(R.id.camButton);
        galleryButton = findViewById(R.id.galleryButton);
        clasifyButton = findViewById(R.id.clasifyButton);

        camButton.setOnClickListener(v -> {
                // Launch camera if we have permission
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, 1);
                } else {
                    //Request camera permission if we don't have it.
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
                }

        });
        galleryButton.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
        });
        clasifyButton.setOnClickListener(v -> {
            String name = preferences.getString("Type", "");
            
            if (!name.equalsIgnoreCase("")){
                Intent i = new Intent(MainActivity.this, ClasifyActivity.class);
                startActivity(i);
            } else Toast.makeText(MainActivity.this, "Insert image first.", Toast.LENGTH_SHORT).show();
        });

    }

    public void classifyImage(Bitmap image){
        try {
            /*Fruitsonion model = Fruitsonion.newInstance(getApplicationContext());*/
            ModelUnquant model = ModelUnquant.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            // get 1D array of 224 * 224 pixels in image
            int [] intValues = new int[imageSize * imageSize];
            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());

            // iterate over pixels and extract R, G, and B values. Add to bytebuffer.
            int pixel = 0;
            for(int i = 0; i < imageSize; i++){
                for(int j = 0; j < imageSize; j++){
                    int val = intValues[pixel++]; // RGB
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat((val & 0xFF) * (1.f / 255.f));
                }
            }

            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.

            ModelUnquant.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();
            // find the index of the class with the biggest confidence.
            int maxPos = 0;
            float maxConfidence = 0;
            for(int i = 0; i < confidences.length; i++){
                if(confidences[i] > maxConfidence){
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }
            String[] classes = {"Apple", "Orange", "Banana", "Onion"};

            editor.putString("Type",classes[maxPos]);
            editor.apply();
//            result.setText(classes[maxPos]);

            String s = "";
            for(int i = 0; i < classes.length; i++){
                s += String.format("%s: %.1f%%\n", classes[i], confidences[i] * 100);
            }
//            confidence.setText(s);
            editor.putString("Confidence", s);
            editor.apply();

            determineCondition(classes[maxPos], image);
            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }
    }

    public void determineCondition(String type, Bitmap image){
        switch (type){
            case "Apple":
                try {
                    AppleGoodBad model = AppleGoodBad.newInstance(MainActivity.this);

                    // Creates inputs for reference.
                    TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
                    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
                    byteBuffer.order(ByteOrder.nativeOrder());

                    // get 1D array of 224 * 224 pixels in image
                    int [] intValues = new int[imageSize * imageSize];
                    image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());

                    // iterate over pixels and extract R, G, and B values. Add to bytebuffer.
                    int pixel = 0;
                    for(int i = 0; i < imageSize; i++){
                        for(int j = 0; j < imageSize; j++){
                            int val = intValues[pixel++]; // RGB
                            byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255.f));
                            byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255.f));
                            byteBuffer.putFloat((val & 0xFF) * (1.f / 255.f));
                        }
                    }

                    inputFeature0.loadBuffer(byteBuffer);

                    // Runs model inference and gets result.
                    AppleGoodBad.Outputs outputs = model.process(inputFeature0);
                    TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

                    float[] confidences = outputFeature0.getFloatArray();
                    // find the index of the class with the biggest confidence.
                    int maxPos = 0;
                    float maxConfidence = 0;
                    for(int i = 0; i < confidences.length; i++){
                        if(confidences[i] > maxConfidence){
                            maxConfidence = confidences[i];
                            maxPos = i;
                        }
                    }
                    String[] classes = {"Good", "Bad"};

                    editor.putString("Condition", classes[maxPos]);
                    editor.apply();

                    String s = "";
                    for(int i = 0; i < classes.length; i++){
                        s += String.format("%s: %.1f%%\n", classes[i], confidences[i] * 100);
                    }

                    editor.putString("Condition Confidence", s);
                    editor.apply();

                    // Releases model resources if no longer used.
                    model.close();
                } catch (IOException e){

                }
                break;
            case "Orange":
                try {
                    OrangeGoodBad model = OrangeGoodBad.newInstance(MainActivity.this);

                    // Creates inputs for reference.
                    TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
                    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
                    byteBuffer.order(ByteOrder.nativeOrder());

                    // get 1D array of 224 * 224 pixels in image
                    int [] intValues = new int[imageSize * imageSize];
                    image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());

                    // iterate over pixels and extract R, G, and B values. Add to bytebuffer.
                    int pixel = 0;
                    for(int i = 0; i < imageSize; i++){
                        for(int j = 0; j < imageSize; j++){
                            int val = intValues[pixel++]; // RGB
                            byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255.f));
                            byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255.f));
                            byteBuffer.putFloat((val & 0xFF) * (1.f / 255.f));
                        }
                    }

                    inputFeature0.loadBuffer(byteBuffer);

                    // Runs model inference and gets result.
                    OrangeGoodBad.Outputs outputs = model.process(inputFeature0);
                    TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

                    float[] confidences = outputFeature0.getFloatArray();
                    // find the index of the class with the biggest confidence.
                    int maxPos = 0;
                    float maxConfidence = 0;
                    for(int i = 0; i < confidences.length; i++){
                        if(confidences[i] > maxConfidence){
                            maxConfidence = confidences[i];
                            maxPos = i;
                        }
                    }
                    String[] classes = {"Bad", "Good"};

                    editor.putString("Condition", classes[maxPos]);
                    editor.apply();

                    String s = "";
                    for(int i = 0; i < classes.length; i++){
                        s += String.format("%s: %.1f%%\n", classes[i], confidences[i] * 100);
                    }

                    editor.putString("Condition Confidence", s);
                    editor.apply();

                    // Releases model resources if no longer used.
                    model.close();
                } catch (IOException e){

                }
                break;
            case "Banana":
                try {
                    BananaGoodBad model = BananaGoodBad.newInstance(MainActivity.this);
                    // Creates inputs for reference.
                    TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
                    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
                    byteBuffer.order(ByteOrder.nativeOrder());

                    // get 1D array of 224 * 224 pixels in image
                    int [] intValues = new int[imageSize * imageSize];
                    image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());

                    // iterate over pixels and extract R, G, and B values. Add to bytebuffer.
                    int pixel = 0;
                    for(int i = 0; i < imageSize; i++){
                        for(int j = 0; j < imageSize; j++){
                            int val = intValues[pixel++]; // RGB
                            byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255.f));
                            byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255.f));
                            byteBuffer.putFloat((val & 0xFF) * (1.f / 255.f));
                        }
                    }

                    inputFeature0.loadBuffer(byteBuffer);

                    // Runs model inference and gets result.
                    BananaGoodBad.Outputs outputs = model.process(inputFeature0);
                    TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

                    float[] confidences = outputFeature0.getFloatArray();
                    // find the index of the class with the biggest confidence.
                    int maxPos = 0;
                    float maxConfidence = 0;
                    for(int i = 0; i < confidences.length; i++){
                        if(confidences[i] > maxConfidence){
                            maxConfidence = confidences[i];
                            maxPos = i;
                        }
                    }
                    String[] classes = {"Good", "Bad"};

                    editor.putString("Condition", classes[maxPos]);
                    editor.apply();

                    String s = "";
                    for(int i = 0; i < classes.length; i++){
                        s += String.format("%s: %.1f%%\n", classes[i], confidences[i] * 100);
                    }

                    editor.putString("Condition Confidence", s);
                    editor.apply();

                    // Releases model resources if no longer used.
                    model.close();
                } catch (IOException e){

                }
                break;
            case "Onion":
                try {
                    OnionGoodBad model = OnionGoodBad.newInstance(MainActivity.this);

                    // Creates inputs for reference.
                    TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
                    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
                    byteBuffer.order(ByteOrder.nativeOrder());

                    // get 1D array of 224 * 224 pixels in image
                    int [] intValues = new int[imageSize * imageSize];
                    image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());

                    // iterate over pixels and extract R, G, and B values. Add to bytebuffer.
                    int pixel = 0;
                    for(int i = 0; i < imageSize; i++){
                        for(int j = 0; j < imageSize; j++){
                            int val = intValues[pixel++]; // RGB
                            byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255.f));
                            byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255.f));
                            byteBuffer.putFloat((val & 0xFF) * (1.f / 255.f));
                        }
                    }

                    inputFeature0.loadBuffer(byteBuffer);

                    // Runs model inference and gets result.
                    OnionGoodBad.Outputs outputs = model.process(inputFeature0);
                    TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

                    float[] confidences = outputFeature0.getFloatArray();
                    // find the index of the class with the biggest confidence.
                    int maxPos = 0;
                    float maxConfidence = 0;
                    for(int i = 0; i < confidences.length; i++){
                        if(confidences[i] > maxConfidence){
                            maxConfidence = confidences[i];
                            maxPos = i;
                        }
                    }
                    String[] classes = {"Good", "Bad"};

                    editor.putString("Condition", classes[maxPos]);
                    editor.apply();

                    String s = "";
                    for(int i = 0; i < classes.length; i++){
                        s += String.format("%s: %.1f%%\n", classes[i], confidences[i] * 100);
                    }

                    editor.putString("Condition Confidence", s);
                    editor.apply();

                    // Releases model resources if no longer used.
                    model.close();
                } catch (IOException e){

                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bitmap image = (Bitmap) data.getExtras().get("data");
            int dimension = Math.min(image.getWidth(), image.getHeight());
            image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
            imageFrame.setImageBitmap(image);

            image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
            classifyImage(image);

        }
        if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK) {
            // Get the url of the image from data
            Uri selectedImageUri = data.getData();
            if (null != selectedImageUri) {
                try {
                    // update the preview image in the layout
                    Bitmap image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);

                    int dimension = Math.min(image.getWidth(), image.getHeight());
                    image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
                    imageFrame.setImageBitmap(image);
                    image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                    classifyImage(image);
                } catch (IOException e){
                    Log.d("Gallery", e.toString());
                }

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}