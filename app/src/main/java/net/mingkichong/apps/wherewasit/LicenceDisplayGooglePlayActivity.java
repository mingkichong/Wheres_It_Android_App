package net.mingkichong.apps.wherewasit;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.common.GoogleApiAvailability;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class LicenceDisplayGooglePlayActivity extends Activity {
    Typeface typeface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_licence_display_google_play);
        typeface = Typeface.createFromAsset(getAssets(), ApplicationConstants.FONT_STYLE);
        initTitleBar();

        TextView licenceTextView = (TextView) findViewById(R.id.licence_google_play_textview);
        licenceTextView.setTypeface(typeface);
        licenceTextView.setTextSize(getResources().getDimension(R.dimen.about_text_font_size));

        String aboutTextFromFileString = null;
        final String nl = System.getProperty("line.separator");

        //read licence text from file
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("text/licence_text.txt")));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                total.append(line);
                total.append(nl);
            }
            reader.close();
            aboutTextFromFileString = total.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        StringBuilder finalLicenceStringBuilder = new StringBuilder();
        finalLicenceStringBuilder.append(aboutTextFromFileString);
        finalLicenceStringBuilder.append(nl);

        //retrieve google api licence text from server
        String googleLicenceString = GoogleApiAvailability.getInstance().getOpenSourceSoftwareLicenseInfo(this);
        if(googleLicenceString != null){
            //licenceTextView.setText(googleLicenceString);
            finalLicenceStringBuilder.append(googleLicenceString);
        }else{
            //licenceTextView.setText(getResources().getText(R.string.google_service_not_available));
            finalLicenceStringBuilder.append(getResources().getText(R.string.google_service_not_available));
        }

        licenceTextView.setText(finalLicenceStringBuilder.toString());
    }

    private void initTitleBar() {
        TextView titleBar = (TextView) getWindow().findViewById(android.R.id.title);
        titleBar.setTypeface(typeface);
    }
}
