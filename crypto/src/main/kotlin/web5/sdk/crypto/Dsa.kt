package web5.sdk.crypto

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.Curve

/**
 * JSON Web Algorithm Curve.
 *
 * @property curveName name of the curve.
 */
public enum class JwaCurve(public val curveName: String) {
  // todo nimbusds had identifier and oid but i don't think i need it
//  SECP256K1("secp256k1", "secp256k1", "1.3.132.0.10"),
//  Ed25519("Ed25519", "Ed25519", null);

  SECP256K1("secp256k1"),
  Ed25519("Ed25519");

  public companion object {
    /**
     * Parse name of a curve into JwaCurve.
     *
     * @param curveName name of the curve
     * @return JwaCurve which corresponds to the curveName
     * @throws IllegalArgumentException if the curveName is not supported
     */
    public fun parse(curveName: String?): JwaCurve? {
      return if (!curveName.isNullOrEmpty()) {
        when (curveName) {
          SECP256K1.curveName -> SECP256K1
          Ed25519.curveName -> Ed25519
          else -> throw IllegalArgumentException("Unknown curve: $curveName")
        }
      } else {
        null
      }
    }

    /**
     * Convert JwaCurve nimbusds JWK curve.
     * Used to temporarily bridge the gap between moving from nimbusds JWK methods
     * to rolling our own JWK methods
     * @param curve
     * @return nimbus JWK Curve
     */
    public fun toJwkCurve(curve: JwaCurve): Curve {
      return when (curve) {
        SECP256K1 -> Curve.SECP256K1
        Ed25519 -> Curve.Ed25519
      }
    }
  }
}

/**
 * JSON Web Algorithm Curve.
 */
public enum class Jwa {
  EdDSA,
  ES256K;

  public companion object {
    /**
     * Parse algorithm name into Jwa.
     *
     * @param algorithmName name of the algorithm
     * @return Jwa Json Web Algorithm
     * @throws IllegalArgumentException if the algorithmName is not supported
     */
    public fun parse(algorithmName: String?): Jwa? {
      return if (!algorithmName.isNullOrEmpty()) {
        when (algorithmName) {
          EdDSA.name -> EdDSA
          ES256K.name -> ES256K
          else -> throw IllegalArgumentException("Unknown algorithm: $algorithmName")
        }
      } else {
        null
      }
    }

    /**
     * Convert Jwa to nimbusds JWSAlgorithm.
     * Used to temporarily bridge the gap between moving from nimbusds JWK methods
     * to rolling our own JWK methods
     *
     * @param algorithm Jwa
     * @return JWSAlgorithm nimbusds JWSAlgorithm
     */
    public fun toJwsAlgorithm(algorithm: Jwa): JWSAlgorithm {
      return when (algorithm) {
        EdDSA -> JWSAlgorithm.EdDSA
        ES256K -> JWSAlgorithm.ES256K
      }
    }
  }
}

/**
 * AlgorithmId - combination of valid curve and algorithm.
 *
 * @property curveName name of the curve
 * @property algorithmName name of the algorithm
 */
public enum class AlgorithmId(public val curveName: String, public val algorithmName: String? = null) {
  secp256k1(JwaCurve.SECP256K1.curveName, Jwa.ES256K.name),
  Ed25519(JwaCurve.Ed25519.curveName);

  public companion object {
    /**
     * Converts JwaCurve and Jwa combination into a valid AlgorithmId.
     *
     * @param curve JwaCurve
     * @param algorithm Jwa
     * @return AlgorithmId that matches the provided curve and algorithm combination
     * @throws IllegalArgumentException if the combination of curve and algorithm is not supported
     */
    @JvmOverloads
    public fun from(curve: JwaCurve?, algorithm: Jwa? = null): AlgorithmId {
      return when (algorithm to curve) {
        // todo do i need to add the null algo or null curve cases?
        Jwa.ES256K to JwaCurve.SECP256K1 -> secp256k1
        Jwa.EdDSA to JwaCurve.Ed25519 -> Ed25519
        null to JwaCurve.Ed25519 -> Ed25519
        else -> throw IllegalArgumentException(
          "Unknown combination of algorithm to curve: " +
            "${algorithm?.name} to ${curve?.name}"
        )
      }
    }
  }
}