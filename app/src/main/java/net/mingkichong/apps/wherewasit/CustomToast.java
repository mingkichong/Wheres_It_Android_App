package net.mingkichong.apps.wherewasit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by mkc on 12/10/2015.
 */
public class CustomToast {

    //Create a custom toast message which changes the background and shows the application logo in an image view
    public static void showToast(Context context, String message, int toastLength) {
        Typeface typeface = Typeface.createFromAsset(context.getAssets(), ApplicationConstants.FONT_STYLE);
        Toast toast = new Toast(context);
        toast.setDuration(toastLength);

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        final LinearLayout.LayoutParams LLParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        linearLayout.setLayoutParams(LLParams);
        linearLayout.setGravity(Gravity.CENTER);
        int messagePadding = (int) context.getResources().getDimension(R.dimen.toast_message_padding_size);
        linearLayout.setPadding(0, messagePadding, 0, messagePadding);

        ImageView imageView = new ImageView(context);
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo_256);
        int bitmapScale = context.getResources().getDimensionPixelSize(R.dimen.toast_logo_size);
        bitmap = Bitmap.createScaledBitmap(bitmap, bitmapScale, bitmapScale, false);
        imageView.setImageBitmap(bitmap);
        imageView.setLayoutParams(LLParams);
        int gapPadding = (int) context.getResources().getDimension(R.dimen.toast_gap_padding_size);
        imageView.setPadding(gapPadding, 0, 0, 0);

        TextView messageTextView = new TextView(context);
        messageTextView.setTypeface(typeface);
        messageTextView.setTextSize(context.getResources().getDimension(R.dimen.toast_message_fontsize));
        messageTextView.setTextColor(context.getResources().getColor(R.color.black));
        messageTextView.setPadding(0, 0, gapPadding, 0);
        messageTextView.setText(message);
        messageTextView.setGravity(Gravity.CENTER_HORIZONTAL);

        linearLayout.addView(imageView);
        linearLayout.addView(messageTextView);
        toast.setView(linearLayout);

        GradientDrawable gd = new GradientDrawable();
        gd.setColor(context.getResources().getColor(R.color.opaque_white));
        gd.setCornerRadius(context.getResources().getDimension(R.dimen.main_activity_button_corner_size));
        gd.setStroke((int) context.getResources().getDimension(R.dimen.main_activity_button_stroke_width), context.getResources().getColor(R.color.black));
        linearLayout.setBackground(gd);

        toast.show();
    }
}
