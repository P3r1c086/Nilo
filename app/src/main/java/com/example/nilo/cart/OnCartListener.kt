package com.example.nilo.cart

import com.example.nilo.entities.Product

/**
 * Proyect: Nilo
 * From: com.example.nilo.cart
 * Create by Pedro Aguilar Fernández on 23/12/2022 at 12:17
 * More info: linkedin.com/in/pedro-aguilar-fernández-167753140
 * All rights reserved 2022
 **/
interface OnCartListener {
    fun setQuantity(product: Product)
    fun showTotal(total: Double)
}