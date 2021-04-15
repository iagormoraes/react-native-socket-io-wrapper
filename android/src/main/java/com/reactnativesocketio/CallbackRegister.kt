package com.reactnativesocketio

import com.facebook.react.bridge.Arguments
import io.socket.emitter.Emitter
import io.socket.engineio.client.EngineIOException
import org.json.JSONArray
import org.json.JSONObject

class CallbackRegister(val eventName: String, val uniqueID: String, private val callback: SocketCallback) {
  val onEventListener = Emitter.Listener { args ->
    val props = Arguments.createMap()

    props.putString("uniqueID", uniqueID)

    if(args.isNotEmpty()) {
      when(val data = args[0]) {
        is Int -> {
          props.putInt("data", data)
        }
        is Double -> {
          props.putDouble("data", data)
        }
        is Boolean -> {
          props.putBoolean("data", data)
        }
        is String -> {
          props.putString("data", data)
        }
        is JSONObject -> {
          val dataMap = Arguments.makeNativeMap(JSONHelpers.toMap(data))

          props.putMap("data", dataMap)
        }
        is JSONArray -> {
          val dataList = Arguments.makeNativeArray(JSONHelpers.toList(data))

          props.putArray("data", dataList)
        }

        is EngineIOException -> {
          props.putString("data", data.message)
        }

        else -> props.putNull("data")
      }
    }

    callback.onReceive(eventName, props)
  }
}
