package com.example.security

import java.util.UUID

/**
 * A mock E2EE engine.
 * In a real app, this would use Elliptic Curve Diffie-Hellman (ECDH) 
 * to securely derive a shared secret.
 */
object MockEncryptionEngine {

    // Mock keys
    var publicKey: String = UUID.randomUUID().toString().take(8)
        private set
    
    private var privateKey: String = UUID.randomUUID().toString()

    /**
     * Mocks key exchange to generate a shared secret.
     */
    fun generateSharedSecret(peerPublicKey: String): String {
        // Mocked secret derivation
        return (publicKey.hashCode() xor peerPublicKey.hashCode()).toString(16)
    }
}
