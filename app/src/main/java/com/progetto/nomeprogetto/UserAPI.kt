package com.progetto.nomeprogetto

import com.google.gson.JsonObject
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface UserAPI {

    @POST("postSelect/")
    @FormUrlEncoded
    fun sendQuery(@Field("query") query: String): Call<JsonObject>

    @POST("postUpdate/")
    @FormUrlEncoded
    fun modifica(@Field("query") query: String): Call<JsonObject>

    @GET
    fun getImage(@Url url: String) : Call<ResponseBody>

}