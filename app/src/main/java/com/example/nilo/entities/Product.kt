package com.example.nilo.entities

import com.google.firebase.firestore.Exclude

/**
 * Proyect: Shop Partner
 * From: com.example.shoppartner
 * Create by Pedro Aguilar Fernández on 29/11/2022 at 13:55
 * More info: linkedin.com/in/pedro-aguilar-fernández-167753140
 * All rights reserved 2022
 **/

//@get:Exclude hace que esa propidad no sea tomada en cuenta a la hora de insertar un nuevo producto
// en la bd, pero si podremos hacer uso de ella en el codigo si lo necesitamos
data class Product(@get:Exclude var id: String? = null,
                   var name: String? = null,
                   var description: String? = null,
                   var imgUrl: String? = null,
                   var quantity: Int = 0,
                   @get:Exclude var newQuantity: Int = 1,
                   var price: Double = 0.0,
                   var partnerId:String = "",
                   var sellerId: String = ""){

    fun totalPrice(): Double = newQuantity * price

    //Los metodos equals y hashCode estan basados en el id, para poder diferenciar uno de otro
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Product

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
