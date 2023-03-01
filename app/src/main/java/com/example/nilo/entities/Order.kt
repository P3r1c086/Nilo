package com.example.nilo.entities

import com.google.firebase.firestore.Exclude

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
                 var status: Int = 0){

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
