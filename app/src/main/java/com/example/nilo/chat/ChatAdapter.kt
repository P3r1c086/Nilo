package com.example.nilo.chat

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.nilo.R
import com.example.nilo.databinding.ItemChatBinding
import com.example.nilo.entities.Message

/**
 * Proyect: Nilo
 * From: com.example.nilo.chat
 * Create by Pedro Aguilar Fernández on 02/01/2023 at 13:37
 * More info: linkedin.com/in/pedro-aguilar-fernández-167753140
 * All rights reserved 2023
 **/
class ChatAdapter(private val messageList: MutableList<Message>, private val listener: OnChatListener)
    : RecyclerView.Adapter<ChatAdapter.ViewHolder>(){

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messageList[position]
        holder.setListener(message)

        //por default el msg ira a la derecha porque se contempla como un msg del cliente
        var gravity = Gravity.END
        var background = ContextCompat.getDrawable(context, R.drawable.background_chat_client)
        var textColor = ContextCompat.getColor(context, R.color.colorOnSecondary)

        val marginHorizontal = context.resources.getDimensionPixelSize(R.dimen.chat_margin_horizontal)
        //variable para manipular los margenes(extraemos los actuales)
        val params = holder.binding.tvMessage.layoutParams as ViewGroup.MarginLayoutParams
        //modificamos los margenes. Por defecto seran los del cliente
        params.marginStart = marginHorizontal
        params.marginEnd = 0
        //esto hace que al hacer scroll no se pinte un marginTop entre los msg de la misma persona
        params.topMargin = 0

        //cada vez que haya un cambio entre mensaje de cliente o support habra una separacion
        //"position > 0" para que no tome en cuenta la primera posicion de este chat. Luego queremos
        //comprobar que el mensaje no sea enviado por la misma persona
        if (position > 0 && message.isSendByMe() != messageList[position - 1].isSendByMe()){
            params.topMargin = context.resources.getDimensionPixelSize(R.dimen.common_padding_min)
        }

        //si el mensaje no es enviado por mi, es decir, por el cliente, cambia estas tres variables
        if (!message.isSendByMe()){
            gravity = Gravity.START
            background = ContextCompat.getDrawable(context, R.drawable.background_chat_support)
            textColor = ContextCompat.getColor(context, R.color.colorOnPrimary)
            params.marginStart = 0
            params.marginEnd = marginHorizontal
        }

        //una vez configuradas las variables, se asignan. <holder.binding.root> es el LinearLayout
        holder.binding.root.gravity = gravity

        holder.binding.tvMessage.layoutParams = params
        holder.binding.tvMessage.setBackground(background)
        holder.binding.tvMessage.setTextColor(textColor)
        holder.binding.tvMessage.text = message.message
    }

    override fun getItemCount(): Int = messageList.size

    fun update(message: Message){
        val index = messageList.indexOf(message)
        if (index != -1){
            messageList.set(index, message)
            notifyItemChanged(index)
        }
    }
    fun delete(message: Message){
        val index = messageList.indexOf(message)
        if (index != -1){
            messageList.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun add(message: Message){
        if (!messageList.contains(message)){
            messageList.add(message)
            notifyItemInserted(messageList.size -1)
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val binding = ItemChatBinding.bind(view)

        fun setListener(message: Message){
            binding.tvMessage.setOnLongClickListener{
                listener.deleteMessage(message)
                true
            }
        }
    }
}