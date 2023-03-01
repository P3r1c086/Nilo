package com.example.nilo.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.nilo.R
import com.example.nilo.databinding.FragmentDetailBinding
import com.example.nilo.entities.Product
import com.example.nilo.product.MainAux

/**
 * Proyect: Nilo
 * From: com.example.nilo.detail
 * Create by Pedro Aguilar Fernández on 23/12/2022 at 19:03
 * More info: linkedin.com/in/pedro-aguilar-fernández-167753140
 * All rights reserved 2022
 **/
class DetailFragment : Fragment() {

    private var binding: FragmentDetailBinding? = null

    //esta variable se encargara del producto seleccionado proveniente de la activity
    private var product: Product? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailBinding.inflate(inflater, container, false)
        binding?.let {
            return  it.root
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getProduct()
        setupButtons()
    }

    private fun getProduct() {
        product = (activity as? MainAux)?.getProductSelected()
        product?.let { product ->
            binding?.let {
                it.tvName.text = product.name
                it.tvDescription.text = product.description
                it.tvQuantity.text = getString(R.string.detail_quantity, product.quantity)
                setNewQuantity(product)

                Glide.with(this)
                    .load(product.imgUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_access_time)
                    .error(R.drawable.ic_broken_image)
                    .centerCrop()
                    .into(it.imgProduct)
            }
        }
    }

    private fun setNewQuantity(product: Product) {
        binding?.let {
            it.etNewQuantity.setText(product.newQuantity.toString())
            val newQuantityStr =  getString(R.string.detail_total_price, product.totalPrice(),
                product.newQuantity, product.price)
            it.tvTotalPrice.text = HtmlCompat.fromHtml(newQuantityStr, HtmlCompat.FROM_HTML_MODE_LEGACY)
        }
    }

    fun setupButtons(){
        product?.let { product ->
            binding?.let { binding ->
                binding.ibSub.setOnClickListener {
                    //se hace esta validacion porque en caso de ser cero es mejor que cancele
                    if (product.newQuantity > 1){
                        product.newQuantity -= 1
                        setNewQuantity(product)
                    }
                }
                binding.ibSum.setOnClickListener {
                    //se valida si newQuantity es menor al limite disponible
                    if (product.newQuantity < product.quantity){
                        product.newQuantity += 1
                        setNewQuantity(product)
                    }
                }
                binding.efab.setOnClickListener {
                    product.newQuantity = binding.etNewQuantity.text.toString().toInt()
                    addToCart(product)
                }
            }
        }
    }

    private fun addToCart(product: Product) {
        (activity as? MainAux)?.let {
            it.addProductToCart(product)
            activity?.onBackPressed()
        }
    }

    override fun onDestroyView() {
        //Se debe volver a mostrar el boton una vez el fragmento finalice
        (activity as? MainAux)?.showButton(true)
        super.onDestroyView()
        binding = null
    }
}