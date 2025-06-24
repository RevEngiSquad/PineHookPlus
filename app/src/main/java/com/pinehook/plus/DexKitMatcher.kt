package com.pinehook.plus

import android.util.Log
import org.luckypray.dexkit.DexKitBridge

class DexKitMatcher {
    private val TAG = "DexKitMatcher"
    fun findField(
        bridge: DexKitBridge,
        modifiers: Int,
        declaredClass: String,
        type: String
    ): String {
        return bridge.findField {
            matcher {
                this.modifiers = modifiers
                this.declaredClass = declaredClass
                this.type = type
            }
        }.single().className
    }

    fun findClass(
        bridge: DexKitBridge,
        source: String,
        modifiers: Int,
        superClass: String,
        usingStrings: Array<String>,
    ): String {
        bridge.findClass {
            matcher {
                this.source = source
                this.modifiers = modifiers
                this.superClass = superClass
                this.usingStrings(*usingStrings)
            }
        }.single().let {
            Log.d(TAG, "Found class: $it")
            return it.name
        }

    }

    fun findMethods(
        bridge: DexKitBridge,
        modifiers: Int,
        returnType: String,
        paramTypes: Array<String>,
        paramCount: Int,
        usingStrings: Array<String>,
        usingNumbers: IntArray,
    ): Pair<String, String> {
        bridge.findMethod {
            matcher {
                this.modifiers = modifiers
                this.returnType = returnType
                this.paramTypes(*paramTypes)
                this.paramCount = paramCount
                this.usingStrings(*usingStrings)
                this.usingNumbers(usingNumbers.toList())
            }
        }.single().let {
            Log.d(TAG, "Found method: $it")
            return Pair(it.name, it.className)
        }
    }
}
