package com.example.nilo.entities

import com.google.firebase.firestore.Exclude

/**
 * Proyect: Nilo
 * From: com.example.nilo.entities
 * Create by Pedro Aguilar Fernández on 24/12/2022 at 18:28
 * More info: linkedin.com/in/pedro-aguilar-fernández-167753140
 * All rights reserved 2022
 **/
data class ProductOrder(@get:Exclude var id: String = "",
                        var name: String = "",
                        var quantity: Int = 0,
                        var partnerId: String = ""){

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProductOrder

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
