package com.example.nilo.order

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.nilo.R
import com.example.nilo.databinding.ItemOrderBinding
import com.example.nilo.entities.Order

/**
 * Proyect: Nilo
 * From: com.example.nilo.order
 * Create by Pedro Aguilar Fernández on 24/12/2022 at 18:33
 * More info: linkedin.com/in/pedro-aguilar-fernández-167753140
 * All rights reserved 2022
 **/
class OrderAdapter(private val orderList: MutableList<Order>, private val listener: OnOrderListener) :
    RecyclerView.Adapter<OrderAdapter.ViewHolder>() {

    private lateinit var context: Context

    private val aValues: Array<String> by lazy {
        context.resources.getStringArray(R.array.status_value)
    }
    private val aKeys: Array<Int> by lazy {
        context.resources.getIntArray(R.array.status_key).toTypedArray()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = orderList[position]

        holder.setListener(order)

        holder.binding.tvId.text = context.getString(R.string.order_id, order.id)

        //variable para poder concatenar todos los nombres
        var names = ""
        order.products.forEach {
            names += "${it.value.name}, "
        }
        //dropLast es para eliminar los 2 ultimos caracteres, para que no se quede la parte final ", "
        holder.binding.tvProductNames.text = names.dropLast(2)
        holder.binding.tvTotalPrice.text = context.getString(R.string.product_full_cart, order.totalPrice)
        val index = aKeys.indexOf(order.status)
        val statusStr = if (index != -1) aValues[index] else context.getString(R.string.order_status_unknown)
        holder.binding.tvStatus.text = context.getString(R.string.order_status, statusStr)

    }

    override fun getItemCount(): Int = orderList.size

    fun add(order: Order){
        orderList.add(order)
        //notificar que nuevo order ha sido insertado en la ultima posicion
        notifyItemInserted(orderList.size - 1)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val binding = ItemOrderBinding.bind(view)

        fun setListener(order: Order){
            binding.btnTrack.setOnClickListener {
                listener.onTrack(order)
            }
            binding.chpChat.setOnClickListener {
                listener.onStarChat(order)
            }
        }
    }


}