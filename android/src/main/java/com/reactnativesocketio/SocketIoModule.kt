package com.reactnativesocketio

import com.facebook.react.bridge.*
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.modules.core.DeviceEventManagerModule

import io.socket.client.IO
import io.socket.client.Socket

import org.json.JSONArray
import org.json.JSONObject

import java.net.URISyntaxException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.round


@ReactModule(name = SocketIoModule.NAME, hasConstants = false)
class SocketIoModule(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
  companion object { const val NAME = "RNSocketIO" }

  private var mSocket: Socket? = null

  private val callbackRegisters: ArrayList<CallbackRegister> = ArrayList()

  private val callbackListener = object : SocketCallback {
    override fun onReceive(eventName: String, props: WritableMap) {
      sendEvent(eventName, props)
    }
  }

  override fun getName(): String {
    return NAME
  }

  override fun onCatalystInstanceDestroy() {
    super.onCatalystInstanceDestroy()
    // clean register to avoid storing unnecessarily data to memory
    callbackRegisters.forEach { mSocket?.off(it.eventName, it.onEventListener) }

    callbackRegisters.clear()
  }

  private fun sendEvent(eventName: String, props: WritableMap) {
    props.putString("eventName", eventName)

    if(reactContext.hasActiveCatalystInstance()) {
      reactContext
        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
        .emit("onEventListener", props)
    }
  }

  private fun buildCallbackRegister(eventName: String): CallbackRegister {
    val uniqueID = UUID.randomUUID().toString()
    val callbackRegister = CallbackRegister(eventName, uniqueID, callbackListener)

    callbackRegisters.add(callbackRegister)

    return callbackRegister
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

      callbackRegisters.clear()

      mSocket?.off()

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
    val callbackRegister = buildCallbackRegister(eventName)

    callback.invoke(eventName, callbackRegister.uniqueID)

    mSocket?.on(eventName, callbackRegister.onEventListener)
  }

  @ReactMethod
  fun once(eventName: String, callback: Callback) {
    val callbackRegister = buildCallbackRegister(eventName)

    callback.invoke(eventName, callbackRegister.uniqueID)

    mSocket?.once(eventName, callbackRegister.onEventListener)
  }

  @ReactMethod
  fun off(eventName: String, uniqueID: String) {
    val index = callbackRegisters.indexOfFirst { it.uniqueID == uniqueID }

    if(index < 0) return

    mSocket?.off(eventName, callbackRegisters[index].onEventListener)

    callbackRegisters.removeAt(index)
  }

  @ReactMethod
  fun connected(promise: Promise) {
    promise.resolve(connectedSync())
  }

  @ReactMethod
  fun getId(promise: Promise) {
    promise.resolve(getIdSync())
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun connectedSync(): Boolean {
    return mSocket?.connected() ?: false
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun getIdSync(): String? {
    return mSocket?.id()
  }
}
