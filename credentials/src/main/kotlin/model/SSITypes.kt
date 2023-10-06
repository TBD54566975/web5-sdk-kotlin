package web5.credentials.model

import com.fasterxml.jackson.annotation.JsonInclude

/** Verifiable Credentials
 *
 * A verifiable credential is a tamper-evident credential that has authorship that can be cryptographically verified.
 *
 * @see [VC Data Model](https://www.w3.org/TR/vc-data-model/)
 */
public typealias VerifiableCredentialType = com.danubetech.verifiablecredentials.VerifiableCredential

/**
 * Status purpose of a status list credential or a credential with a credential status.
 */
public enum class StatusPurpose {
  REVOCATION,
  SUSPENSION
}

/**
 * The JSON property key for an encoded list.
 */
public const val ENCODED_LIST: String = "encodedList"

/**
 * The JSON property key for a status purpose.
 */
public const val STATUS_PURPOSE: String = "statusPurpose"


/**
 * Represents the status of a credential.
 */
public typealias CredentialStatus = com.danubetech.verifiablecredentials.credentialstatus.CredentialStatus

/**
 * Represents a type of credential status a StatusList2021Entry.
 */
public typealias StatusList2021Entry = com.danubetech.verifiablecredentials.credentialstatus.StatusList2021Entry

/**
 * Represents the subject of a verifiable credential.
 */
public typealias CredentialSubject = com.danubetech.verifiablecredentials.CredentialSubject

/**
 * Represents a verifiable presentation.
 */
public typealias VerifiablePresentationType = com.danubetech.verifiablecredentials.VerifiablePresentation

/**
 * Presentation Exchange
 *
 * Presentation Exchange specification codifies a Presentation Definition data format Verifiers can use to articulate
 * proof requirements, and a Presentation Submission data format Holders can use to describe proofs submitted in
 * accordance with them.
 *
 * @see [Presentation Definition](https://identity.foundation/presentation-exchange/#presentation-definition)
 */

public class PresentationDefinitionV2(
  public val id: String,
  public val name: String? = null,
  public val purpose: String? = null,
  public val format: Format? = null,
  public val submissionRequirements: List<SubmissionRequirement>? = null,
  public val inputDescriptors: List<InputDescriptorV2>,
  public val frame: Map<String, Any>? = null
)

/**
 * Represents an input descriptor in a presentation definition.
 *
 * @see [Input Descriptor](https://identity.foundation/presentation-exchange/#input-descriptor-object)
 */
public class InputDescriptorV2(
  public val id: String,
  public val name: String? = null,
  public val purpose: String? = null,
  public val format: Format? = null,
  public val constraints: ConstraintsV2
)

/**
 * Represents constraints for an input descriptor.
 *
 * @See 'contraints object' defined in [Input Descriptor](https://identity.foundation/presentation-exchange/#input-descriptor-object)
 */
public class ConstraintsV2(
  public val fields: List<FieldV2>? = null,
  public val limitDisclosure: ConformantConsumerDisclosure? = null
)

/**
 * Represents a field in a presentation input descriptor.
 *
 * @See 'fields object' as defined in [Input Descriptor](https://identity.foundation/presentation-exchange/#input-descriptor-object)
 */
public class FieldV2(
  public val id: String? = null,
  public val path: List<String>,
  public val purpose: String? = null,
  public val filter: FilterV2? = null,
  public val predicate: Optionality? = null,
  public val name: String? = null,
  public val optional: Boolean? = null
)

/**
 * Enumeration representing consumer disclosure options. Represents the possible values of `limit_disclosure' property
 * as defined in [Input Descriptor](https://identity.foundation/presentation-exchange/#input-descriptor-object)
 */
public enum class ConformantConsumerDisclosure(public val str: String) {
  REQUIRED("required"),
  PREFERRED("preferred")
}

/**
 * Represents the format of a presentation definition.
 *
 * @See 'format' as defined in [Input Descriptor](https://identity.foundation/presentation-exchange/#input-descriptor-object) and [Registry](https://identity.foundation/claim-format-registry/#registry)
 */
public class Format(
  public val jwt: JwtObject? = null,
  public val jwtVc: JwtObject? = null,
  public val jwtVp: JwtObject? = null
)

/**
 * Represents a JWT object.
 */
public class JwtObject(
  public val alg: List<String>
)

/**
 * Represents submission requirements for a presentation definition.
 */
public class SubmissionRequirement(
  public val name: String? = null,
  public val purpose: String? = null,
  public val rule: Rules,
  public val count: Int? = null,
  public val min: Int? = null,
  public val max: Int? = null,
  public val from: String? = null,
  public val fromNested: List<SubmissionRequirement>? = null
)

/**
 * Enumeration representing presentation rule options.
 */
public enum class Rules {
  All, Pick
}

/**
 * Represents a number or string value.
 */
public sealed class NumberOrString {
  /**
   * Creates a NumberOrString from a number value.
   */
  public class NumberValue(public val value: Double) : NumberOrString()

  /**
   * Creates a NumberOrString from a string value.
   */
  public class StringValue(public val value: String) : NumberOrString()
}

/**
 * Enumeration representing optionality.
 */
public enum class Optionality {
  Required,
  Preferred
}

/**
 * Represents filtering constraints.
 */
public class FilterV2(
  public val const: NumberOrString? = null,
  public val enum: List<NumberOrString>? = null,
  public val exclusiveMinimum: NumberOrString? = null,
  public val exclusiveMaximum: NumberOrString? = null,
  public val format: String? = null,
  public val formatMaximum: String? = null,
  public val formatMinimum: String? = null,
  public val formatExclusiveMaximum: String? = null,
  public val formatExclusiveMinimum: String? = null,
  public val minLength: Int? = null,
  public val maxLength: Int? = null,
  public val minimum: NumberOrString? = null,
  public val maximum: NumberOrString? = null,
  public val not: Any? = null,
  public val pattern: String? = null,
  public val type: String
)

/**
 * Represents a presentation submission object.
 *
 * @see [Presentation Submission](https://identity.foundation/presentation-exchange/spec/v2.0.0/#presentation-submission)
 */
public data class PresentationSubmission(
  val id: String,
  val definitionId: String,
  val descriptorMap: List<DescriptorMap>
)

/**
 * Represents descriptor map for a presentation submission.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public data class DescriptorMap(
  val id: String,
  val path: String,
  val pathNested: DescriptorMap? = null,
  val format: String
)