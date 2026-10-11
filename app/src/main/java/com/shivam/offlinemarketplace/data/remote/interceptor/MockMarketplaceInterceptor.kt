package com.shivam.offlinemarketplace.data.remote.interceptor

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.shivam.offlinemarketplace.data.remote.model.RemoteListingResponse
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
Mock OkHttp interceptor for listing and sync REST endpoints.

POST creates listings (201); PUT updates listings (200).

[fail] simulates a 400 error; [retry] simulates a network timeout.
 */
@Singleton
class MockMarketplaceInterceptor @Inject constructor() : Interceptor {

    private val gson = Gson()
    var simulatedDelayMs: Long = 300L

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath
        val method = request.method

        if (simulatedDelayMs > 0) {
            try {
                Thread.sleep(simulatedDelayMs)
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }

        // GET /api/v1/listings - Fetch initial / remote listings (Mock 200 seed items)
        if (method.equals("GET", ignoreCase = true) && path.endsWith("api/v1/listings")) {
            val seedListings = generate200MockListings()
            return buildSuccessResponse(
                chain = chain,
                code = 200,
                message = "OK",
                jsonBody = gson.toJson(seedListings)
            )
        }

        // POST /api/v1/listings - Create listing
        if (method.equals("POST", ignoreCase = true) && path.endsWith("api/v1/listings")) {
            val bodyString = readRequestBody(request)

            if (bodyString.contains("[fail]", ignoreCase = true)) {
                return buildErrorResponse(
                    chain = chain,
                    code = 400,
                    message = "Bad Request",
                    errorDetail = "Simulated 400 Bad Request: Server content policy rejection"
                )
            }

            if (bodyString.contains("[retry]", ignoreCase = true)) {
                throw IOException("Simulated 503 Network Timeout: Server unreachable")
            }

            val jsonObject = parseJson(bodyString)
            val title = jsonObject?.get("title")?.asString ?: "Untitled"
            val description = jsonObject?.get("description")?.asString ?: ""
            val price = jsonObject?.get("price")?.asDouble ?: 0.0
            val category = jsonObject?.get("category")?.asString ?: "Other"
            val localImageUri = jsonObject?.get("localImageUri")?.asString

            val remoteId = "remote_${UUID.randomUUID().toString().take(8)}"
            val simulatedImageUrl = if (!localImageUri.isNullOrBlank()) {
                "https://marketplace.mock.api/images/$remoteId.jpg"
            } else {
                null
            }

            val responseDto = RemoteListingResponse(
                remoteId = remoteId,
                title = title,
                description = description,
                price = price,
                category = category,
                remoteImageUrl = simulatedImageUrl,
                updatedAt = System.currentTimeMillis()
            )

            return buildSuccessResponse(
                chain = chain,
                code = 201,
                message = "Created",
                jsonBody = gson.toJson(responseDto)
            )
        }

        // PUT /api/v1/listings/{remoteId} - Update listing
        if (method.equals("PUT", ignoreCase = true) && path.contains("api/v1/listings/")) {
            val remoteId = path.substringAfterLast("/")
            val bodyString = readRequestBody(request)

            if (bodyString.contains("[fail]", ignoreCase = true)) {
                return buildErrorResponse(
                    chain = chain,
                    code = 400,
                    message = "Bad Request",
                    errorDetail = "Simulated 400 Bad Request: Server content policy rejection"
                )
            }

            if (bodyString.contains("[retry]", ignoreCase = true)) {
                throw IOException("Simulated 503 Network Timeout: Server unreachable")
            }

            val jsonObject = parseJson(bodyString)
            val title = jsonObject?.get("title")?.asString ?: "Untitled"
            val description = jsonObject?.get("description")?.asString ?: ""
            val price = jsonObject?.get("price")?.asDouble ?: 0.0
            val category = jsonObject?.get("category")?.asString ?: "Other"

            val responseDto = RemoteListingResponse(
                remoteId = remoteId,
                title = title,
                description = description,
                price = price,
                category = category,
                remoteImageUrl = null,
                updatedAt = System.currentTimeMillis()
            )

            return buildSuccessResponse(
                chain = chain,
                code = 200,
                message = "OK",
                jsonBody = gson.toJson(responseDto)
            )
        }

        return chain.proceed(request)
    }

    private fun readRequestBody(request: okhttp3.Request): String {
        val requestBody = request.body ?: return ""
        val buffer = Buffer()
        requestBody.writeTo(buffer)
        return buffer.readString(StandardCharsets.UTF_8)
    }

    private fun parseJson(body: String): JsonObject? {
        return try {
            JsonParser.parseString(body).asJsonObject
        } catch (_: Exception) {
            null
        }
    }

    private fun buildSuccessResponse(
        chain: Interceptor.Chain,
        code: Int,
        message: String,
        jsonBody: String
    ): Response {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        return Response.Builder()
            .request(chain.request())
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message(message)
            .body(jsonBody.toResponseBody(mediaType))
            .addHeader("Content-Type", "application/json")
            .build()
    }

    private fun buildErrorResponse(
        chain: Interceptor.Chain,
        code: Int,
        message: String,
        errorDetail: String
    ): Response {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val errorJson = gson.toJson(mapOf("error" to errorDetail, "statusCode" to code))
        return Response.Builder()
            .request(chain.request())
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message(message)
            .body(errorJson.toResponseBody(mediaType))
            .addHeader("Content-Type", "application/json")
            .build()
    }

    private fun generate200MockListings(): List<RemoteListingResponse> {
        val categories = listOf("Electronics", "Furniture", "Books", "Fashion", "Other")

        val titleCatalog = mapOf(
            "Electronics" to listOf(
                "Noise-Canceling Wireless Headphones",
                "Ergonomic Mechanical Keyboard",
                "Ultra-Wide 4K Gaming Monitor",
                "Compact Bluetooth Speaker",
                "Fast Qi Wireless Charging Pad",
                "USB-C Multi-Port Hub Adapter",
                "4K Streaming Media Player",
                "High-Speed External SSD 1TB"
            ),
            "Furniture" to listOf(
                "Ergonomic Mesh Office Chair",
                "Solid Oak Minimalist Desk",
                "Adjustable Height Standing Desk",
                "Modern Scandinavian Coffee Table",
                "Comfortable Fabric Recliner",
                "Modular Bookshelf Storage Unit",
                "Dimmable LED Floor Arc Lamp",
                "Mid-Century Velvet Armchair"
            ),
            "Books" to listOf(
                "Clean Architecture Handbook",
                "Kotlin in Action 2nd Edition",
                "The Pragmatic Programmer Guide",
                "Designing Data-Intensive Apps",
                "Refactoring and Design Patterns",
                "Effective Kotlin Masterclass",
                "Domain-Driven Design Reference",
                "Modern Android Development Manual"
            ),
            "Fashion" to listOf(
                "Classic Leather Biker Jacket",
                "Waterproof Trail Running Shoes",
                "Vintage Denim Overcoat",
                "Merino Wool Winter Sweater",
                "Casual Linen Button-Down Shirt",
                "Polarized Aviator Sunglasses",
                "Premium Canvas Backpack",
                "Tailored Wool Blazer"
            ),
            "Other" to listOf(
                "Stainless Steel Thermal Flask",
                "Ceramic Pour-Over Coffee Dripper",
                "Handcrafted Scented Soy Candle",
                "Compact Multi-Tool Survival Kit",
                "Adjustable Resistance Exercise Bands",
                "Hard-Shell Travel Suitcase",
                "Organic Gardening Seed Starter Kit",
                "Smart Wi-Fi LED Desk Lamp"
            )
        )

        val seedList = ArrayList<RemoteListingResponse>(200)
        val baseTimestamp = 1710000000000L

        for (i in 1..200) {
            val category = categories[(i - 1) % categories.size]
            val titles = titleCatalog[category] ?: listOf("Marketplace Item")
            val baseTitle = titles[((i - 1) / categories.size) % titles.size]
            val variant = (i - 1) / (categories.size * titles.size) + 1
            val title = if (variant > 1) "$baseTitle (Model $variant)" else baseTitle
            val price = 10.0 + ((i * 23) % 490) + 0.99
            val description = "Authentic and tested $title in great working condition. Includes original packaging."
            val imageUrl = "https://picsum.photos/seed/$i/400/300"

            seedList.add(
                RemoteListingResponse(
                    remoteId = "sample_$i",
                    title = title,
                    description = description,
                    price = price,
                    category = category,
                    remoteImageUrl = imageUrl,
                    updatedAt = baseTimestamp + (i * 60_000L)
                )
            )
        }

        return seedList
    }
}
