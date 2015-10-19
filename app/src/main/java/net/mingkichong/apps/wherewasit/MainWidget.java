package net.mingkichong.apps.wherewasit;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.widget.RemoteViews;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by mkc on 23/09/2015.
 */
public class MainWidget extends AppWidgetProvider {

    Typeface typeface;

    private static final String WIDGET_RECORD_LOCATION_BUTTON = "WIDGET_RECORD_LOCATION_BUTTON";
    private static final String WIDGET_RECORD_IMAGE_LOCATION_BUTTON = "WIDGET_RECORD_IMAGE_BUTTON";
    private static final String WIDGET_ADDRESS_TEXTVIEW = "WIDGET_ADDRESS_TEXTVIEW";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_main);
        remoteViews.setOnClickPendingIntent(R.id.imageButtonRecordLocation, getPendingSelfIntent(context, WIDGET_RECORD_LOCATION_BUTTON));
        remoteViews.setOnClickPendingIntent(R.id.imageButtonRecordImageLocation, getPendingSelfIntent(context, WIDGET_RECORD_IMAGE_LOCATION_BUTTON));
        remoteViews.setOnClickPendingIntent(R.id.textViewAddress, getPendingSelfIntent(context, WIDGET_ADDRESS_TEXTVIEW));
        remoteViews.setOnClickPendingIntent(R.id.textViewCoordinate, getPendingSelfIntent(context, WIDGET_ADDRESS_TEXTVIEW));
        appWidgetManager.updateAppWidget(new ComponentName(context, MainWidget.class), remoteViews);

        String jsonString = getJSONStringFromFile(context);
        if (jsonString == null) {
            return;
        }
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(jsonString);
            double recordedLatitude = jsonObject.getDouble(ApplicationConstants.RECORDED_LATITUDE_KEY);
            double recordedLongitude = jsonObject.getDouble(ApplicationConstants.RECORDED_LONGITUDE_KEY);
            String recordedAddress = jsonObject.getString(ApplicationConstants.RECORDED_ADDRESS_KEY);
            remoteViews.setTextViewText(R.id.textViewAddress, recordedAddress);
            remoteViews.setTextViewText(R.id.textViewCoordinate, recordedLatitude + "," + recordedLongitude);
            AppWidgetManager.getInstance(context).updateAppWidget(new ComponentName(context, MainWidget.class), remoteViews);
        } catch (JSONException e) {
            remoteViews.setTextViewText(R.id.textViewAddress, ApplicationConstants.DEFAULT_ADDRESS);
            remoteViews.setTextViewText(R.id.textViewCoordinate, ApplicationConstants.DEFAULT_INVALID_COORDINATES);
            AppWidgetManager.getInstance(context).updateAppWidget(new ComponentName(context, MainWidget.class), remoteViews);
//            e.printStackTrace();
        }
    }

    private String getJSONStringFromFile(Context context) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            FileInputStream fis = context.openFileInput(ApplicationConstants.FILENAME_COORDINATES);
            byte[] buffer = new byte[1024];
            while (fis.read(buffer) != -1) {
                os.write(buffer);
            }
            fis.close();
        } catch (FileNotFoundException e) {
//            e.printStackTrace();
            return null;
        } catch (IOException e) {
//            e.printStackTrace();
            return null;
        }
        String jsonStringFromFile = os.toString();
        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonStringFromFile.trim();
    }

    //process users' input interaction
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        switch (intent.getAction()) {
            case WIDGET_RECORD_LOCATION_BUTTON:
                processRecordLocationWidgetButtonClicked(context);
                break;
            case WIDGET_RECORD_IMAGE_LOCATION_BUTTON:
                processRecordLocationImageWidgetButtonClicked(context);
                break;
            case WIDGET_ADDRESS_TEXTVIEW:
                displayLocation(context);
                break;
        }
    }

    private void displayLocation(Context context) {
        Intent intent = new Intent(context, ShowMapLocationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        vibrateShort(context, 25L);
    }

    private void processRecordLocationWidgetButtonClicked(final Context context) {
        recordLocation(context, false); //false = record with no image
        vibrateShort(context, 25L);
    }

    private void vibratePattern(Context context) {
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0, 25, 150, 25};
        v.vibrate(pattern, -1);
    }

    private void vibrateShort(Context context, long duration) {
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0, duration};
        v.vibrate(pattern, -1);
    }


    //record current location and store the location information in internal storage
    private void recordLocation(final Context context, final boolean withImage) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        final LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Geocoder geocoder = new Geocoder(context);

//                //FIXED LOCATION TEST: Big Ben London (51.500914, -0.125535)
//                //Remove the following lines for non-fixed location tests
//                //--------
//                location.setLatitude(51.500914);
//                location.setLongitude(-0.125535);
//                //--------

                List<Address> list = null;
                try {
                    list = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String addressText = ApplicationConstants.NO_ADDRESS_INFO_FROM_GOOGLE;
                if (list != null && list.size() > 0) {
                    addressText = "";
                    Address address = list.get(0);
                    for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                        addressText += address.getAddressLine(i);
                        if (i < address.getMaxAddressLineIndex() - 1) {
                            addressText += ", ";
                        }
                    }
                }
                long currentTime = System.currentTimeMillis();
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(ApplicationConstants.RECORDED_LATITUDE_KEY, location.getLatitude());
                    jsonObject.put(ApplicationConstants.RECORDED_LONGITUDE_KEY, location.getLongitude());
                    jsonObject.put(ApplicationConstants.RECORDED_ADDRESS_KEY, addressText);
                    jsonObject.put(ApplicationConstants.RECORDED_CURRENT_TIME, currentTime);
                    jsonObject.put(ApplicationConstants.RECORDED_WITH_IMAGE_KEY, withImage);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    FileOutputStream fos = context.openFileOutput(ApplicationConstants.FILENAME_COORDINATES, Context.MODE_PRIVATE);
                    fos.write(jsonObject.toString().getBytes());
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                final String nl = System.getProperty("line.separator");
                CustomToast.showToast(context, "New Location Recorded: " + nl + "\"" + addressText + "\"", Toast.LENGTH_SHORT);
                vibratePattern(context);
                String coordinate = location.getLatitude() + "," + location.getLongitude();
                RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_main);
                remoteViews.setTextViewText(R.id.textViewAddress, addressText);
                remoteViews.setTextViewText(R.id.textViewCoordinate, coordinate);
                AppWidgetManager.getInstance(context).updateAppWidget(new ComponentName(context, MainWidget.class), remoteViews);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        try {
            locationManager.requestSingleUpdate(criteria, locationListener, null);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void processRecordLocationImageWidgetButtonClicked(final Context context) {
        startCameraImageCaptureActivity(context);
        recordLocation(context, true); //false = record with no image
        vibrateShort(context, 25L);
    }

    private void startCameraImageCaptureActivity(Context context) {
        Intent startCameraIntent = new Intent(context, TakeASnapshotActivity.class);
        startCameraIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(startCameraIntent);
    }


    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }
}
