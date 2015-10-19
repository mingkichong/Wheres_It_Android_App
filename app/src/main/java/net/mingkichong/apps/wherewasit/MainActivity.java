package net.mingkichong.apps.wherewasit;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    ImageButton locateButton, recordLocationButton, recordWithImageButton, clearLocationImageButton;
    Typeface typeface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        typeface = Typeface.createFromAsset(getAssets(), ApplicationConstants.FONT_STYLE);
        initTitleBar();
        initUI();
    }

    private void initUI() {
        locateButton = (ImageButton) findViewById(R.id.main_activity_locate_image_button);
        recordLocationButton = (ImageButton) findViewById(R.id.main_activity_record_location_image_button);
        recordWithImageButton = (ImageButton) findViewById(R.id.main_activity_record_with_image_image_button);
        clearLocationImageButton = (ImageButton) findViewById(R.id.main_activity_clear_location_image_button);
        initImageButton(locateButton);
        initImageButton(recordLocationButton);
        initImageButton(recordWithImageButton);
        initImageButton(clearLocationImageButton);
    }

    //released state has dark gray background with light gray border
    //pressed state has white background with dark gray border (contrast of the released state)
    private void initImageButton(ImageButton imageButton) {
        final GradientDrawable gdRelease = new GradientDrawable();
        gdRelease.setColor(getResources().getColor(R.color.dark_gray));
        gdRelease.setCornerRadius(getResources().getDimension(R.dimen.main_activity_button_corner_size));
        gdRelease.setStroke((int) getResources().getDimension(R.dimen.main_activity_button_stroke_width), getResources().getColor(R.color.light_gray));
        final GradientDrawable gdPress = new GradientDrawable();
        gdPress.setColor(getResources().getColor(R.color.white));
        gdPress.setCornerRadius(getResources().getDimension(R.dimen.main_activity_button_corner_size));
        gdPress.setStroke((int) getResources().getDimension(R.dimen.main_activity_button_stroke_width), getResources().getColor(R.color.dark_gray));
        imageButton.setBackground(gdRelease);
        imageButton.setOnClickListener(this);
        imageButton.setOnLongClickListener(this);
        imageButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setBackground(gdPress);
                        v.invalidate();
                        break;
                    case MotionEvent.ACTION_UP:
                        v.setBackground(gdRelease);
                        v.invalidate();
                        break;
                }
                return false;
            }
        });
    }

    private void initTitleBar() {
        // http://stackoverflow.com/a/15181195
        SpannableString s = new SpannableString(getTitle());
        s.setSpan(new TypefaceSpan(this, ApplicationConstants.FONT_STYLE), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        getSupportActionBar().setTitle(s);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_activity_locate_image_button:
                processLocateButtonClicked();
                break;
            case R.id.main_activity_record_location_image_button:
                processRecordLocationButtonClicked();
                break;
            case R.id.main_activity_record_with_image_image_button:
                processRecordWithImageButtonClicked();
                break;
            case R.id.main_activity_clear_location_image_button:
                processClearLocationButtonClicked();
                break;
        }
    }

    //clear recorded data from the device's internal memory
    private void processClearLocationButtonClicked() {
        vibrateShort(this, 25L);
        try {
            FileOutputStream fos = openFileOutput(ApplicationConstants.FILENAME_COORDINATES, Context.MODE_PRIVATE);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        deleteBitmapAndThumbnail();

        RemoteViews remoteViews = new RemoteViews(this.getPackageName(), R.layout.widget_main);
        remoteViews.setTextViewText(R.id.textViewAddress, ApplicationConstants.DEFAULT_ADDRESS);
        remoteViews.setTextViewText(R.id.textViewCoordinate, ApplicationConstants.DEFAULT_INVALID_COORDINATES);
        AppWidgetManager.getInstance(this).updateAppWidget(new ComponentName(this, MainWidget.class), remoteViews);

        CustomToast.showToast(this, "  Recorded Location Cleared  ", Toast.LENGTH_SHORT);
    }

    //delete the recorded snapshot image and its thumbnail
    private void deleteBitmapAndThumbnail() {
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = new File(storageDir, ApplicationConstants.FILENAME_IMAGE_PHOTO + ".jpg");
        File imageThumbnail = new File(storageDir, ApplicationConstants.FILENAME_IMAGE_THUMBNAIL + ".jpg");
        image.delete();
        imageThumbnail.delete();
    }

    private void processRecordWithImageButtonClicked() {
        Intent startCameraIntent = new Intent(this, TakeASnapshotActivity.class);
        startCameraIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startCameraIntent);
        recordLocation(this, true);
        vibrateShort(this, 25L);
    }

    private void processRecordLocationButtonClicked() {
        vibrateShort(this, 25L);
        recordLocation(this, false);
    }

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
                CustomToast.showToast(MainActivity.this, "New Location Recorded: " + nl + "\"" + addressText + "\"", Toast.LENGTH_SHORT);
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

    //start a new activity to display the recorded location
    private void processLocateButtonClicked() {
        Intent intent = new Intent(this, ShowMapLocationActivity.class);
        startActivity(intent);
        vibrateShort(this, 25L);
    }

    //vibrate in a predefined pattern
    private void vibratePattern(Context context) {
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0, 25, 150, 25};
        v.vibrate(pattern, -1);
    }

    //vibrate once with a specified duration
    private void vibrateShort(Context context, long duration) {
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0, duration};
        v.vibrate(pattern, -1);
    }

    Menu menu;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.main_activity_menu_google_play_licence:
                Intent intentLicenceDisplayGooglePlayActivity = new Intent(this, LicenceDisplayGooglePlayActivity.class);
                startActivity(intentLicenceDisplayGooglePlayActivity);
                return true;
            case R.id.main_activity_menu_about:
                Intent intentAboutActivity = new Intent(this, AboutActivity.class);
                startActivity(intentAboutActivity);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Users can long click on a button to reveal its corresponding help text
    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.main_activity_locate_image_button:
                showHelpAlertDialog(getResources().getString(R.string.help_message_show_on_map));
                break;
            case R.id.main_activity_record_location_image_button:
                showHelpAlertDialog(getResources().getString(R.string.help_message_record_location));
                break;
            case R.id.main_activity_record_with_image_image_button:
                showHelpAlertDialog(getResources().getString(R.string.help_message_record_with_image));
                break;
            case R.id.main_activity_clear_location_image_button:
                showHelpAlertDialog(getResources().getString(R.string.help_message_clear_data));
                break;
        }
        return true;
    }

    //display a help message in a custom alert dialog box
    private void showHelpAlertDialog(String message) {
        TextView titleTextView = new TextView(this);
        titleTextView.setTypeface(typeface);
        titleTextView.setText("Help: " + message);
        titleTextView.setTextSize(getResources().getDimension(R.dimen.alert_dialog_help_title_font_size));
        titleTextView.setGravity(Gravity.CENTER);
        titleTextView.setPadding(
                titleTextView.getPaddingLeft() + getResources().getDimensionPixelSize(R.dimen.alert_dialog_horizontal_padding),
                titleTextView.getPaddingTop() + getResources().getDimensionPixelSize(R.dimen.alert_dialog_vertical_padding),
                titleTextView.getPaddingRight() + getResources().getDimensionPixelSize(R.dimen.alert_dialog_horizontal_padding),
                titleTextView.getPaddingBottom() + getResources().getDimensionPixelSize(R.dimen.alert_dialog_vertical_padding)
        );

        ImageView imageView = new ImageView(this);
        imageView.setImageResource(android.R.drawable.ic_menu_help);

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.addView(imageView);
        linearLayout.addView(titleTextView);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setBackgroundColor(getResources().getColor(R.color.gray));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCustomTitle(linearLayout)
//                .setCustomTitle(titleTextView)
                .show();
        TextView textView = (TextView) dialog.findViewById(android.R.id.message);
        textView.setTypeface(typeface);
    }
}
