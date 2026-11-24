package pe.indumotos.mobile

import kotlin.test.*
import pe.indumotos.mobile.domain.*

class ProfileAndPhotosTest {
    @Test
    fun authorizedAccountCanEditDownloadedGuideRegardlessOfEntryScreen() {
        val access = Access(
            1,
            "admin.indumotos",
            "ADMIN",
            listOf(Permission("FICHAS_REGISTRAR", true), Permission("INVENTARIO_CONSULTAR", true))
        )
        assertTrue(access.canRegisterGuideDrafts())
    }

    @Test
    fun administratorRoleDoesNotBypassMissingRegistrationPermission() {
        val access = Access(
            1,
            "admin.indumotos",
            "ADMIN",
            listOf(Permission("FICHAS_REGISTRAR", false), Permission("INVENTARIO_CONSULTAR", true))
        )
        assertFalse(access.canRegisterGuideDrafts())
    }

    @Test
    fun profileGroupsOnlyEffectiveAccessAndDoesNotDuplicateActions() {
        val account = Access(
            1,
            "almacen.indumotos",
            "ALMACEN",
            listOf(
                Permission("LLEGADAS_REGISTRAR", true),
                Permission("LLEGADAS_REGISTRAR", true),
                Permission("PAGOS_CONFIRMAR", false),
                Permission("INVENTARIO_CONSULTAR", true)
            )
        )
        val groups = account.profileAccessGroups()
        assertEquals(
            setOf("Recepción de motocicletas", "Inventario y fichas"),
            groups.map { it.name }.toSet()
        )
        assertEquals(2, groups.sumOf { it.permissions.size })
        assertTrue(groups.flatMap { it.permissions }.none { it.codigo == "PAGOS_CONFIRMAR" })
    }

    @Test
    fun photographsCanBeAddedWithoutReplacingEarlierCaptures() {
        val first = GuidePhoto("Foto 1", "image/jpeg", byteArrayOf(1, 2))
        val second = GuidePhoto("Foto 2", "image/jpeg", byteArrayOf(3, 4))
        val result = GuideRules.addPhotos(listOf(first), listOf(second, first))
        assertEquals(listOf(first, second), result)
        assertEquals(result, GuideRules.addPhotos(result, emptyList()))
    }

    @Test
    fun fourthPhotographIsRejectedWithoutChangingPreviousSelection() {
        val current = (1..3).map { GuidePhoto("Foto $it", "image/jpeg", byteArrayOf(it.toByte())) }
        assertFailsWith<IllegalArgumentException> {
            GuideRules.addPhotos(
                current,
                listOf(GuidePhoto("Foto 4", "image/jpeg", byteArrayOf(4)))
            )
        }
        assertEquals(3, current.size)
    }
}
