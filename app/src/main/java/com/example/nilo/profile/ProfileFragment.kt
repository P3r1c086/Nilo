package com.example.nilo.profile

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.nilo.R
import com.example.nilo.databinding.FragmentProfileBinding
import com.example.nilo.product.MainAux
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

/**
 * Proyect: Nilo
 * From: com.example.nilo.profile
 * Create by Pedro Aguilar Fernández on 06/04/2023 at 16:52
 * More info: linkedin.com/in/pedro-aguilar-fernández-167753140
 * All rights reserved 2023
 **/
class ProfileFragment : Fragment() {

    private var binding: FragmentProfileBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater,container,false)
        binding?.let {
            return it.root
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getUser()
        configButtons()
    }

    private fun getUser() {
        binding?.let { binding ->
            FirebaseAuth.getInstance().currentUser?.let { user ->
                binding.etFullName.setText(user.displayName)
                binding.etPhotoUrl.setText(user.photoUrl.toString())

                //cargar imagen
                Glide.with(this)
                    .load(user.photoUrl)
                    //este es para que almacene la imagen descargada, para que no tenga que estar
                    // consultando cada vez que inicie la app. Tiene la desventaja que hasta que no cambie
                    // la url, la imagen va a ser la misma sin importar que el servidor si cambie
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    //poner este icono en lugar de la imagen para que el usuario sepa que la imagen esta
                    // cargando
                    .placeholder(R.drawable.ic_access_time)
                    //poner este icono en lugar de la imagen para que el usuario sepa que la imagen contiene
                    // algun error
                    .error(R.drawable.ic_broken_image)
                    .centerCrop()
                    .circleCrop()
                    .into(binding.ibProfile)

                setupActionBar()
            }
        }
    }


    private fun setupActionBar(){
        (activity as? AppCompatActivity)?.let {
            it.supportActionBar?.setDisplayHomeAsUpEnabled(true)
            it.supportActionBar?.title = getString(R.string.profile_title)
            setHasOptionsMenu(true)//con esto pongo la flecha de retroceso, pero hay que ponerle ese evento a la flecha
        }
    }

    private fun configButtons() {
        binding?.let { binding ->
            binding.btnUpdate.setOnClickListener {
                //las dos siguientes lineas ocultan el teclado
                binding.etFullName.clearFocus()
                binding.etPhotoUrl.clearFocus()

                updateUserProfile(binding)
            }
        }

    }

    private fun updateUserProfile(binding: FragmentProfileBinding) {
        //comprobamos que el usuario no sea nulo
        FirebaseAuth.getInstance().currentUser?.let { user ->
            //creamos un objeto para poder editar nombre y foto del user
            val profileUpdated = UserProfileChangeRequest.Builder()
                .setDisplayName(binding.etFullName.text.toString().trim())
                //aqui ponemos Uri.parse porque en setPhotoUri nos esta pidiendo una Uri
                .setPhotoUri(Uri.parse(binding.etPhotoUrl.text.toString().trim()))
                .build()//con esto construimos el objeto que hemos configurado

            //tenemos que decirle al usuario las nuevas actualizaciones
            user.updateProfile(profileUpdated)
                .addOnSuccessListener {
                    Toast.makeText(activity, "Usuario actualizado.", Toast.LENGTH_SHORT).show()
                    //actualizo el nombre en la actionbar
                    //primero hago el casting correspondiente
                    (activity as? MainAux)?.updateTitle(user)
                    activity?.onBackPressed()//para que se cierre el fragmento
                }
                .addOnFailureListener {
                    Toast.makeText(activity, "Error al actualizar el usuario.", Toast.LENGTH_SHORT).show()
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
            setHasOptionsMenu(false)
        }
        super.onDestroy()
    }
}