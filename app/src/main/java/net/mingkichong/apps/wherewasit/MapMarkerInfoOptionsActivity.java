package net.mingkichong.apps.wherewasit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class MapMarkerInfoOptionsActivity extends Activity implements View.OnClickListener {
    Typeface typeface;
    Button shareLocationButton, copyLocationButton, getMeThereButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_map_marker_info_options);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = (int) getResources().getDimension(R.dimen.mapmarker_info_options_width);
        getWindow().setAttributes(params);

        typeface = Typeface.createFromAsset(getAssets(), ApplicationConstants.FONT_STYLE);
        initUI();
    }

    private void initUI() {
        copyLocationButton = (Button) findViewById(R.id.option_copy_location_button);
        shareLocationButton = (Button) findViewById(R.id.option_share_location_button);
        getMeThereButton = (Button) findViewById(R.id.option_get_me_there_button);
        initButton(copyLocationButton);
        initButton(shareLocationButton);
        initButton(getMeThereButton);
    }

    private void initButton(Button button) {
        button.setOnClickListener(this);
        final GradientDrawable gdReleased, gdPressed;
        gdReleased = new GradientDrawable();
        gdReleased.setColor(getResources().getColor(R.color.dark_gray));
        gdReleased.setCornerRadius(getResources().getDimension(R.dimen.main_activity_button_corner_size));
        gdReleased.setStroke((int) getResources().getDimension(R.dimen.main_activity_button_stroke_width), getResources().getColor(R.color.light_gray));
        gdPressed = new GradientDrawable();
        gdPressed.setColor(getResources().getColor(R.color.white));
        gdPressed.setCornerRadius(getResources().getDimension(R.dimen.main_activity_button_corner_size));
        gdPressed.setStroke((int) getResources().getDimension(R.dimen.main_activity_button_stroke_width), getResources().getColor(R.color.dark_gray));
        button.setBackground(gdReleased);
        button.setOnTouchListener(new View.OnTouchListener() {
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
        button.setTypeface(typeface);
    }

    @Override
    public void onClick(View v) {
        double lat = getIntent().getExtras().getDouble(ApplicationConstants.RECORDED_LATITUDE_KEY);
        double lng = getIntent().getExtras().getDouble(ApplicationConstants.RECORDED_LONGITUDE_KEY);
        String address = getIntent().getExtras().getString(ApplicationConstants.RECORDED_ADDRESS_KEY);
        String recordedTime = getIntent().getExtras().getString(ApplicationConstants.RECORDED_CURRENT_TIME);

        switch (v.getId()) {
            case R.id.option_copy_location_button:
                String copiedLocationText = lat + "," + lng;
                MyClipboardManager myClipboardManager = new MyClipboardManager();
                if (myClipboardManager.copyToClipboard(this, copiedLocationText)) {
                    CustomToast.showToast(this, "Location \"" + copiedLocationText + "\" copied to clipboard", Toast.LENGTH_SHORT);
                    vibrateShort(this, 25L);
                } else {
                    CustomToast.showToast(this, "Failed to copy location to clipboard", Toast.LENGTH_SHORT);
                    vibratePattern(this);
                }
                break;
            case R.id.option_share_location_button:
                //http://stackoverflow.com/a/9462834
                Intent intentSendLocation = new Intent(Intent.ACTION_SENDTO); // it's not ACTION_SEND
                intentSendLocation.setType("text/plain");
                intentSendLocation.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.title_activity_main) + " : (" + lat + "," + lng + ") " + recordedTime);
                String nl = System.getProperty("line.separator");
                String copiedLocationMessageText = getResources().getString(R.string.title_activity_main)
                        + nl + nl
                        + address
                        + " " + "(" + lat + "," + lng + ")"
                        + nl + nl
                        + recordedTime
                        + nl + nl
                        + "https://maps.google.com/maps" + "?q=" + lat + "," + lng
                        + nl + nl;
                intentSendLocation.putExtra(Intent.EXTRA_TEXT, copiedLocationMessageText);
                intentSendLocation.setData(Uri.parse("mailto:"));
                intentSendLocation.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentSendLocation);
                vibrateShort(this, 25L);
                break;
            case R.id.option_get_me_there_button:
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat + "," + lng);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
                vibrateShort(this, 25L);
                break;
        }
    }

    private void vibrateShort(Context context, long duration) {
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0, duration};
        v.vibrate(pattern, -1);
    }

    private void vibratePattern(Context context) {
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0, 25, 150, 25};
        v.vibrate(pattern, -1);
    }
}
