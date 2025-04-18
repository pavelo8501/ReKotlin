package po.auth.authentication.jwt.interfaces

import po.auth.authentication.interfaces.AuthenticationPrincipal

/**
 * Interface responsible for issuing tokens and generating a reference ID.
 */
interface TokenIssuer {
    fun issueToken(principal: AuthenticationPrincipal): Pair<String /* refId */, String /* token */>
}

/**
 * Resolves the authenticated user (principal) from a stored token.
 */
interface AuthResolver {
    fun resolve(token: String): AuthenticationPrincipal?
}