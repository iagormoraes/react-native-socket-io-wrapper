package com.reactnativesocketio

import com.facebook.react.bridge.WritableMap

interface SocketCallback {
  fun onReceive(eventName: String, props: WritableMap)
}
