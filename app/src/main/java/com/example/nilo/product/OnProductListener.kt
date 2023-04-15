package com.example.nilo.product

import com.example.nilo.entities.Product

/**
 * Proyect: Shop Partner
 * From: com.example.shoppartner
 * Create by Pedro Aguilar Fernández on 29/11/2022 at 14:00
 * More info: linkedin.com/in/pedro-aguilar-fernández-167753140
 * All rights reserved 2022
 **/
interface OnProductListener {
    fun onClick(product: Product)
    fun loadMore()
}