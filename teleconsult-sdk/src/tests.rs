use crate::TeleconsultSdk;

#[cfg(test)]
mod tests {
    use super::*;

    #[tokio::test]
    async fn test_sdk_version() {
        let sdk = TeleconsultSdk::new();
        assert_eq!(sdk.version(), "0.1.0-olm-apache");
    }

    #[tokio::test]
    async fn test_key_generation() {
        let sdk = TeleconsultSdk::new();
        let keys = sdk.get_public_keys().await;
        assert!(!keys.curve25519.is_empty());
        assert!(!keys.ed25519.is_empty());
    }

    #[tokio::test]
    async fn test_e2ee_flow() {
        let alice = TeleconsultSdk::new();
        let bob = TeleconsultSdk::new();

        let alice_keys = alice.get_public_keys().await;
        let bob_keys = bob.get_public_keys().await;

        // Bob generates OTKs
        bob.generate_one_time_keys(1).await;
        let bob_otks = bob.get_one_time_keys().await;
        let bob_otk = bob_otks[0].clone();

        // Alice creates outbound session to Bob
        alice.create_outbound_session(
            "bob".to_string(),
            bob_keys.curve25519.clone(),
            bob_otk
        ).await.unwrap();

        // Alice encrypts a message
        let original_msg = "Hello Bob!".to_string();
        let encrypted = alice.encrypt_message("bob".to_string(), original_msg.clone()).await.unwrap();
        assert!(encrypted.is_pre_key);

        // Bob creates inbound session from Alice's pre-key message
        let decrypted_by_bob_first = bob.create_inbound_session(
            "alice".to_string(),
            alice_keys.curve25519.clone(),
            encrypted.ciphertext.clone()
        ).await.unwrap();

        assert_eq!(decrypted_by_bob_first, original_msg);

        // Bob replies
        let reply_msg = "Hi Alice!".to_string();
        let encrypted_reply = bob.encrypt_message("alice".to_string(), reply_msg.clone()).await.unwrap();
        
        let decrypted_by_alice = alice.decrypt_message(
            "bob".to_string(),
            encrypted_reply.is_pre_key,
            encrypted_reply.ciphertext
        ).await.unwrap();

        assert_eq!(decrypted_by_alice, reply_msg);
    }
}
