package com.example.nilo.entities

import com.google.firebase.database.Exclude

/**
 * Proyect: Nilo
 * From: com.example.nilo.entities
 * Create by Pedro Aguilar Fernández on 02/01/2023 at 13:09
 * More info: linkedin.com/in/pedro-aguilar-fernández-167753140
 * All rights reserved 2023
 **/
data class Message(@get:Exclude var id: String = "",
                   var message: String = "",
                   var sender: String = "",
                   @get:Exclude var myUid: String = ""){

    /**
     * Funcion que determina si el msg es enviado por nosotros. Si es asi se pintara en el lado derecho
     * y si es enviado por el vendedor se pintara en el lado izquierdo
     */
    @Exclude
    fun isSendByMe(): Boolean = sender.equals(myUid)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Message

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
