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
import androidx.health.platform.client.permission.Permission
import io.flutter.Log
import io.flutter.embedding.android.FlutterFragmentActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.*
import java.time.ZonedDateTime
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.random.Random
import androidx.health.connect.client.units.Energy
import androidx.health.connect.client.units.Length
import androidx.health.connect.client.units.Mass
import androidx.health.connect.client.units.Velocity


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

                if (HealthConnectClient.isProviderAvailable(applicationContext)) {
                    // Health Connect is available and installed.
                    val map = HashMap<String, Any>()

                    val healthConnectClient = HealthConnectClient.getOrCreate(applicationContext)

                    map["success"] = "success"

                    CoroutineScope(Dispatchers.Main).launch {
                        readWeight(
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


                    HealthPermission.createWritePermission(ExerciseSessionRecord::class),
                    HealthPermission.createReadPermission(ExerciseSessionRecord::class),
                    HealthPermission.createWritePermission(ExerciseEventRecord::class),
                    HealthPermission.createWritePermission(StepsRecord::class),
                    HealthPermission.createWritePermission(SpeedRecord::class),
                    HealthPermission.createWritePermission(DistanceRecord::class),
                    HealthPermission.createWritePermission(TotalCaloriesBurnedRecord::class),
                    HealthPermission.createWritePermission(HeartRateRecord::class),
                    HealthPermission.createWritePermission(SleepSessionRecord::class),
                    HealthPermission.createWritePermission(SleepStageRecord::class),
                    HealthPermission.createWritePermission(WeightRecord::class),


                    HealthPermission.createReadPermission(StepsRecord::class),
                    HealthPermission.createReadPermission(SpeedRecord::class),
                    HealthPermission.createReadPermission(DistanceRecord::class),
                    HealthPermission.createReadPermission(TotalCaloriesBurnedRecord::class),
                    HealthPermission.createReadPermission(HeartRateRecord::class),
                    HealthPermission.createReadPermission(SleepSessionRecord::class),
                    HealthPermission.createReadPermission(SleepStageRecord::class),
                    HealthPermission.createReadPermission(WeightRecord::class)

                )



                if (HealthConnectClient.isProviderAvailable(applicationContext)) {
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

            }else if (call.method == "generateExerciseSessionMethod"){
                val healthConnectClient = HealthConnectClient.getOrCreate(applicationContext)



                CoroutineScope(Dispatchers.Main).launch {
                    writeExerciseSession(healthConnectClient)
                }

                Toast.makeText(this , "Exercise Session Generated", Toast.LENGTH_SHORT).show()


            }else if (call.method == "generateSleepSessionMethod"){

                CoroutineScope(Dispatchers.Main).launch {
                    generateSleepData()
                }
                Toast.makeText(this , "Sleep Session Generated", Toast.LENGTH_SHORT).show()

            }else if (call.method == "generateWeightRecordMethod"){
                val healthConnectClient = HealthConnectClient.getOrCreate(applicationContext)
                val checkArguments = call.arguments<Map<String, Any>>()

                var weightVal= checkArguments!!["weightVal"].toString().toDouble()

                CoroutineScope(Dispatchers.Main).launch {
                    writeWeightRecord(healthConnectClient, weightVal)
                }

                Toast.makeText(this , "$weightVal kg Weight Recorded", Toast.LENGTH_SHORT).show()

            }
        }
    }


    suspend fun writeWeightRecord(healthConnectClient: HealthConnectClient, weightArgument: Double) {

        val healthConnectClient = HealthConnectClient.getOrCreate(applicationContext)
        val startTime = ZonedDateTime.now().minusSeconds(1).toInstant()

        val records = listOf(
            WeightRecord(
                weight = Mass.kilograms(weightArgument),
                time = startTime,
                zoneOffset = null,
            )
        )
        healthConnectClient.insertRecords(records)

    }

    suspend fun writeExerciseSession(healthConnectClient: HealthConnectClient) {

        val healthConnectClient = HealthConnectClient.getOrCreate(applicationContext)
        val startTime = ZonedDateTime.now().minusMinutes(30).toInstant()
        val endTime = ZonedDateTime.now().toInstant()

        val records = listOf(
            ExerciseSessionRecord(
                startTime = startTime,
                startZoneOffset = null,
                endTime = endTime,
                endZoneOffset = null,
                exerciseType = ExerciseSessionRecord.EXERCISE_TYPE_RUNNING,
                title = "My Run #${Random.nextInt(0, 60)}"
            ),
            StepsRecord(
                startTime = startTime,
                startZoneOffset = null,
                endTime = endTime,
                endZoneOffset = null,
                count = (1000 + 1000 * Random.nextInt(3)).toLong()
            ),
            // Mark a 5 minute pause during the workout
            ExerciseEventRecord(
                startTime = startTime.plus(10, ChronoUnit.MINUTES),
                startZoneOffset = null,
                endTime = endTime.plus(15, ChronoUnit.MINUTES),
                endZoneOffset = null,
                eventType = ExerciseEventRecord.EVENT_TYPE_PAUSE
            ),
            DistanceRecord(
                startTime = startTime,
                startZoneOffset = null,
                endTime = endTime,
                endZoneOffset = null,
                distance = Length.meters((1000 + 100 * Random.nextInt(20)).toDouble())
            ),
            TotalCaloriesBurnedRecord(
                startTime = startTime,
                startZoneOffset = null,
                endTime = endTime,
                endZoneOffset = null,
                energy = Energy.kilocalories((140 + Random.nextInt(20)) + 0.01)
            )
        ) + buildHeartRateSeries() + buildSpeedSeries()

        healthConnectClient.insertRecords(records)
        //Toast.makeText(this , "Successfully insert records", Toast.LENGTH_SHORT).show()

    }

    private fun buildHeartRateSeries(): HeartRateRecord {
        val startTime = ZonedDateTime.now().minusMinutes(30).toInstant()
        val endTime = ZonedDateTime.now().toInstant()
        val samples = mutableListOf<HeartRateRecord.Sample>()
        var time = startTime
        while (time.isBefore(endTime)) {
            samples.add(
                HeartRateRecord.Sample(
                    time = time,
                    beatsPerMinute = (80 + Random.nextInt(80)).toLong()
                )
            )
            time = time.plusSeconds(30)
        }
        return HeartRateRecord(
            startTime = startTime,
            startZoneOffset = null,
            endTime = endTime,
            endZoneOffset = null,
            samples = samples
        )
    }

    private fun buildSpeedSeries() = SpeedRecord(
        startTime = ZonedDateTime.now().minusMinutes(30).toInstant(),
        startZoneOffset = null,
        endTime = ZonedDateTime.now().toInstant(),
        endZoneOffset = null,
        samples = listOf(
            SpeedRecord.Sample(
                time = ZonedDateTime.now().minusMinutes(30).toInstant(),
                speed = Velocity.metersPerSecond(2.5)
            ),
            SpeedRecord.Sample(
                time = ZonedDateTime.now().minusMinutes(30).plus(5, ChronoUnit.MINUTES).toInstant(),
                speed = Velocity.metersPerSecond(2.7)
            ),
            SpeedRecord.Sample(
                time = ZonedDateTime.now().minusMinutes(30).plus(10, ChronoUnit.MINUTES).toInstant(),
                speed = Velocity.metersPerSecond(2.9)
            )
        )
    )


    suspend fun generateSleepData() {
        val healthConnectClient = HealthConnectClient.getOrCreate(applicationContext)
        val records = mutableListOf<Record>()
        // Make yesterday the last day of the sleep data
        val lastDay = ZonedDateTime.now().minusDays(1).truncatedTo(ChronoUnit.DAYS)
        val notes = this.resources.getStringArray(R.array.sleep_notes_array)
        // Create 7 days-worth of sleep data
        for (i in 0..7) {
            val wakeUp = lastDay.minusDays(i.toLong())
                .withHour(Random.nextInt(7, 10))
                .withMinute(Random.nextInt(0, 60))
            val bedtime = wakeUp.minusDays(1)
                .withHour(Random.nextInt(19, 22))
                .withMinute(Random.nextInt(0, 60))
            val sleepSession = SleepSessionRecord(
                notes = notes[Random.nextInt(0, notes.size)],
                startTime = bedtime.toInstant(),
                startZoneOffset = bedtime.offset,
                endTime = wakeUp.toInstant(),
                endZoneOffset = wakeUp.offset
            )
            val sleepStages = generateSleepStages(bedtime, wakeUp)
            records.add(sleepSession)
            records.addAll(sleepStages)
        }
        healthConnectClient.insertRecords(records)
    }

    private fun generateSleepStages(
        start: ZonedDateTime,
        end: ZonedDateTime
    ): List<SleepStageRecord> {
        val sleepStages = mutableListOf<SleepStageRecord>()
        var stageStart = start
        while (stageStart < end) {
            val stageEnd = stageStart.plusMinutes(Random.nextLong(30, 120))
            val checkedEnd = if (stageEnd > end) end else stageEnd
            sleepStages.add(
                SleepStageRecord(
                    stage = Random.nextInt(0, 6),
                    startTime = stageStart.toInstant(),
                    startZoneOffset = stageStart.offset,
                    endTime = checkedEnd.toInstant(),
                    endZoneOffset = checkedEnd.offset
                )
            )
            stageStart = checkedEnd
        }
        return sleepStages
    }

    suspend fun readWeight(
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

            map.put("weightKg", weightRecord.weight.inKilograms.toString())
            map.put("type", "WEIGHT")
            map.put("time", weightRecord.time.toString())
            map.put("zoneOffset", weightRecord.zoneOffset.toString())

            responseList.add(map)

        }
        _callBackChannel?.invokeMethod("weightRecord", responseList)
    }

}
