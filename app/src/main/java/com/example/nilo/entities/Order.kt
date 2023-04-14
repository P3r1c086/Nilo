package com.example.nilo.entities

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp

/**
 * Proyect: Nilo
 * From: com.example.nilo.entities
 * Create by Pedro Aguilar Fernández on 24/12/2022 at 18:23
 * More info: linkedin.com/in/pedro-aguilar-fernández-167753140
 * All rights reserved 2022
 **/
data class Order(@get:Exclude var id: String = "",
                 var clientId: String = "",
                 var products: Map<String, ProductOrder> = hashMapOf(),
                 var totalPrice: Double = 0.0,
                 var status: Int = 0,
                 @ServerTimestamp var date: Timestamp? = null){
    //con la anotacion @ServerTimestamp automaticamente al insertar una nueva orden el servidor de
    // Firebase va a detectar esta anotacion y le va a insertar la fecha actual del servidor

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Order

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
