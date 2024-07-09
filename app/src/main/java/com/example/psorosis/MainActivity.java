package com.example.psorosis;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.psorosis.ml.Model;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MainActivity extends AppCompatActivity {

    Button camera, gallery;
    ImageView imageview;
    TextView result, more;
    int imageSize = 224;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        camera = findViewById(R.id.button1);
        gallery = findViewById(R.id.button2);

        result = findViewById(R.id.textviewid);
        more = findViewById(R.id.more_info_id);

        imageview = findViewById(R.id.imageviewid);

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent, 3);
                    }
                    else{
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
                    }
                }
            }
        });
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cameraIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(cameraIntent, 1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == RESULT_OK){
            if (requestCode == 3){
                Bitmap image = (Bitmap) data.getExtras().get("data");
                int dimension = Math.min(image.getWidth(), image.getHeight());
                image = ThumbnailUtils.extractThumbnail(image,dimension,dimension);
                imageview.setImageBitmap(image);

                image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                classifyImage(image);
            }
            else{
                Uri dat = data.getData();
                Bitmap image =  null;
                try {
                    image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), dat);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imageview.setImageBitmap(image);

                image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                classifyImage(image);

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void classifyImage(Bitmap image) {
        try {
            Model model = Model.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize* 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] intValues = new int[imageSize * imageSize];
            image.getPixels(intValues, 0, image.getWidth(),0,0,image.getWidth(),image.getHeight());
            int pixel = 0;

            for(int i = 0; i < imageSize; i++) {
                for(int j=0;j < imageSize; j++){
                    int val = intValues[pixel++];
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f/1));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f/1));
                    byteBuffer.putFloat((val & 0xFF) * (1.f/1));
                }
            }

            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            Model.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();

            int maxpos = 0;
            float maxconfidence = 0;
            for(int i = 0; i < confidences.length; i++){
                if(confidences[i] > maxconfidence){
                    maxconfidence = confidences[i];
                    maxpos = i;
                }
            }

            String[] classes = {"Anthracnose Disease Found","Leaves Curl Disease Found","No Disease Found"};
            result.setText(classes[maxpos]);

            if(maxpos == 0){
                more.setVisibility(View.VISIBLE);
                Intent more_info = new Intent(this,MoreInfo.class);
                more.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(more_info);
                        overridePendingTransition(R.anim.left_slide_in,R.anim.right_slide_out);
                    }
                });
            }
            else if(maxpos == 1){
                more.setVisibility(View.VISIBLE);
                Intent more_info = new Intent(this,Leave_curl.class);
                more.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(more_info);
                        overridePendingTransition(R.anim.left_slide_in,R.anim.right_slide_out);
                    }
                });
            }
            else{
                more.setVisibility(View.GONE);
            }

            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }
    }
}