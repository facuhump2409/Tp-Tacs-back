package com.grupox.wololo.model.services

import arrow.core.*
import arrow.core.Option
import arrow.core.extensions.list.foldable.foldLeft
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.grupox.wololo.model.Coordinates
import com.grupox.wololo.model.Province
import com.grupox.wololo.model.Town
import io.github.rybalkinsd.kohttp.ext.httpGet
import io.github.rybalkinsd.kohttp.jackson.ext.toType
import okhttp3.Response

@JsonIgnoreProperties(ignoreUnknown = true)
sealed class GeoRefResponse() {
    data class ProvinceQuery(
        @JsonProperty("provincias") val matches: List<LocationData>
    ) : GeoRefResponse()

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class TownsQuery(
        @JsonProperty("municipios") val matches: List<LocationData>
    ) : GeoRefResponse()
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class LocationData (
        @JsonProperty("centroide") val coordinates: Coordinates,
        @JsonProperty("id") val id: Int,
        @JsonProperty("nombre") val name: String
)

object GeoRef {
    private const val exactValue: Boolean = true
    private const val maxMatches: Int = 200 // Numero maximo de resultados que devuelve una query a georef
    private const val provinceDataUrl = "https://apis.datos.gob.ar/georef/api/provincias"
    private const val townsDataUrl = "https://apis.datos.gob.ar/georef/api/municipios"

    fun generateProvince(provinceName: String): Option<Province> {
        return httpGetProvinceData(provinceName).map {
            Province(
                    it.id,
                    it.name,
                    ArrayList(httpGetTownsData(it.id).map { townData -> Town(townData.id, townData.name, townData.coordinates) }),
                    it.coordinates
            )
        }
    }

    private fun httpGetProvinceData(name: String): Option<LocationData> {
        val queryData: Option<GeoRefResponse.ProvinceQuery> =
                httpGetQueryData(provinceDataUrl, mapOf("nombre" to name, "exacto" to exactValue.toString()))

        return queryData.mapNotNull { it.matches.firstOrNull() }
    }

    private fun httpGetTownsData(provinceId: Int): List<LocationData> =
        httpGetQueryData<GeoRefResponse.TownsQuery>(townsDataUrl, mapOf("provincia" to provinceId.toString(), "max" to maxMatches.toString()))
            .toList().flatMap { it.matches }

    private inline fun <reified QueryDataT : GeoRefResponse>httpGetQueryData(url: String, queryParams: Map<String, String>): Option<QueryDataT> {
        val finalUrl = appendQueryParams(url, queryParams)
        println(finalUrl)
        val queryResponse: Response = finalUrl.httpGet()
        val mapper = ObjectMapper().registerKotlinModule()
        return Some(queryResponse).mapNotNull { it.toType<QueryDataT>(mapper) }
    }

    private fun appendQueryParams(url: String, queryParams: Map<String, String>): String =
        queryParams.toList().foldLeft("$url?") { unf, (key, value) -> "$unf$key=$value&" }.removeSuffix("&")

}
