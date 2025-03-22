package po.auth.authentication.tokens.interfaces

import po.auth.authentication.interfaces.AuthenticatedPrincipal

/**
 * Interface responsible for issuing tokens and generating a reference ID.
 */
interface TokenIssuer {
    fun issueToken(principal: AuthenticatedPrincipal): Pair<String /* refId */, String /* token */>
}

/**
 * Resolves the authenticated user (principal) from a stored token.
 */
interface AuthResolver {
    fun resolve(token: String): AuthenticatedPrincipal?
}