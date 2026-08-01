package com.walkroute.app.data.repository

import com.walkroute.app.domain.model.Route
import com.walkroute.app.domain.model.RoutePoint
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

/**
 * Алгоритм генерации пешеходных маршрутов на основе желаемого количества шагов или времени.
 * Использует простую эвристику для создания кольцевых маршрутов от точки старта.
 */
@Singleton
class RouteGenerator @Inject constructor() {
    
    // Средняя длина шага в метрах (можно настроить в настройках пользователя)
    private val averageStepLengthMeters = 0.75
    
    // Средняя скорость пешехода км/ч
    private val walkingSpeedKmh = 5.0
    
    /**
     * Генерирует маршрут на основе количества шагов
     */
    fun generateRouteBySteps(
        startPoint: RoutePoint,
        targetSteps: Int,
        difficulty: Route.Difficulty = Route.Difficulty.EASY
    ): Route {
        val totalDistanceMeters = (targetSteps * averageStepLengthMeters).toInt()
        val estimatedDurationMinutes = calculateDuration(totalDistanceMeters)
        
        // Создаем точки маршрута - простой алгоритм "спирали" или "восьмерки"
        val points = generateLoopRoute(startPoint, totalDistanceMeters, difficulty)
        
        return Route(
            name = "Маршрут на $targetSteps шагов",
            description = "Кольцевой маршрут от вашей позиции",
            points = points,
            totalDistanceMeters = totalDistanceMeters,
            estimatedSteps = targetSteps,
            estimatedDurationMinutes = estimatedDurationMinutes,
            difficulty = difficulty,
            startPoint = startPoint,
            endPoint = points.lastOrNull() ?: startPoint
        )
    }
    
    /**
     * Генерирует маршрут на основе времени прогулки
     */
    fun generateRouteByTime(
        startPoint: RoutePoint,
        durationMinutes: Int,
        difficulty: Route.Difficulty = Route.Difficulty.EASY
    ): Route {
        val totalDistanceMeters = calculateDistance(durationMinutes)
        val estimatedSteps = (totalDistanceMeters / averageStepLengthMeters).toInt()
        
        val points = generateLoopRoute(startPoint, totalDistanceMeters, difficulty)
        
        return Route(
            name = "Прогулка на $durationMinutes минут",
            description = "Кольцевой маршрут от вашей позиции",
            points = points,
            totalDistanceMeters = totalDistanceMeters,
            estimatedSteps = estimatedSteps,
            estimatedDurationMinutes = durationMinutes,
            difficulty = difficulty,
            startPoint = startPoint,
            endPoint = points.lastOrNull() ?: startPoint
        )
    }
    
    /**
     * Генерирует кольцевой маршрут от стартовой точки
     * Использует упрощенную модель - создает несколько точек по окружности с вариациями
     */
    private fun generateLoopRoute(
        startPoint: RoutePoint,
        totalDistanceMeters: Int,
        difficulty: Route.Difficulty
    ): List<RoutePoint> {
        val points = mutableListOf<RoutePoint>()
        points.add(startPoint) // Начальная точка
        
        // Определяем радиус маршрута исходя из расстояния
        // Для кольцевого маршрута: circumference = 2 * pi * r => r = C / (2*pi)
        // Но мы хотим не полный круг, а путь туда-обратно с петлями
        val radiusMeters = totalDistanceMeters / 4.0
        
        // Количество промежуточных точек зависит от сложности
        val numPoints = when (difficulty) {
            Route.Difficulty.EASY -> 6
            Route.Difficulty.MEDIUM -> 10
            Route.Difficulty.HARD -> 15
        }
        
        // Генерируем точки по спирали/восьмерке
        for (i in 1 until numPoints) {
            val progress = i.toDouble() / numPoints
            
            // Угол движения (создаем восьмерку)
            val angle = when (difficulty) {
                Route.Difficulty.EASY -> progress * 2 * PI // Простой круг
                Route.Difficulty.MEDIUM -> progress * 4 * PI // Двойная петля
                Route.Difficulty.HARD -> progress * 6 * PI // Тройная петля с вариациями
            }
            
            // Вариация радиуса для более интересного маршрута
            val radiusVariation = if (difficulty == Route.Difficulty.HARD) {
                radiusMeters * (0.8 + 0.4 * sin(progress * 3 * PI))
            } else {
                radiusMeters
            }
            
            // Вычисляем новую точку
            val newPoint = calculatePointAtDistanceAndBearing(
                startPoint = startPoint,
                distanceMeters = radiusVariation,
                bearingDegrees = Math.toDegrees(angle)
            )
            
            points.add(newPoint.copy(
                instruction = generateInstruction(i, numPoints, difficulty)
            ))
        }
        
        // Добавляем конечную точку (возврат к началу)
        points.add(startPoint.copy(instruction = "Вы прибыли к месту начала"))
        
        return points
    }
    
    /**
     * Вычисляет новую гео-точку на заданном расстоянии и направлении от исходной
     */
    private fun calculatePointAtDistanceAndBearing(
        startPoint: RoutePoint,
        distanceMeters: Double,
        bearingDegrees: Double
    ): RoutePoint {
        val earthRadius = 6371000.0 // метров
        
        val lat1Rad = Math.toRadians(startPoint.latitude)
        val lon1Rad = Math.toRadians(startPoint.longitude)
        val bearingRad = Math.toRadians(bearingDegrees)
        val angularDistance = distanceMeters / earthRadius
        
        val lat2Rad = asin(
            sin(lat1Rad) * cos(angularDistance) +
            cos(lat1Rad) * sin(angularDistance) * cos(bearingRad)
        )
        
        val lon2Rad = lon1Rad + atan2(
            sin(bearingRad) * sin(angularDistance) * cos(lat1Rad),
            cos(angularDistance) - sin(lat1Rad) * sin(lat2Rad)
        )
        
        return RoutePoint(
            latitude = Math.toDegrees(lat2Rad),
            longitude = Math.toDegrees(lon2Rad)
        )
    }
    
    /**
     * Генерирует текстовую инструкцию для точки маршрута
     */
    private fun generateInstruction(
        pointIndex: Int,
        totalPoints: Int,
        difficulty: Route.Difficulty
    ): String {
        val instructions = listOf(
            "Идите прямо",
            "Поверните налево",
            "Поверните направо",
            "Продолжайте движение",
            "Следуйте по дорожке",
            "Держитесь правой стороны",
            "Держитесь левой стороны"
        )
        
        // Выбираем инструкцию на основе индекса и направления
        val instructionIndex = when {
            pointIndex % 3 == 0 -> 1 // На左转
            pointIndex % 3 == 1 -> 2 // Направо
            else -> 0 // Прямо
        }
        
        return instructions[instructionIndex]
    }
    
    /**
     * Рассчитывает длительность прогулки в минутах на основе расстояния
     */
    private fun calculateDuration(distanceMeters: Int): Int {
        val distanceKm = distanceMeters / 1000.0
        val hours = distanceKm / walkingSpeedKmh
        return (hours * 60).toInt().coerceAtLeast(1)
    }
    
    /**
     * Рассчитывает расстояние в метрах на основе желаемой длительности
     */
    private fun calculateDistance(durationMinutes: Int): Int {
        val hours = durationMinutes / 60.0
        val distanceKm = hours * walkingSpeedKmh
        return (distanceKm * 1000).toInt()
    }
    
    /**
     * Получает следующую точку маршрута с учетом текущего положения
     */
    fun getNextWaypoint(
        currentLocation: RoutePoint,
        route: Route,
        currentIndex: Int
    ): Pair<RoutePoint?, Int> {
        if (currentIndex >= route.points.size - 1) {
            return null to currentIndex // Маршрут завершен
        }
        
        val nextPoint = route.points[currentIndex + 1]
        val distanceToNext = calculateDistanceBetween(currentLocation, nextPoint)
        
        // Если до следующей точки меньше 5 метров, переходим к следующей
        return if (distanceToNext < 5.0) {
            route.points.getOrNull(currentIndex + 2) to (currentIndex + 1)
        } else {
            nextPoint to currentIndex
        }
    }
    
    /**
     * Вычисляет расстояние между двумя точками в метрах (формула гаверсинусов)
     */
    fun calculateDistanceBetween(point1: RoutePoint, point2: RoutePoint): Double {
        val earthRadius = 6371000.0 // метров
        
        val lat1Rad = Math.toRadians(point1.latitude)
        val lat2Rad = Math.toRadians(point2.latitude)
        val deltaLat = Math.toRadians(point2.latitude - point1.latitude)
        val deltaLon = Math.toRadians(point2.longitude - point1.longitude)
        
        val a = sin(deltaLat / 2).pow(2) +
                cos(lat1Rad) * cos(lat2Rad) * sin(deltaLon / 2).pow(2)
        
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        
        return earthRadius * c
    }
    
    /**
     * Вычисляет направление (bearing) между двумя точками в градусах
     */
    fun calculateBearing(point1: RoutePoint, point2: RoutePoint): Double {
        val lat1Rad = Math.toRadians(point1.latitude)
        val lat2Rad = Math.toRadians(point2.latitude)
        val deltaLon = Math.toRadians(point2.longitude - point1.longitude)
        
        val y = sin(deltaLon) * cos(lat2Rad)
        val x = cos(lat1Rad) * sin(lat2Rad) -
                sin(lat1Rad) * cos(lat2Rad) * cos(deltaLon)
        
        val bearingRad = atan2(y, x)
        var bearingDeg = Math.toDegrees(bearingRad)
        
        // Нормализуем к диапазону [0, 360)
        bearingDeg = (bearingDeg + 360) % 360
        
        return bearingDeg
    }
}
