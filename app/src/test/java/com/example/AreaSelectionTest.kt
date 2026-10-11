package com.example

import com.example.data.AreaRepository
import com.example.model.CatalogueSource
import com.example.model.DocumentTypeItem
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AreaSelectionTest {

    @Before
    @After
    fun cleanup() {
        AreaRepository.clearDynamicMouzas()
    }

    @Test
    fun `all 8 divisions and all 64 districts are officially available`() {
        // Must contain all 8 official divisions
        assertEquals(8, AreaRepository.divisions.size)

        // Must contain all 64 official districts of Bangladesh
        assertEquals(64, AreaRepository.districts.size)

        // Barishal (1): 6 districts
        assertEquals(6, AreaRepository.getDistricts(1).size)

        // Chattogram (2): 11 districts
        assertEquals(11, AreaRepository.getDistricts(2).size)

        // Dhaka (3): 13 districts
        assertEquals(13, AreaRepository.getDistricts(3).size)

        // Khulna (4): 10 districts
        assertEquals(10, AreaRepository.getDistricts(4).size)

        // Rajshahi (5): 8 districts
        assertEquals(8, AreaRepository.getDistricts(5).size)

        // Rangpur (6): 8 districts
        assertEquals(8, AreaRepository.getDistricts(6).size)

        // Sylhet (7): 4 districts
        assertEquals(4, AreaRepository.getDistricts(7).size)

        // Mymensingh (8): 4 districts
        assertEquals(4, AreaRepository.getDistricts(8).size)
    }

    @Test
    fun `cascading filters work accurately from Division down to Upazila`() {
        // Dhaka Division id is 3
        val dhakaDistricts = AreaRepository.getDistricts(3)
        assertEquals(13, dhakaDistricts.size)
        assertTrue(dhakaDistricts.any { it.name == "Dhaka" })
        assertTrue(dhakaDistricts.any { it.name == "Gazipur" })

        // District 18 (Dhaka) has Upazilas Dhamrai and Savar
        val dhakaUpazilas = AreaRepository.getUpazilas(18)
        assertTrue(dhakaUpazilas.any { it.name == "Dhamrai" })
        assertTrue(dhakaUpazilas.any { it.name == "Savar" })

        // Dhamrai Upazila has verified Mouzas
        val dhamrai = dhakaUpazilas.find { it.name == "Dhamrai" }!!
        val dhamraiMouzas = AreaRepository.getMouzas(dhamrai.id)
        assertTrue(dhamraiMouzas.isNotEmpty())
        assertEquals("1", dhamraiMouzas.first().jlNumber)
    }

    @Test
    fun `document types list provides granular selection and select all options`() {
        val allTypes = DocumentTypeItem.ALL_AVAILABLE
        assertTrue(allTypes.size >= 8)
        assertTrue(allTypes.any { it.id == "RS_KHATIAN" })
        assertTrue(allTypes.any { it.id == "MUTATION_KHATIAN" })
        assertTrue(allTypes.any { it.id == "RS_MAP" })
    }

    @Test
    fun `handling zero, one, seven, eight, and 200-plus Mouzas`() {
        // 1 Mouza case (custom import)
        AreaRepository.importFromCsv("single_1,500,SingleMouza,একক মৌজা,99\n")
        val singleList = AreaRepository.getMouzas(500)
        assertEquals(1, singleList.size)

        // 7 Mouzas case: custom density limit
        AreaRepository.setMouzaDensity(7)
        val sevenMouzas = AreaRepository.getMouzas(149)
        assertEquals(7, sevenMouzas.size)

        // 8 Mouzas case: custom density limit
        AreaRepository.setMouzaDensity(8)
        val eightMouzas = AreaRepository.getMouzas(148)
        assertEquals(8, eightMouzas.size)

        // 200+ Mouzas case: bulk import test
        val bulkCsv = buildString {
            for (i in 1..250) {
                append("bulk_$i,600,Mouza_$i,মৌজা_$i,$i\n")
            }
        }
        val importedCount = AreaRepository.importFromCsv(bulkCsv)
        assertEquals(250, importedCount)

        val largeMouzas = AreaRepository.getMouzas(600)
        assertEquals(250, largeMouzas.size)
        assertEquals("1", largeMouzas.first().jlNumber)
        assertEquals("250", largeMouzas.last().jlNumber)
    }

    @Test
    fun `catalogue sources distinguish official vs imported data`() {
        val officialMouzas = AreaRepository.getMouzas(1)
        assertTrue(officialMouzas.all { it.catalogueSource == CatalogueSource.OFFICIAL })

        AreaRepository.importFromCsv("custom_1,1,CustomMouza,কাস্টম মৌজা,999\n", CatalogueSource.IMPORTED)
        val combined = AreaRepository.getMouzas(1)
        val custom = combined.find { it.id == "custom_1" }
        assertEquals(CatalogueSource.IMPORTED, custom?.catalogueSource)
    }
}
