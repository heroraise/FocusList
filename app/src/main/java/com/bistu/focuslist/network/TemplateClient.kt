package com.bistu.focuslist.network

import okhttp3.OkHttpClient

/**
 * 学习模板网络客户端。
 *
 * 模板 JSON 放在仓库中，可通过 GitHub Raw 作为静态接口读取。
 * 请求失败时模板仓库会回退到本地模板。
 */
object TemplateClient {

    private const val MAIN_BASE_URL =
        "https://raw.githubusercontent.com/lgydhei/FocusList/main/app/src/main/assets/"
    private const val PREVIEW_BASE_URL =
        "https://raw.githubusercontent.com/lgydhei/FocusList/codex/fix-network-config/app/src/main/assets/"

    private val okHttp: OkHttpClient by lazy {
        createJsonOkHttpClient(timeoutSeconds = 6)
    }

    val api: TemplateApi by lazy {
        createTemplateApi(MAIN_BASE_URL, okHttp)
    }

    private val previewApi: TemplateApi by lazy {
        createTemplateApi(PREVIEW_BASE_URL, okHttp)
    }

    suspend fun getTemplates(): TemplateResponse {
        return try {
            api.getTemplates()
        } catch (_: Exception) {
            previewApi.getTemplates()
        }
    }

    internal fun createTemplateApi(
        baseUrl: String,
        okHttpClient: OkHttpClient = createJsonOkHttpClient(timeoutSeconds = 6)
    ): TemplateApi {
        return createJsonApi(baseUrl, okHttpClient)
    }
}
