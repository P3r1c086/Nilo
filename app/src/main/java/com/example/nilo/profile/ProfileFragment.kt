package com.example.nilo.profile

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.nilo.Constants
import com.example.nilo.R
import com.example.nilo.databinding.FragmentProfileBinding
import com.example.nilo.product.MainAux
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

/**
 * Proyect: Nilo
 * From: com.example.nilo.profile
 * Create by Pedro Aguilar Fernández on 06/04/2023 at 16:52
 * More info: linkedin.com/in/pedro-aguilar-fernández-167753140
 * All rights reserved 2023
 **/
class ProfileFragment : Fragment() {

    private var binding: FragmentProfileBinding? = null

    //variables globales para cargar imagen en la imageView o subirlo a cloud Storage
    private var photoSelectedUri: Uri? = null
    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode == Activity.RESULT_OK){
            photoSelectedUri = it.data?.data

            //Glide tb puede cargar una imagen que venga localmente
            binding?.let {
                //cargar imagen
                Glide.with(this)
                    .load(photoSelectedUri)
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
                    .into(it.ibProfile)
            }
        }
    }

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
//                binding.etPhotoUrl.setText(user.photoUrl.toString())

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
            binding.ibProfile.setOnClickListener {
                openGallery()
            }
            binding.btnUpdate.setOnClickListener {
                //las dos siguientes lineas ocultan el teclado
                binding.etFullName.clearFocus()
//                binding.etPhotoUrl.clearFocus()
                FirebaseAuth.getInstance().currentUser?.let { user ->
                    if (photoSelectedUri == null){//significa que el usuario solo modifico el campo nombre
                        //en este caso la uri estara vacia
                        updateUserProfile(binding, user, Uri.parse(""))
                    }else{//sino significa que el usuario ha elegido una imagen de la galeria
                        uploadReducedImage(user)
                    }
                }
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher.launch(intent)
    }

    private fun updateUserProfile(binding: FragmentProfileBinding, user: FirebaseUser, uri: Uri) {
        //creamos un objeto para poder editar nombre y foto del user
        val profileUpdated = UserProfileChangeRequest.Builder()
            .setDisplayName(binding.etFullName.text.toString().trim())
            //aqui ponemos Uri.parse porque en setPhotoUri nos esta pidiendo una Uri
            .setPhotoUri(uri)
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

    /**
     * metodo para subir imagenes comprimidas al storage
     */
    private fun uploadReducedImage(user: FirebaseUser){

        //hacemos referencia a la carpeta contenedora de cada usuario
        val profileRef = FirebaseStorage.getInstance().reference.child(user.uid)
            //ponemos como hijo una carpeta donde almacenar las imagenes
            //creamos una nueva referencia que apunta al id de la foto
            .child(Constants.PATH_PROFILE).child(Constants.MY_PHOTO)
        //si photoSelectedUri es != de null y binding tb
        photoSelectedUri?.let { uri ->
            binding?.let { binding ->
                //validamos que el metodo de getBitmapFromUri no es null
                getBitmapFromUri(uri)?.let { bitmap ->
                    //ahora subimos la imagen en formato bitmap
                    //hacemos visible el progressbar
                        binding.progressBar.visibility = View.VISIBLE

                    //subiremos la foto en vez de con un URL, con un BitMap
                    val baos = ByteArrayOutputStream()
                    //comprimimos el bitmap. JPEG es el formato con menos peso
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                    //comenzamos a subir la imagen. uri es photoSelectedUri
                    profileRef.putBytes(baos.toByteArray())
                        //para la barra de progreso al subir la foto
                        .addOnProgressListener {
                            //con esto obtenemos los bytes tranferidos respecto al total
                            val progress = (100 * it.bytesTransferred / it.totalByteCount).toInt()
                            it.run {
                                    binding.progressBar.progress = progress
                                    binding.tvProgress.text = String.format("%s%%", progress)
                            }
                        }
                        .addOnCompleteListener {
                            binding.progressBar.visibility = View.INVISIBLE
                            //limpiar el texto que se encuentra dentro del progress
                            binding.tvProgress.text = ""
                        }
                        .addOnSuccessListener {
                            //extraemos la url para descargar
                            it.storage.downloadUrl.addOnSuccessListener { downloadUrl ->
                                //actualizar la url de autentication en base a la url de Storage
                                updateUserProfile(binding, user, downloadUrl)

                            }
                        }
                        .addOnFailureListener{
                            Toast.makeText(activity, "Error al subir imagen.", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }
    }

    /**
     * Metodo para no saturar el proceso de construir un BitMap a partir de nuestra URI
     */
    private fun getBitmapFromUri(uri: Uri): Bitmap?{
        //verificamos si nuestra activity es diferente de null
        activity?.let {
            //construimos un bitmap. Para versiones mas modernas de android se hara asi
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
                val source = ImageDecoder.createSource(it.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            }else{
                //para versiones anteriores a android P se hara de esta forma
                MediaStore.Images.Media.getBitmap(it.contentResolver, uri)
            }
            return  getResizedImage(bitmap, 256)
        }
        return null//retornamos null en caso de que la activity sea null
    }

    /**
     * Metodo para redimensionar las imagenes
     */
    private fun getResizedImage(image: Bitmap, maxSize: Int): Bitmap {
        var width = image.width
        var height = image.height
        if (width <= maxSize && height <= maxSize) return image

        //si entra aqui es porque la imagen tiene una dimension mas grande que el tamaña max
        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1){
            width = maxSize
            //la altura sera de un tamaño proporcional
            height = (width / bitmapRatio).toInt()
        }else{
            height = maxSize
            //la anchura sera de un tamaño proporcional
            width = (height / bitmapRatio).toInt()
        }
        //procedemos a crear esa escala del bitmap
        return Bitmap.createScaledBitmap(image, width, height, true)
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