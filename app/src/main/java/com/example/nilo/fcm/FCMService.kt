package com.example.nilo.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.nilo.Constants
import com.example.nilo.R
import com.example.nilo.order.OrderActivity
import com.example.nilo.product.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Proyect: Nilo
 * From: com.example.nilo.fcm
 * Create by Pedro Aguilar Fernández on 03/01/2023 at 19:35
 * More info: linkedin.com/in/pedro-aguilar-fernández-167753140
 * All rights reserved 2023
 **/
class FCMService : FirebaseMessagingService(){

    //Un token es una clave unica con la cual se puede comunicar el servidor de Firebase, para que
    // de esta forma le haga llegar la notificacion
    /**
     * Se llama cuando se genera un nuevo token para el proyecto predeterminado de Firebase.
     * Esto se invoca (automaticamente?) después de la instalación de la aplicación cuando se genera un token por primera
     * vez, y nuevamente si el token cambia.
     */
    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)

        registerNewTokenLocal(newToken)
    }

    /**
     * Funcion que nos ayuda a guardar ese token dentro del sistema nativo de Android de las preferencias
     */
    private fun registerNewTokenLocal(newToken: String){
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)

        preferences.edit {
            //guardamos el token
            putString(Constants.PROP_TOKEN, newToken)
            //como solo es una variable, procedemos a aplicar los cambios
                .apply()
        }
        Log.i("new token", newToken)
    }

    /**
     * Se llama cuando se recibe un mensaje.
     * Esto también se llama (automaticamente?) cuando se recibe un mensaje de notificación mientras la
     * aplicación está en primer plano. Los parámetros de notificación se pueden recuperar con getNotification.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        //en caso afirmativo significa que probiene de sendNotificationByTokens del php. En caso de
        // que venga de sendNotificationByTopic, data estara vacio
        if (remoteMessage.data.isNotEmpty()){
            sendNotificationByData(remoteMessage.data)
        }

        //verificar que la notificacion no sea null. notification es getNotification
        remoteMessage.notification?.let {
            //antes de llamar al metodo sendNotification, vamos a extraer la imagen de la notificacion
            // en un bitmap con la ayuda de Glide
            //podemos cargar la url directamente aqui o desde el servidor
            val imgUrl = it.imageUrl//"https://i.imgur.com/cfVGIA8.png"
            //si la imagen que viene del servidor es igual a null, llamanos a sendNotification(), le
            // pasamos la notificacion "it" y el bitmap no se lo pasamos porque ya tiene valor null
            if (imgUrl == null){
                sendNotification(it)
            }else{//solo si imgUrl es distinto de null procedemos a cargar esa imagen
                Glide.with(applicationContext)
                    .asBitmap()
                    .load(imgUrl)
                    //en into() hacemos una implementacion de la interfaz
                    .into(object : CustomTarget<Bitmap?>(){
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap?>?
                        ) {
                            //resource es nuestro bitmap ya cargado
                            sendNotification(it, resource)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {}//este metodo no lo vamos a necesitar
                    })
            }
        }
    }

    //Cuando estas en segundo plano o con la app sin iniciar se envia la notificacion con la configuracion
    // del servidor (php), si estas dentro de la app se envia la notificacion con la configuracion de la app (kotlin)
    private fun sendNotificationByData(data: Map<String, String>){
        //construir una notificacion
        //OrderActivity::class.java es la actividad que se va a mostrar al pulsar la notificacion
        val intent = Intent(this, OrderActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT)

        val channelId = getString(R.string.notification_channel_id_default)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_stat_name)
            //title y body son la keys pasadas en el map
            .setContentTitle(data.get("title"))
            .setContentText(data.get("body"))
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setColor(ContextCompat.getColor(this, R.color.yellow_a700))
            .setContentIntent(pendingIntent)
            //hacemos que la notificacion sea expandible para que pueda verse un texto largo
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(data.get("body")))

        val trackIntent = Intent(this, OrderActivity::class.java).apply {
            //estos tres argumentos son requeridos en el metodo getOrder() de TrackFragment, es decir,
            // son requeridos por el fragment que deseamos que abra el boton de la notificacion. Ese
            // fragmento es lanzado desde el metodo checkIntent() de OrderActivity
            val actionIntent = data.get(Constants.ACTION_INTENT)?.toInt()
            val orderId = data.get(Constants.PROP_ID)
            val status = data.get(Constants.PROP_STATUS)?.toInt()
            putExtra(Constants.ACTION_INTENT, actionIntent) // 1 = track
            putExtra(Constants.PROP_ID, orderId)//id del pedido
            putExtra(Constants.PROP_STATUS, status)
        }
        val trackPendingIntent = PendingIntent.getActivity(this, System.currentTimeMillis().toInt(),
            trackIntent, 0)
        //construimos la accion para colocarle un boton a la notificacion
        val action = NotificationCompat.Action.Builder(R.drawable.ic_local_shipping, "Rastrear ahora",
        trackPendingIntent).build()
        notificationBuilder.addAction(action)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(channelId,
                getString(R.string.notification_channel_name_default),
                NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }

    //Cuando estas en segundo plano o con la app sin iniciar se envia la notificacion con la configuracion
    // del servidor (php), si estas dentro de la app se envia la notificacion con la configuracion de la app (kotlin)
    private fun sendNotification(notification: RemoteMessage.Notification, bitmap: Bitmap? = null){
        //construir una notificacion
        //OrderActivity::class.java es la actividad que se va a mostrar al pulsar la notificacion
        val intent = Intent(this, OrderActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT)

        val channelId = getString(R.string.notification_channel_id_default)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle(notification.title)
            .setContentText(notification.body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setColor(ContextCompat.getColor(this, R.color.yellow_a700))
            .setContentIntent(pendingIntent)
            //hacemos que la notificacion sea expandible para que pueda verse un texto largo
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(notification.body))

        //verificamos si bitmap es diferente de null para que si no encuentra la imagen no quede un
        // espacio en blanco en la notificacion
        bitmap?.let {
            notificationBuilder
                //para poner la imagen en pequeño cuando la notificacion este contraida
                .setLargeIcon(bitmap)
                //para poner una imagen, en formato bitmap, en la notificacion
                .setStyle(NotificationCompat.BigPictureStyle()
                    .bigPicture(bitmap)
                    //indicamos que cuando este la notificacion expandida ya no queremos ver el la imagen en pequeño
                    .bigLargeIcon(null))
        }



        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(channelId,
                getString(R.string.notification_channel_name_default),
                NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }
}