package m.tri.trafficsignrecognition.activity;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TimingLogger;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import m.tri.trafficsignrecognition.R;
import m.tri.trafficsignrecognition.activity.ui.FaceView;
import m.tri.trafficsignrecognition.adapter.ImagePreviewAdapter;
import m.tri.trafficsignrecognition.model.FaceResult;
import m.tri.trafficsignrecognition.tsr.surf.ImgMatrixFormat;
import m.tri.trafficsignrecognition.tsr.surf.Surf;
import m.tri.trafficsignrecognition.tsr.system.TSRSystem;
import m.tri.trafficsignrecognition.utils.ImageUtils;


/**
 * Created by Nguyen on 5/20/2016.
 */

/**
 * Demonstrates basic usage of the GMS vision face detector by running face landmark detection on a
 * photo and displaying the photo with associated landmarks in the UI.
 */
public class PhotoDetectActivity extends AppCompatActivity {

    private static final String TAG = PhotoDetectActivity.class.getSimpleName();

    private static final int RC_HANDLE_WRITE_EXTERNAL_STORAGE_PERM = 3;
    private static int PICK_IMAGE_REQUEST = 5;
    private FaceView faceView;
    private RecyclerView recyclerView;
    private ImagePreviewAdapter imagePreviewAdapter;
    private ArrayList<Bitmap> facesBitmap;

    ArrayList<FaceResult> detectedSignList;

    private static final int MAX_FACE = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_viewer);

        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Face Detect Image");

        faceView = (FaceView) findViewById(R.id.faceView);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());


//        System.out.println("permission = " + checkCallingOrSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE));
//
//        /*
//        File imgFile = new File(Environment.getExternalStorageDirectory(),"DCIM/Camera/20150811_195814.jpg");
//
//        if(imgFile.exists())
//        {
//            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
//            //ImageView myImage = (ImageView) findViewById(R.id.imageView1);
//           // myImage.setImageBitmap(myBitmap);
//        }
//        else
//            Toast.makeText(this,"no IMAGE IS PRESENT'",Toast.LENGTH_SHORT).show();
//        */
//
//        /*
//        File downloadsFolder= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//
//        if(downloadsFolder.exists())
//        {
//            //GET ALL FILES IN DOWNLOAD FOLDER
//            File[] files=downloadsFolder.listFiles();
//
//            //LOOP THRU THOSE FILES GETTING NAME AND URI
//            for (int i=0;i<files.length;i++)
//            {
//                File file=files[i];
//
//                System.out.println("File name = " + file.getName() + " File URI = " + Uri.fromFile(file));
//
//                if(file.getName() == "0_teste_surf.png")
//                {
//                    System.out.println("Tentando abrir imagem 0_teste_surf");
//
//                    Bitmap bitmap = ImageUtils.getBitmap(ImageUtils.getRealPathFromURI(this, Uri.fromFile(file)), 200, 200);
//                }
//
//            }
//
//        }
//        else
//            Toast.makeText(this,"no IMAGE IS PRESENT'",Toast.LENGTH_SHORT).show();
//
//        */
//        // Celular
//        String sdCardPath = "storage/sdcard1/TCC-TSR-2017/SURF implementation/ntl1.bmp"; //"storage/sdcard1/TCC-TSR-2017/SURF implementation/ntl1.bmp";
//
//        // Tablet
//        //String sdCardPath = "storage/emulated/0/TCC-TSR-2017/SURF implementation/ntl1.bmp";
//
//        System.out.println("tablet path for img:" + Environment.getExternalStorageDirectory().getPath());
//
//        System.out.println("sdCardPath:" + sdCardPath);
//
//        //sdCardPath =  "storage/sdcard2/Download/0_teste_surf.png";
//
//        File f = new File(sdCardPath);
//        Bitmap bmpSD = BitmapFactory.decodeFile(f.getAbsolutePath());
//
//        int pixelColor = bmpSD.getPixel(0,0);
//        int a1 = (pixelColor >> 24) & 0xff; // or color >>> 24
//        int r1 = (pixelColor >> 16) & 0xff;
//        int g1 = (pixelColor >>  8) & 0xff;
//        int b1 = (pixelColor      ) & 0xff;
//
//        System.out.println("Cor do pixel: 0,0");
//        System.out.println("R:" + r1);
//        System.out.println("G:" + g1);
//        System.out.println("B:" + b1);
//
//        pixelColor = bmpSD.getPixel(1,0);
//        a1 = (pixelColor >> 24) & 0xff; // or color >>> 24
//        r1 = (pixelColor >> 16) & 0xff;
//        g1 = (pixelColor >>  8) & 0xff;
//        b1 = (pixelColor      ) & 0xff;
//
//        System.out.println("Cor do pixel: 1,0");
//        System.out.println("R:" + r1);
//        System.out.println("G:" + g1);
//        System.out.println("B:" + b1);
//
//        pixelColor = bmpSD.getPixel(2,0);
//        a1 = (pixelColor >> 24) & 0xff; // or color >>> 24
//        r1 = (pixelColor >> 16) & 0xff;
//        g1 = (pixelColor >>  8) & 0xff;
//        b1 = (pixelColor      ) & 0xff;
//
//        System.out.println("Cor do pixel: 2,0");
//        System.out.println("R:" + r1);
//        System.out.println("G:" + g1);
//        System.out.println("B:" + b1);
//
//        Bitmap bmp2 = bmpSD.copy(bmpSD.getConfig(),true);
//
//        //storeImage(bmp2);
//
//        // DESENHAR CIRCULOS
//
////        Canvas canvas;
////
////        Bitmap bmOverlay = Bitmap.createBitmap(bmpSD.getWidth(), bmpSD.getHeight(), bmpSD.getConfig());
////        canvas = new Canvas(bmOverlay);
////        Paint paint = new Paint();
////        //paint.set(Color.RED);
////        canvas.drawBitmap(bmpSD, new Matrix(), null);
////        //canvas.drawCircle(50, 50, 25, paint);
////
////        paint.setColor(Color.GREEN);
////        paint.setStrokeWidth(2);
////        paint.setStyle(Paint.Style.STROKE);
////        canvas.drawCircle(50, 50, 25, paint);
//
//        detectedSignList = new ArrayList<>();
//
//        ImgMatrixFormat testImgMatrixFormat = new ImgMatrixFormat(200,200);
//
//        testImgMatrixFormat.ImgMatrix[0][0] = 1;
//        testImgMatrixFormat.ImgMatrix[0][1] = 2;
//        testImgMatrixFormat.ImgMatrix[0][2] = 3;
//        testImgMatrixFormat.ImgMatrix[0][3] = 4;
//        testImgMatrixFormat.ImgMatrix[0][4] = 5;
//
//        System.out.println("testImgMatrixFormat.ImgMatrix[0][2]: " + testImgMatrixFormat.ImgMatrix[0][2]);
//
//        showImg(bmpSD);
//
//        Surf surfObj = new Surf(this,bmpSD);
//
//        //File f = new File("/mnt/sdcard/download/esdras_chicago_hancok.jpg");
//        //Bitmap bmpSD = BitmapFactory.decodeFile(f.getAbsolutePath());
//        //detectFace(bmpSD);
//
//
//
//        /*
//        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        if (rc == PackageManager.PERMISSION_GRANTED) {
//            getImage();
//        } else {
//            requestWriteExternalPermission();
//        }
//        */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_photo, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;

            case R.id.gallery:

                int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (rc == PackageManager.PERMISSION_GRANTED) {
                    getImage();
                } else {
                    requestWriteExternalPermission();
                }

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        resetData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            //Bitmap bitmap = ImageUtils.getBitmap(ImageUtils.getRealPathFromURI(this, uri), 2048, 1232);

            Bitmap bitmap = ImageUtils.getBitmap(ImageUtils.getRealPathFromURI(this, uri), 2048, 1232);
            if (bitmap != null)
                detectFace(bitmap);
            else
                Toast.makeText(this, "Cann't open this image.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != RC_HANDLE_WRITE_EXTERNAL_STORAGE_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Write External permission granted");
            // we have permission
            getImage();
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));
    }

    public void getImage() {
        // Create intent to Open Image applications like Gallery, Google Photos
        try {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            // Start the Intent
            startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
        } catch (ActivityNotFoundException i) {
            Toast.makeText(PhotoDetectActivity.this, "Your Device can not select image from gallery.", Toast.LENGTH_LONG).show();
            finish();
        }
    }


    private void detectFace(Bitmap bitmap) {
        resetData();

        // Debug

        int pixelColor = bitmap.getPixel(0,0);
        int a1 = (pixelColor >> 24) & 0xff; // or color >>> 24
        int r1 = (pixelColor >> 16) & 0xff;
        int g1 = (pixelColor >>  8) & 0xff;
        int b1 = (pixelColor      ) & 0xff;

        System.out.println("Cor do pixel:");
        System.out.println("R:" + r1);
        System.out.println("G:" + g1);
        System.out.println("B:" + b1);

        // Debug

        android.media.FaceDetector fdet_ = new android.media.FaceDetector(bitmap.getWidth(), bitmap.getHeight(), MAX_FACE);

        android.media.FaceDetector.Face[] fullResults = new android.media.FaceDetector.Face[MAX_FACE];
        fdet_.findFaces(bitmap, fullResults);

        ArrayList<FaceResult> faces_ = new ArrayList<>();


        for (int i = 0; i < MAX_FACE; i++) {
            if (fullResults[i] != null) {
                PointF mid = new PointF();
                fullResults[i].getMidPoint(mid);

                float eyesDis = fullResults[i].eyesDistance();
                float confidence = fullResults[i].confidence();
                float pose = fullResults[i].pose(android.media.FaceDetector.Face.EULER_Y);

                Rect rect = new Rect(
                        (int) (mid.x - eyesDis * 1.20f),
                        (int) (mid.y - eyesDis * 0.55f),
                        (int) (mid.x + eyesDis * 1.20f),
                        (int) (mid.y + eyesDis * 1.85f));

                /**
                 * Only detect face size > 100x100
                 */
                if (rect.height() * rect.width() > 100 * 100) {
                    FaceResult faceResult = new FaceResult();
                    faceResult.setFace(0, mid, eyesDis, confidence, pose, System.currentTimeMillis());
                    faces_.add(faceResult);

                    //
                    // Crop Face to display in RecylerView
                    //
                    Bitmap cropedFace = ImageUtils.cropFace(faceResult, bitmap, 0);
                    if (cropedFace != null) {
                        imagePreviewAdapter.add(cropedFace);
                    }
                }
            }
        }

        FaceView overlay = (FaceView) findViewById(R.id.faceView);
        overlay.setContent(bitmap, faces_);
    }


    private void requestWriteExternalPermission() {
        Log.w(TAG, "Write External permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_WRITE_EXTERNAL_STORAGE_PERM);
    }

    private void resetData() {

        if (imagePreviewAdapter == null) {
            facesBitmap = new ArrayList<>();
            imagePreviewAdapter = new ImagePreviewAdapter(PhotoDetectActivity.this, facesBitmap, new ImagePreviewAdapter.ViewHolder.OnItemClickListener() {
                @Override
                public void onClick(View v, int position) {
                    imagePreviewAdapter.setCheck(position);
                    imagePreviewAdapter.notifyDataSetChanged();
                }
            });
            recyclerView.setAdapter(imagePreviewAdapter);
        } else {
            imagePreviewAdapter.clearAll();
        }

        faceView.reset();
    }

    private void storeImage(Bitmap image) {
        File pictureFile = getOutputMediaFile();
        if (pictureFile == null) {
            Log.d(TAG,
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            System.out.println("Error creating media file, check storage permissions");
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
            System.out.println("salvo?");
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
            System.out.println("File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
            System.out.println("Error accessing file: " + e.getMessage());
        }
    }

    /** Create a File for saving an image or video */
    private  File getOutputMediaFile(){

        File mediaFile;

        // Celular
        mediaFile = new File("storage/sdcard1/TCC-TSR-2017/SURF implementation/saveImgTeste2.png");

        // Tablet
        //mediaFile = new File("storage/emulated/0/TCC-TSR-2017/SURF implementation/saveImgTeste2.png");

        return mediaFile;
    }

    public void showImg(Bitmap img)
    {
        FaceView overlay = (FaceView) findViewById(R.id.faceView);
        overlay.setContent(img, detectedSignList);
    }


    /*
    private File savebitmap(String filename) {
        String extStorageDirectory = "storage/sdcard1/TCC-TSR-2017/SURF implementation"; //Environment.getExternalStorageDirectory().toString();
        OutputStream outStream = null;

        File file = new File(filename + ".png");
        if (file.exists()) {
            file.delete();
            file = new File(extStorageDirectory, filename + ".png");
            Log.e("file exist", "" + file + ",Bitmap= " + filename);
        }
        try {
            // make a new bitmap from your file
            Bitmap bitmap = BitmapFactory.decodeFile(file.getName());

            outStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("file", "" + file);
        return file;

    }
    */
}
