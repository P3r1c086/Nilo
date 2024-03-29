package com.example.nilo.cart

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.example.nilo.Constants
import com.example.nilo.R
import com.example.nilo.databinding.FragmentCartBinding
import com.example.nilo.entities.Order
import com.example.nilo.entities.Product
import com.example.nilo.entities.ProductOrder
import com.example.nilo.order.OrderActivity
import com.example.nilo.product.MainAux
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Proyect: Nilo
 * From: com.example.nilo.cart
 * Create by Pedro Aguilar Fernández on 23/12/2022 at 12:01
 * More info: linkedin.com/in/pedro-aguilar-fernández-167753140
 * All rights reserved 2022
 **/
class CartFragment : BottomSheetDialogFragment(), OnCartListener {

    private var binding: FragmentCartBinding? = null
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    private lateinit var adapter: ProductCartAdapter

    private var totalPrice = 0.0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = FragmentCartBinding.inflate(LayoutInflater.from(activity))

        binding?.let {
            val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
            //A esta variable le configuramos la vista
            bottomSheetDialog.setContentView(it.root)

            //inicializamos la variable bottomSheetBehavior
            bottomSheetBehavior = BottomSheetBehavior.from(it.root.parent as View)
            //manipulamos el comportamiento a traves de los estados
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

            setupRecyclerView()
            setupButtons()

            getProducts()
            return bottomSheetDialog
        }
        return super.onCreateDialog(savedInstanceState)
    }

    private fun setupRecyclerView() {
        binding?.let {
            adapter = ProductCartAdapter(mutableListOf(), this)

            it.recyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = this@CartFragment.adapter
            }

//            (1..5).forEach {
//                val product = Product(it.toString(), "Producto $it", "This product is $it",
//                    "", it, 2.0 * it)
//                adapter.add(product)
//            }
        }
    }

    private fun setupButtons() {
        binding?.let {
            it.ibCancel.setOnClickListener {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
            it.efab.setOnClickListener {
//                requestOrder()
                requestOrderTransction()
            }
        }
    }

    private fun getProducts(){
        (activity as? MainAux)?.getProductsCart()?.forEach {
            adapter.add(it)
        }
    }

    /**
     * Hacemos el proceso para generar la compra
     */
    private fun requestOrder(){
        //Obtenemos el id del usuario cliente de Firebase
        val user = FirebaseAuth.getInstance().currentUser

        user?.let { myUser ->
            enableUI(false)

            val products = hashMapOf<String, ProductOrder>()
            adapter.getProducts().forEach{ product ->
                products.put(product.id!!, ProductOrder(product.id!!, product.name!!, product.newQuantity, product.partnerId))
            }
            val order = Order(clientId = myUser.uid, products = products, totalPrice = totalPrice, status = 1)

            val db = FirebaseFirestore.getInstance()
            db.collection(Constants.COLL_REQUESTS)
                .add(order)
                .addOnSuccessListener {
                    dismiss()//quitar el fragmento
                    (activity as? MainAux)?.clearCart()
                    startActivity(Intent(context, OrderActivity::class.java))

                    Toast.makeText(activity, "Compra realizada.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(activity, "Error al comprar.", Toast.LENGTH_SHORT).show()
                }
                .addOnCompleteListener {
                    enableUI(true)
                }
        }
    }
    /**
     * Hacemos el proceso para generar la compra y que se descuente el numero de articulos en Shop Parner
     */
    private fun requestOrderTransction(){
        //Obtenemos el id del usuario cliente de Firebase
        val user = FirebaseAuth.getInstance().currentUser

        user?.let { myUser ->
            enableUI(false)

            val products = hashMapOf<String, ProductOrder>()
            adapter.getProducts().forEach{ product ->
                products.put(product.id!!, ProductOrder(product.id!!, product.name!!, product.newQuantity, product.partnerId))
            }
            val order = Order(clientId = myUser.uid, products = products, totalPrice = totalPrice, status = 1)

            val db = FirebaseFirestore.getInstance()

            //reservamos el espacio del documento que vamos a sobreescribir
            val requestDoc = db.collection(Constants.COLL_REQUESTS).document()
            //hacemos referencia a la coleccion donde se encuentran los productos
            val productRef = db.collection(Constants.COLL_PRODUCTS)
            //para ejecutar la transaccion por lotes
            db.runBatch { batch ->
                //ponemos todas las transacciones que queremos ejecutar de forma continua, para que
                // pueda ser tratada como una sola transaccion

                //primero insertamos la orden
                batch.set(requestDoc, order)
                //ahora queremos que se vayan descontando las cantidades del inventario de ShorParner
                //con esta funcion se hara una lectura de la cantidad actual del campo quantity,
                // despues hara la resta correspondiente y aplicara los cambios. En caso de fallar
                // porque otro usuario este queriendo comprar el mismo producto, este se va a
                // reintentar automaticamente, lo que garantiza que no sean corruptos los datos.
                order.products.forEach {
                    //debido a que podrian existir dos o mas usuarios queriendo comprar el mismo producto,
                    // vamos a usar una transaccion nativa de Firebase con la clase FieldValue.increment()
                    batch.update(productRef.document(it.key), Constants.PROP_QUANTITY,
                        FieldValue.increment(-it.value.quantity.toLong()))
                }
            }
                .addOnSuccessListener {
                    dismiss()//quitar el fragmento
                    (activity as? MainAux)?.clearCart()
                    startActivity(Intent(context, OrderActivity::class.java))

                    Toast.makeText(activity, "Compra realizada.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(activity, "Error al comprar.", Toast.LENGTH_SHORT).show()
                }
                .addOnCompleteListener {
                    enableUI(true)
                }
        }
    }

    private fun enableUI(enable: Boolean){
        binding?.let {
            it.ibCancel.isEnabled = enable
            it.efab.isEnabled = enable
        }
    }

    override fun onDestroyView() {
        (activity as? MainAux)?.updateTotal()
        super.onDestroyView()
        //al destruir la vista, binding vuelve a ser null
        binding = null
    }

    override fun setQuantity(product: Product) {
        adapter.update(product)
    }

    override fun showTotal(total: Double) {
        totalPrice = total //este total viene del adaptador
        binding?.let {
            it.tvTotal.text = getString(R.string.product_full_cart, total)
        }
    }
}