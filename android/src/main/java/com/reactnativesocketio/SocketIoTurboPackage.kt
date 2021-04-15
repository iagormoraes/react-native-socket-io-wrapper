package com.reactnativesocketio

import com.facebook.react.TurboReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.model.ReactModuleInfo
import com.facebook.react.module.model.ReactModuleInfoProvider
import java.util.*

class SocketIoTurboPackage: TurboReactPackage() {
  override fun getModule(name: String?, reactContext: ReactApplicationContext): NativeModule {
    return when(name) {
      SocketIoModule.NAME -> SocketIoModule(reactContext)
      else -> throw IllegalArgumentException("Could not find module $name")
    }
  }

  override fun getReactModuleInfoProvider(): ReactModuleInfoProvider? {
    return ReactModuleInfoProvider {
      val map = HashMap<String, ReactModuleInfo>()

      map[SocketIoModule.NAME] = ReactModuleInfo(SocketIoModule.NAME, "com.reactnativesocketio.SocketIoModule", false, false, true, false, true)

      map
    }
  }
}
