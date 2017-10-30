package m.tri.trafficsignrecognition;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TimingLogger;
import android.view.View;
import android.widget.Button;

import java.io.File;

import m.tri.trafficsignrecognition.activity.FaceDetectGrayActivity;
import m.tri.trafficsignrecognition.activity.TSRSystemRGBActivity;
import m.tri.trafficsignrecognition.activity.PhotoDetectActivity;
import m.tri.trafficsignrecognition.tsr.system.RectangleData;
import m.tri.trafficsignrecognition.tsr.system.TSRSystem;

/**
 * Created by Nguyen on 5/20/2016.
 */

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    private static final int RC_HANDLE_CAMERA_PERM_RGB = 1;
    private static final int RC_HANDLE_CAMERA_PERM_GRAY = 2;

    private Context mContext;

    Bitmap bmpSD;
    TSRSystem tsrSystem;
    TimingLogger timings;
    File f;

    // For execution time measurement
    long startnow;
    long endnow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        String sdCardPath = "storage/sdcard1/TCC-TSR-2017/SURF implementation/pare6.bmp";
        f = new File(sdCardPath);

        // Find the interest regions
        tsrSystem = new TSRSystem();

        Button btnCameraRGB = (Button) findViewById(R.id.btnRGB);
        btnCameraRGB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int rc = ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA);
                if (rc == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(mContext, TSRSystemRGBActivity.class);
                    startActivity(intent);
                } else {
                    requestCameraPermission(RC_HANDLE_CAMERA_PERM_RGB);
                }
            }
        });

        Button btnCameraGray = (Button) findViewById(R.id.btnGray);
        btnCameraGray.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int rc = ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA);
                if (rc == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(mContext, FaceDetectGrayActivity.class);
                    startActivity(intent);
                } else {
                    requestCameraPermission(RC_HANDLE_CAMERA_PERM_GRAY);
                }
            }
        });

        Button btnPhoto = (Button) findViewById(R.id.btnImage);
        btnPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                System.out.print("Loading input image for detection...");
                startnow = android.os.SystemClock.uptimeMillis();
                tsrSystem.loadInputImageFromBmp(f);
                endnow = android.os.SystemClock.uptimeMillis();
                Log.d("Loading input image", "Execution time: " + (endnow - startnow) + " ms");
                System.out.println("Done");

                System.out.print("Finding interest regions...");
                startnow = android.os.SystemClock.uptimeMillis();
                RectangleData[] interestRegions = tsrSystem.findInterestRegions();
                endnow = android.os.SystemClock.uptimeMillis();
                Log.d("Find interest regions", "Execution time: " + (endnow - startnow) + " ms");
                System.out.println("Done");

                tsrSystem.recognizeSignWithinRIO(interestRegions);

//                System.out.print("Save input frame img...");
//                startnow = android.os.SystemClock.uptimeMillis();
//                tsrSystem.saveInputFramePixelsAsBmp();
//                endnow = android.os.SystemClock.uptimeMillis();
//                Log.d("Save input frame", "Execution time: " + (endnow - startnow) + " ms");
//                System.out.println("Done");
//
//                System.out.print("Save binary img...");
//                startnow = android.os.SystemClock.uptimeMillis();
//                tsrSystem.saveBinaryImgPixels();
//                endnow = android.os.SystemClock.uptimeMillis();
//                Log.d("Save binary img", "Execution time: " + (endnow - startnow) + " ms");
//                System.out.println("Done");
//
//                System.out.print("Save erosion img...");
//                startnow = android.os.SystemClock.uptimeMillis();
//                tsrSystem.saveErosionImgPixelsAsBmp();
//                endnow = android.os.SystemClock.uptimeMillis();
//                Log.d("Save erosion img", "Execution time: " + (endnow - startnow) + " ms");
//                System.out.println("Done");
//
//                System.out.print("Save dilation img...");
//                startnow = android.os.SystemClock.uptimeMillis();
//                tsrSystem.saveDilationImgPixelsAsBmp();
//                endnow = android.os.SystemClock.uptimeMillis();
//                Log.d("Save dilation img", "Execution time: " + (endnow - startnow) + " ms");
//                System.out.println("Done");
//
//                System.out.print("Save interest regions img...");
//                startnow = android.os.SystemClock.uptimeMillis();
//                tsrSystem.saveImgWithInterestRegionsAsBmp(interestRegions);
//                endnow = android.os.SystemClock.uptimeMillis();
//                Log.d("Save interest img", "Execution time: " + (endnow - startnow) + " ms");
//                System.out.println("Done");

                //Intent intent = new Intent(mContext, PhotoDetectActivity.class);
                //startActivity(intent);
            }
        });
    }


    private void requestCameraPermission(final int RC_HANDLE_CAMERA_PERM) {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == RC_HANDLE_CAMERA_PERM_RGB) {
            Intent intent = new Intent(mContext, TSRSystemRGBActivity.class);
            startActivity(intent);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == RC_HANDLE_CAMERA_PERM_GRAY) {
            Intent intent = new Intent(mContext, FaceDetectGrayActivity.class);
            startActivity(intent);
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));
    }

}
