package com.example.nilo.settings

import android.os.Bundle
import android.widget.Toast
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.example.nilo.R
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        //detectar SwitchPreferenceCompat
        val switchPreferenceCompat = findPreference<SwitchPreferenceCompat>(getString(R.string.pref_offers_key))
        switchPreferenceCompat?.setOnPreferenceChangeListener { preference, newValue ->
            //aqui mandamos la accion de suscribirnos
            //newValue podria ser de cualquier tipo, asi que lo casteamos a boolean
            (newValue as? Boolean)?.let { isChecked ->
                //para enviar notificaciones de forma masiva:
                //creamos un topic
                val topic = getString(R.string.settings_topic_offers)
                if (isChecked){
                    //mensaje para suscribirnos
                    Firebase.messaging.subscribeToTopic(topic)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Notificacines Activadas.", Toast.LENGTH_SHORT).show()
                        }
                }else{
                    Firebase.messaging.unsubscribeFromTopic(topic)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Notificacines Desactivadas.", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            true
        }
    }
}