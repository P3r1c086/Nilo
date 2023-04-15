package com.example.nilo.product

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.nilo.R
import com.example.nilo.databinding.ItemProductBinding
import com.example.nilo.entities.Product

/**
 * Proyect: Shop Partner
 * From: com.example.shopparner
 * Create by Pedro Aguilar Fernández on 29/11/2022 at 13:59
 * More info: linkedin.com/in/pedro-aguilar-fernández-167753140
 * All rights reserved 2022
 **/
class ProductAdapter(private val productList: MutableList<Product>,
                     private val listener: OnProductListener) :
    RecyclerView.Adapter<ProductAdapter.ViewHolder>(){

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        //creamos una vista del item para que la infle el ViewHolder
        val view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //asignamos un valor a cada componente
        val product = productList[position]

        holder.setListener(product)
        //si el producto es igual a null, lo tomamos como un producto invalido y mostramos el boton "mas"
        if (product.id == null){
            holder.binding.containerProduct.visibility = View.GONE
            holder.binding.btnMore.visibility = View.VISIBLE
        }else{
            holder.binding.containerProduct.visibility = View.VISIBLE
            holder.binding.btnMore.visibility = View.GONE

            holder.binding.tvName.text = product.name
            holder.binding.tvPrice.text = product.price.toString()
            holder.binding.tvQuantity.text = product.quantity.toString()

            //cargar imagen
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
                .into(holder.binding.imgProduct)
        }
    }

    override fun getItemCount(): Int = productList.size

    /**
     * Agreagamos el producto a la lista antes del ultimo elemento
     */
    fun add(product: Product){
        //en caso de que la lista no contenga el producto, agregalo
        if (!productList.contains(product)){
//            productList.add(product)
            productList.add(productList.size - 1, product)
            //notificar que un producto(item) ha sido insertado en la ultima posicion
            notifyItemInserted(productList.size - 2)
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
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        //instanciamos la vista del ItemProduct
        val binding = ItemProductBinding.bind(view)

        fun setListener(product: Product){
            binding.root.setOnClickListener {
                listener.onClick(product)
            }
            binding.btnMore.setOnClickListener {
                listener.loadMore()
            }
        }
    }
}