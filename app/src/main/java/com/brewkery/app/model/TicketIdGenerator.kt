package com.brewkery.app.model

import kotlin.random.Random

object TicketIdGenerator {

    private const val TICKET_PREFIX = "#BK-"
    private const val MAX_TICKET_NUMBER = 99999
    private const val TICKET_NUMBER_PADDING = 5

    fun generateTicketId(): String {
        val digits = Random.nextInt(0, MAX_TICKET_NUMBER).toString().padStart(TICKET_NUMBER_PADDING, '0')
        return "$TICKET_PREFIX$digits"
    }

    // Alias for existing callers
    fun generate(): String = generateTicketId()
}
