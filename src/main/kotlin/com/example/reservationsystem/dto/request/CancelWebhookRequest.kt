package com.example.reservationsystem.dto.request

data class CancelWebhookRequest(
    override val orderUid: String,
    override val amount: Long,
    val status: String,
) : WebhookRequest
