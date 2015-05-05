package no.ntnu.ttm4115.happyhomeheater;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Vibrator;
import no.ntnu.ttm4115.happyhomeheater.MainActivity;

import static no.ntnu.ttm4115.happyhomeheater.Constants.CONNECTION_TIME_OUT_MS;
import static no.ntnu.ttm4115.happyhomeheater.Constants.GEOFENCE_DATA_ITEM_PATH;
import static no.ntnu.ttm4115.happyhomeheater.Constants.GEOFENCE_DATA_ITEM_URI;
import static no.ntnu.ttm4115.happyhomeheater.Constants.KEY_GEOFENCE_ID;
import static no.ntnu.ttm4115.happyhomeheater.Constants.TAG;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.PutDataMapRequest;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * Listens for geofence transition changes.
 */
public class GeofenceTransitionsIntentService extends IntentService {
    public static final String PARAM_IN_MSG = "imsg";
    public static final String PARAM_OUT_MSG = "omsg";
    private GoogleApiClient mApiClient;

    public GeofenceTransitionsIntentService() {
        super(GeofenceTransitionsIntentService.class.getSimpleName());
    }

    protected void onHandleIntent(Intent intent) {
        Log.d("HandleIntent", "Handling intent");
        GeofencingEvent geoFenceEvent = GeofencingEvent.fromIntent(intent);
        if (geoFenceEvent.hasError()) {
            int errorCode = geoFenceEvent.getErrorCode();
            Log.e(TAG, "Location Services error: " + errorCode);
        } else {

            int transitionType = geoFenceEvent.getGeofenceTransition();
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(MainActivity.ResponseReceiver.ACTION_RESP);
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            if (Geofence.GEOFENCE_TRANSITION_ENTER == transitionType) {
                // Get the geofence id triggered. Note that only one geofence can be triggered at a
                // time in this example, but in some cases you might want to consider the full list
                // of geofences triggered.
                String triggeredGeoFenceId = geoFenceEvent.getTriggeringGeofences().get(0)
                        .getRequestId();
                showToast(this, R.string.entering_geofence);
                // Vibrate for 500 milliseconds
                v.vibrate(500);
                broadcastIntent.putExtra(PARAM_IN_MSG, getString(R.string.entering_geofence));
                sendBroadcast(broadcastIntent);

            } else if (Geofence.GEOFENCE_TRANSITION_EXIT == transitionType) {
                showToast(this, R.string.exiting_geofence);
                v.vibrate(500);
                // processing done here
                broadcastIntent.putExtra(PARAM_OUT_MSG, getString(R.string.exiting_geofence));
                sendBroadcast(broadcastIntent);
            }
        }
    }

    /**
     * Showing a toast message, using the Main thread
     */
    private void showToast(final Context context, final int resourceId) {
        Handler mainThread = new Handler(Looper.getMainLooper());
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, context.getString(resourceId), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
