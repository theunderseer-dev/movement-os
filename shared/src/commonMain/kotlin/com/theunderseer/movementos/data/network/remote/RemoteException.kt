package com.theunderseer.movementos.data.network.remote

import com.theunderseer.movementos.data.network.ApiResult

internal class RemoteException(
    val result: ApiResult.Error,
) : RuntimeException("Remote call failed: $result")
