package web5.sdk.credentials

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import web5.sdk.common.Convert
import web5.sdk.common.Json
import web5.sdk.crypto.AlgorithmId
import web5.sdk.crypto.AwsKeyManager
import web5.sdk.crypto.InMemoryKeyManager
import web5.sdk.crypto.Jwa
import web5.sdk.dids.didcore.Purpose
import web5.sdk.jose.jws.JwsHeader
import web5.sdk.jose.jwt.Jwt
import web5.sdk.jose.jwt.JwtClaimsSet
import web5.sdk.dids.methods.dht.CreateDidDhtOptions
import web5.sdk.dids.methods.dht.DidDht
import web5.sdk.dids.methods.key.DidKey
import web5.sdk.testing.TestVectors
import java.io.File
import java.security.SignatureException
import kotlin.test.Ignore
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNotNull

data class StreetCredibility(val localRespect: String, val legit: Boolean)
class VerifiableCredentialTest {
  @Test
  @Ignore("Testing with a prev created ion did")
  fun `create a vc with a previously created DID in the key manager`() {
    val keyManager = AwsKeyManager()
    val issuerDid = DidDht.create(keyManager)
    val holderDid = DidKey.create(keyManager)

    val vc = VerifiableCredential.create(
      type = "StreetCred",
      issuer = issuerDid.did.uri,
      subject = holderDid.did.uri,
      data = StreetCredibility(localRespect = "high", legit = true)
    )

    val vcJwt = vc.sign(issuerDid)

    assertDoesNotThrow {
      VerifiableCredential.verify(vcJwt)
    }
  }

  @Test
  fun `create works`() {
    val keyManager = InMemoryKeyManager()
    val issuerDid = DidKey.create(keyManager)
    val holderDid = DidKey.create(keyManager)

    val vc = VerifiableCredential.create(
      type = "StreetCred",
      issuer = issuerDid.did.uri,
      subject = holderDid.did.uri,
      data = StreetCredibility(localRespect = "high", legit = true)
    )
    assertNotNull(vc)
  }

  @Test
  fun `create throws if data cannot be parsed into a json object`() {
    val keyManager = InMemoryKeyManager()
    val issuerDid = DidKey.create(keyManager)
    val holderDid = DidKey.create(keyManager)

    val exception = assertThrows(IllegalArgumentException::class.java) {
      VerifiableCredential.create(
        type = "StreetCred",
        issuer = issuerDid.did.uri,
        subject = holderDid.did.uri,
        data = "trials & tribulations"
      )
    }

    // Optionally, further verify the exception (e.g., check the message)
    assertEquals("expected data to be parseable into a JSON object", exception.message)
  }

  @Test
  fun `verify does not throw an exception if vc is legit`() {
    val keyManager = InMemoryKeyManager()
    val issuerDid = DidKey.create(keyManager)
    val holderDid = DidKey.create(keyManager)

    val vc = VerifiableCredential.create(
      type = "StreetCred",
      issuer = issuerDid.did.uri,
      subject = holderDid.did.uri,
      data = StreetCredibility(localRespect = "high", legit = true)
    )

    val vcJwt = vc.sign(issuerDid)
    VerifiableCredential.verify(vcJwt)
  }

  @Test
  fun `verify handles DIDs without an assertionMethod`() {
    val keyManager = InMemoryKeyManager()

    // Create a DHT DID without an assertionMethod
    val alias = keyManager.generatePrivateKey(AlgorithmId.secp256k1)
    val verificationJwk = keyManager.getPublicKey(alias)

    val verificationMethodsToAdd = listOf(Triple(
      verificationJwk,
      emptyList<Purpose>(),
      "did:web:tbd.website"
    ))
    val issuerDid = DidDht.create(
      InMemoryKeyManager(),
      CreateDidDhtOptions(verificationMethods = verificationMethodsToAdd)
    )

    val header = JwsHeader.Builder()
      .type("JWT")
      .algorithm(Jwa.ES256K.name)
      .keyId(issuerDid.did.uri)
      .build()
    // A detached payload JWT
    val vcJwt = "${Convert(Json.stringify(header)).toBase64Url()}..fakeSig"

    val exception = assertThrows(SignatureException::class.java) {
      VerifiableCredential.verify(vcJwt)
    }
    assertContains(
      exception.message!!, "not found in list of assertion methods",
    )
  }

  @Test
  fun `parseJwt throws IllegalStateException if argument is not a valid JWT`() {
    assertThrows(IllegalStateException::class.java) {
      VerifiableCredential.parseJwt("hi")
    }
  }

  @Test
  fun `parseJwt throws if vc property is missing in JWT`() {
    val signerDid = DidDht.create(InMemoryKeyManager())

    val claimsSet = JwtClaimsSet.Builder()
      .subject("alice")
      .build()

    val signedJWT = Jwt.sign(signerDid, claimsSet)

    val exception = assertThrows(IllegalArgumentException::class.java) {
      VerifiableCredential.parseJwt(signedJWT)
    }

    assertEquals("jwt payload missing vc property", exception.message)
  }

  @Test
  fun `parseJwt throws if vc property in JWT payload is not an object`() {
    val signerDid = DidDht.create(InMemoryKeyManager())

    val claimsSet = JwtClaimsSet.Builder()
      .subject("alice")
      .misc("vc", "hehe troll")
      .build()

    val signedJWT = Jwt.sign(signerDid, claimsSet)
    val exception = assertThrows(IllegalArgumentException::class.java) {
      VerifiableCredential.parseJwt(signedJWT)
    }

    assertEquals("expected vc property in JWT payload to be an object", exception.message)
  }

  @Test
  fun `parseJwt returns an instance of VerifiableCredential on success`() {
    val keyManager = InMemoryKeyManager()
    val issuerDid = DidKey.create(keyManager)
    val holderDid = DidKey.create(keyManager)

    val vc = VerifiableCredential.create(
      type = "StreetCred",
      issuer = issuerDid.did.uri,
      subject = holderDid.did.uri,
      data = StreetCredibility(localRespect = "high", legit = true)
    )

    val vcJwt = vc.sign(issuerDid)

    val parsedVc = VerifiableCredential.parseJwt(vcJwt)
    assertNotNull(parsedVc)

    assertEquals(vc.type, parsedVc.type)
    assertEquals(vc.issuer, parsedVc.issuer)
    assertEquals(vc.subject, parsedVc.subject)
  }
}

class Web5TestVectorsCredentials {

  data class CreateTestInput(
    val signerDidUri: String?,
    val signerPrivateJwk: Map<String, Any>?,
    val credential: Map<String, Any>?,
  )

  data class VerifyTestInput(
    val vcJwt: String,
  )

  private val mapper = jacksonObjectMapper()

  @Test
  fun create() {
    val typeRef = object : TypeReference<TestVectors<CreateTestInput, String>>() {}
    val testVectors = mapper.readValue(File("../web5-spec/test-vectors/credentials/create.json"), typeRef)

    testVectors.vectors.filterNot { it.errors ?: false }.forEach { vector ->
      println(vector.description)
      val vc = VerifiableCredential.fromJson(mapper.writeValueAsString(vector.input.credential))

      val keyManager = InMemoryKeyManager()
      keyManager.import(listOf(vector.input.signerPrivateJwk!!))
      val issuerDid = DidKey.create(keyManager)
      // todo need to update test vectors
      //  input should have portable did and credential
      // want to be able to call BearerDID.import()
      val vcJwt = vc.sign(issuerDid)

      assertEquals(vector.output, vcJwt, vector.description)
    }

    testVectors.vectors.filter { it.errors ?: false }.forEach { vector ->
      assertFails(vector.description) {
        VerifiableCredential.fromJson(mapper.writeValueAsString(vector.input.credential))
      }
    }
  }

  @Test
  fun verify() {
    val typeRef = object : TypeReference<TestVectors<VerifyTestInput, Unit>>() {}
    val testVectors = mapper.readValue(File("../web5-spec/test-vectors/credentials/verify.json"), typeRef)

    testVectors.vectors.filterNot { it.errors ?: false }.forEach { vector ->
      println(vector.description)
      assertDoesNotThrow {
        VerifiableCredential.verify(vector.input.vcJwt)
      }
    }

    testVectors.vectors.filter { it.errors ?: false }.forEach { vector ->
      assertFails {
        VerifiableCredential.verify(vector.input.vcJwt)
      }
    }
  }
}