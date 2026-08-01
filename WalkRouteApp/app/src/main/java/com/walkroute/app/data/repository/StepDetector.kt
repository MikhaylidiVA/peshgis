package com.walkroute.app.data.repository

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

interface StepDetector {
    fun getStepCount(): Flow<Int>
    fun getStepFlow(): Flow<Unit> // Поток событий шагов для навигации
}

@Singleton
class AndroidStepDetector @Inject constructor(
    private val sensorManager: SensorManager
) : StepDetector {
    
    override fun getStepCount(): Flow<Int> = callbackFlow {
        val stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        
        if (stepCounterSensor == null) {
            trySend(0)
            close()
            return@callbackFlow
        }
        
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    val steps = it.values[0].toInt()
                    trySend(steps)
                }
            }
            
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        
        sensorManager.registerListener(
            listener,
            stepCounterSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
        
        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
    
    override fun getStepFlow(): Flow<Unit> = callbackFlow {
        // Используем STEP_DETECTOR для мгновенных событий о каждом шаге
        val stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        
        if (stepDetectorSensor == null) {
            // Если датчик шагов не доступен, пробуем использовать акселерометр
            val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            
            if (accelerometerSensor == null) {
                close()
                return@callbackFlow
            }
            
            // Простой алгоритм обнаружения шагов через акселерометр
            var lastStepTime = 0L
            val stepThreshold = 2.0f // Порог для обнаружения шага
            
            val listener = object : SensorEventListener {
                private var lastX = 0f
                private var lastY = 0f
                private var lastZ = 0f
                
                override fun onSensorChanged(event: SensorEvent?) {
                    event?.let {
                        if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                            val x = it.values[0]
                            val y = it.values[1]
                            val z = it.values[2]
                            
                            // Вычисляем ускорение
                            val acceleration = kotlin.math.sqrt(
                                (x - lastX) * (x - lastX) +
                                (y - lastY) * (y - lastY) +
                                (z - lastZ) * (z - lastZ)
                            )
                            
                            // Если ускорение выше порога и прошло достаточно времени с последнего шага
                            val currentTime = System.currentTimeMillis()
                            if (acceleration > stepThreshold && currentTime - lastStepTime > 300) {
                                lastStepTime = currentTime
                                trySend(Unit) // Отправляем событие шага
                            }
                            
                            lastX = x
                            lastY = y
                            lastZ = z
                        }
                    }
                }
                
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }
            
            sensorManager.registerListener(
                listener,
                accelerometerSensor,
                SensorManager.SENSOR_DELAY_GAME // Более высокая частота для акселерометра
            )
            
            awaitClose {
                sensorManager.unregisterListener(listener)
            }
            return@callbackFlow
        }
        
        // Если TYPE_STEP_DETECTOR доступен
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    if (it.values[0] == 1f) { // STEP_DETECTOR отправляет 1.0 при каждом шаге
                        trySend(Unit)
                    }
                }
            }
            
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        
        sensorManager.registerListener(
            listener,
            stepDetectorSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
        
        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
}
