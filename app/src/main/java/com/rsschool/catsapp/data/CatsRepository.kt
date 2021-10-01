package com.rsschool.catsapp.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.liveData
import com.rsschool.catsapp.api.CatsApi
import javax.inject.Inject

class CatsRepository @Inject constructor(private val catsApi: CatsApi) {

    fun getImagesList() = Pager(
        config = PagingConfig(
            pageSize = 10,
            maxSize = 50,
            enablePlaceholders = false
        ), pagingSourceFactory = { CatsPagingSource(catsApi) }
    ).liveData
}
