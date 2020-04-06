package com.example.visited_places_app

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.example.visited_places_app.database.DatabaseHelper
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import android.content.Context.NOTIFICATION_SERVICE
import android.app.NotificationManager
import java.time.LocalDateTime
import java.util.*
import kotlin.random.Random


class PlacesCheckerService : Service() {
    var locationManager: LocationManager? = null
    var channelID = "com.example.visited_places_app.channel"

    override fun onBind(intent: Intent?) = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onCreate() {
        Log.i(TAG, "created")

        notificationManager =
            getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        createNotificationChannel(
            channelID,
            "Map application",
            "Example Channel"
        )

        notificationBuilder = Notification.Builder(this, channelID)

        if (locationManager == null)
            locationManager =
                applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        try {
            locationManager?.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                INTERVAL,
                DISTANCE,
                locationListeners[1]
            )
        } catch (e: SecurityException) {
            Log.e(TAG, "Fail to request location update", e)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Network provider does not exist", e)
        }

        try {
            locationManager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                INTERVAL,
                DISTANCE,
                locationListeners[0]
            )
        } catch (e: SecurityException) {
            Log.e(TAG, "Fail to request location update", e)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "GPS provider does not exist", e)
        }
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        val restartService = Intent(
            applicationContext,
            this.javaClass
        )
        restartService.setPackage(packageName)
        val restartServicePI = PendingIntent.getService(
            applicationContext, 1, restartService,
            PendingIntent.FLAG_ONE_SHOT
        )

        val alarmService =
            applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmService.set(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 100,
            restartServicePI
        )
    }

    private fun createNotificationChannel(
        id: String, name: String,
        description: String
    ) {
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(id, name, importance)

        channel.description = description
        channel.enableLights(true)
        channel.lightColor = Color.RED
        channel.enableVibration(true)
        channel.vibrationPattern =
            longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
        notificationManager?.createNotificationChannel(channel)
    }


    companion object {
        val TAG = "LocationTrackingService"

        val INTERVAL = 1000.toLong() // In milliseconds
        val DISTANCE = 10.toFloat() // In meters
        val maxDistance = 40000.0

        var notificationManager: NotificationManager? = null
        var notificationBuilder: Notification.Builder? = null
        var lastNotificationTime: LocalDateTime = LocalDateTime.now()

        val locationListeners = arrayOf(
            LTRLocationListener(LocationManager.GPS_PROVIDER),
            LTRLocationListener(LocationManager.NETWORK_PROVIDER)
        )

        class LTRLocationListener(provider: String) :
            android.location.LocationListener {
            val lastLocation = Location(provider)

            override fun onLocationChanged(location: Location?) {
                lastLocation.set(location)
                Log.i(TAG, "Location changed")

                val places = DatabaseHelper.getAllData()
                for (place in places.keys) {
                    val d = distanceBetween(
                        location!!.latitude,
                        location.longitude,
                        place.latitude,
                        place.longitude
                    )
                    if (d < maxDistance && LocalDateTime.now().isAfter(
                            lastNotificationTime.plusMinutes(
                                10
                            )
                        )
                    ) {
                        val notification = notificationBuilder!!
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("Your favourite place!")
                            .setContentText("Next to you is the place you have been to.")
                            .build()

                        notificationManager!!.notify(Random.nextInt(0, 1000), notification)

                        lastNotificationTime = LocalDateTime.now()
                    }
                }
            }

            override fun onProviderDisabled(provider: String?) {
            }

            override fun onProviderEnabled(provider: String?) {
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            }

            fun distanceBetween(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Float {

                val loc1 = Location(LocationManager.GPS_PROVIDER)
                val loc2 = Location(LocationManager.GPS_PROVIDER)

                loc1.latitude = lat1
                loc1.longitude = lng1

                loc2.latitude = lat2
                loc2.longitude = lng2


                return loc1.distanceTo(loc2)
            }
        }
    }
}
