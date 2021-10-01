package com.rsschool.catsapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.rsschool.catsapp.data.CatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CatsViewModel @Inject constructor(repo: CatsRepository) : ViewModel() {
    val cats = repo.getImagesList().cachedIn(viewModelScope)
}