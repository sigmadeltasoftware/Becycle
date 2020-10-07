package be.sigmadelta.common.accesstoken

import be.sigmadelta.common.util.Response
import be.sigmadelta.common.util.apiRequestToFlow
import kotlinx.coroutines.flow.Flow


class AccessTokenRepository(private val accessTokenApi: AccessTokenApi) {

    suspend fun getAccessToken() : Flow<Response<AccessToken>> =
        accessTokenApi.getAccessToken().apiRequestToFlow()
}