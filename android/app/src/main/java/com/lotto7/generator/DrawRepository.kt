package com.lotto7.generator

import android.content.Context
import org.json.JSONArray

object DrawRepository {
    fun loadDraws(context: Context): List<Draw> {
        val json = context.assets.open("draws.json").bufferedReader().use { it.readText() }
        val array = JSONArray(json)
        val draws = mutableListOf<Draw>()

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val numsArray = obj.getJSONArray("nums")
            val nums = List(numsArray.length()) { idx -> numsArray.getInt(idx) }
            val month = if (obj.isNull("month")) null else obj.getInt("month")
            draws.add(
                Draw(
                    round = obj.getString("round"),
                    date = obj.getString("date"),
                    month = month,
                    nums = nums
                )
            )
        }
        return draws
    }
}
