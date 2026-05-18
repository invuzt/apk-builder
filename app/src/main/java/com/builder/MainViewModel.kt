package com.builder

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.AsStateFlow

class MainViewModel: ViewModel() {
    private val _bitmaps = MutableStateFlow<List<Bitmap>>(emptyList())
    val bitmaps = _bitmaps.AsStateFlow()

    fun onTakePhoto(bitmap: Bitmap) {
        _bitmaps.value = _bitmaps.value + bitmap
    }
}
