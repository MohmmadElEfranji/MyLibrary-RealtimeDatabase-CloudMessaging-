package com.example.mylibrary3.model.remote

import com.example.mylibrary3.model.entity.PushNotification
import com.example.mylibrary3.util.Constant.Companion.SERVER_KEY
import com.example.mylibrary3.util.Constant.Companion.CONTENT_TYPE
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface NotificationAPI {

    @Headers("Authorization: key=$SERVER_KEY", "Content-Type:$CONTENT_TYPE")
    @POST("fcm/send")
    suspend fun pushNotification(
        @Body notification: PushNotification

    ):Response<ResponseBody>


}