package net.mingkichong.apps.wherewasit;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class AboutActivity extends Activity {
    Typeface typeface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        typeface = Typeface.createFromAsset(getAssets(), ApplicationConstants.FONT_STYLE);
        initTitleBar();
        TextView aboutTextView = (TextView) findViewById(R.id.about_text_text_view);
        aboutTextView.setTypeface(typeface);
        aboutTextView.setTextSize(getResources().getDimension(R.dimen.about_text_font_size));
        String aboutTextString = null;
        final String nl = System.getProperty("line.separator");

        //read about text from file
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("text/about_text.txt")));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                total.append(line);
                total.append(nl);
            }
            reader.close();
            aboutTextString = total.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //http://stackoverflow.com/a/4790977
        aboutTextView.setText(
                Html.fromHtml(aboutTextString) //convert about text to html format
        );
        aboutTextView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void initTitleBar() {
        TextView titleBar = (TextView) getWindow().findViewById(android.R.id.title);
        titleBar.setTypeface(typeface);
    }
}
