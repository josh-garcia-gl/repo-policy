package me.frmr.github.repopolicy.core.parser

import me.frmr.github.repopolicy.core.model.PolicyRule as ModelPolicyRule
import me.frmr.github.repopolicy.core.model.PolicyDescription
import me.frmr.github.repopolicy.core.model.PolicyRuleOperator
import me.frmr.github.repopolicy.core.model.PolicySubjectMatchers
import me.frmr.github.repopolicy.core.operators.branch.BranchProtectionOperator
import me.frmr.github.repopolicy.core.operators.repo.*

object PolicyParser {
  private fun createRepoOperators(input: PolicyRuleRepo?): List<PolicyRuleOperator> {
    val resultingOperators = mutableListOf<PolicyRuleOperator>()

    if (input?.license_key != null) {
      resultingOperators.add(LicenseOperator(input.license_key))
    }

    if (input?.delete_branch_on_merge != null) {
      resultingOperators.add(DeleteBranchOnMergeOperator(input.delete_branch_on_merge))
    }

    if (input?.visibility != null) {
      resultingOperators.add(VisibilityOperator(input.visibility))
    }

    if (input?.default_branch != null) {
      resultingOperators.add(DefaultBranchOperator(input.default_branch))
    }

    if (input?.features != null) {
      resultingOperators.add(FeaturesOperator(
        input.features.issues,
        input.features.projects,
        input.features.wiki
      ))
    }

    if (input?.collaborators != null) {
      resultingOperators.add(CollaboratorsOperator(
        input.collaborators
      ))
    }

    return resultingOperators
  }

  private fun createBranchOperators(input: PolicyRuleBranch?): List<PolicyRuleOperator> {
    // Nothing to see here!
    if (input == null) {
      return emptyList()
    }

    // Branch isn't specified or isn't specified correctly
    if (input.branch.trim() == "") {
      return emptyList()
    }

    // Also nothing to see here
    if (input.protection == null) {
      return emptyList()
    }

    val resultingOperators = mutableListOf<PolicyRuleOperator>()

    resultingOperators.add(BranchProtectionOperator(
      branch = input.branch,
      enabled = input.protection.enabled,
      requiredChecks = input.protection.required_checks,
      dismissStaleReviews = input.protection.dismiss_stale_reviews,
      includeAdmins = input.protection.include_admins,
      requireBranchIsUpToDate = input.protection.require_up_to_date,
      requireCodeOwnerReviews = input.protection.require_code_owner_reviews,
      requiredReviewCount = input.protection.required_review_count,
      restrictPushAccess = input.protection.restrict_push_access,
      restrictReviewDismissals = input.protection.restrict_review_dismissals,
      pushTeams = input.protection.push_teams,
      pushUsers = input.protection.push_users,
      reviewDismissalUsers = input.protection.review_dismissal_users
    ))

    return resultingOperators
  }

  private fun parseRule(input: PolicyRule): ModelPolicyRule {
    val subjectMatchers = PolicySubjectMatchers(user = input.user, topic = input.topic, org = input.org)
    return ModelPolicyRule(
      subjectMatchers,
      createRepoOperators(input.repo)
    )
  }

  fun parseDataFile(input: PolicyDataFile): PolicyDescription {
    return PolicyDescription(
      name = input.name,
      version = input.version,
      author = input.author,
      rules = input.rules.map { parseRule(it) }
    )
  }
}
