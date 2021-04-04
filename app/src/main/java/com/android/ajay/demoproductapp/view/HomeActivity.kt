package com.android.ajay.demoproductapp.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.android.ajay.demoproductapp.R
import com.android.ajay.demoproductapp.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {
    private var binding: ActivityHomeBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.toolbarLayout?.backImageButton?.visibility = View.GONE
        binding?.toolbarLayout?.subHeaderText?.text = getString(R.string.home)

        //region============OnClick Event of Show Product Button
        binding?.showProductBTN?.setOnClickListener {
            startActivity(Intent(this, ShowProduct::class.java))
        }
        //endregion

        //region===============OnClick Event of Create Product Button
        binding?.createProductBTN?.setOnClickListener {
            startActivity(Intent(this, CreateProductActivity::class.java).apply {
                putExtra("type", DataInsertType.INSERT.type)
            })
        }
        //endregion

    }
}

//region===================ENUM for DataInsertType to Manage Whether we need to Insert Data or Update:-
enum class DataInsertType(var type: String) {
    INSERT("11"),
    UPDATE("12")
}
//endregion
