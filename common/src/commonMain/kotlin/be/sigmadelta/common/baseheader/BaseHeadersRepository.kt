package be.sigmadelta.common.baseheader

import be.sigmadelta.common.util.ApiResponse
import be.sigmadelta.common.util.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn


class BaseHeadersRepository(private val baseHeadersApi: BaseHeadersApi) {

    fun observeBaseHeaders() : Flow<Response<BaseHeadersResponse>> = flow {
        emit(Response.Loading())

        emit(when(val apiResponse = baseHeadersApi.getBaseHeaders()) {
            is ApiResponse.Success -> Response.Success(apiResponse.body)
            is ApiResponse.Error -> Response.Error(apiResponse.error)
        })
    }.flowOn(Dispatchers.Default)
}