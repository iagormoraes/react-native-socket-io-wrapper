package com.reactnativesocketio

import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule

import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import io.socket.engineio.client.EngineIOException

import org.json.JSONArray
import org.json.JSONObject

import java.net.URISyntaxException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.round


class SocketIoModule(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
  private val callbackRegisters: ArrayList<CallbackRegister> = ArrayList()

  private var mSocket: Socket? = null

  override fun getName(): String {
    return "SocketIo"
  }

  override fun onCatalystInstanceDestroy() {
    super.onCatalystInstanceDestroy()
    // clean register to avoid storing unnecessarily data to memory
    callbackRegisters.forEach { mSocket?.off(it.eventName, it.onEventListener) }

    callbackRegisters.clear()
  }

  @ReactMethod
  fun initialize(url: String, options: ReadableMap, callback: Callback) {
    try {
      val socketOptions = IO.Options()

      if(options.hasKey("transports")) {
        options.getArray("transports")?.let {
          val transportsList = it.toArrayList()
          val transportsArr = arrayOfNulls<String>(transportsList.size)

          socketOptions.transports = transportsList.toArray(transportsArr)
        }
      }

      if(options.hasKey("forceNew")) {
        options.getBoolean("forceNew").let { socketOptions.forceNew = it }
      }

      if(options.hasKey("multiplex")) {
        options.getBoolean("multiplex").let { socketOptions.multiplex = it }
      }

      if(options.hasKey("reconnectionAttempts")) {
        options.getInt("reconnectionAttempts").let { socketOptions.reconnectionAttempts = it }
      }

      if(options.hasKey("reconnectionDelay")) {
        options.getInt("reconnectionDelay").let { socketOptions.reconnectionDelay = it.toLong() }
      }

      if(options.hasKey("reconnectionDelayMax")) {
        options.getInt("reconnectionDelayMax").let { socketOptions.reconnectionDelayMax = it.toLong() }
      }

      if(options.hasKey("randomizationFactor")) {
        options.getInt("randomizationFactor").let { socketOptions.randomizationFactor = it.toDouble() }
      }

      if(options.hasKey("timeout")) {
        options.getInt("timeout").let { socketOptions.timeout = it.toLong() }
      }

      if(options.hasKey("query")) {
        options.getString("query").let { socketOptions.query = it }
      }

      mSocket = IO.socket(url, socketOptions)

      callback.invoke(null)
    } catch (e: URISyntaxException) {
      callback.invoke(e)
    }
  }

  @ReactMethod
  fun connect() {
    mSocket?.connect()
  }

  @ReactMethod
  fun disconnect() {
    mSocket?.disconnect()
  }

  @ReactMethod
  fun emit(eventName: String, options: ReadableMap) {
    if(options.hasKey("data")) {

      when(options.getType("data")) {
        ReadableType.Map -> {
          mSocket?.emit(eventName, JSONObject(options.getMap("data")!!.toHashMap()))
        }

        ReadableType.Array -> {
          mSocket?.emit(eventName, JSONArray(options.getArray("data")!!.toArrayList()))
        }

        ReadableType.Number -> {
          val number: Double = options.getDouble("data")
          if (number == round(number)) {
            mSocket?.emit(eventName, number.toInt())
          } else {
            mSocket?.emit(eventName, number)
          }
        }

        ReadableType.Boolean -> {
          mSocket?.emit(eventName, options.getBoolean("data"))
        }

        ReadableType.Null -> {
          mSocket?.emit(eventName, null)
        }

        else -> mSocket?.emit(eventName, options.getString("data")!!)
      }
    }
  }

  @ReactMethod
  fun on(eventName: String, callback: Callback) {
    val uniqueID = UUID.randomUUID().toString()
    val callbackRegister = CallbackRegister(eventName, uniqueID)

    callbackRegisters.add(callbackRegister)

    callback.invoke(eventName, uniqueID)

    mSocket?.on(eventName, callbackRegister.onEventListener)
  }

  @ReactMethod
  fun once(eventName: String, callback: Callback) {
    this.on(eventName, callback)
  }

  @ReactMethod
  fun off(eventName: String, uniqueID: String) {
    val index = callbackRegisters.indexOfFirst { it.uniqueID == uniqueID }

    if(index < 0) return

    mSocket?.off(eventName, callbackRegisters[index].onEventListener)

    callbackRegisters.removeAt(index)
  }

  fun sendEvent(eventName: String, props: WritableMap) {
    props.putString("eventName", eventName)

    if(reactContext.hasActiveCatalystInstance()) {
      reactContext
        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
        .emit("onEventListener", props)
    }
  }

  inner class CallbackRegister(val eventName: String, val uniqueID: String) {
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

      sendEvent(eventName, props)
    }
  }
}
