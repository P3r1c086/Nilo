package com.example.nilo.product

import com.example.nilo.entities.Product
import com.google.firebase.auth.FirebaseUser

/**
 * Proyect: Nilo
 * From: com.example.nilo.product
 * Create by Pedro Aguilar Fernández on 23/12/2022 at 17:16
 * More info: linkedin.com/in/pedro-aguilar-fernández-167753140
 * All rights reserved 2022
 **/
interface MainAux {
    fun getProductsCart(): MutableList<Product>
    fun updateTotal()
    fun clearCart()

    fun getProductSelected(): Product?
    fun showButton(isVisible: Boolean)
    fun addProductToCart(product: Product)

    fun updateTitle(user: FirebaseUser)
}