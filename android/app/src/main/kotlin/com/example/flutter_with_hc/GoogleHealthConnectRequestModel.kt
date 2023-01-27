package com.example.flutter_with_hc

data class GoogleHealthConnectRequestModel(
    val map: Map<String, Any>
) {
    val startTime: String by map
    val endTime: String by map
}
