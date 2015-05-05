package no.ntnu.ttm4115.happyhomeheater;

import android.net.Uri;
import com.google.android.gms.location.Geofence;

/**
 * Created by Stian on 01.05.2015.
 */
public class Constants {

    private Constants() {
    }

    public static final String TAG = "Happy home heater";

    // Request code to attempt to resolve Google Play services connection failures.
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    // Timeout for making a connection to GoogleApiClient (in milliseconds).
    public static final long CONNECTION_TIME_OUT_MS = 100;

    // Geofence parameters for the Android building on Google's main campus in Mountain View.
    public static final String GEOFENCE_ID = "Home";
    public static final float GEOFENCE_RADIUS = 50;
    public static final long GEOFENCE_EXPIRATION_TIME = Geofence.NEVER_EXPIRE;

    // The constants below are less interesting than those above.

    // Path for the DataItem containing the last geofence id entered.
    public static final String GEOFENCE_DATA_ITEM_PATH = "/geofenceid";
    public static final Uri GEOFENCE_DATA_ITEM_URI =
            new Uri.Builder().scheme("hhh").path(GEOFENCE_DATA_ITEM_PATH).build();
    public static final String KEY_GEOFENCE_ID = "geofence_id";

    // Keys for flattened geofences stored in SharedPreferences.
    public static final String KEY_LATITUDE = "no.ntnu.ttm4115.happyhomeheater.KEY_LATITUDE";
    public static final String KEY_LONGITUDE = "no.ntnu.ttm4115.happyhomeheater.KEY_LONGITUDE";
    public static final String KEY_RADIUS = "no.ntnu.ttm4115.happyhomeheater.KEY_RADIUS";
    public static final String KEY_EXPIRATION_DURATION =
            "no.ntnu.ttm4115.happyhomeheater.KEY_EXPIRATION_DURATION";
    public static final String KEY_TRANSITION_TYPE =
            "no.ntnu.ttm4115.happyhomeheater.KEY_TRANSITION_TYPE";
    // The prefix for flattened geofence keys.
    public static final String KEY_PREFIX = "no.ntnu.ttm4115.happyhomeheater.KEY";

    // Invalid values, used to test geofence storage when retrieving geofences.
    public static final long INVALID_LONG_VALUE = -999l;
    public static final float INVALID_FLOAT_VALUE = -999.0f;
    public static final int INVALID_INT_VALUE = -999;
}
