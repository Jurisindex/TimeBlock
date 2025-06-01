package com.example.timeblock.garmin

import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.LocalDate

/**
 * Simple client used for fetching step data from the Garmin API. This class
 * only exposes the pieces that the repository currently needs. OAuth
 * negotiation and actual endpoint details are left as TODOs so that this file
 * can be expanded later without affecting the rest of the code base.
 */
class GarminClient {

    private val http = OkHttpClient()

    /**
     * Retrieve the list of devices connected to the user's Garmin account.
     */
    suspend fun fetchDevices(): List<String> {
        // TODO: Query Garmin API for device list. Placeholder implementation
        // returns a single generic device.
        return listOf("Connected Garmin")
    }

    /**
     * Fetch daily step counts from the Garmin API for the authenticated user.
     *
     * @return map of [LocalDate] to step count for that day.
     */
    suspend fun fetchSteps(): Map<LocalDate, Int> {
        // TODO: Perform OAuth and real API calls.
        // This placeholder simply returns an empty map so that the rest of the
        // application can compile and tests can run without network access.
        return emptyMap()
    }
}
