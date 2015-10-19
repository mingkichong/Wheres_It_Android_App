package net.mingkichong.apps.wherewasit;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;

public class ShowMapLocationActivity extends AppCompatActivity implements View.OnClickListener, GoogleMap.OnInfoWindowClickListener {
    GoogleMap googleMap;
    Marker marker;
    boolean isLocationWithImage = false;
    ImageView markerImageView;
    ImageButton repositionImageButton;
    Typeface typeface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_map_location);
        typeface = Typeface.createFromAsset(getAssets(), ApplicationConstants.FONT_STYLE);
        initTitleBar();
        initMap();
    }

    private void initTitleBar() {
        // http://stackoverflow.com/a/15181195
        SpannableString s = new SpannableString(getTitle());
        s.setSpan(new TypefaceSpan(this, ApplicationConstants.FONT_STYLE), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        getSupportActionBar().setTitle(s);
    }

    private void initRepositionButton() {
        repositionImageButton = (ImageButton) findViewById(R.id.show_map_location_reposition_imagebutton);
        int markerSizeDP = (int) getResources().getDimension(R.dimen.show_map_loc_reposition_size);
        Bitmap bitmap = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(getResources(), R.drawable.reposition_128),
                markerSizeDP,
                markerSizeDP,
                false);
        repositionImageButton.setImageBitmap(bitmap);
        int offset = (int) getResources().getDimension(R.dimen.show_map_loc_reposition_size_offset);
        repositionImageButton.getLayoutParams().height = markerSizeDP + offset;
        repositionImageButton.getLayoutParams().width = markerSizeDP + offset;
        if (marker == null) {
            final GradientDrawable gdDisabled = new GradientDrawable();
            gdDisabled.setColor(getResources().getColor(R.color.gray));
            gdDisabled.setCornerRadius(getResources().getDimension(R.dimen.show_map_loc_reposition_corner_size));
            gdDisabled.setStroke(0, getResources().getColor(R.color.gray));
            repositionImageButton.setBackground(gdDisabled);
            repositionImageButton.setVisibility(View.INVISIBLE);
            return;
        }

        repositionImageButton.setOnClickListener(this);
        final GradientDrawable gdReleased = new GradientDrawable();
        gdReleased.setColor(getResources().getColor(R.color.orange));
        gdReleased.setCornerRadius(getResources().getDimension(R.dimen.show_map_loc_reposition_corner_size));
        gdReleased.setStroke((int) getResources().getDimension(R.dimen.show_map_loc_reposition_stroke_width), getResources().getColor(R.color.dark_gray));
        final GradientDrawable gdPressed = new GradientDrawable();
        gdPressed.setColor(getResources().getColor(R.color.purple));
        gdPressed.setCornerRadius(getResources().getDimension(R.dimen.show_map_loc_reposition_corner_size));
        gdPressed.setStroke((int) getResources().getDimension(R.dimen.show_map_loc_reposition_stroke_width), getResources().getColor(R.color.red));
        repositionImageButton.setBackground(gdReleased);
        repositionImageButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setBackground(gdPressed);
                        v.invalidate();
                        break;
                    case MotionEvent.ACTION_UP:
                        v.setBackground(gdReleased);
                        v.invalidate();
                        break;
                }
                return false;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        String jsonString = getJSONStringFromFile();
        if (jsonString == null) {
            initRepositionButton();
            return;
        }
        JSONObject jsonObject = null;

        try {
            jsonObject = new JSONObject(jsonString);
            double recordedLatitude = jsonObject.getDouble(ApplicationConstants.RECORDED_LATITUDE_KEY);
            double recordedLongitude = jsonObject.getDouble(ApplicationConstants.RECORDED_LONGITUDE_KEY);
            long currentTime = jsonObject.getLong(ApplicationConstants.RECORDED_CURRENT_TIME);
            String recordedAddress = jsonObject.getString(ApplicationConstants.RECORDED_ADDRESS_KEY);

            //attempt to update address if the previous one was not available
            if (recordedAddress.compareTo(ApplicationConstants.NO_ADDRESS_INFO_FROM_GOOGLE) == 0) {
                Geocoder geocoder = new Geocoder(this);
                List<Address> list = null;
                try {
                    list = geocoder.getFromLocation(recordedLatitude, recordedLongitude, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                recordedAddress = ApplicationConstants.NO_ADDRESS_INFO_FROM_GOOGLE;
                if (list != null && list.size() > 0) {
                    recordedAddress = "";
                    Address address = list.get(0);
                    for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                        recordedAddress += address.getAddressLine(i);
                        if (i < address.getMaxAddressLineIndex() - 1) {
                            recordedAddress += ", ";
                        }
                    }
                }
                jsonObject.put(ApplicationConstants.RECORDED_ADDRESS_KEY, recordedAddress);
                String coordinate = recordedLatitude + "," + recordedLongitude;
                RemoteViews remoteViews = new RemoteViews(this.getPackageName(), R.layout.widget_main);
                remoteViews.setTextViewText(R.id.textViewAddress, recordedAddress);
                remoteViews.setTextViewText(R.id.textViewCoordinate, coordinate);
                AppWidgetManager.getInstance(this).updateAppWidget(new ComponentName(this, MainWidget.class), remoteViews);
            }

            LatLng latLng = new LatLng(recordedLatitude, recordedLongitude);

            isLocationWithImage = jsonObject.getBoolean(ApplicationConstants.RECORDED_WITH_IMAGE_KEY);
            googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {
                    return initInfoWindow(marker, isLocationWithImage);
                }

                @Override
                public View getInfoContents(Marker marker) {
                    return null;
                }
            });
            showLocation(latLng, recordedAddress, currentTime, isLocationWithImage);
        } catch (JSONException e) {
            displayAlert();
//            e.printStackTrace();
        }
        initRepositionButton();
    }

    private void displayAlert() {
        TextView titleTextView = new TextView(this);
        titleTextView.setTypeface(typeface);
        titleTextView.setText(ApplicationConstants.DEFAULT_NO_LOCATION_ALERT_DIALOG_TITLE);
        titleTextView.setTextSize(getResources().getDimension(R.dimen.alert_dialog_title_font_size));
        titleTextView.setGravity(Gravity.CENTER);
        titleTextView.setPadding(
                titleTextView.getPaddingLeft() + getResources().getDimensionPixelSize(R.dimen.alert_dialog_horizontal_padding),
                titleTextView.getPaddingTop() + getResources().getDimensionPixelSize(R.dimen.alert_dialog_vertical_padding),
                titleTextView.getPaddingRight() + getResources().getDimensionPixelSize(R.dimen.alert_dialog_horizontal_padding),
                titleTextView.getPaddingBottom() + getResources().getDimensionPixelSize(R.dimen.alert_dialog_vertical_padding)
        );
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCustomTitle(titleTextView)
                .show();
        TextView textView = (TextView) dialog.findViewById(android.R.id.message);
        textView.setTypeface(typeface);
    }


    private Bitmap getImageBitmap(boolean withImage) {
        int markerSizeDP = (int) getResources().getDimension(R.dimen.mapmarker_imageview_size);
        Bitmap image = null;
        if (withImage) {
            File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File imageThumbnail = new File(storageDir, ApplicationConstants.FILENAME_IMAGE_THUMBNAIL + ".jpg");
            if (imageThumbnail.exists()) {
                image = BitmapFactory.decodeFile(imageThumbnail.getAbsolutePath());
            } else {
                image = BitmapFactory.decodeResource(getResources(), R.drawable.logo_bw_128);
            }
        } else {
            image = BitmapFactory.decodeResource(getResources(), R.drawable.logo_loc);
        }
        return Bitmap.createScaledBitmap(image, markerSizeDP, markerSizeDP, false);
    }

    private String getJSONStringFromFile() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            FileInputStream fis = this.openFileInput(ApplicationConstants.FILENAME_COORDINATES);
            byte[] buffer = new byte[1024];
            while (fis.read(buffer) != -1) {
                os.write(buffer);
            }
            fis.close();
        } catch (FileNotFoundException e) {
            displayAlert();
//            e.printStackTrace();
            return null;
        } catch (IOException e) {
            displayAlert();
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


    private boolean initMap() {
        if (googleMap == null) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.show_map_location_map);
            googleMap = mapFragment.getMap();
            UiSettings uiSettings = googleMap.getUiSettings();
            uiSettings.setZoomControlsEnabled(true);
//            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(0.0, 0.0), 14.0f));
            googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    CameraUpdate update = CameraUpdateFactory.newLatLngZoom(new LatLng(marker.getPosition().latitude, marker.getPosition().longitude), googleMap.getCameraPosition().zoom);
                    googleMap.animateCamera(update);
                    return false;
                }
            });
            googleMap.setOnInfoWindowClickListener(this);
        }
        return (googleMap != null);
    }

    @NonNull
    private View initInfoWindow(Marker marker, boolean withImage) {
        View v = getLayoutInflater().inflate(R.layout.map_marker, null);
        TextView markerInfoTextView = (TextView) v.findViewById(R.id.map_marker_info);
        TextView markerTitleTextView = (TextView) v.findViewById(R.id.map_marker_title);
        TextView markerInfoTimeTextView = (TextView) v.findViewById(R.id.map_marker_info_time);
        String[] snippets = marker.getSnippet().split(Pattern.quote(ApplicationConstants.MARKER_SNIPPET_INFO_SPLIT_PATTERN));
        markerInfoTextView.setText(snippets[0]);
        markerInfoTimeTextView.setText(snippets[1]);
        markerTitleTextView.setText(marker.getTitle());
        Typeface typeface = Typeface.createFromAsset(getAssets(), ApplicationConstants.FONT_STYLE);
        markerInfoTextView.setTypeface(typeface);
        markerTitleTextView.setTypeface(typeface);
        markerInfoTimeTextView.setTypeface(typeface);

        LinearLayout linearLayout = (LinearLayout) v.findViewById(R.id.map_marker_linearlayout);
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(getResources().getColor((withImage) ? R.color.light_blue : R.color.white)); //set info background colour
        gd.setCornerRadius(getResources().getDimension(R.dimen.mapmarker_corner_size));
        gd.setStroke((int) getResources().getDimension(R.dimen.mapmarker_stroke_width), getResources().getColor(R.color.dark_gray));
        linearLayout.setBackground(gd);

        markerImageView = (ImageView) v.findViewById(R.id.map_marker_imageview);
        markerImageView.setImageBitmap(getImageBitmap(isLocationWithImage));

        return v;
    }


    private void showLocation(LatLng latLng, String address, long currentTime, boolean withImage) {
        if (marker != null) {
            marker.remove();
        }

        MarkerOptions options = new MarkerOptions()
                .title(" " + address + " ")
                .position(latLng)
                .snippet(latLng.toString() + ApplicationConstants.MARKER_SNIPPET_INFO_SPLIT_PATTERN + "recorded on: (" + formatTimeToString(currentTime) + ")")
                .flat(true)
                .icon(getMarkerBitmapDescriptor(withImage))
                .anchor(0.5f, 0.5f);
        marker = googleMap.addMarker(options);
        marker.setInfoWindowAnchor(0.5f, -0.2f);
        marker.showInfoWindow();

        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, ApplicationConstants.DEFAULT_MAP_ZOOM_LEVEL);
        googleMap.animateCamera(update);
    }

    private String formatTimeToString(long milliSeconds) {
        SimpleDateFormat formatter = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    private BitmapDescriptor getMarkerBitmapDescriptor(boolean withImage) {
        int markerSizeDP = (int) getResources().getDimension(R.dimen.mapmarker_marker_size);
        Bitmap bitmap = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(getResources(), ((withImage) ? R.drawable.marker_cam_blue : R.drawable.marker)),
                markerSizeDP,
                markerSizeDP,
                true);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.show_map_location_reposition_imagebutton:
                processRepositionButtonClicked();
                break;
        }
    }

    private void processRepositionButtonClicked() {
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(new LatLng(marker.getPosition().latitude, marker.getPosition().longitude), googleMap.getCameraPosition().zoom);
        googleMap.animateCamera(update, new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                marker.showInfoWindow();
            }

            @Override
            public void onCancel() {
            }
        });
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Intent intent = new Intent();
        intent.putExtra(ApplicationConstants.RECORDED_ADDRESS_KEY, marker.getTitle());
        intent.putExtra(ApplicationConstants.RECORDED_LATITUDE_KEY, marker.getPosition().latitude);
        intent.putExtra(ApplicationConstants.RECORDED_LONGITUDE_KEY, marker.getPosition().longitude);
        String[] snippets = marker.getSnippet().split(Pattern.quote(ApplicationConstants.MARKER_SNIPPET_INFO_SPLIT_PATTERN));
        String recordedTime = snippets[1];
        intent.putExtra(ApplicationConstants.RECORDED_CURRENT_TIME, recordedTime);

        if (isLocationWithImage) {
            intent.setClass(this, MapMarkerInfoOptionsWithImageActivity.class);
        } else {
            intent.setClass(this, MapMarkerInfoOptionsActivity.class);
        }
        startActivity(intent);
    }
}
