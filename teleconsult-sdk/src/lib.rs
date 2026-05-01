uniffi::setup_scaffolding!();

use std::collections::HashMap;
use std::sync::Arc;
use thiserror::Error;
use vodozemac::olm::{Account, Session, PreKeyMessage, OlmMessage, Message, SessionConfig};
use vodozemac::Curve25519PublicKey;
use tokio::sync::Mutex;

#[derive(Error, Debug, uniffi::Error)]
pub enum SdkError {
    #[error("Encryption error: {0}")]
    EncryptionError(String),
    #[error("Handshake failed: {0}")]
    HandshakeError(String),
    #[error("Key error: {0}")]
    KeyError(String),
    #[error("Internal error: {0}")]
    InternalError(String),
}

#[derive(uniffi::Record)]
pub struct PublicKeys {
    pub curve25519: String,
    pub ed25519: String,
}

#[derive(uniffi::Record)]
pub struct EncryptedMessage {
    pub is_pre_key: bool,
    pub ciphertext: String,
}

#[derive(uniffi::Object)]
pub struct TeleconsultSdk {
    account: Mutex<Account>,
    sessions: Mutex<HashMap<String, Session>>,
}

#[uniffi::export]
impl TeleconsultSdk {
    #[uniffi::constructor]
    pub fn new() -> Arc<Self> {
        Arc::new(Self {
            account: Mutex::new(Account::new()),
            sessions: Mutex::new(HashMap::new()),
        })
    }

    pub fn version(&self) -> String {
        "0.1.0-olm-apache".to_string()
    }

    pub async fn get_public_keys(&self) -> PublicKeys {
        let account = self.account.lock().await;
        PublicKeys {
            curve25519: account.curve25519_key().to_base64(),
            ed25519: account.ed25519_key().to_base64(),
        }
    }

    pub async fn generate_one_time_keys(&self, count: u32) {
        let mut account = self.account.lock().await;
        account.generate_one_time_keys(count as usize);
    }

    pub async fn get_one_time_keys(&self) -> Vec<String> {
        let account = self.account.lock().await;
        account
            .one_time_keys()
            .values()
            .map(|k| k.to_base64())
            .collect()
    }

    /// Creates an outbound session to a recipient.
    pub async fn create_outbound_session(
        &self,
        recipient_id: String,
        recipient_curve25519_key: String,
        one_time_key: String,
    ) -> Result<(), SdkError> {
        let account = self.account.lock().await;
        
        let curve_key = Curve25519PublicKey::from_base64(&recipient_curve25519_key)
            .map_err(|e| SdkError::KeyError(e.to_string()))?;
        let otk = Curve25519PublicKey::from_base64(&one_time_key)
            .map_err(|e| SdkError::KeyError(e.to_string()))?;

        // In vodozemac 0.8, create_outbound_session takes (config, identity_key, one_time_key)
        let config = SessionConfig::default();
        let session = account.create_outbound_session(config, curve_key, otk);
        
        let mut sessions = self.sessions.lock().await;
        sessions.insert(recipient_id, session);
        Ok(())
    }

    /// Creates an inbound session from a received message.
    pub async fn create_inbound_session(
        &self,
        sender_id: String,
        sender_curve25519_key: String,
        pre_key_message_base64: String,
    ) -> Result<String, SdkError> {
        let mut account = self.account.lock().await;
        
        let sender_key = Curve25519PublicKey::from_base64(&sender_curve25519_key)
            .map_err(|e| SdkError::KeyError(e.to_string()))?;
        
        let pre_key_message = PreKeyMessage::from_base64(&pre_key_message_base64)
            .map_err(|e| SdkError::EncryptionError(e.to_string()))?;

        let result = account.create_inbound_session(sender_key, &pre_key_message)
            .map_err(|e| SdkError::HandshakeError(e.to_string()))?;
        
        let decrypted_payload = String::from_utf8(result.plaintext)
            .map_err(|e| SdkError::InternalError(e.to_string()))?;

        let mut sessions = self.sessions.lock().await;
        sessions.insert(sender_id, result.session);
        
        Ok(decrypted_payload)
    }

    pub async fn encrypt_message(&self, recipient_id: String, payload: String) -> Result<EncryptedMessage, SdkError> {
        let mut sessions = self.sessions.lock().await;
        let session = sessions.get_mut(&recipient_id)
            .ok_or_else(|| SdkError::InternalError("No session for recipient".to_string()))?;
        
        let message = session.encrypt(payload);
        match message {
            OlmMessage::PreKey(m) => Ok(EncryptedMessage {
                is_pre_key: true,
                ciphertext: m.to_base64(),
            }),
            OlmMessage::Normal(m) => Ok(EncryptedMessage {
                is_pre_key: false,
                ciphertext: m.to_base64(),
            }),
        }
    }

    pub async fn decrypt_message(&self, sender_id: String, is_pre_key: bool, encrypted_payload: String) -> Result<String, SdkError> {
        let mut sessions = self.sessions.lock().await;
        let session = sessions.get_mut(&sender_id)
            .ok_or_else(|| SdkError::InternalError("No session for sender".to_string()))?;
        
        let olm_message = if is_pre_key {
            let m = PreKeyMessage::from_base64(&encrypted_payload)
                .map_err(|e| SdkError::EncryptionError(e.to_string()))?;
            OlmMessage::PreKey(m)
        } else {
            let m = Message::from_base64(&encrypted_payload)
                .map_err(|e| SdkError::EncryptionError(e.to_string()))?;
            OlmMessage::Normal(m)
        };
            
        let decrypted_bytes = session.decrypt(&olm_message)
            .map_err(|e| SdkError::EncryptionError(e.to_string()))?;
            
        String::from_utf8(decrypted_bytes)
            .map_err(|e| SdkError::InternalError(e.to_string()))
    }
}

#[cfg(test)]
mod tests;
