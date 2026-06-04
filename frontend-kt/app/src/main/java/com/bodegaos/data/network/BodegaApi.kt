package com.bodegaos.data.network

import com.bodegaos.data.model.Product
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import com.google.gson.annotations.SerializedName

data class MovementDTO(
    val product_id: String,
    val quantity_change: Int,
    val movement_type: String
)

data class MovementResponse(
    val id: String,
    val product_id: String,
    val sku: String,
    @SerializedName("product_name") val productName: String,
    @SerializedName("quantity_change") val quantityChange: Int,
    @SerializedName("movement_type") val movementType: String,
    @SerializedName("created_at") val createdAt: String
)

interface BodegaApi {
    @GET("/api/products")
    suspend fun getProducts(): List<Product>

    @POST("/api/products")
    suspend fun createProduct(@Body product: Product): Product

    @PUT("/api/products/{id}")
    suspend fun updateProduct(@Path("id") id: String, @Body product: Product): Product

    @DELETE("/api/products/{id}")
    suspend fun deleteProduct(@Path("id") id: String)

    @GET("/api/inventory/movements")
    suspend fun getAllMovements(): List<MovementResponse>

    @POST("/api/inventory/movements")
    suspend fun recordMovement(@Body movement: MovementDTO): Any
}

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8000/" // El puerto correcto de tu log

    val api: BodegaApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BodegaApi::class.java)
    }
}