package no.ntnu.ttm4115.happyhomeheater;

/**
 * Created by Stian on 04.05.2015.
 */
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import no.ntnu.ttm4115.happyhomeheater.Connection.ConnectionStatus;

/**
 * Handles call backs from the MQTT Client
 *
 */
public class MqttCallbackHandler implements MqttCallback {

    /** {@link Context} for the application used to format and import external strings**/
    private Context context;
    /** Client handle to reference the connection that this handler is attached to**/
    private String clientHandle;
    SharedPreferences sharedPref;

    /**
     * Creates an <code>MqttCallbackHandler</code> object
     * @param context The application's context
     * @param clientHandle The handle to a {@link Connection} object
     */
    public MqttCallbackHandler(Context context, String clientHandle)
    {
        this.context = context;
        this.clientHandle = clientHandle;
    }

    /**
     * @see org.eclipse.paho.client.mqttv3.MqttCallback#connectionLost(java.lang.Throwable)
     */
    @Override
    public void connectionLost(Throwable cause) {
//	  cause.printStackTrace();
        if (cause != null) {

            String message = context.getString(R.string.connection_lost);

            //build intent
            Intent intent = new Intent();
            intent.setClassName(context, "org.eclipse.paho.android.service.sample.ConnectionDetails");
            intent.putExtra("handle", clientHandle);

            //notify the user
            Notify.notifcation(context, message, intent, "MQTT connection lost");
        }

    }

    /**
     * @see org.eclipse.paho.client.mqttv3.MqttCallback#messageArrived(java.lang.String, org.eclipse.paho.client.mqttv3.MqttMessage)
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        Log.d("MessageArv", message.getPayload().toString());
        sharedPref = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        String[] msg = new String(message.getPayload()).split("\\s+");
        if (msg[0].equals("Current:")) {
            //this.CurrentTemperature = Float.valueOf(message[1]);
            String current_temp;
            current_temp = msg[1] + " \u2109";

            Intent intent = new Intent();
            intent.setClassName(context, "no.ntnu.ttm4115.happyhomeheater.ConnectionDetails");
            intent.putExtra("handle", clientHandle);

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("currentTempKey", current_temp);
            editor.commit();
            //notify the user
            Notify.notifcation(context, current_temp, intent, "Current temperature:");


        } else if (msg[0].equals("Desired:")) {
        }
        //create intent to start activity
    }

    /**
     * @see org.eclipse.paho.client.mqttv3.MqttCallback#deliveryComplete(org.eclipse.paho.client.mqttv3.IMqttDeliveryToken)
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // Do nothing
    }

}
