package tech.relaycorp.relaydroid

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import tech.relaycorp.relaydroid.storage.mockStorage
import tech.relaycorp.relaydroid.test.FirstPartyEndpointFactory
import tech.relaycorp.relaynet.messages.control.PrivateNodeRegistration
import tech.relaycorp.relaynet.testing.pki.KeyPairSet
import tech.relaycorp.relaynet.testing.pki.PDACertPath
import tech.relaycorp.relaynet.wrappers.privateAddress
import java.security.KeyPair
import java.util.UUID

internal class FirstPartyEndpointTest {

    private val gateway = mock<GatewayClientImpl>()
    private val storage = mockStorage()

    @Before
    fun setUp() {
        runBlockingTest {
            Relaynet.storage = storage
            Relaynet.gatewayClientImpl = gateway
        }
    }

    @Test
    fun address() {
        val endpoint = FirstPartyEndpointFactory.build()
        assertEquals(endpoint.keyPair.public.privateAddress, endpoint.thirdPartyAddress)
    }

    @Test
    fun publicKey() {
        val endpoint = FirstPartyEndpointFactory.build()
        assertEquals(endpoint.keyPair.public, endpoint.publicKey)
    }

    @Test
    fun register() = runBlockingTest {
        whenever(gateway.registerEndpoint(any())).thenReturn(PrivateNodeRegistration(
            PDACertPath.PRIVATE_ENDPOINT,
            PDACertPath.PRIVATE_GW
        ))

        val endpoint = FirstPartyEndpoint.register()

        val keyPairCaptor = argumentCaptor<KeyPair>()
        verify(gateway)
            .registerEndpoint(keyPairCaptor.capture())
        verify(storage.identityKeyPair)
            .set(eq(endpoint.thirdPartyAddress), eq(keyPairCaptor.firstValue))
        verify(storage.identityCertificate)
            .set(eq(endpoint.thirdPartyAddress), eq(PDACertPath.PRIVATE_ENDPOINT))
        verify(storage.gatewayCertificate)
            .set(eq(PDACertPath.PRIVATE_GW))
    }

    @Test(expected = RegistrationFailedException::class)
    fun register_failed() = runBlockingTest {
        whenever(gateway.registerEndpoint(any())).thenThrow(RegistrationFailedException(""))

        FirstPartyEndpoint.register()

        verifyZeroInteractions(storage)
    }

    @Test(expected = GatewayProtocolException::class)
    fun register_failedDueToProtocol() = runBlockingTest {
        whenever(gateway.registerEndpoint(any())).thenThrow(GatewayProtocolException(""))

        FirstPartyEndpoint.register()

        verifyZeroInteractions(storage)
    }

    @Test
    fun load_nonExistent() = runBlockingTest {
        assertNull(FirstPartyEndpoint.load("non-existent"))
    }

    @Test
    fun load_withResult() = runBlockingTest {
        val address = UUID.randomUUID().toString()

        whenever(storage.identityKeyPair.get(eq(address)))
            .thenReturn(KeyPairSet.PRIVATE_ENDPOINT)
        whenever(storage.identityCertificate.get(eq(address)))
            .thenReturn(PDACertPath.PRIVATE_ENDPOINT)
        whenever(storage.gatewayCertificate.get())
            .thenReturn(PDACertPath.PRIVATE_GW)

        with(FirstPartyEndpoint.load(address)) {
            assertNotNull(this)
            assertEquals(KeyPairSet.PRIVATE_ENDPOINT, this?.keyPair)
            assertEquals(PDACertPath.PRIVATE_ENDPOINT, this?.certificate)
            assertEquals(PDACertPath.PRIVATE_GW, this?.gatewayCertificate)
        }
    }
}
