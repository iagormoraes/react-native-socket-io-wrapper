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
import kotlin.collections.HashMap
import kotlin.math.round


@ReactModule(name = SocketIoModule.NAME, hasConstants = false)
class SocketIoModule(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
  companion object { const val NAME = "RNSocketIO" }

  private var mSocketList = HashMap<String, Socket>()

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
    callbackRegisters.forEach {
      for(key in mSocketList.keys) {
        getSocketInstance(key)?.off(it.eventName, it.onEventListener)
      }
    }

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

  private fun getSocketInstance(path: String): Socket? {
    return mSocketList[path]
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

      if(options.hasKey("path")) {
        options.getString("path")?.let {
          socketOptions.path = it
        }
      }

      mSocketList[socketOptions.path] = IO.socket(url, socketOptions)

      callback.invoke(null)
    } catch (e: URISyntaxException) {
      callback.invoke(e)
    }
  }

  @ReactMethod
  fun connect(path: String) {
    val mSocket = getSocketInstance(path)

    mSocket?.connect()
  }

  @ReactMethod
  fun disconnect(path: String) {
    val mSocket = getSocketInstance(path)

    mSocket?.disconnect()
  }

  @ReactMethod
  fun emit(path: String, eventName: String, options: ReadableMap) {
    val mSocket = getSocketInstance(path)
    var data: Any? = null

    if(options.hasKey("data")) {
      when(options.getType("data")) {
        ReadableType.Map -> {
          options.getMap("data")?.let { data = JSONObject(it.toHashMap()) }
        }

        ReadableType.Array -> {
          options.getArray("data")?.let { data = JSONArray(it.toArrayList()) }
        }

        ReadableType.Number -> {
          val number: Double = options.getDouble("data")

          data = if (number == round(number)) {
            number.toInt()
          } else {
            number
          }
        }

        ReadableType.Boolean -> {
          data = options.getBoolean("data")
        }

        ReadableType.Null -> {
          data = null
        }

        ReadableType.String -> {
          options.getString("data")?.let { data = it }
        }
      }
    }

    mSocket?.emit(eventName, data)
  }

  @ReactMethod
  fun on(path: String, eventName: String, callback: Callback) {
    val mSocket = getSocketInstance(path)
    val callbackRegister = buildCallbackRegister(eventName)

    callback.invoke(eventName, callbackRegister.uniqueID)

    mSocket?.on(eventName, callbackRegister.onEventListener)
  }

  @ReactMethod
  fun once(path: String, eventName: String, callback: Callback) {
    val mSocket = getSocketInstance(path)
    val callbackRegister = buildCallbackRegister(eventName)

    callback.invoke(eventName, callbackRegister.uniqueID)

    mSocket?.once(eventName, callbackRegister.onEventListener)
  }

  @ReactMethod
  fun off(path: String, eventName: String, uniqueID: String) {
    val mSocket = getSocketInstance(path)
    val index = callbackRegisters.indexOfFirst { it.uniqueID == uniqueID }

    if(index < 0) return

    mSocket?.off(eventName, callbackRegisters[index].onEventListener)

    callbackRegisters.removeAt(index)
  }

  @ReactMethod
  fun connected(path: String, promise: Promise) {
    promise.resolve(connectedSync(path))
  }

  @ReactMethod
  fun getId(path: String, promise: Promise) {
    promise.resolve(getIdSync(path))
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun connectedSync(path: String): Boolean {
    val mSocket = getSocketInstance(path)

    return mSocket?.connected() ?: false
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun getIdSync(path: String): String? {
    val mSocket = getSocketInstance(path)

    return mSocket?.id()
  }
}
