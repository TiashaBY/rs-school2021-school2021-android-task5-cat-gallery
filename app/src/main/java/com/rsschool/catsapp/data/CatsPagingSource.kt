package com.rsschool.catsapp.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.rsschool.catsapp.api.CatsApi
import com.rsschool.catsapp.model.Cat
import retrofit2.HttpException
import java.io.IOException

private const val START_PAGE_INDEX = 0

class CatsPagingSource (
    private val catsApi: CatsApi
) : PagingSource<Int, Cat>() {
    override fun getRefreshKey(state: PagingState<Int, Cat>): Int? {
        return state.anchorPosition?.let { state.closestItemToPosition(it)?.id?.toInt() }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Cat> {
        val position = params.key ?: START_PAGE_INDEX
        return try {
            val response = catsApi.loadImages(params.loadSize, position)
            val images = response
            LoadResult.Page(
                data = images,
                prevKey = if (position == START_PAGE_INDEX) null else position - 1,
                nextKey = if (images.isEmpty()) null else position + 1
            )
        } catch (ex : IOException) {
            LoadResult.Error(ex)
        } catch (ex: HttpException) {
            LoadResult.Error(ex)
        }
    }
}
