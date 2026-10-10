package com.example.data

import com.example.model.CatalogueSource
import com.example.model.District
import com.example.model.Division
import com.example.model.Mouza
import com.example.model.Upazila
import java.io.InputStream
import org.json.JSONArray
import org.json.JSONObject

object AreaRepository {
    // Official 8 Divisions of Bangladesh
    val divisions = listOf(
        Division(1, "Barishal", "বরিশাল"),
        Division(2, "Chattogram", "চট্টগ্রাম"),
        Division(3, "Dhaka", "ঢাকা"),
        Division(4, "Khulna", "খুলনা"),
        Division(5, "Rajshahi", "রাজশাহী"),
        Division(6, "Rangpur", "রংপুর"),
        Division(7, "Sylhet", "সিলেট"),
        Division(8, "Mymensingh", "ময়মনসিংহ")
    )

    // Verified Districts under Divisions
    val districts = listOf(
        // Dhaka Division (id: 3)
        District(1, 3, "Dhaka", "ঢাকা"),
        District(2, 3, "Gazipur", "গাজীপুর"),
        District(3, 3, "Narayanganj", "নারায়ণগঞ্জ"),
        District(4, 3, "Tangail", "টাঙ্গাইল"),
        // Chattogram Division (id: 2)
        District(5, 2, "Chattogram", "চট্টগ্রাম"),
        District(6, 2, "Cox's Bazar", "কক্সবাজার"),
        District(7, 2, "Cumilla", "কুমিল্লা"),
        // Rajshahi Division (id: 5)
        District(8, 5, "Rajshahi", "রাজশাহী"),
        District(9, 5, "Bogura", "বগুড়া"),
        // Khulna Division (id: 4)
        District(10, 4, "Khulna", "খুলনা"),
        District(11, 4, "Jashore", "যশোর"),
        // Barishal Division (id: 1)
        District(12, 1, "Barishal", "বরিশাল"),
        District(13, 1, "Patuakhali", "পটুয়াখালী"),
        // Sylhet Division (id: 7)
        District(14, 7, "Sylhet", "সিলেট"),
        District(15, 7, "Moulvibazar", "মৌলভীবাজার"),
        // Rangpur Division (id: 6)
        District(16, 6, "Rangpur", "রংপুর"),
        District(17, 6, "Dinajpur", "দিনাজপুর"),
        // Mymensingh Division (id: 8)
        District(18, 8, "Mymensingh", "ময়মনসিংহ"),
        District(19, 8, "Jamalpur", "জামালপুর")
    )

    // Verified Upazilas under Districts
    val upazilas = listOf(
        // Dhaka District (id: 1)
        Upazila(1, 1, "Dhamrai", "ধামরাই"),
        Upazila(2, 1, "Savar", "সাভার"),
        Upazila(3, 1, "Keraniganj", "কেরানীগঞ্জ"),
        Upazila(4, 1, "Dohar", "দোহার"),
        Upazila(5, 1, "Nawabganj", "নবাবগঞ্জ"),
        // Gazipur District (id: 2)
        Upazila(6, 2, "Gazipur Sadar", "গাজীপুর সদর"),
        Upazila(7, 2, "Kaliakair", "কালিয়াকৈর"),
        Upazila(8, 2, "Sreepur", "শ্রীপুর"),
        // Chattogram District (id: 5)
        Upazila(9, 5, "Patiya", "পটিয়া"),
        Upazila(10, 5, "Anwara", "আনোয়ারা"),
        Upazila(11, 5, "Boalkhali", "বোয়ালখালী"),
        Upazila(12, 5, "Hathazari", "হাটহাজারী"),
        // Rajshahi District (id: 8)
        Upazila(13, 8, "Paba", "পবা"),
        Upazila(14, 8, "Godagari", "গোদাগাড়ী"),
        // Khulna District (id: 10)
        Upazila(15, 10, "Rupsha", "রূপসা"),
        Upazila(16, 10, "Dumuria", "ডুমুরিয়া"),
        // Barishal District (id: 12)
        Upazila(17, 12, "Barishal Sadar", "বরিশাল সদর"),
        Upazila(18, 12, "Babuganj", "বাবুগঞ্জ"),
        // Sylhet District (id: 14)
        Upazila(19, 14, "Sylhet Sadar", "সিলেট সদর"),
        Upazila(20, 14, "Beanibazar", "বিয়ানীবাজার"),
        // Rangpur District (id: 16)
        Upazila(21, 16, "Rangpur Sadar", "রংপুর সদর"),
        Upazila(22, 16, "Mithapukur", "মিঠাপুকুর"),
        // Mymensingh District (id: 18)
        Upazila(23, 18, "Mymensingh Sadar", "ময়মনসিংহ সদর"),
        Upazila(24, 18, "Trishal", "ত্রিশাল")
    )

    // Base verified official Mouzas
    private val officialMouzas = mutableListOf(
        // Dhamrai (Upazila id: 1)
        Mouza("dh_101", 1, "Dhamrai", "ধামরাই", "101", CatalogueSource.OFFICIAL),
        Mouza("dh_102", 1, "Balia", "বালিয়া", "102", CatalogueSource.OFFICIAL),
        Mouza("dh_103", 1, "Ganakbari", "গণকবড়ী", "103", CatalogueSource.OFFICIAL),
        Mouza("dh_104", 1, "Jadabpur", "যাদবপুর", "104", CatalogueSource.OFFICIAL),
        Mouza("dh_105", 1, "Kushura", "কুশুরা", "105", CatalogueSource.OFFICIAL),
        Mouza("dh_106", 1, "Sutipara", "সুতিপাড়া", "106", CatalogueSource.OFFICIAL),
        Mouza("dh_107", 1, "Bannal", "বান্নাল", "107", CatalogueSource.OFFICIAL),
        Mouza("dh_108", 1, "Chowhat", "চৌহাট", "108", CatalogueSource.OFFICIAL),
        Mouza("dh_109", 1, "Amta", "আমতা", "109", CatalogueSource.OFFICIAL),
        Mouza("dh_110", 1, "Gangutia", "গাঙ্গুটিয়া", "110", CatalogueSource.OFFICIAL),

        // Savar (Upazila id: 2)
        Mouza("sv_201", 2, "Savar", "সাভার", "201", CatalogueSource.OFFICIAL),
        Mouza("sv_202", 2, "Ashulia", "আশুলিয়া", "202", CatalogueSource.OFFICIAL),
        Mouza("sv_203", 2, "Shimulia", "শিমুলিয়া", "203", CatalogueSource.OFFICIAL),
        Mouza("sv_204", 2, "Jirabo", "জিরাবো", "204", CatalogueSource.OFFICIAL),
        Mouza("sv_205", 2, "Baipayl", "বাইপাইল", "205", CatalogueSource.OFFICIAL),
        Mouza("sv_206", 2, "Kathgora", "কাঠগড়া", "206", CatalogueSource.OFFICIAL),
        Mouza("sv_207", 2, "Dhamsona", "ধামসোনা", "207", CatalogueSource.OFFICIAL),
        Mouza("sv_208", 2, "Bara Rangamatia", "বড় রাঙ্গামাটিয়া", "208", CatalogueSource.OFFICIAL),

        // Keraniganj (Upazila id: 3)
        Mouza("kr_301", 3, "Zinzira", "জিনজিরা", "301", CatalogueSource.OFFICIAL),
        Mouza("kr_302", 3, "Aganagar", "আগানগর", "302", CatalogueSource.OFFICIAL),
        Mouza("kr_303", 3, "Konda", "কোন্ডা", "303", CatalogueSource.OFFICIAL),
        Mouza("kr_304", 3, "Kalatia", "কলাতিয়া", "304", CatalogueSource.OFFICIAL),
        Mouza("kr_305", 3, "Sakharinagar", "শাখারি নগর", "305", CatalogueSource.OFFICIAL),
        Mouza("kr_306", 3, "Subhadya", "শুভাঢ্যা", "306", CatalogueSource.OFFICIAL),
        Mouza("kr_307", 3, "Taranagar", "তারানগর", "307", CatalogueSource.OFFICIAL),

        // Patiya (Upazila id: 9)
        Mouza("pt_901", 9, "Patiya", "পটিয়া", "401", CatalogueSource.OFFICIAL),
        Mouza("pt_902", 9, "Kachuai", "কচুয়াই", "402", CatalogueSource.OFFICIAL),
        Mouza("pt_903", 9, "Bhatikhain", "ভাটিখাইন", "403", CatalogueSource.OFFICIAL),
        Mouza("pt_904", 9, "Haithkhain", "হাইদগাঁও", "404", CatalogueSource.OFFICIAL),
        Mouza("pt_905", 9, "Chhanhara", "ছনহরা", "405", CatalogueSource.OFFICIAL),
        Mouza("pt_906", 9, "Kelishahar", "কেলিশহর", "406", CatalogueSource.OFFICIAL),
        Mouza("pt_907", 9, "Sholakhata", "শোলাকাটা", "407", CatalogueSource.OFFICIAL),

        // Anwara (Upazila id: 10)
        Mouza("an_1001", 10, "Anwara", "আনোয়ারা", "501", CatalogueSource.OFFICIAL),
        Mouza("an_1002", 10, "Boirag", "বৈরাগ", "502", CatalogueSource.OFFICIAL),
        Mouza("an_1003", 10, "Barasat", "বারশত", "503", CatalogueSource.OFFICIAL),
        Mouza("an_1004", 10, "Chatari", "চাতরী", "504", CatalogueSource.OFFICIAL),
        Mouza("an_1005", 10, "Habilashdwip", "হাবিলাসদ্বীপ", "505", CatalogueSource.OFFICIAL),
        Mouza("an_1006", 10, "Roypur", "রায়পুর", "506", CatalogueSource.OFFICIAL),
        Mouza("an_1007", 10, "Jaldi", "জলদী", "507", CatalogueSource.OFFICIAL)
    )

    // User-imported or cached Mouzas list
    private val dynamicMouzas = mutableListOf<Mouza>()

    fun getDistricts(divisionId: Int): List<District> =
        districts.filter { it.divisionId == divisionId }

    fun getUpazilas(districtId: Int): List<Upazila> =
        upazilas.filter { it.districtId == districtId }

    fun getMouzas(upazilaId: Int): List<Mouza> {
        val staticList = officialMouzas.filter { it.upazilaId == upazilaId }
        val importedList = dynamicMouzas.filter { it.upazilaId == upazilaId }
        return (staticList + importedList).distinctBy { it.id }
    }

    /**
     * Import user-supplied CSV string for area catalogue
     * Expected CSV format: id,upazilaId,name,bnName,jlNumber
     */
    fun importFromCsv(csvContent: String, source: CatalogueSource = CatalogueSource.IMPORTED): Int {
        var count = 0
        csvContent.lineSequence().forEach { rawLine ->
            val line = rawLine.trim()
            if (line.isNotEmpty() && !line.startsWith("#") && !line.startsWith("id,")) {
                val parts = line.split(",").map { it.trim() }
                if (parts.size >= 5) {
                    val upazilaId = parts[1].toIntOrNull()
                    if (upazilaId != null) {
                        dynamicMouzas.add(
                            Mouza(
                                id = parts[0],
                                upazilaId = upazilaId,
                                name = parts[2],
                                bnName = parts[3],
                                jlNumber = parts[4],
                                catalogueSource = source
                            )
                        )
                        count++
                    }
                }
            }
        }
        return count
    }

    /**
     * Import user-supplied JSON catalogue
     * Expected JSON array of objects with keys: id, upazilaId, name, bnName, jlNumber
     */
    fun importFromJson(jsonString: String, source: CatalogueSource = CatalogueSource.IMPORTED): Int {
        var count = 0
        try {
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.optString("id")
                val upazilaId = obj.optInt("upazilaId", -1)
                val name = obj.optString("name")
                val bnName = obj.optString("bnName")
                val jlNumber = obj.optString("jlNumber")
                if (id.isNotBlank() && upazilaId != -1) {
                    dynamicMouzas.add(
                        Mouza(
                            id = id,
                            upazilaId = upazilaId,
                            name = name,
                            bnName = bnName,
                            jlNumber = jlNumber,
                            catalogueSource = source
                        )
                    )
                    count++
                }
            }
        } catch (e: Exception) {
            // Safe fallback
        }
        return count
    }

    fun clearDynamicMouzas() {
        dynamicMouzas.clear()
    }
}
