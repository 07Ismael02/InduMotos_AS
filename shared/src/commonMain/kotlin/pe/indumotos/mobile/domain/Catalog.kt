package pe.indumotos.mobile.domain

import kotlinx.serialization.Serializable

const val MANAGE_CATALOG = "CATALOGO_GESTIONAR"
@Serializable data class CatalogOption(val marca: String, val modelo: String, val color: String)
interface CatalogRepository {
    suspend fun catalog(): List<CatalogOption>
    suspend fun requestOptions(): List<CatalogOption>
    suspend fun addCatalog(option: CatalogOption): CatalogOption
}
fun List<CatalogOption>.brands() = map { it.marca }.distinct().sorted()
fun List<CatalogOption>.models(brand: String) = filter { it.marca == brand }.map { it.modelo }.distinct().sorted()
fun List<CatalogOption>.colors(brand: String, model: String) = filter { it.marca == brand && it.modelo == model }.map { it.color }.distinct().sorted()
fun List<CatalogOption>.containsCombination(brand: String, model: String, color: String) =
    any { it.marca == brand && it.modelo == model && it.color == color }
