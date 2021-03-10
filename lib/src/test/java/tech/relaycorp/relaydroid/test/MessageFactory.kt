package tech.relaycorp.relaydroid.test

import java.util.UUID
import tech.relaycorp.relaydroid.messaging.IncomingMessage
import tech.relaycorp.relaydroid.messaging.OutgoingMessage
import tech.relaycorp.relaydroid.messaging.ParcelId
import tech.relaycorp.relaynet.messages.payloads.ServiceMessage
import tech.relaycorp.relaynet.ramf.RecipientAddressType

internal object MessageFactory {
    val serviceMessage = ServiceMessage("application/foo", "the content".toByteArray())

    suspend fun buildOutgoing(recipientType: RecipientAddressType) = OutgoingMessage.build(
        serviceMessage.type,
        serviceMessage.content,
        senderEndpoint = FirstPartyEndpointFactory.build(),
        recipientEndpoint = ThirdPartyEndpointFactory.build(recipientType)
    )

    fun buildIncoming() = IncomingMessage(
        parcelId = ParcelId(UUID.randomUUID().toString()),
        type = serviceMessage.type,
        content = serviceMessage.content,
        senderEndpoint = ThirdPartyEndpointFactory.buildPublic(),
        recipientEndpoint = FirstPartyEndpointFactory.build()
    ) {}
}
