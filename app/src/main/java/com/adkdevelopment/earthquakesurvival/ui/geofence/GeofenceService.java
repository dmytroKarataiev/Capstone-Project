/*
 * MIT License
 *
 * Copyright (c) 2016. Dmytro Karataiev
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.adkdevelopment.earthquakesurvival.ui.geofence;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.adkdevelopment.earthquakesurvival.data.objects.earthquake.Feature;
import com.adkdevelopment.earthquakesurvival.ui.DetailActivity;
import com.adkdevelopment.earthquakesurvival.ui.PagerActivity;
import com.adkdevelopment.earthquakesurvival.R;
import com.adkdevelopment.earthquakesurvival.data.objects.earthquake.EarthquakeObject;
import com.adkdevelopment.earthquakesurvival.utils.LocationUtils;
import com.adkdevelopment.earthquakesurvival.utils.Utilities;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Sends
 * Created by karataev on 4/4/16.
 */
public class GeofenceService extends IntentService {

    private static final String TAG = GeofenceService.class.getSimpleName();

    public GeofenceService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        // if the event has an error - log the textual info about it
        if (geofencingEvent.hasError()) {
            String textualError = LocationUtils.getErrorString(this, geofencingEvent.getErrorCode());
            Log.e(TAG, "onHandleIntent: " + textualError);
            return;
        }

        // extract geofence event
        int transition = geofencingEvent.getGeofenceTransition();

        if (transition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                transition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            List<Geofence> geoEvents = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            List<String> geofenceDetails = LocationUtils
                    .getTransitionDetails(this, transition, geoEvents);

            if (Utilities.getNotificationsPrefs(getBaseContext())
                    && geofenceDetails != null && geofenceDetails.size() > 0) {
                sendNotification(geofenceDetails);
            }

        } else {
            Log.e(TAG, getString(R.string.geofence_error_invalid_type, transition));
        }
    }

    /**
     * Sends a notification to the phone
     * @param notificationDetails String with details to show in the notification
     */
    private void sendNotification(List<String> notificationDetails) {

        Context context = getBaseContext();

        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(context, DetailActivity.class);
        notificationIntent.putStringArrayListExtra(Feature.GEOFENCE, (ArrayList<String>) notificationDetails);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(PagerActivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        Bitmap largeIcon = null;
        try {
            largeIcon = Picasso.with(context).load(R.mipmap.ic_launcher).get();
        } catch (IOException e) {
            Log.e(TAG, "e:" + e);
        }

        // Define the notification settings.
        builder.setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setDefaults(Notification.DEFAULT_ALL)
                .setLargeIcon(largeIcon)
                .setColor(Color.RED)
                .setTicker(getString(R.string.geofence_notification))
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.geofence_notification_text))
                .setContentIntent(notificationPendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(notificationDetails.get(0) + "\n"
                                + getString(R.string.geofence_notification_text)))
                .setGroup(EarthquakeObject.NOTIFICATION_GROUP);

        // Get an instance of the Notification manager
        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(context);

        // Issue the notification
        mNotificationManager.notify(EarthquakeObject.NOTIFICATION_ID_1, builder.build());
    }
}
