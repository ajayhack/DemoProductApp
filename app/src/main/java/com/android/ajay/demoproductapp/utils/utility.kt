package com.android.ajay.demoproductapp.utils

import com.android.ajay.demoproductapp.modal.AppDatabase

val appDatabase by lazy { AppDatabase.getDatabase(ProductApplication.appContext) }