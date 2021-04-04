package com.android.ajay.demoproductapp.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.ajay.demoproductapp.R
import com.android.ajay.demoproductapp.databinding.ActivityShowProductBinding
import com.android.ajay.demoproductapp.databinding.ItemProductBinding
import com.android.ajay.demoproductapp.modal.ProductTable
import com.android.ajay.demoproductapp.utils.appDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ShowProduct : BaseActivity() {
    private var binding: ActivityShowProductBinding? = null
    private var productList: MutableList<ProductTable?>? = mutableListOf()
    private val productAdapter by lazy { ProductAdapter(productList, ::onItemClickEvent) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowProductBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        binding?.toolbarLayout?.backImageButton?.setOnClickListener { onBackPressed() }
        binding?.toolbarLayout?.subHeaderText?.text = getString(R.string.products)

        //region===================Getting All Product Data to Display:-
        GlobalScope.launch(Dispatchers.IO) {
            productList = appDatabase?.dao()?.getAllProductData()
            withContext(Dispatchers.Main) {
                showRecyclerViewData()
            }
        }
        //endregion
    }

    override fun onResume() {
        super.onResume()
        //region===================Getting All Product Data to Display:-
        GlobalScope.launch(Dispatchers.IO) {
            productList = appDatabase?.dao()?.getAllProductData()
            withContext(Dispatchers.Main) {
                showRecyclerViewData()
            }
        }
    }

    //region====================================SetUp RecyclerView:-
    private fun showRecyclerViewData() {
        if (productList?.isNotEmpty() == true) {
            binding?.productRV?.apply {
                layoutManager = LinearLayoutManager(context)
                itemAnimator = DefaultItemAnimator()
                adapter = productAdapter
            }
        } else {
            showToast("No Data Found!!!!")
        }
    }
    //endregion

    //region========================Onclick Event of Recyclerview Item:-
    private fun onItemClickEvent(position: Int) {
        Log.d("Position:- ", position.toString())
        if (position > -1) {
            val bundle = Bundle()
            bundle.putSerializable("Data", productList?.get(position))
            startActivity(Intent(this, ProductDetailPage::class.java).apply {
                putExtras(bundle)
            })
        }
    }
    //endregion

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    //region=================Show Toast:-
    private fun showToast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    //endregion
}

internal class ProductAdapter(
    private val list: MutableList<ProductTable?>?,
    var cb: (Int) -> Unit
) :
    RecyclerView.Adapter<ProductAdapter.ProductHolder>() {
    private var binding: ItemProductBinding? = null
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ProductHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(p0.context), p0, false)
        return ProductHolder(binding)
    }

    override fun getItemCount(): Int {
        return list?.size ?: 0
    }

    override fun onBindViewHolder(holder: ProductHolder, p1: Int) {
        holder.binding.productName.text = list?.get(p1)?.productName
    }

    inner class ProductHolder(val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding?.productLV?.setOnClickListener {
                cb(adapterPosition)
            }
        }
    }
}