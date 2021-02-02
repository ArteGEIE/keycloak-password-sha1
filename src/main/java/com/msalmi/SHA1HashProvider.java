package com.msalmi;

import java.math.BigInteger;
import java.security.MessageDigest;

import org.keycloak.credential.hash.PasswordHashProvider;
import org.keycloak.models.PasswordPolicy;
import org.keycloak.models.credential.PasswordCredentialModel;

public class SHA1HashProvider implements PasswordHashProvider {

	private final String providerId;
	public static final String ALGORITHM = "SHA-1";

	public SHA1HashProvider(String providerId) {
		this.providerId = providerId;
	}

	@Override
	public void close() {
	}

	@Override
	public boolean policyCheck(PasswordPolicy policy, PasswordCredentialModel credential) {
		return this.providerId.equals(credential.getPasswordCredentialData().getAlgorithm());
	}

	@Override
	public PasswordCredentialModel encodedCredential(String rawPassword, int iterations) {
		String encodedPassword = this.encode(rawPassword, iterations);
		return PasswordCredentialModel.createFromValues(this.providerId, new byte[0], iterations, encodedPassword);
	}

	@Override
	public boolean verify(String rawPassword, PasswordCredentialModel credential) {
		String salt = new String(credential.getPasswordSecretData().getSalt(), java.nio.charset.StandardCharsets.UTF_8);
		String encodedPassword = this.encode(salt + rawPassword, credential.getPasswordCredentialData().getHashIterations());
		String hash = credential.getPasswordSecretData().getValue();
		return encodedPassword.equals(hash);
	}

	@Override
	public String encode(String rawPassword, int iterations) {
		try {
			MessageDigest md = MessageDigest.getInstance(ALGORITHM);
			md.update(rawPassword.getBytes());

			// convert the digest byte[] to BigInteger
			var aux = new BigInteger(1, md.digest());

			// convert BigInteger to 40-char lowercase string using leading 0s
			return String.format("%040x", aux);
		} catch (Exception e) {
			// fail silently
		}

		return null;
	}

}
