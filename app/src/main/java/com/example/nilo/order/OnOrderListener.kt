package com.example.nilo.order

import com.example.nilo.entities.Order

/**
 * Proyect: Nilo
 * From: com.example.nilo.order
 * Create by Pedro Aguilar Fernández on 24/12/2022 at 18:31
 * More info: linkedin.com/in/pedro-aguilar-fernández-167753140
 * All rights reserved 2022
 **/
interface OnOrderListener {
    fun onTrack(order: Order)
    fun onStarChat(order: Order)
}