package com.example.nilo.product

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import com.example.nilo.Constants
import com.example.nilo.R
import com.example.nilo.cart.CartFragment
import com.example.nilo.databinding.ActivityMainBinding
import com.example.nilo.detail.DetailFragment
import com.example.nilo.entities.Product
import com.example.nilo.order.OrderActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity(), OnProductListener, MainAux {

    private lateinit var binding : ActivityMainBinding

    //Estas dos variables globales sirven para detectar el estado actual de una sesion, con el fin
    // de retener una sesioon activa
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener

    private lateinit var adapter: ProductAdapter

    //variable para listar en tiempo real
    private lateinit var firestoreListener: ListenerRegistration

    private var productSelected: Product? = null
    private val productCartList = mutableListOf<Product>()

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        //dentro procesamos la respuesta
        val response = IdpResponse.fromResultIntent(it.data)
        if (it.resultCode == RESULT_OK){
            //corroboramos que exista un usuario autenticado
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null){
                Toast.makeText(this, "Bienvenido", Toast.LENGTH_SHORT).show()

                //Una vez que se le da la bienvenida al usuario, podemos proceder a registrar su token
                //Antes tenemos que extraerlo
                val preferences = PreferenceManager.getDefaultSharedPreferences(this)
                val token = preferences.getString(Constants.PROP_TOKEN, null)

                token?.let {
                    val db = FirebaseFirestore.getInstance()
                    val tokenMap = hashMapOf(Pair(Constants.PROP_TOKEN, token))

                    //creamos una nueva coleccion
                    db.collection(Constants.COLL_USERS)
                        //creamos un nuevo documento
                        .document(user.uid)
                        //como el usuario podria tener la app en varios dispositivos creamos una nueva coleccion
                        .collection(Constants.COLL_TOKENS)
                        //dentro de esta nueva coleccion procedemos a agregar el token
                        .add(tokenMap)
                        .addOnSuccessListener {
                            Log.i("registered token", token)
                            //Limpiamos las preferencias
                            preferences.edit {
                                putString(Constants.PROP_TOKEN, null)
                                    //aplicamos los cambios
                                    .apply()
                            }
                        }
                        .addOnFailureListener {
                            Log.i("no registered token", token)
                        }
                }
            }
        }else{
            //para poder salirnos de la app y que no vuelva a lanzar esa pantalla de inicio de sesion
            if (response == null){//significa que el usuario ha pulsado hacia atras
                Toast.makeText(this, "Hasta pronto", Toast.LENGTH_SHORT).show()
                //finalizamos la actividad
                finish()
            }else{//si se ha producido algun tipo de error distinto de que el usuario retroceda porque quiere
                //let quiere decir que si no es null haga lo que le indicamos entre llaves
                response.error?.let {
                    if (it.errorCode == ErrorCodes.NO_NETWORK){//si pulsas en NO_NETWORK se ven todos los tipos de errores que puedes abordar
                        Toast.makeText(this, "Sin red.", Toast.LENGTH_SHORT).show()
                    }else{//si el error no esta contemplado
                        Toast.makeText(this, "Código de error: ${it.errorCode}",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configAuth()
        configRecyclerView()
        configButtons()

        //FCM
//        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
//            if (task.isSuccessful){
//                val token = task.result
//                Log.i("get token", token.toString())
//            }else{
//
//                Log.i("get token", task.exception.toString())
//            }
//        }
    }

    private fun configAuth(){
        //instanciamos las variables globales
        firebaseAuth = FirebaseAuth.getInstance()
        authStateListener = FirebaseAuth.AuthStateListener { auth ->
            //si el usuario que entra esta autenticado
            if (auth.currentUser != null){
                //ponemos el nombre del usuario en la barra de accion
                supportActionBar?.title = auth.currentUser?.displayName
                //por default el contenido estara oculto y si el usuario se autentica correctamente
                //podra ver el contenido de la app, en este caso tvInit, pero se puede cambiar por un
                // contenedor , un formulario, etc, es decir el padre que contenga el contenido.
                binding.nsvProducts.visibility = View.VISIBLE
                binding.llProgress.visibility = View.GONE
            }else{ //si no lo esta
                //crear variable con todos los proveedores de autenticado
                val providers = arrayListOf(
                    AuthUI.IdpConfig.EmailBuilder().build(),
                    AuthUI.IdpConfig.GoogleBuilder().build())

                resultLauncher.launch(
                    AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .setIsSmartLockEnabled(false)//para que no aparezca el dialog con las opciones de los usuarios que ya han logueado antes
                    .build())
            }
        }
    }

    //VINCULAR LAS DOS VARIABLES GLOBALES EN LOS METODOS DEL CICLO DE VIDA onResume() Y onPause()
    override fun onResume() {
        super.onResume()
        firebaseAuth.addAuthStateListener(authStateListener)
        // Lo agregaremos cada vez que se reanude la app.
        configFirestoreRealtime()
    }

    override fun onPause() {
        super.onPause()
        //sera para remover
        firebaseAuth.removeAuthStateListener(authStateListener)
        //quitamos el listener cada vez que se pause la app para que no se quede constantemente
        // escuchando y la app consuma menos recursos.
        firestoreListener.remove()
    }

    private fun configRecyclerView(){
        //inicializar el adaptador
        adapter = ProductAdapter(mutableListOf(), this)
        binding.recyclerView.apply {
            // 2 es el num de columnas, HORIZONTAL es la orientacion
            layoutManager = GridLayoutManager(this@MainActivity, 2,
                GridLayoutManager.VERTICAL, false)
            adapter = this@MainActivity.adapter
        }
    }

    private fun configButtons(){
        binding.btnViewCart.setOnClickListener {
            //hacemos una nueva instancia de nuestro fragmento
            val fragment = CartFragment()
            fragment.show(supportFragmentManager.beginTransaction(), CartFragment::class.java.simpleName)
        }

    }

    //inflar el munu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    //programarle las acciones al menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_sign_out ->{
                AuthUI.getInstance().signOut(this)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Sesión terminada.", Toast.LENGTH_SHORT).show()
                    }
                    .addOnCompleteListener {
                        if (it.isSuccessful){//si la tarea fue exitosa, en este caso es cerrar sesion
                            //haremos invisible el contenido
                            binding.nsvProducts.visibility = View.GONE
                            binding.llProgress.visibility = View.VISIBLE
                        }else{
                            Toast.makeText(this, "No se puedo cerrar la sesión.", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
            R.id.action_order_history -> startActivity(Intent(this, OrderActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    private fun configFirestoreRealtime(){
        //instanciamos la bd
        val db = FirebaseFirestore.getInstance()
        //creamos una referencia a la coleccion donde estan los productos
        val productRef = db.collection(Constants.COLL_PRODUCTS)

        //para capturar los cambios
        firestoreListener = productRef.addSnapshotListener { snapshots, error ->
            if (error != null){
                Toast.makeText(this, "Error al consultar datos.", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }
            //en caso de que no exista error
            for (snapshot in snapshots!!.documentChanges){

                //extraer cada documento y convertirlo a producto
                val product = snapshot.document.toObject(Product::class.java)
                //asignamos como id el id aleatorio que crea la bd
                product.id = snapshot.document.id
                //sentencia when para detectar el tipo de evento
                when(snapshot.type){
                    DocumentChange.Type.ADDED -> adapter.add(product)
                    DocumentChange.Type.MODIFIED -> adapter.update(product)
                    DocumentChange.Type.REMOVED -> adapter.delete(product)
                }
            }
        }
    }

    /**
     * Al hacer click se lanza el fragmento con los detalles
     */
    override fun onClick(product: Product) {
        //comprobar que el producto seleccionado no este dentro de compras
        val index = productCartList.indexOf(product)
        if (index != -1){//significa que si fue encontrado el index
            productSelected = productCartList[index]
        }else{//lo agregamos por primera vez
            productSelected = product
        }


        val fragment = DetailFragment()
        supportFragmentManager
            .beginTransaction()
            .add(R.id.containerMain, fragment)
            .addToBackStack(null)
            .commit()
        showButton(false)
    }

    override fun getProductsCart(): MutableList<Product> = productCartList

    override fun getProductSelected(): Product? = productSelected

    override fun showButton(isVisible: Boolean) {
        binding.btnViewCart.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    override fun addProductToCart(product: Product) {
        //comprobar que el producto no este dentro de compras
        val index = productCartList.indexOf(product)
        if (index != -1){//significa que tenemos que actualizar porque si fue encontrado el index
            productCartList.set(index, product)
        }else{//lo agregamos por primera vez
            productCartList.add(product)
        }
        updateTotal()
    }

    override fun updateTotal() {
        var total = 0.0
        productCartList.forEach { product ->
            total += product.totalPrice()
        }
        if (total == 0.0){
            binding.tvTotal.text = getString(R.string.product_empty_cart)
        }else{
            binding.tvTotal.text = getString(R.string.product_full_cart, total)
        }
    }


    override fun clearCart() {
        productCartList.clear()
    }
}