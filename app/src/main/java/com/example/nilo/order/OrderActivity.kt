package com.example.nilo.order

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nilo.Constants
import com.example.nilo.R
import com.example.nilo.chat.ChatFragment
import com.example.nilo.databinding.ActivityOrderBinding
import com.example.nilo.entities.Order
import com.example.nilo.track.TrackFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class OrderActivity : AppCompatActivity(), OnOrderListener, OrderAux{

    private lateinit var binding: ActivityOrderBinding
    private lateinit var adapter: OrderAdapter

    private lateinit var orderSelected: Order

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupFirestore()
    }

    private fun setupRecyclerView() {
        adapter = OrderAdapter(mutableListOf(), this)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@OrderActivity)
            adapter = this@OrderActivity.adapter
        }
    }

    private fun setupFirestore(){
        FirebaseAuth.getInstance().currentUser?.let { user ->
            val db = FirebaseFirestore.getInstance()

            db.collection(Constants.COLL_REQUESTS)

                    //Querys para bd Firebase

                //primero ordeno los productos por su fecha
//                .orderBy(Constants.PROP_DATE, Query.Direction.ASCENDING)
                //con esto se optienen las ordenes de compra en funcion del id del usuario
//                .whereEqualTo(Constants.PROP_CLIENT_ID, user.uid)
                //con esto le estamos diciendo trae todas los pedidos donde la propiedad estatus
                // tenga cualquiera de los siguientes valores, en este caso 1 o 2
//                .whereIn(Constants.PROP_STATUS, listOf(1, 2))
                //con esto le estamos diciendo trae todas los pedidos donde la propiedad estatus
                //no tenga cualquiera de los siguientes valores, en este caso 2 o 4
//                .whereNotIn(Constants.PROP_STATUS, listOf(2, 4))
                //filtrar a apartir de un valor. Esto nos trae todos los pedidos que sean superiores
                // al valor de status 2
//                .whereGreaterThan(Constants.PROP_STATUS, 2)
                //filtrar a apartir de un valor. Esto nos trae todos los pedidos que sean inferiores
                // al valor de status 4
//                .whereLessThan(Constants.PROP_STATUS, 4)
                //Filtrar pedidos que sean iguales a 3
//                .whereEqualTo(Constants.PROP_STATUS, 3)
                //Filtrar pedidos que sean mayores o igual a 2 o menores o igual a 2
//                .whereGreaterThanOrEqualTo(Constants.PROP_STATUS, 2)
//                .whereLessThanOrEqualTo(Constants.PROP_STATUS, 2)

                //combinacion de consultas que no estan con la misma propiedad, por ej status.
                //Se debe de crear un indice en Firebase.
                //Cada indice debe ser personalizado, si cambio el orden de ascendente a descendente,
                // debere crear un nuevo indice en Firebase

//                .whereEqualTo(Constants.PROP_CLIENT_ID, user.uid)
//                .orderBy(Constants.PROP_DATE, Query.Direction.DESCENDING)

                .whereEqualTo(Constants.PROP_CLIENT_ID, user.uid)
                .orderBy(Constants.PROP_STATUS, Query.Direction.ASCENDING)
                .whereLessThan(Constants.PROP_STATUS, 4)
                .orderBy(Constants.PROP_DATE, Query.Direction.DESCENDING)

                .get()
                .addOnSuccessListener {
                    for (document in it){
                        val order = document.toObject(Order::class.java)
                        order.id = document.id
                        adapter.add(order)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al consultar los datos.", Toast.LENGTH_SHORT)
                        .show()
                }
        }

    }

    override fun onTrack(order: Order) {
        orderSelected = order
        //lanzar el fragmento
        val fragment = TrackFragment()
        supportFragmentManager
            .beginTransaction()
            .add(R.id.containerMain, fragment)
            .addToBackStack(null)
            .commit()

    }

    override fun onStarChat(order: Order) {
        orderSelected = order

        val fragment = ChatFragment()

        //lanzamos el fragmento
        supportFragmentManager
            .beginTransaction()
            .add(R.id.containerMain, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun getOrderSelected(): Order = orderSelected
}