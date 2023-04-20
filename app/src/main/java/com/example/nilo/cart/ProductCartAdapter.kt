package com.example.nilo.cart

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.nilo.R
import com.example.nilo.databinding.ItemProductCartBinding
import com.example.nilo.entities.Product

/**
 * Proyect: Nilo
 * From: com.example.nilo.cart
 * Create by Pedro Aguilar Fernández on 23/12/2022 at 12:20
 * More info: linkedin.com/in/pedro-aguilar-fernández-167753140
 * All rights reserved 2022
 **/
class ProductCartAdapter(private val productList: MutableList<Product>,
                         private val listener: OnCartListener) :
    RecyclerView.Adapter<ProductCartAdapter.ViewHolder>(){

    lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.item_product_cart, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = productList[position]

        holder.setListener(product)

        holder.binding.tvName.text = product.name
        holder.binding.tvQuantity.text = product.newQuantity.toString()

        //cargamos la imagen con Glide
        Glide.with(context)
            .load(product.imgUrl)
            //este es para que almacene la imagen descargada, para que no tenga que estar
            // consultando cada vez que inicie la app. Tiene la desventaja que hasta que no cambie
            // la url, la imagen va a ser la misma sin importar que el servidor si cambie
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            //poner este icono en lugar de la imagen para que el usuario sepa que la imagen esta
            // cargando
            .placeholder(R.drawable.ic_access_time)
            //poner este icono en lugar de la imagen para que el usuario sepa que la imagen contiene
            // algun error
            .error(R.drawable.ic_broken_image)
            .centerCrop()
            .circleCrop()
            .into(holder.binding.imgProduct)
    }

    override fun getItemCount(): Int = productList.size

    fun add(product: Product){
        //en caso de que la lista no contenga el producto, agregalo
        if (!productList.contains(product)){
            productList.add(product)
            //notificar que un producto(item) ha sido insertado en la ultima posicion
            notifyItemInserted(productList.size - 1)
            calcTotal()
        }else{//si si lo contine, actualizalo
            update(product)
        }
    }
    fun update(product: Product){
        //obtenemos el index
        val index = productList.indexOf(product)
        //si index es != de -1(exista), actualiza este producto
        if (index != -1){
            productList.set(index, product)
            //notificar que un producto(item) ha sido actualizado
            notifyItemChanged(index)
            calcTotal()
        }
    }
    fun delete(product: Product){
        //obtenemos el index
        val index = productList.indexOf(product)
        //si index es != de -1(exista), elimina este producto
        if (index != -1){
            productList.removeAt(index)
            //notificar que un producto(item) ha sido eliminado
            notifyItemRemoved(index)
            calcTotal()
        }
    }

    private fun calcTotal(){
        var result = 0.0
        for(product in productList){
            result += product.totalPrice()
        }
        listener.showTotal(result)
    }

    fun getProducts(): List<Product> = productList

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val binding = ItemProductCartBinding.bind(view)

        fun setListener(product: Product){
//            binding.ibSum.setOnClickListener {
//                product.newQuantity += 1
//                listener.setQuantity(product)
//            }
//            binding.ibSub.setOnClickListener {
//                product.newQuantity -= 1
//                listener.setQuantity(product)
//            }
            //incrementar cantidad
            binding.ibSum.setOnClickListener {
                if (product.newQuantity < product.quantity) {
                    product.newQuantity += 1
                    listener.setQuantity(product)
                }
            }
            //disminuir cantidad
            binding.ibSub.setOnClickListener {
                if (product.newQuantity > 0) {
                    product.newQuantity -= 1
                    listener.setQuantity(product)
                }
            }
        }
    }
}