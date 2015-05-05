package no.ntnu.ttm4115.happyhomeheater;

import android.app.Application;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.WifiConfiguration;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationServices;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import no.ntnu.ttm4115.happyhomeheater.ActionListener.Action;

import static no.ntnu.ttm4115.happyhomeheater.ActivityConstants.clientId;
import static no.ntnu.ttm4115.happyhomeheater.ActivityConstants.server;
import static no.ntnu.ttm4115.happyhomeheater.ActivityConstants.defaultPort;

import static no.ntnu.ttm4115.happyhomeheater.ActivityConstants.topic;
import static no.ntnu.ttm4115.happyhomeheater.Constants.GEOFENCE_EXPIRATION_TIME;
import static no.ntnu.ttm4115.happyhomeheater.Constants.GEOFENCE_ID;
import static no.ntnu.ttm4115.happyhomeheater.Constants.GEOFENCE_RADIUS;
import static no.ntnu.ttm4115.happyhomeheater.Constants.CONNECTION_FAILURE_RESOLUTION_REQUEST;
import static no.ntnu.ttm4115.happyhomeheater.Constants.TAG;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    NumberPicker DesiredTemperature;
    String CurrentTemperature;
    SharedPreferences sharedPref;
    Switch OnOffSwitch;
    TextView CurrTemp;
    TextView StatusText;

    public static final String MyPREFERENCES = "MyPrefs";
    public static final String DesiredTemp = "desiredTempKey";
    public static final String CurrentTemp = "currentTempKey";
    public static final String TimeToHome = "timeToHomeKey";
    public static final String OnOff = "onOffKey";
    private static final String clientHandle = "mobileClientHandle";
    private int temp;

    private Connection connection;
    private ChangeListener changeListener = new ChangeListener();
    private ResponseReceiver receiver;
    MqttAndroidClient client;
    private SharedPreferences.OnSharedPreferenceChangeListener prefListener;


    //Geofence viarables:
    List<Geofence> GeofenceList;
    private SimpleManagement mSimpleManagement;
    private GoogleApiClient mApiClient;

    private SimpleGeofence homeGeofence;
    private LocationServices locationService;
    private PendingIntent geofenceRequestIntent;

    private enum REQUEST_TYPE {ADD, REMOVE}

    private REQUEST_TYPE mRequestType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IntentFilter filter = new IntentFilter(ResponseReceiver.ACTION_RESP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new ResponseReceiver();
        registerReceiver(receiver, filter);

        sharedPref = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        prefListener=new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                Log.d("Note", "sharePrefListener");
                if (key.equals(CurrentTemp)) {
                    CurrTemp = (TextView) findViewById(R.id.currentTemperature);
                    String ctemp = sharedPref.getString(CurrentTemp, "20°");
                    CurrTemp.setText(ctemp);
                    //CurrTemp.invalidate();
                }
            }
        };
        sharedPref.registerOnSharedPreferenceChangeListener(prefListener);

        if (!isGooglePlayServicesAvailable()) {
            Log.e(TAG, "Google Play services unavailable.");
            finish();
            return;
        }

        mqttConnect();

        Log.d("building", "mAPIClient building");
        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        Log.d("Connecting", "Trying to connect.");
        if (mApiClient != null) {
            mApiClient.connect();
        }
        Log.d("Connected", "mAPIClient connected");
        // Instantiate a new geofence storage area.
        mSimpleManagement = new SimpleManagement(this);
        // Instantiate the current List of geofences.
        GeofenceList = new ArrayList<Geofence>();

        createGeofences();

        if (sharedPref.contains(DesiredTemp)) {
            temp = sharedPref.getInt(DesiredTemp, 20);
            Log.d("Enters contains", String.valueOf(temp));

        } else if (!sharedPref.contains(DesiredTemp)) {
            temp = 20;
            Log.d("Enters not contains", String.valueOf(temp));
        }
        if (sharedPref.contains(TimeToHome)) {

        }
        Log.d("Value numbpicker", String.valueOf(temp));
        DesiredTemperature = (NumberPicker) findViewById(R.id.desiredTemperature);
        DesiredTemperature.setMinValue(15);
        DesiredTemperature.setMaxValue(25);
        DesiredTemperature.setValue(temp);
        DesiredTemperature.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt(DesiredTemp, newVal);
                editor.commit();
            }
        });

        OnOffSwitch = (Switch) findViewById(R.id.onOffToggle);
        OnOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(OnOff, isChecked);
                editor.commit();
            }
        });
        if (sharedPref.contains(OnOff)) {
            OnOffSwitch.setChecked(sharedPref.getBoolean(OnOff, true));
        } else {
            OnOffSwitch.setChecked(false);
        }
        CurrTemp = (TextView) findViewById(R.id.currentTemperature);
        if (sharedPref.contains(CurrentTemp)) {
            CurrTemp.setText(sharedPref.getString(CurrentTemp, "0"));
        }
        Log.d("onCreate", "Finished");
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void createGeofences() {
        Log.d("createGeofences", "Starting");
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
        Log.d("Longitude", String.valueOf(longitude));
        Log.d("Latitude", String.valueOf(latitude));

        // Create internal "flattened" objects containing the geofence data.
        homeGeofence = new SimpleGeofence(
                GEOFENCE_ID,                // geofenceId.
                latitude,
                longitude,
                GEOFENCE_RADIUS,
                GEOFENCE_EXPIRATION_TIME,
                Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT
        );

        // Store these flat versions in SharedPreferences and add them to the geofence list.
        mSimpleManagement.setGeofence(GEOFENCE_ID, homeGeofence);
        GeofenceList.add(homeGeofence.toGeofence());
        Log.d("createGeofences", "Finished");
        Log.d("Geofence", String.valueOf(GeofenceList));
        Log.d("Geofence lat", String.valueOf(mSimpleManagement.getGeofence("Home").getLatitude()));
        Log.d("Geofence long", String.valueOf(mSimpleManagement.getGeofence("Home").getLongitude()));
    }

    private PendingIntent getGeofencePendingIntent() {
        Log.d("PendingIntent", "Starting");
        // Reuse the PendingIntent if we already have it.
        if (geofenceRequestIntent != null) {
            return geofenceRequestIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        Log.d("PendingIntent", "Finished");
        return PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("connFailed", "Starting");
        // If the error has a resolution, start a Google Play services activity to resolve it.
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG, "Exception while resolving connection error.", e);
            }
        } else {
            int errorCode = connectionResult.getErrorCode();
            Log.e(TAG, "Connection to Google Play services failed with error code " + errorCode);
        }
        Log.d("connFailed", "Finished");
    }

    public void onDisconnected() {
    }

    /**
     * Once the connection is available, send a request to add the Geofences.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d("onConnected", "Starting");

        // Get the PendingIntent for the geofence monitoring request.
        // Send a request to add the current geofences.
        geofenceRequestIntent = getGeofencePendingIntent();
        LocationServices.GeofencingApi.addGeofences(mApiClient, GeofenceList,
                geofenceRequestIntent);
        Toast.makeText(this, getString(R.string.start_geofence_service), Toast.LENGTH_SHORT).show();
        Log.d("onConnected", "Finishing");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("onConnSusp", "Starting");
        if (null != geofenceRequestIntent) {
            LocationServices.GeofencingApi.removeGeofences(mApiClient, geofenceRequestIntent);
        }
        Log.d("onConnSusp", "Ended");
    }

    private boolean isGooglePlayServicesAvailable() {
        Log.d("isplayservices", "Starting");
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == resultCode) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Google Play services is available.");
            }
            return true;
        } else {
            Log.e(TAG, "Google Play services is unavailable.");
            return false;
        }
    }

    public void setHome(View view) {

        createGeofences();
        if (OnOffSwitch.isChecked()) {
            setStatusText("Heating home");
            if (sharedPref.contains(DesiredTemp)) {
                temp = sharedPref.getInt(DesiredTemp, 20);
                mqttPublish("Desired: " + temp);
            }

            mqttSubscribe();
        }
    }

    public void setStatusText(String text) {
        TextView statusText = (TextView) findViewById(R.id.statusText);
        statusText.setText(text);
    }

    public void mqttConnect() {
        MqttConnectOptions conOpt = new MqttConnectOptions();
        String uri = null;
        uri = "tcp://" + server + ":" + defaultPort;
        client = createClient(this, uri, clientId);
        connection = new Connection(clientHandle, clientId, server, defaultPort,
                this, client, false);
        client.setCallback(new MqttCallbackHandler(this, clientHandle));
        client.setTraceCallback(new MqttTraceCallback());
        try {
            client.connect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void mqttDisconnect() {
        try {
            connection.getClient().disconnect();
        }
        catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void mqttReconnect() {

    }

    public void mqttSubscribe() {
        try {
            String[] topics = new String[1];
            topics[0] = topic;
            Log.d("ServerURISub", client.getServerURI());
            client.subscribe(topic, 2, null, new ActionListener(this, Action.SUBSCRIBE, clientHandle, topics));
        }
        catch (MqttSecurityException e) {
            Log.e(this.getClass().getCanonicalName(), "Failed to subscribe to" + topic + " the client with the handle " + clientHandle, e);
        }
        catch (MqttException e) {
            Log.e(this.getClass().getCanonicalName(), "Failed to subscribe to" + topic + " the client with the handle " + clientHandle, e);
        }

    }

    public void mqttPublish(String message) {
        String[] args = new String[1];
        args[0] = message;
        Log.d("ServerURI", client.getServerURI());
        Log.d("Message", message);

        try {
            client.publish("hhh/server", message.getBytes(), 2, false, null, new ActionListener(this, Action.PUBLISH, clientHandle, args));
        }
        catch (MqttSecurityException e) {
            Log.e(this.getClass().getCanonicalName(), "Failed to publish a messged from the client with the handle " + clientHandle, e);
        }
        catch (MqttException e) {
            Log.e(this.getClass().getCanonicalName(), "Failed to publish a messged from the client with the handle " + clientHandle, e);
        }

    }

    public MqttAndroidClient createClient(Context context, String serverURI, String clientId)
    {
        MqttAndroidClient client = new MqttAndroidClient(context, serverURI, clientId);
        return client;
    }

    private class ChangeListener implements PropertyChangeListener {

        /**
         * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
         */
        @Override
        public void propertyChange(PropertyChangeEvent event) {

            if (!event.getPropertyName().equals(ActivityConstants.ConnectionStatusProperty)) {
                return;
            }
        }

    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public class ResponseReceiver extends BroadcastReceiver {
        public static final String ACTION_RESP =
                "no.ntnu.ttm4115.happyhomeheater.intent.action.MESSAGE_PROCESSED";

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("onReceive", "Entered");
            TextView StatusText = (TextView) findViewById(R.id.statusText);
            if (intent.getStringExtra(GeofenceTransitionsIntentService.PARAM_OUT_MSG) == getString(R.string.entering_geofence)) {
                String text = intent.getStringExtra(GeofenceTransitionsIntentService.PARAM_OUT_MSG);
                StatusText.setText(text);
                temp = sharedPref.getInt(DesiredTemp, 20);
                mqttPublish("Desired: " + temp);
                Log.d("onReceive", "Entered");

            } else if (intent.getStringExtra(GeofenceTransitionsIntentService.PARAM_OUT_MSG) == getString(R.string.exiting_geofence)) {
                String text = intent.getStringExtra(GeofenceTransitionsIntentService.PARAM_OUT_MSG);
                StatusText.setText(text);
                mqttPublish("Desired: " + 15);
                Log.d("onReceive", "Exited");
            }
            Log.d("onReceive", "Exited");
        }
    }
    @Override
    protected void onDestroy() {
        client.unregisterResources();
        super.onDestroy();
    }


}
