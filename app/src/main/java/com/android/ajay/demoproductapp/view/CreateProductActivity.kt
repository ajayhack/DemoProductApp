package com.android.ajay.demoproductapp.view

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.core.content.FileProvider
import com.android.ajay.demoproductapp.R
import com.android.ajay.demoproductapp.databinding.ActivityCreateProductBinding
import com.android.ajay.demoproductapp.modal.ProductTable
import com.android.ajay.demoproductapp.utils.FileUtils
import com.android.ajay.demoproductapp.utils.MultiSelectSpinner
import com.android.ajay.demoproductapp.utils.appDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private const val PICK_IMAGE_VIA_GALLERY_REQUEST_CODE = 300
private const val PICK_IMAGE_VIA_CAMERA_REQUEST_CODE = 301

class CreateProductActivity : BaseActivity() {
    private var binding: ActivityCreateProductBinding? = null
    private var dateFormat = SimpleDateFormat("yyyyMMddhmmss", Locale.getDefault())
    private lateinit var _capturedFile: File
    private lateinit var _mainUri: Uri
    private val FILE_NAME_JPG = "product_image.jpg"
    private lateinit var calendar: Calendar
    private var uniqueID: String? = null
    private var insertStatus = 0L
    private val insertType by lazy { intent.getStringExtra("type") }
    private val productTable by lazy { ProductTable() }
    private var colorsDataList = mutableListOf<SpinnerModal>()
    private var storeDataList = mutableListOf<SpinnerModal>()
    private var colorIDS: IntArray? = null
    private var storeIDS: IntArray? = null
    private var colorsName = mutableListOf<String>()
    private var storesName = mutableListOf<String>()
    private var updateData: ProductTable? = null
    private var defaultStatus: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateProductBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.toolbarLayout?.backImageButton?.setOnClickListener { onBackPressed() }
        if (insertType == DataInsertType.INSERT.type)
            binding?.toolbarLayout?.subHeaderText?.text = getString(R.string.createProduct)
        else
            binding?.toolbarLayout?.subHeaderText?.text = getString(R.string.update_product)


        //region================check Data Condition for Update:-
        if (insertType == DataInsertType.UPDATE.type) {
            val intent = this.intent
            val bundle = intent.extras
            updateData = bundle?.getSerializable("Data") as ProductTable
            if (updateData != null) {
                binding?.createProductButton?.text = getString(R.string.update_product)
                binding?.createProductButton?.setBackgroundColor(Color.parseColor("#0000FF"))
                binding?.nameET?.setText(updateData?.productName)
                binding?.descriptionET?.setText(updateData?.description)
                binding?.regularPriceET?.setText(updateData?.regularPrice)
                binding?.salePriceET?.setText(updateData?.salePrice)
                val file = File(updateData?.productImagePath ?: "")
                binding?.productImage?.setImageBitmap(BitmapFactory.decodeFile(file.absolutePath))
                binding?.productFileName?.text = FileUtils().getPath(this, Uri.fromFile(file))
                Log.d("UpdateFileName:- ", binding?.productFileName?.text?.toString() ?: "")

            }
        } else {
            binding?.createProductButton?.text = getString(R.string.createProduct)
            binding?.createProductButton?.setBackgroundColor(Color.parseColor("#008000"))
        }
        //endregion

        stubbingColorMultiSelectData()
        stubbingStoreMultiSelectData()

        //region==============Ask User To Permit Camera and Gallery Permission for Product Image Capturing:-
        binding?.productImageViaGallery?.setOnClickListener {
            askGalleryPermission {
                startActivityForResult(galleryIntent(), PICK_IMAGE_VIA_GALLERY_REQUEST_CODE)
            }
        }
        //endregion

        //region==============Ask User To Permit Camera and Gallery Permission for Product Image Capturing:-
        binding?.productImageViaCamera?.setOnClickListener {
            askGalleryPermission {
                startActivityForResult(cameraIntent(), PICK_IMAGE_VIA_CAMERA_REQUEST_CODE)
            }
        }
        //endregion

        //region==================Create Product Button OnClick:-
        binding?.createProductButton?.setOnClickListener {
            if (validateFields()) {
                showProgress()
                when (insertType) {
                    DataInsertType.INSERT.type -> {
                        productTable.productID = generateUniqueID()
                        productTable.productName = binding?.nameET?.text?.toString() ?: ""
                        productTable.description = binding?.descriptionET?.text?.toString() ?: ""
                        productTable.regularPrice = binding?.regularPriceET?.text?.toString() ?: ""
                        productTable.salePrice = binding?.salePriceET?.text?.toString() ?: ""
                        productTable.productImagePath = FileUtils().getPath(this, _mainUri) ?: ""
                        productTable.colors = Arrays.toString(colorIDS)
                            .replace("[", "")
                            .replace(" ", "")
                            .replace("]", "")
                        productTable.stores = Arrays.toString(storeIDS)
                            .replace("[", "")
                            .replace(" ", "")
                            .replace("]", "")
                        productTable.colorsName = colorsName.toTypedArray().contentToString()
                            .replace("[", "")
                            .replace(" ", "")
                            .replace("]", "")
                        productTable.storesName = storesName.toTypedArray().contentToString()
                            .replace("[", "")
                            .replace(" ", "")
                            .replace("]", "")
                        GlobalScope.launch(Dispatchers.IO) {
                            insertStatus = appDatabase?.dao()?.insertProductData(productTable) ?: 0L
                            Log.d("Inserted:- ", insertStatus.toString())
                            withContext(Dispatchers.Main) {
                                hideProgress()
                                showToast("Product Inserted Successfully!!!!")
                                onBackPressed()
                            }
                        }
                    }
                    DataInsertType.UPDATE.type -> {
                        productTable.productID = updateData?.productID ?: ""
                        productTable.productName = binding?.nameET?.text?.toString() ?: ""
                        productTable.description = binding?.descriptionET?.text?.toString() ?: ""
                        productTable.regularPrice = binding?.regularPriceET?.text?.toString() ?: ""
                        productTable.salePrice = binding?.salePriceET?.text?.toString() ?: ""
                        productTable.productImagePath =
                            binding?.productFileName?.text?.toString() ?: ""
                        if (colorIDS != null) {
                            productTable.colors = Arrays.toString(colorIDS)
                                .replace("[", "")
                                .replace(" ", "")
                                .replace("]", "")
                            productTable.colorsName = colorsName.toTypedArray().contentToString()
                                .replace("[", "")
                                .replace(" ", "")
                                .replace("]", "")
                        } else {
                            productTable.colors = updateData?.colors ?: ""
                            productTable.colorsName = updateData?.colorsName ?: ""
                        }

                        if (storeIDS != null) {
                            productTable.stores = Arrays.toString(storeIDS)
                                .replace("[", "")
                                .replace(" ", "")
                                .replace("]", "")

                            productTable.storesName = storesName.toTypedArray().contentToString()
                                .replace("[", "")
                                .replace(" ", "")
                                .replace("]", "")
                        } else {
                            productTable.stores = updateData?.stores ?: ""
                            productTable.storesName = updateData?.storesName ?: ""
                        }

                        GlobalScope.launch(Dispatchers.IO) {
                            insertStatus = appDatabase?.dao()?.insertProductData(productTable) ?: 0L
                            Log.d("Inserted:- ", insertStatus.toString())
                            withContext(Dispatchers.Main) {
                                hideProgress()
                                showToast("Product Updated Successfully!!!!")
                                startActivity(
                                    Intent(
                                        this@CreateProductActivity,
                                        HomeActivity::class.java
                                    ).apply {
                                        flags =
                                            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    })
                            }
                        }
                    }
                    else -> showToast("Something Went Wrong!!!!")
                }
            }
        }
        //endregion
    }

    //region=========================To Show colors Option MultiSelectionSpinner:-
    private fun stubbingColorMultiSelectData() {
        colorsDataList.add(SpinnerModal(1, "Black"))
        colorsDataList.add(SpinnerModal(2, "Red"))
        colorsDataList.add(SpinnerModal(3, "Blue"))
        colorsDataList.add(SpinnerModal(4, "Green"))
        colorsDataList.add(SpinnerModal(5, "Brown"))
        val tempDataList = ArrayList<String>()
        for (x in colorsDataList.indices) {
            tempDataList.add(colorsDataList[x].name)
        }

        binding?.colorSpinner?.setItems(tempDataList)

        //region===============Below Logic To make Colors Checked in Update Product Time:-
        if (insertType == DataInsertType.UPDATE.type) {
            val selectedStores = updateData?.colors?.split(",")
            val arrayToPass = IntArray(selectedStores?.size ?: 0)
            if (selectedStores != null) {
                for (value in selectedStores.indices) {
                    arrayToPass[value] = (selectedStores[value].toInt() - 1)
                }
            }
            binding?.colorSpinner?.setSelection(arrayToPass)
        } else {
            binding?.colorSpinner?.setSelection(intArrayOf())
        }
        //endregion

        binding?.colorSpinner?.setListener(object :
            MultiSelectSpinner.OnMultipleItemsSelectedListener {
            override fun selectedIndices(indices: List<Int?>) {
                colorIDS = IntArray(indices.size)
                for (i in indices.indices) {
                    colorIDS!![i] = indices[i]?.let { colorsDataList[it].id }!!
                }
            }

            override fun selectedStrings(strings: List<String?>?) {
                Log.d("Colors:- ", strings.toString())
                colorsName = strings as MutableList<String>
            }
        })
    }
    //endregion


    // region=========================To Show Stores Option MultiSelectionSpinner:-
    private fun stubbingStoreMultiSelectData() {
        storeDataList.add(SpinnerModal(1, "Store 1"))
        storeDataList.add(SpinnerModal(2, "Store 2"))
        storeDataList.add(SpinnerModal(3, "Store 3"))
        storeDataList.add(SpinnerModal(4, "Store 4"))
        storeDataList.add(SpinnerModal(5, "Store 5"))

        val tempDataList = ArrayList<String>()
        for (x in storeDataList.indices) {
            tempDataList.add(storeDataList[x].name)
        }

        binding?.storeSpinner?.setItems(tempDataList)

        //region=====================Below Logic is to make Stores Checked In Update Product Time:-
        if (insertType == DataInsertType.UPDATE.type) {
            val selectedStores = updateData?.stores?.split(",")
            val arrayToPass = IntArray(selectedStores?.size ?: 0)
            if (selectedStores != null) {
                for (value in selectedStores.indices) {
                    arrayToPass[value] = (selectedStores[value].toInt() - 1)
                }
            }
            binding?.storeSpinner?.setSelection(arrayToPass)
        } else {
            binding?.storeSpinner?.setSelection(intArrayOf())
        }
        //endregion

        binding?.storeSpinner?.setListener(object :
            MultiSelectSpinner.OnMultipleItemsSelectedListener {
            override fun selectedIndices(indices: List<Int?>) {
                storeIDS = IntArray(indices.size)
                for (i in indices.indices) {
                    storeIDS!![i] = indices[i]?.let { storeDataList[it].id }!!
                }
            }

            override fun selectedStrings(strings: List<String?>?) {
                storesName = strings as MutableList<String>
            }
        })
    }
    //endregion

    //region==================Validate Field:-
    private fun validateFields(): Boolean {
        when {
            TextUtils.isEmpty(binding?.nameET?.text) -> {
                showToast("Name Field Should not be empty")
                return false
            }
            TextUtils.isEmpty(binding?.descriptionET?.text) -> {
                showToast("Descripton Field Should not be empty")
                return false
            }
            TextUtils.isEmpty(binding?.regularPriceET?.text) -> {
                showToast("Regular Price Field Should not be empty")
                return false
            }
            TextUtils.isEmpty(binding?.salePriceET?.text) -> {
                showToast("Sale Price Field Should not be empty")
                return false
            }

            TextUtils.isEmpty(binding?.productFileName?.text) -> {
                showToast("Please Capture Product Image")
                return false
            }

            !colorChoose() -> {
                showToast("Please Select Color")
                return false
            }

            !storeChoose() -> {
                showToast("Please Select Store")
                return false
            }
            else -> {
                return true
            }
        }
    }
    //endregion

    //region===============Validate colors Choose or not:-
    private fun colorChoose(): Boolean {
        var status = false
        if (insertType == DataInsertType.INSERT.type) {
            status = colorsName.isNotEmpty()
        } else if (insertType == DataInsertType.UPDATE.type) {
            status = !(colorsName.isNullOrEmpty() && updateData?.colorsName.isNullOrEmpty())
        }
        return status
    }
    //endregion

    //region===============Validate stores Choose or not:-
    private fun storeChoose(): Boolean {
        var status = false
        if (insertType == DataInsertType.INSERT.type) {
            status = storesName.isNotEmpty()
        } else if (insertType == DataInsertType.UPDATE.type) {
            status = !(storesName.isNullOrEmpty() && updateData?.storesName.isNullOrEmpty())
        }
        return status
    }
    //endregion

    //region===============Camera Intent to Fetch Image Via Camera:-
    private fun cameraIntent(): Intent {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getCaptureImageOutputUri())
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        return intent
    }
    //endregion

    //region================Get Camera Image Captured URI:-
    private fun getCaptureImageOutputUri(): Uri? {
        _capturedFile = File(externalCacheDir, generateUniqueID() + FILE_NAME_JPG)
        return FileProvider.getUriForFile(
            this@CreateProductActivity,
            "$packageName.provider",
            _capturedFile
        )
    }
    //endregion

    //region================Gallery Intent to Fetch Image Via Gallery:-
    //Method to fetch Gallery Image:-
    private fun galleryIntent(): Intent {
        val galleryIntent = Intent(Intent.ACTION_PICK)
        galleryIntent.type = "image/*"
        return galleryIntent
    }
    //endregion

    //Method to Generate UniqueID for Image Saving:-
    private fun generateUniqueID(): String {
        calendar = Calendar.getInstance()
        uniqueID = dateFormat.format(calendar.time)
        return uniqueID ?: ""
    }


    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    //region======================OnActivityResult To Handle Image Capture Result :-
    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            try {
                when (requestCode) {
                    PICK_IMAGE_VIA_GALLERY_REQUEST_CODE -> {
                        displayImagePathName(
                            data, findViewById(R.id.product_file_name), findViewById(
                                R.id.product_image
                            )
                        )
                    }

                    PICK_IMAGE_VIA_CAMERA_REQUEST_CODE -> {
                        displayImagePathName(
                            data, findViewById(R.id.product_file_name), findViewById(
                                R.id.product_image
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    //endregion

    //region======================Display Product Image Name and Also Inflate on Product Image UI:-
    private fun displayImagePathName(data: Intent?, bhTextView: TextView, iv: ImageView) {
        try {
            //Below case for API Level Below and Equal to API28:-
            _mainUri = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                if (data != null) {
                    data.data as Uri
                } else {
                    Uri.fromFile(_capturedFile)
                }
            } else {
                //Below case for API Level Equal and Above to API29:-
                if (data?.data != null) {
                    data.data as Uri
                } else {
                    Uri.fromFile(_capturedFile)
                }
            }

            _capturedFile = FileUtils().getFile(this@CreateProductActivity, _mainUri)!!

            bhTextView.text = FileUtils().getPath(this, _mainUri)

            iv.setImageBitmap(BitmapFactory.decodeFile(_capturedFile.absolutePath))
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
    //endregion

    //region=================Show Toast:-
    private fun showToast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    //endregion
}

//region====================MultiSelection Spinner Modals:-
data class SpinnerModal(var id: Int, var name: String)
//endregion