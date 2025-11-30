package com.example.dailydoodle.data.model

data class ModerationReport(
    val id: String = "",
    val panelId: String = "",
    val chainId: String = "",
    val reporterId: String = "",
    val reason: String = "",
    val status: ReportStatus = ReportStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
)

enum class ReportStatus {
    PENDING,
    REVIEWED,
    RESOLVED,
    DISMISSED
}
