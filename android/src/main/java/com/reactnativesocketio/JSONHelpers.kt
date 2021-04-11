package com.reactnativesocketio

import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList


class JSONHelpers {
  companion object {
    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    fun toMap(jsonObject: JSONObject): Map<String, Any?> {
      val map: MutableMap<String, Any?> = HashMap()
      val keysItr: Iterator<String> = jsonObject.keys().iterator()

      while (keysItr.hasNext()) {
        val key = keysItr.next()
        var value = jsonObject[key]

        when (value) {
          JSONObject.NULL -> {
            value = null
          }
          is JSONArray -> {
            value = toList(value)
          }
          is JSONObject -> {
            value = toMap(value)
          }
        }

        map[key] = value
      }

      return map
    }

    fun toList(array: JSONArray): ArrayList<Any> {
      val list: ArrayList<Any> = ArrayList()

      for (i in 0 until array.length()) {
        var value = array[i]

        if (value is JSONArray) {
          value = toList(value)
        }
        if (value is JSONObject) {
          value = toMap(value)
        }

        list.add(value)
      }

      return list
    }
  }
}
