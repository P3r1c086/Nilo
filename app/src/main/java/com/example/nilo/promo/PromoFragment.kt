package com.example.nilo.promo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.nilo.R
import com.example.nilo.databinding.FragmentPromoBinding
import com.example.nilo.product.MainAux
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig

class PromoFragment : Fragment() {

    private var binding: FragmentPromoBinding? = null

    //nos va a ayudar a retener el no mbre de usuario que actualmente se usa como titulo dentro de la mainActivity
    private var mainTitle: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPromoBinding.inflate(inflater, container, false)
        binding?.let {
            return  it.root
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configRemoteConfig()
        configActionBar()
    }

    private fun configActionBar() {
        (activity as? AppCompatActivity)?.let {
            it.supportActionBar?.setDisplayHomeAsUpEnabled(true)
            mainTitle = it.supportActionBar?.title.toString()
            it.supportActionBar?.title = getString(R.string.promo_title)
            setHasOptionsMenu(true)//con esto pongo la flecha de retroceso, pero hay que ponerle ese evento a la flecha
        }
    }

    private fun configRemoteConfig() {
        val remoteConfig = Firebase.remoteConfig

        //le agregamos valores por default
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        //le indicamos que solicite (fetch) y active (activate) los cambios despues de la respuesta
        // del servidor
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener {
                if (it.isSuccessful){
                    //comenzamos a extraer los valores independientemente de si son remotos o locales
                    val percentaje = remoteConfig.getDouble("percentaje")
                    val photoUrl = remoteConfig.getString("photoUrl")
                    val message = remoteConfig.getString("message")

                    binding?.let {
                        it.tvMessage.text = message
                        it.tvPercentaje.text = percentaje.toString()

                        Glide.with(this)
                            .load(photoUrl)
                            //definimos que la cache no se encuentre disponible, ya que al tratarse
                            // de promociones, estas pueden vencer muy rapido y no nos combiene que
                            // se este almacenando alguna imagen que pueda confundir al usuario
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .placeholder(R.drawable.ic_access_time)
                            //para que no se quede como un error, vamos a poner lo que tenemos en la oferta
                            .error(R.drawable.ic_local_offer)
                            .centerCrop()
                            .into(it.imgPromo)
                    }
                }
            }
    }

    //evento para que la flecha de retroceso de la appbar vuelva a la home
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home){
            activity?.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        //con esto oculto el boton de ver carrito
        (activity as? MainAux)?.showButton(true)
        binding = null
    }

    override fun onDestroy() {
        (activity as? AppCompatActivity)?.let {
            it.supportActionBar?.setDisplayHomeAsUpEnabled(false)
            it.supportActionBar?.title = mainTitle
            setHasOptionsMenu(false)
        }
        super.onDestroy()
    }
}