package com.example.data

import com.example.model.District
import com.example.model.Division
import com.example.model.Mouza
import com.example.model.Upazila

object AreaRepository {
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

    val districts = listOf(
        District(1, 3, "Dhaka", "ঢাকা"),
        District(2, 3, "Gazipur", "গাজীপুর"),
        District(3, 2, "Chattogram", "চট্টগ্রাম"),
        District(4, 2, "Cox's Bazar", "কক্সবাজার")
    )

    val upazilas = listOf(
        Upazila(1, 1, "Dhamrai", "ধামরাই"),
        Upazila(2, 1, "Savar", "সাভার"),
        Upazila(3, 3, "Patiya", "পটিয়া"),
        Upazila(4, 3, "Anwara", "আনোয়ারা")
    )

    val mouzas = listOf(
        Mouza("1", 1, "Dhamrai", "ধামরাই", "101"),
        Mouza("2", 1, "Balia", "বালিয়া", "102"),
        Mouza("3", 2, "Savar", "সাভার", "201"),
        Mouza("4", 3, "Patiya", "পটিয়া", "301")
    )

    fun getDistricts(divisionId: Int) = districts.filter { it.divisionId == divisionId }
    fun getUpazilas(districtId: Int) = upazilas.filter { it.districtId == districtId }
    fun getMouzas(upazilaId: Int) = mouzas.filter { it.upazilaId == upazilaId }
}
