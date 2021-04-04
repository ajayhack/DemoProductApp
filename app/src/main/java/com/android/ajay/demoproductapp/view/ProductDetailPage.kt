package com.android.ajay.demoproductapp.view

import android.app.Dialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.android.ajay.demoproductapp.R
import com.android.ajay.demoproductapp.databinding.ActivityProductDetailBinding
import com.android.ajay.demoproductapp.modal.AppDatabase
import com.android.ajay.demoproductapp.modal.ProductTable
import kotlinx.coroutines.*
import java.io.File


class ProductDetailPage : BaseActivity() {
    private var binding: ActivityProductDetailBinding? = null
    private var data: ProductTable? = null
    private var appDatabase: AppDatabase? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        appDatabase = AppDatabase.getDatabase(this)
        val intent = this.intent
        val bundle = intent.extras
        data = bundle?.getSerializable("Data") as ProductTable

        binding?.toolbarLayout?.backImageButton?.setOnClickListener { onBackPressed() }
        binding?.toolbarLayout?.subHeaderText?.text = getString(R.string.product_detail)

        binding?.productImageView?.setOnClickListener {
            showLargeImageDialog(data?.productImagePath)
        }

        //region======================Delete Product:-
        binding?.deleteProduct?.setOnClickListener {
            showProgress()
            GlobalScope.launch(Dispatchers.IO) {
                if (!data?.productID.isNullOrEmpty()) {
                    val deleteStatus = appDatabase?.dao()?.deleteProductByID(data?.productID ?: "")
                    Log.d("Delete Item:- ", deleteStatus.toString())
                }

                withContext(Dispatchers.Main) {
                    hideProgress()
                    showToast("Product Deleted Successfully!!")
                    startActivity(Intent(this@ProductDetailPage, HomeActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                }
            }
        }
        //endregion

        //region=================Update Product Intent:-
        binding?.updateProduct?.setOnClickListener {
            val bundle = Bundle()
            bundle.putSerializable("Data", data)
            startActivity(Intent(this, CreateProductActivity::class.java).apply {
                putExtras(bundle)
                putExtra("type", DataInsertType.UPDATE.type)
            })
        }
        //endregion

        //region======================Stubbing Data on UI:-
        if (data != null) {
            binding?.productName?.text = "Product Name:- ${data?.productName}"
            binding?.productDescription?.text = "Product Description:- ${data?.description}"
            binding?.productRegularPrice?.text = "Regular Price:- ${data?.regularPrice}"
            binding?.ProductSalePrice?.text = "Sale Price:- ${data?.salePrice}"
            if (!data?.productImagePath.isNullOrEmpty()) {
                val file = File(data?.productImagePath ?: "")
                binding?.productImageView?.setImageBitmap(BitmapFactory.decodeFile(file.absolutePath))
            }

            binding?.productColors?.text = "Colors:- ${data?.colorsName}"
            binding?.productStores?.text = "Stores:- ${data?.storesName}"
        }
        //endregion
    }

    //region=================Show Product Image In Bigger Size:-
    private fun showLargeImageDialog(productImagePath: String?) {
        val dialog = Dialog(this@ProductDetailPage)
        val inflate = LayoutInflater.from(this@ProductDetailPage)
            .inflate(R.layout.show_large_image_view, null)
        dialog.setContentView(inflate)
        dialog.setCancelable(false)
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        val window = dialog.window
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        val productImage = dialog.findViewById<ImageView>(R.id.productImage)
        val cancelDialog = dialog.findViewById<AppCompatButton>(R.id.cancelDialog)
        cancelDialog.setOnClickListener {
            dialog.dismiss()
        }
        val file = File(productImagePath ?: "")
        productImage?.setImageBitmap(BitmapFactory.decodeFile(file.absolutePath))
        dialog.show()
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