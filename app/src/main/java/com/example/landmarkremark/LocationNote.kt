package com.example.landmarkremark

/**
 * Lớp dữ liệu đại diện cho một ghi chú với tên, nội dung ghi chú,
 * và tọa độ địa lý (latitude và longitude) của vị trí.
 */
data class LocationNote(
    val name: String = "", // Tên của ghi chú
    val note: String = "", // Nội dung của ghi chú
    val latitude: Double = 0.0, // Vĩ độ của vị trí
    val longitude: Double = 0.0 // Kinh độ của vị trí
)
