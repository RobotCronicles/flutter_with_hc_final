package com.example.flutter_with_hc

import android.os.Bundle
import android.os.Build
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.*
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.BloodGlucose
import androidx.health.platform.client.permission.Permission
import io.flutter.Log
import io.flutter.embedding.android.FlutterFragmentActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.*

import androidx.health.connect.client.records.StepsRecord
//import androidx.lifecycle.lifecycleScope
import java.time.ZonedDateTime
import java.time.Instant


class MainActivity : FlutterFragmentActivity() {
    private val CHANNEL = "google-health-connect"
    private var _callBackChannel: MethodChannel? = null


    @RequiresApi(Build.VERSION_CODES.O)
    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        _callBackChannel =
            MethodChannel(flutterEngine.dartExecutor, "google-health-connect")
        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            CHANNEL
        ).setMethodCallHandler { call, result ->

            if (call.method == "getGoogleHealthConnectData") {

                val googleHealthConnectRequestModel =
                    GoogleHealthConnectRequestModel(call.arguments as Map<String, Any>)

                Log.i("Status", "Beginning")

                if (HealthConnectClient.isAvailable(applicationContext)) {
                    // Health Connect is available and installed.
                    val map = HashMap<String, Any>()

                    val healthConnectClient = HealthConnectClient.getOrCreate(applicationContext)

                    map["success"] = "success"


                    CoroutineScope(Dispatchers.Main).launch {
                        readStepsByTimeRange(
                            healthConnectClient,
                            Instant.parse(googleHealthConnectRequestModel.startTime),
                            Instant.parse(googleHealthConnectRequestModel.endTime)
                        )
                        readBloodPressureByTimeRange(
                            healthConnectClient,
                            Instant.parse(googleHealthConnectRequestModel.startTime),
                            Instant.parse(googleHealthConnectRequestModel.endTime)
                        )
                        readOxygenSaturationByTimeRange(
                            healthConnectClient,
                            Instant.parse(googleHealthConnectRequestModel.startTime),
                            Instant.parse(googleHealthConnectRequestModel.endTime)
                        )
                        readSleepSessionByTimeRange(
                            healthConnectClient,
                            Instant.parse(googleHealthConnectRequestModel.startTime),
                            Instant.parse(googleHealthConnectRequestModel.endTime)
                        )
                        readSleepStageByTimeRange(
                            healthConnectClient,
                            Instant.parse(googleHealthConnectRequestModel.startTime),
                            Instant.parse(googleHealthConnectRequestModel.endTime)
                        )
                        readHeartRateSeriesByTimeRange(
                            healthConnectClient,
                            Instant.parse(googleHealthConnectRequestModel.startTime),
                            Instant.parse(googleHealthConnectRequestModel.endTime)
                        )
                        readBloodGlucoseByTimeRange(
                            healthConnectClient,
                            Instant.parse(googleHealthConnectRequestModel.startTime),
                            Instant.parse(googleHealthConnectRequestModel.endTime)
                        )
                        readTotalCaloriesBurnedByTimeRange(
                            healthConnectClient,
                            Instant.parse(googleHealthConnectRequestModel.startTime),
                            Instant.parse(googleHealthConnectRequestModel.endTime)
                        )
                        readWeightByTimeRange(
                            healthConnectClient,
                            Instant.parse(googleHealthConnectRequestModel.startTime),
                            Instant.parse(googleHealthConnectRequestModel.endTime)
                        )

                    }
                    result.success(map)

                } else {
                    val map = HashMap<String, Any>()
                    map["success"] = "fail"
                    result.success(map)
                }
            } else if (call.method == "hasGoogleHealthConnectPermission") {
                val PERMISSIONS = setOf(
                    HealthPermission.createReadPermission(BloodGlucoseRecord::class),
                    HealthPermission.createWritePermission(BloodGlucoseRecord::class),
                    HealthPermission.createReadPermission(BloodPressureRecord::class),
                    HealthPermission.createWritePermission(BloodPressureRecord::class),
                    HealthPermission.createReadPermission(HeartRateRecord::class),
                    HealthPermission.createWritePermission(HeartRateRecord::class),
                    HealthPermission.createReadPermission(OxygenSaturationRecord::class),
                    HealthPermission.createWritePermission(OxygenSaturationRecord::class),
                    HealthPermission.createReadPermission(SleepSessionRecord::class),
                    HealthPermission.createWritePermission(SleepSessionRecord::class),
                    HealthPermission.createWritePermission(SleepStageRecord::class),
                    HealthPermission.createWritePermission(SleepStageRecord::class),
                    HealthPermission.createReadPermission(StepsRecord::class),
                    HealthPermission.createWritePermission(StepsRecord::class),
                    HealthPermission.createReadPermission(TotalCaloriesBurnedRecord::class),
                    HealthPermission.createWritePermission(TotalCaloriesBurnedRecord::class),
                    HealthPermission.createReadPermission(WeightRecord::class),
                    HealthPermission.createWritePermission(WeightRecord::class),
                )



                if (HealthConnectClient.isAvailable(applicationContext)) {
                    val healthConnectClient = HealthConnectClient.getOrCreate(applicationContext)

                    suspend fun hasAllPermissions(permissions: Set<HealthPermission>): Boolean {
                        return permissions == healthConnectClient.permissionController.getGrantedPermissions(
                            permissions
                        )
                    }

                    CoroutineScope(Dispatchers.Main).launch {
                        if (hasAllPermissions(permissions = PERMISSIONS)) {
                            // Method Channel to Flutter application(com.example.flutter_with_hc)
                            result.success(true)
                        } else {
//                            We want to use the exist function like this link (https://developer.android.com/codelabs/health-connect#2)
//                            But it doesn't work
//                            PermissionController.createRequestPermissionResultContract()

                            result.success(false)
                        }
                    }
                }

            }else if (call.method == "writeStepDataMethod"){
                val healthConnectClient = HealthConnectClient.getOrCreate(applicationContext)
                val checkArguments = call.arguments<Map<String, Any>>()

                var stepVal= checkArguments!!["stepVal"]

                if(stepVal == "10") {
                    Toast.makeText(this , "Correct", Toast.LENGTH_SHORT).show()
                }else {
                    Toast.makeText(this , "Wrong", Toast.LENGTH_SHORT).show()
                }

                //Plan is to get data from Flutter to Kotlin and replace the static value off 100 for steps
                CoroutineScope(Dispatchers.Main).launch {
                    insertData(healthConnectClient, 100)
                }
            }
        }
    }

    suspend fun insertData(healthConnectClient: HealthConnectClient, steps: Long) {

        val healthConnectClient = HealthConnectClient.getOrCreate(applicationContext)
        val startTime = ZonedDateTime.now().minusSeconds(1).toInstant()
        val endTime = ZonedDateTime.now().toInstant()

        val records = listOf(
            StepsRecord(
                count = steps,
                startTime = startTime,
                endTime = endTime,
                startZoneOffset = null,
                endZoneOffset = null,
            )
        )
        healthConnectClient.insertRecords(records)
        //Toast.makeText(this , "Successfully insert records", Toast.LENGTH_SHORT).show()

    }

    suspend fun readBloodGlucoseByTimeRange(
        healthConnectClient: HealthConnectClient,
        startTime: Instant,
        endTime: Instant
    ) {

        val response =
            healthConnectClient.readRecords(
                ReadRecordsRequest(
                    BloodGlucoseRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )

        val responseList = mutableListOf<HashMap<String, Any>>()

        for (bloodGlucoseRecord in response.records) {

            val map = HashMap<String, Any>()

//            map.put("levelMillimolesPerLiter", bloodGlucoseRecord.levelMillimolesPerLiter.toString())
            map.put(
                "levelMillimolesPerLiter",
                bloodGlucoseRecord.level.inMillimolesPerLiter.toString()
            )
            map.put("type", "BLOOD_GLUCOSE")
            map.put("mealType", bloodGlucoseRecord.mealType.toString())
            map.put("relationToMeal", bloodGlucoseRecord.relationToMeal.toString())
            map.put("specimenSource", bloodGlucoseRecord.specimenSource.toString())
            map.put("time", bloodGlucoseRecord.time.toString())
            map.put("zoneOffset", bloodGlucoseRecord.zoneOffset.toString())
            responseList.add(map)
        }

        _callBackChannel?.invokeMethod("bloodGlucoseRecord", responseList)
    }

    suspend fun readOxygenSaturationByTimeRange(
        healthConnectClient: HealthConnectClient,
        startTime: Instant,
        endTime: Instant
    ) {

        val response =
            healthConnectClient.readRecords(
                ReadRecordsRequest(
                    OxygenSaturationRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )

        val responseList = mutableListOf<HashMap<String, Any>>()

        for (oxygenSaturationRecord in response.records) {
            val map = HashMap<String, Any>()
            map.put("percentage", oxygenSaturationRecord.percentage.value.toString())
            map.put("type", "BLOOD_OXYGEN")
            map.put("time", oxygenSaturationRecord.time.toString())
            map.put("zoneOffset", oxygenSaturationRecord.zoneOffset.toString())
            responseList.add(map)
        }
        _callBackChannel?.invokeMethod("oxygenSaturationRecord", responseList)
    }

    suspend fun readBloodPressureByTimeRange(
        healthConnectClient: HealthConnectClient,
        startTime: Instant,
        endTime: Instant
    ) {

        val response =
            healthConnectClient.readRecords(
                ReadRecordsRequest(
                    BloodPressureRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )

        val responseList = mutableListOf<HashMap<String, Any>>()

        for (bloodPressureRecord in response.records) {

            val map = HashMap<String, Any>()

            map.put("bodyPosition", bloodPressureRecord.bodyPosition.toString())
            map.put("type", "BLOOD_PRESSURE")
//            map.put("diastolicMillimetersOfMercury", bloodPressureRecord.diastolicMillimetersOfMercury.toString())
            map.put(
                "diastolicMillimetersOfMercury",
                bloodPressureRecord.diastolic.inMillimetersOfMercury.toString()
            )
            map.put("measurementLocation", bloodPressureRecord.measurementLocation.toString())
//            map.put("systolicMillimetersOfMercury",bloodPressureRecord.systolicMillimetersOfMercury.toString())
            map.put(
                "systolicMillimetersOfMercury",
                bloodPressureRecord.systolic.inMillimetersOfMercury.toString()
            )
            map.put("time", bloodPressureRecord.time.toString())
            map.put("zoneOffset", bloodPressureRecord.zoneOffset.toString())
            responseList.add(map)
        }
        _callBackChannel?.invokeMethod("bloodPressureRecord", responseList)
    }

    suspend fun readHeartRateSeriesByTimeRange(
        healthConnectClient: HealthConnectClient,
        startTime: Instant,
        endTime: Instant
    ) {

        val response =
            healthConnectClient.readRecords(
                ReadRecordsRequest(
                    HeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )

        val responseList = mutableListOf<HashMap<String, Any>>()

        for (heartSeriesRecord in response.records) {
            val map = HashMap<String, Any>()
            // Process each step record
//            Log.i(
//                "heartRecord",
//                "samples: ${HeartRateSeriesRecord.samples}, startTime: ${HeartRateSeriesRecord.startTime.toString()}, endTime: ${HeartRateSeriesRecord.endTime.toString()}"
//            )

            val sampleMapList = mutableListOf<HashMap<String, Any>>()
            for (sample in heartSeriesRecord.samples) {
                val sampleMap = HashMap<String, Any>()
                sampleMap.put("beatsPerMinute", sample.beatsPerMinute.toString())
                sampleMap.put("time", sample.time.toString())
                sampleMapList.add(sampleMap)
            }
            map.put("samples", sampleMapList)
            map.put("type", "HEART_RATE")
            map.put("startTime", heartSeriesRecord.startTime.toString())
            map.put("endTime", heartSeriesRecord.endTime.toString())
            map.put("startZoneOffset", heartSeriesRecord.startZoneOffset.toString())
            map.put("endZoneOffset", heartSeriesRecord.endZoneOffset.toString())
            responseList.add(map)
        }
        _callBackChannel?.invokeMethod("heartSeriesRecord", responseList)
    }

    suspend fun readSleepSessionByTimeRange(
        healthConnectClient: HealthConnectClient,
        startTime: Instant,
        endTime: Instant
    ) {

        val response =
            healthConnectClient.readRecords(
                ReadRecordsRequest(
                    SleepSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )

        val responseList = mutableListOf<HashMap<String, Any>>()

        for (sleepSessionRecord in response.records) {
            val map = HashMap<String, Any>()
            map.put("title", sleepSessionRecord.title.toString())
            map.put("type", "SLEEP_ASLEEP")
            map.put("notes", sleepSessionRecord.notes.toString())
            map.put("startTime", sleepSessionRecord.startTime.toString())
            map.put("endTime", sleepSessionRecord.endTime.toString())
            map.put("startZoneOffset", sleepSessionRecord.startZoneOffset.toString())
            map.put("endZoneOffset", sleepSessionRecord.endZoneOffset.toString())
            responseList.add(map)
        }
        _callBackChannel?.invokeMethod("sleepSessionRecord", responseList)
    }

    suspend fun readSleepStageByTimeRange(
        healthConnectClient: HealthConnectClient,
        startTime: Instant,
        endTime: Instant
    ) {

        val response =
            healthConnectClient.readRecords(
                ReadRecordsRequest(
                    SleepStageRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )

        val responseList = mutableListOf<HashMap<String, Any>>()

        for (sleepStageRecord in response.records) {
            val map = HashMap<String, Any>()
            map.put("stage", sleepStageRecord.stage.toString())
            map.put("type", "sleepStageRecord")
            map.put("startTime", sleepStageRecord.startTime.toString())
            map.put("endTime", sleepStageRecord.endTime.toString())
            map.put("startZoneOffset", sleepStageRecord.startZoneOffset.toString())
            map.put("endZoneOffset", sleepStageRecord.endZoneOffset.toString())
            responseList.add(map)
        }
        _callBackChannel?.invokeMethod("sleepStageRecord", responseList)
    }


    suspend fun readStepsByTimeRange(
        healthConnectClient: HealthConnectClient,
        startTime: Instant,
        endTime: Instant

    ) {

        val response =
            healthConnectClient.readRecords(
                ReadRecordsRequest(
                    StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )

        val responseList = mutableListOf<HashMap<String, Any>>()

        for (stepsRecord in response.records) {
            val map = HashMap<String, Any>()
            // Process each step record
//            Log.i(
//                "stepRecord",
//                "count: ${stepRecord.count}, startTime: ${stepRecord.startTime.toString()}, endTime: ${stepRecord.endTime.toString()}"
//            )

            map.put("count", stepsRecord.count.toString())
            map.put("type", "STEPS")
            map.put("startTime", stepsRecord.startTime.toString())
            map.put("endTime", stepsRecord.endTime.toString())
            map.put("startZoneOffset", stepsRecord.startZoneOffset.toString())
            map.put("endZoneOffset", stepsRecord.endZoneOffset.toString())

            responseList.add(map)

        }
        _callBackChannel?.invokeMethod("stepsRecord", responseList)
    }

    suspend fun readTotalCaloriesBurnedByTimeRange(
        healthConnectClient: HealthConnectClient,
        startTime: Instant,
        endTime: Instant

    ) {

        val response =
            healthConnectClient.readRecords(
                ReadRecordsRequest(
                    TotalCaloriesBurnedRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )

        val responseList = mutableListOf<HashMap<String, Any>>()

        for (totalCaloriesBurnedRecord in response.records) {
            val map = HashMap<String, Any>()
            // Process each step record
//            Log.i(
//                "stepRecord",
//                "count: ${stepRecord.count}, startTime: ${stepRecord.startTime.toString()}, endTime: ${stepRecord.endTime.toString()}"
//            )

//            map.put("energyKcal",totalCaloriesBurnedRecord.energyKcal.toString())
            map.put("energyKcal", totalCaloriesBurnedRecord.energy.inKilocalories.toString())
            map.put("type", "ACTIVE_ENERGY_BURNED")
            map.put("startTime", totalCaloriesBurnedRecord.startTime.toString())
            map.put("endTime", totalCaloriesBurnedRecord.endTime.toString())
            map.put("startZoneOffset", totalCaloriesBurnedRecord.startZoneOffset.toString())
            map.put("endZoneOffset", totalCaloriesBurnedRecord.endZoneOffset.toString())

            responseList.add(map)

        }
        _callBackChannel?.invokeMethod("totalCaloriesBurnedRecord", responseList)
    }

    suspend fun readWeightByTimeRange(
        healthConnectClient: HealthConnectClient,
        startTime: Instant,
        endTime: Instant

    ) {

        val response =
            healthConnectClient.readRecords(
                ReadRecordsRequest(
                    WeightRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )

        val responseList = mutableListOf<HashMap<String, Any>>()

        for (weightRecord in response.records) {
            val map = HashMap<String, Any>()
            // Process each step record
//            Log.i(
//                "stepRecord",
//                "count: ${stepRecord.count}, startTime: ${stepRecord.startTime.toString()}, endTime: ${stepRecord.endTime.toString()}"
//            )

            map.put("weightKg", weightRecord.weight.inKilograms.toString())
            map.put("type", "WEIGHT")
            map.put("time", weightRecord.time.toString())
            map.put("zoneOffset", weightRecord.zoneOffset.toString())

            responseList.add(map)

        }
        _callBackChannel?.invokeMethod("weightRecord", responseList)
    }
}
