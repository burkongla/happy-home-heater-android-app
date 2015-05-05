package no.ntnu.ttm4115.happyhomeheater;

/**
 * Created by Stian on 04.05.2015.
 */
import no.ntnu.ttm4115.happyhomeheater.Connection.ConnectionStatus;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;

import android.content.Context;
import android.widget.Toast;

class ActionListener implements IMqttActionListener {

    /**
     * Actions that can be performed Asynchronously <strong>and</strong> associated with a
     * {@link ActionListener} object
     *
     */
    enum Action {
        /** Connect Action **/
        CONNECT,
        /** Disconnect Action **/
        DISCONNECT,
        /** Subscribe Action **/
        SUBSCRIBE,
        /** Publish Action **/
        PUBLISH
    }

    /**
     * The {@link Action} that is associated with this instance of
     * <code>ActionListener</code>
     **/
    private Action action;
    /** The arguments passed to be used for formatting strings**/
    private String[] additionalArgs;
    /** Handle of the {@link Connection} this action was being executed on **/
    private String clientHandle;
    /** {@link Context} for performing various operations **/
    private Context context;

    /**
     * Creates a generic action listener for actions performed for any activity
     *
     * @param context
     *            The application context
     * @param action
     *            The action that is being performed
     * @param clientHandle
     *            The handle for the client which the action is being performed
     *            on
     * @param additionalArgs
     *            Used for as arguments for string formating
     */
    public ActionListener(Context context, Action action,
                          String clientHandle, String... additionalArgs) {
        this.context = context;
        this.action = action;
        this.clientHandle = clientHandle;
        this.additionalArgs = additionalArgs;
    }

    /**
     * The action associated with this listener has been successful.
     *
     * @param asyncActionToken
     *            This argument is not used
     */
    @Override
    public void onSuccess(IMqttToken asyncActionToken) {
        switch (action) {
            case CONNECT :
                connect();
                break;
            case DISCONNECT :
                disconnect();
                break;
            case SUBSCRIBE :
                subscribe();
                break;
            case PUBLISH :
                publish();
                break;
        }

    }

    /**
     * A publish action has been successfully completed, update connection
     * object associated with the client this action belongs to, then notify the
     * user of success
     */
    private void publish() {
        Notify.toast(context, "Published", Toast.LENGTH_SHORT);
    }

    /**
     * A subscribe action has been successfully completed, update the connection
     * object associated with the client this action belongs to and then notify
     * the user of success
     */
    private void subscribe() {
        Notify.toast(context, "Subscribed", Toast.LENGTH_SHORT);

    }

    /**
     * A disconnection action has been successfully completed, update the
     * connection object associated with the client this action belongs to and
     * then notify the user of success.
     */
    private void disconnect() {
        Notify.toast(context, "Disconnected", Toast.LENGTH_SHORT);
    }

    /**
     * A connection action has been successfully completed, update the
     * connection object associated with the client this action belongs to and
     * then notify the user of success.
     */
    private void connect() {
        Notify.toast(context, "Connected", Toast.LENGTH_SHORT);
    }

    /**
     * The action associated with the object was a failure
     *
     * @param token
     *            This argument is not used
     * @param exception
     *            The exception which indicates why the action failed
     */
    @Override
    public void onFailure(IMqttToken token, Throwable exception) {
        switch (action) {
            case CONNECT :
                connect(exception);
                break;
            case DISCONNECT :
                disconnect(exception);
                break;
            case SUBSCRIBE :
                subscribe(exception);
                break;
            case PUBLISH :
                publish(exception);
                break;
        }

    }

    /**
     * A publish action was unsuccessful, notify user and update client history
     *
     * @param exception
     *            This argument is not used
     */
    private void publish(Throwable exception) {
        Notify.toast(context, "Error publishing", Toast.LENGTH_SHORT);

    }

    /**
     * A subscribe action was unsuccessful, notify user and update client history
     * @param exception This argument is not used
     */
    private void subscribe(Throwable exception) {
        Notify.toast(context, "Error subscribing", Toast.LENGTH_SHORT);

    }

    /**
     * A disconnect action was unsuccessful, notify user and update client history
     * @param exception This argument is not used
     */
    private void disconnect(Throwable exception) {
        Notify.toast(context, "Error disconnecting", Toast.LENGTH_SHORT);

    }

    /**
     * A connect action was unsuccessful, notify the user and update client history
     * @param exception This argument is not used
     */
    private void connect(Throwable exception) {
        Notify.toast(context, "Error connecting", Toast.LENGTH_SHORT);

    }

}