package com.rsschool.catsapp.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.rsschool.catsapp.model.Cat
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ImageDetailViewModel @Inject constructor(private val state: SavedStateHandle) : ViewModel() {
    val image by lazy { state.get<Cat>("image") }
}
