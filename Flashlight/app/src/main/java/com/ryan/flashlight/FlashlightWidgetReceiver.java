package com.ryan.flashlight;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.widget.RemoteViews;
import android.widget.Toast;

/**
 * Created by Ryan on 8/28/2015.
 */
public class FlashlightWidgetReceiver extends BroadcastReceiver{
    private static boolean isLightOn = false;
    private static Camera camera;

    @Override
    public void onReceive(Context context, Intent intent) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_view);

        if(isLightOn) {
            views.setImageViewResource(R.id.button, R.drawable.off);
        } else {
            views.setImageViewResource(R.id.button, R.drawable.on);
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(new ComponentName(context,     FlashlightWidgetProvider.class),
                views);

        if (isLightOn) {
            if (camera != null) {
                camera.stopPreview();
                camera.release();
                camera = null;
                isLightOn = false;
            }

        } else {
            // Open the default i.e. the first rear facing camera.
            camera = Camera.open();

            if(camera == null) {
                Toast.makeText(context,"No camera", Toast.LENGTH_SHORT).show();
            } else {
                // Set the torch flash mode
                Camera.Parameters param = camera.getParameters();
                param.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                try {
                    camera.setParameters(param);
                    camera.startPreview();
                    isLightOn = true;
                    wait(4000);
                    camera.stopPreview();
                    camera.release();
                    camera = null;
                    isLightOn = false;
                } catch (Exception e) {
                    Toast.makeText(context,"No flash", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
