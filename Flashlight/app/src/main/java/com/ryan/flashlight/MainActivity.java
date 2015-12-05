package com.ryan.flashlight;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends ActionBarActivity implements SurfaceHolder.Callback {
    public static Camera mCamera;
    public SurfaceHolder mHolder;
    public Button powerButton;
    boolean on = false;
    int unit = 50;// each unit=200ms
    long currentTime = 0;
    final Integer[] sos = {1, 0, 1, 0, 1, 0, 0, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0, 0, 0, 1, 0, 1, 0, 1,0};
    int counter = 0;
    int type = 0; //0 is light, 1=sos, 2=strobe
    ArrayList<Integer> SOSCode = new ArrayList<>();
    Bitmap torch_on, torch_off;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("FLASHLIGHT", "Entered onCreate");
        setContentView(R.layout.activity_main);
        SurfaceView preview = (SurfaceView) findViewById(R.id.Preview);
        mHolder = preview.getHolder();
        mHolder.addCallback(this);
        mCamera = Camera.open();

        SOSCode = new ArrayList<Integer>(Arrays.asList(sos));

        Log.e("SOS", SOSCode.get(1).toString());
        // powerButton=(Button) findViewById(R.id.flashLight_button);
//        if (savedInstanceState != null) {
//            on=savedInstanceState.getBoolean("flashLight");
//            if (!on) {
//                flashLightOff();
//            } else {
//                flashLightOn();
//            }
//        }
//        powerButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (on == false) {
//                    flashLightOn();
//                } else {
//                    flashLightOff();
//                }
//            }
//        });
        try {
            mCamera.setPreviewDisplay(mHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Display display = getWindowManager().getDefaultDisplay();
        Point resolution = new Point();
        display.getSize(resolution);
        Bitmap bm_on = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.torch_on);
        Bitmap bm_off = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.torch_off);
        ImageButton b = (ImageButton) findViewById(R.id.imageButton);
        torch_on = resize(bm_on, resolution.x / 4, resolution.y / 4);
        torch_off = resize(bm_off, resolution.x / 4, resolution.y / 4);
        b.setImageBitmap(torch_off);
        Button flashlight = (Button) findViewById(R.id.type_flashlight);


    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean("flashLight", on);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        on = savedInstanceState.getBoolean("flashLight");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }

    public void surfaceCreated(SurfaceHolder holder) {
        mHolder = holder;
        try {
            mCamera.setPreviewDisplay(mHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        mHolder = null;
    }

    public void clickPower(View v) {
        ImageButton imageB = (ImageButton) findViewById(R.id.imageButton);
        if (on == false) {
            imageB.setImageBitmap(torch_on);
            flashLightOn();

        } else {
            imageB.setImageBitmap(torch_off);
            flashLightOff();


        }
    }

    public void clickSOS(View v) {
        type = 1;
        Button sos = (Button) findViewById(R.id.type_SOS);
        sos.setBackground(getResources().getDrawable(R.drawable.sos_clicked));
        Button light = (Button) findViewById(R.id.type_flashlight);
        light.setBackground(getResources().getDrawable(R.drawable.flashlight_not_clicked));

        try {
            if (getPackageManager().hasSystemFeature(
                    PackageManager.FEATURE_CAMERA_FLASH)) {
                if(on == true ){
                    counter = 0;
                    new CountDownTimer(5600, 400) {

                        // sos is 600+300+1100+300+600
                        public void onTick(long millisUntilFinished) {
                            if (on == false || type == 0) {
                                flashLightOff();
                                counter=0;
                                cancel();
                                Log.e("SOSCanel", "canceled");
                            }
                            if (SOSCode.get(counter) == 1 && on==true) {
                                Camera.Parameters params = mCamera.getParameters();
                                params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                                mCamera.setParameters(params);
                                mCamera.startPreview();
                                Log.e("SOSon", SOSCode.get(counter).toString());

                            }
                            if (SOSCode.get(counter) == 0&& on==true) {
                                Camera.Parameters params = mCamera.getParameters();
                                params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                                mCamera.setParameters(params);
                                Log.e("SOSoff", SOSCode.get(counter).toString());

                            }
                            counter++;
                        }

                        public void onFinish() {
                            if(on==true){
                                counter = 0;
                                on = false;
                                Log.e("SOS", "done");
                                flashLightOn();}
                        }
                    }.start();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getBaseContext(), "Exception flashLightOn()",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void clickFlashlight(View v) {
        type = 0;
        Button sos = (Button) findViewById(R.id.type_SOS);
        sos.setBackground(getResources().getDrawable(R.drawable.sos_not_clicked));
        Button light = (Button) findViewById(R.id.type_flashlight);
        light.setBackground(getResources().getDrawable(R.drawable.flashlight_clicked));
    }

    public void flashLightOn() {
Log.e("FLASHLIGHTon","called flashlight on");
        try {
            if (getPackageManager().hasSystemFeature(
                    PackageManager.FEATURE_CAMERA_FLASH)) {


                if (on == false && type == 0) {// just light
                    Camera.Parameters params = mCamera.getParameters();
                    params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    mCamera.setParameters(params);
                    mCamera.startPreview();
                    on = true;
                }
                if (on == false && type == 1) {//sos light
                    counter = 0;
                    on = true;
                    new CountDownTimer(14000, 500) {

                        // sos is 600+300+1100+300+600
                        public void onTick(long millisUntilFinished) {
                            if (on == false || type == 0) {
                                flashLightOff();
                                mCamera.stopPreview();
                                cancel();
                                counter=0;
                                Log.e("SOSCanel", "canceled");
                            }
                            if (SOSCode.get(counter) == 1 && on==true) {
                                Camera.Parameters params = mCamera.getParameters();
                                params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                                mCamera.setParameters(params);
                                mCamera.startPreview();
                                Log.e("SOSon", SOSCode.get(counter).toString());

                            }
                            if (SOSCode.get(counter) == 0 && on==true) {
                                Camera.Parameters params = mCamera.getParameters();
                                params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                                mCamera.setParameters(params);
                                Log.e("SOSoff", SOSCode.get(counter).toString());

                            }
                            counter++;
                        }

                        public void onFinish() {
                            if(on==true){
                            counter = 0;
                            on = false;
                            Log.e("SOS", "done");
                            flashLightOn();}
                        }
                    }.start();

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getBaseContext(), "Exception flashLightOn()",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void flashLightOff() {
        try {
            if (getPackageManager().hasSystemFeature(
                    PackageManager.FEATURE_CAMERA_FLASH)) {
                Log.e("Flashlightoff","OFF called");
                Camera.Parameters params = mCamera.getParameters();
                params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(params);
                if (on == true) {
                    mCamera.stopPreview();
                    on = false;
                }

//                powerButton.setText("Click to turn on");

            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getBaseContext(), "Exception flashLightOff",
                    Toast.LENGTH_SHORT).show();
        }
    }

//    public void SOSLight() {
//
//        if (type == 1 && on == true) {
//            new CountDownTimer(1450, 50) {
//
//                // sos is 600+300+1100+300+600
//                public void onTick(long millisUntilFinished) {
//                    if (SOSCode.get(counter) == 1) {
//                        flashLightOn();
//                    }
//                    if (SOSCode.get(counter) == 0) {
//                        flashLightOff();
//                    }
//                }
//
//                public void onFinish() {
//                    counter = 0;
//                }
//            }.start();
//        }
//    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("FLASHLIGHT", "Released camera");
        mCamera.release();
    }

    private static Bitmap resize(Bitmap image, int maxWidth, int maxHeight) {
        if (maxHeight > 0 && maxWidth > 0) {
            int width = image.getWidth();
            int height = image.getHeight();
            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) maxWidth / (float) maxHeight;

            int finalWidth = maxWidth;
            int finalHeight = maxHeight;
            if (ratioMax > 1) {
                finalWidth = (int) ((float) maxHeight * ratioBitmap);
            } else {
                finalHeight = (int) ((float) maxWidth / ratioBitmap);
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
            return image;
        } else {
            return image;
        }
    }

}

