package net.mingkichong.apps.wherewasit;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

//an activity to contain android's camera app
public class TakeASnapshotActivity extends AppCompatActivity implements View.OnClickListener {

//    ImageButton retakeButton;
//    ImageButton saveButton;
//    ImageView snapshotImageView;
    Typeface typeface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        typeface = Typeface.createFromAsset(getAssets(), ApplicationConstants.FONT_STYLE);
        deleteBitmapAndThumbnail();
//        setContentView(R.layout.activity_take_a_snapshot);
//        initUI();
    }

    @Override
    protected void onStart() {
        super.onStart();
        dispatchTakePictureIntent();
    }

//    private void initUI() {
//        retakeButton = (ImageButton) findViewById(R.id.snapshot_retake_button);
//        saveButton = (ImageButton) findViewById(R.id.snapshot_save_button);
//        snapshotImageView = (ImageView) findViewById(R.id.snapshot_imageview);
//
//        retakeButton.setOnClickListener(this);
//        saveButton.setOnClickListener(this);
//
//        int iconSizeDP = (int) getResources().getDimension(R.dimen.snapshot_button_icon_size);
//        Bitmap bitmapRetake = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.retake_128), iconSizeDP, iconSizeDP, false);
//        Bitmap bitmapSave = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.save_128), iconSizeDP, iconSizeDP, false);
//        retakeButton.setImageBitmap(bitmapRetake);
//        saveButton.setImageBitmap(bitmapSave);
//        int offset = (int) getResources().getDimension(R.dimen.snapshot_button_icon_size_offset);
//        retakeButton.getLayoutParams().height = iconSizeDP + offset;
//        retakeButton.getLayoutParams().width = iconSizeDP + offset;
//        saveButton.getLayoutParams().height = iconSizeDP + offset;
//        saveButton.getLayoutParams().width = iconSizeDP + offset;
//
//        final GradientDrawable gdRetake = new GradientDrawable();
//        gdRetake.setColor(getResources().getColor(R.color.orange));
//        gdRetake.setCornerRadius(getResources().getDimension(R.dimen.snapshot_button_corner_size));
//        gdRetake.setStroke((int) getResources().getDimension(R.dimen.snapshot_button_stroke_width), getResources().getColor(R.color.dark_gray));
//        retakeButton.setBackground(gdRetake);
//
//        final GradientDrawable gdSave = new GradientDrawable();
//        gdSave.setColor(getResources().getColor(R.color.green));
//        gdSave.setCornerRadius(getResources().getDimension(R.dimen.snapshot_button_corner_size));
//        gdSave.setStroke((int) getResources().getDimension(R.dimen.snapshot_button_stroke_width), getResources().getColor(R.color.dark_gray));
//        saveButton.setBackground(gdSave);
//
//        final GradientDrawable gdPressed = new GradientDrawable();
//        gdPressed.setColor(getResources().getColor(R.color.purple));
//        gdPressed.setCornerRadius(getResources().getDimension(R.dimen.snapshot_button_corner_size));
//        gdPressed.setStroke((int) getResources().getDimension(R.dimen.snapshot_button_stroke_width), getResources().getColor(R.color.red));
//
//        // http://stackoverflow.com/a/14814595
//        retakeButton.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                switch (event.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                        v.setBackground(gdPressed);
//                        v.invalidate();
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        v.setBackground(gdRetake);
//                        v.invalidate();
//                        break;
//                }
//                return false;
//            }
//        });
//        saveButton.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                switch (event.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                        v.setBackground(gdPressed);
//                        v.invalidate();
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        v.setBackground(gdSave);
//                        v.invalidate();
//                        break;
//                }
//                return false;
//            }
//        });
//
//
//    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.snapshot_retake_button:
                processSnapshotRetakeButtonClicked();
                break;
            case R.id.snapshot_save_button:
                processSnapshotSaveButtonClicked();
                break;
        }
    }

    private void processSnapshotSaveButtonClicked() {
    }

    private void processSnapshotRetakeButtonClicked() {
//        startCamera();
    }

    //Taking Photos Simply https://developer.android.com/training/camera/photobasics.html
    private void startCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, ApplicationConstants.REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == ApplicationConstants.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bitmap thumbnailBitmap = getThumbnailBitmap(mCurrentPhotoPath);
            Log.e("onActivityResult", mCurrentPhotoPath);
            if (thumbnailBitmap != null) {
                //Store image to file http://stackoverflow.com/a/7780289
                try {
                    File photoThumbnailFile = createImageFile(ApplicationConstants.FILENAME_IMAGE_THUMBNAIL);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    thumbnailBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                    byte[] bitmapData = bos.toByteArray();
                    //write the bytes in file
                    FileOutputStream fos = new FileOutputStream(photoThumbnailFile);
                    fos.write(bitmapData);
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        finish();
    }

    //http://stackoverflow.com/a/18183552
    private Bitmap getThumbnailBitmap(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;
        return BitmapFactory.decodeFile(path, options);
    }

    String mCurrentPhotoPath;

    private File createImageFile(String imageFileName) throws IOException {
        // Create an image file name
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = new File(storageDir, imageFileName + ".jpg");

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile(ApplicationConstants.FILENAME_IMAGE_PHOTO);
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, ApplicationConstants.REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void deleteBitmapAndThumbnail() {
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = new File(storageDir, ApplicationConstants.FILENAME_IMAGE_PHOTO + ".jpg");
        File imageThumbnail = new File(storageDir, ApplicationConstants.FILENAME_IMAGE_THUMBNAIL + ".jpg");
        image.delete();
        imageThumbnail.delete();
    }
}
