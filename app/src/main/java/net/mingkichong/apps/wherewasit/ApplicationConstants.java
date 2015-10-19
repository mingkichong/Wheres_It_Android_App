package net.mingkichong.apps.wherewasit;

/**
 * Created by mkc on 25/09/2015.
 */
public class ApplicationConstants {
    public static final String FILENAME_RANDOM_STRING = "EDEA64DC743FFAB4FBBEE3";
    public static final String FILENAME_COORDINATES = "coordinates_file" + FILENAME_RANDOM_STRING;
    public static final String FILENAME_IMAGE_PHOTO = "image_photo_file" + FILENAME_RANDOM_STRING;
    public static final String FILENAME_IMAGE_THUMBNAIL = "image_photo_thumbnail_file" + FILENAME_RANDOM_STRING;

    public static final String RECORDED_LATITUDE_KEY = "RECORDED_LATITUDE_KEY_STRING";
    public static final String RECORDED_LONGITUDE_KEY = "RECORDED_LONGITUDE_KEY_STRING";
    public static final String RECORDED_ADDRESS_KEY = "RECORDED_ADDRESS_KEY_STRING";
    public static final String RECORDED_WITH_IMAGE_KEY = "RECORDED_WITH_IMAGE_KEY_STRING";
    public static final String RECORDED_CURRENT_TIME = "RECORDED_CURRENT_TIME_STRING";

    public static final String MARKER_SNIPPET_INFO_SPLIT_PATTERN = "|-|";

    public static final int DEFAULT_MAP_ZOOM_LEVEL = 14;

    public static final int REQUEST_IMAGE_CAPTURE = 1;

    public static final String FONT_STYLE = "fonts/abel.ttf";

    public static final String DEFAULT_NO_LOCATION_ALERT_DIALOG_TITLE = "No Recorded Location Found";
    public static final String NO_ADDRESS_INFO_FROM_GOOGLE = "Address Info Unavailable";

    public static final String DEFAULT_ADDRESS = "No address on record";
    public static final String DEFAULT_INVALID_COORDINATES = "No geographic coordinates";
//    public static final long DEFAULT_TIMESTAMP = -1;
//    public static final double DEFAULT_LATITUDE = -1.0;
//    public static final double DEFAULT_LONGITUDE = -1.0;
//    public static final boolean DEFAULT_WITH_IMAGE = false;

//    public static final String HELP_MESSAGE_CLEAR_DATA = "Erase recorded location data";
//    public static final String HELP_MESSAGE_RECORD_LOCATION = "Record the current geo-location only";
//    public static final String HELP_MESSAGE_RECORD_WITH_IMAGE = "Record the current geo-location and take an image snapshot";
//    public static final String HELP_MESSAGE_SHOW_ON_MAP = "Show recorded location on a map";
}
