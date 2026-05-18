package com.builder.utils

import android.content.Context
import android.location.Geocoder
import android.location.Location
import java.util.*

object LocationHelper {
    fun getAddress(context: Context, loc: Location?): String {
        if (loc == null) return ""
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
            addresses?.get(0)?.getAddressLine(0) ?: ""
        } catch (e: Exception) { "" }
    }
}
