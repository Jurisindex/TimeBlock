package com.example.timeblock.util

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.aggregate.AggregateRequest
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

interface StepCountProvider {
    suspend fun getTodaySteps(): Long
}

class GarminHealthConnectStepsProvider(private val context: Context) : StepCountProvider {
    private val client: HealthConnectClient = HealthConnectClient.getOrCreate(context)
    private val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class)
    )

    override suspend fun getTodaySteps(): Long {
        val granted = client.permissionController.getGrantedPermissions(permissions)
        if (!granted.containsAll(permissions)) {
            return 0L
        }
        val end = Instant.now()
        val start = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()
        val request = AggregateRequest(
            metrics = setOf(StepsRecord.COUNT_TOTAL),
            timeRangeFilter = TimeRangeFilter.between(start, end)
        )
        val response = client.aggregate(request)
        return response[StepsRecord.COUNT_TOTAL] ?: 0L
    }
}
