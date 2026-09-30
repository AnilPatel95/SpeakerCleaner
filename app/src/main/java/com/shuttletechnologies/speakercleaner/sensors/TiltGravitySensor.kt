package com.shuttletechnologies.speakercleaner.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.atan2
import kotlin.math.sqrt

data class TiltState(
    val pitchDeg: Float = 0f,
    val rollDeg: Float = 0f,
    val isScreenDown: Boolean = false,
    val isOptimalAngle: Boolean = false,
    val alignmentProgress: Float = 0f
)

class TiltGravitySensor(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_GRAVITY)
        ?: sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _tiltState = MutableStateFlow(TiltState())
    val tiltState: StateFlow<TiltState> = _tiltState.asStateFlow()

    private var isListening = false

    fun startListening() {
        if (isListening || accelerometer == null) return
        sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        isListening = true
    }

    fun stopListening() {
        if (!isListening) return
        sensorManager?.unregisterListener(this)
        isListening = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.values.size < 3) return

        val ax = event.values[0]
        val ay = event.values[1]
        val az = event.values[2]

        // Pitch & Roll calculation
        val pitch = (atan2(ay.toDouble(), sqrt((ax * ax + az * az).toDouble())) * 180.0 / Math.PI).toFloat()
        val roll = (atan2(-ax.toDouble(), az.toDouble()) * 180.0 / Math.PI).toFloat()

        // az < -2.0 means phone face/screen is pointing downward towards the floor
        val isScreenDown = az < -2.0f

        // Optimal angle: Phone held screen-down at a 35° to 70° decline so water flows out
        // of speaker grille ports naturally by gravity
        val isOptimalAngle = isScreenDown && (pitch in 25f..75f || pitch in -75f..-25f)

        // Alignment progress 0.0 to 1.0
        val alignment = when {
            isOptimalAngle -> 1.0f
            isScreenDown -> 0.65f
            az < 0f -> 0.35f
            else -> 0.1f
        }

        _tiltState.value = TiltState(
            pitchDeg = pitch,
            rollDeg = roll,
            isScreenDown = isScreenDown,
            isOptimalAngle = isOptimalAngle,
            alignmentProgress = alignment
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}
