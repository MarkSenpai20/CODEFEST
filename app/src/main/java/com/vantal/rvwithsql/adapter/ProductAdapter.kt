package com.vantal.rvwithsql.adapter// ProductAdapter.kt
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vantal.rvwithsql.model.Product
import com.bumptech.glide.Glide
import com.vantal.rvwithsql.R


class ProductAdapter(private val context: Context, private val onItemClick: (Product) -> Unit) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    private var products: List<Product> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val v = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(v)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.bind(product)

        holder.itemView.setOnClickListener {
            onItemClick(product)
        }
    }

    override fun getItemCount(): Int = products.size

    fun updateProducts(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productImage: ImageView = itemView.findViewById(R.id.productImage)
        private val productName: TextView = itemView.findViewById(R.id.productName)
        private val productDesc: TextView = itemView.findViewById(R.id.productDesc)
        private val productPrice: TextView = itemView.findViewById(R.id.productPrice)

        fun bind(product: Product) {
            productName.text = product.name
            productDesc.text = product.description
            productPrice.text = "$${product.price}"

            val resourceId = context.resources.getIdentifier(product.imagePath, "drawable", context.packageName)
            if (resourceId != 0) {
                Glide.with(context).load(resourceId).into(productImage)
            } else {
                Glide.with(context).load(R.drawable.ic_placeholder).into(productImage)
            }
        }
    }
}