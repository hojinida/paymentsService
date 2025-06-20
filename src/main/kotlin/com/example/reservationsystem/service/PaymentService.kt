package com.example.reservationsystem.service

import com.example.reservationsystem.dto.request.CancelWebhookRequest
import com.example.reservationsystem.dto.request.PaymentWebhookRequest
import com.example.reservationsystem.dto.request.WebhookRequest
import com.example.reservationsystem.repository.RedisReservationRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service

@Service
class PaymentService(
    private val orderService: OrderService,
    private val signatureService: SignatureService,
    private val redisReservationRepository: RedisReservationRepository,
    private val objectMapper: ObjectMapper
) {
    fun processPaymentSuccess(request: PaymentWebhookRequest, signature: String) {
        verifySignature(request, signature)
        val order = orderService.findByOrderUidOrThrow(request.orderUid)
        redisReservationRepository.persistReservation(order.seatNumber)
        orderService.complete(request.orderUid)
    }

    fun processPaymentFailure(request: PaymentWebhookRequest, signature: String) {
        verifySignature(request, signature)
        val order = orderService.findByOrderUidOrThrow(request.orderUid)
        redisReservationRepository.cancelReservation(order.seatNumber, order.userId)
        orderService.fail(request.orderUid)
    }

    fun successfulCancel(request: CancelWebhookRequest, signature: String) {
        verifySignature(request, signature)
        val order = orderService.findByOrderUidOrThrow(request.orderUid)
        redisReservationRepository.cancelReservation(order.seatNumber, order.userId)
        orderService.cancel(request.orderUid)
    }

    fun failedCancel(request: CancelWebhookRequest, signature: String) {
        verifySignature(request, signature)
    }

    private fun verifySignature(request: WebhookRequest, signature: String) {
        val dataToVerify = objectMapper.writeValueAsString(request)
        if (!signatureService.verify(dataToVerify, signature)) {
            throw SecurityException("서명 검증 실패")
        }
    }
}
