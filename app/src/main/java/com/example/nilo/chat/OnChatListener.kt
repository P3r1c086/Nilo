package com.example.nilo.chat

import com.example.nilo.entities.Message

/**
 * Proyect: Nilo
 * From: com.example.nilo.chat
 * Create by Pedro Aguilar Fernández on 02/01/2023 at 13:21
 * More info: linkedin.com/in/pedro-aguilar-fernández-167753140
 * All rights reserved 2023
 **/
interface OnChatListener {
    fun deleteMessage(message: Message)
}