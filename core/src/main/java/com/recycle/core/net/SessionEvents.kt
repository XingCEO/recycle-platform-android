package com.recycle.core.net

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Process-wide signal that the auth session became *unrecoverable*: the refresh
 * token is missing/expired/revoked, so a 401 can no longer be retried. The
 * network layer ([SupabaseClient.refreshSession]) fires it via [AppPrefs];
 * by the time it fires, the persisted session has already been wiped.
 *
 * The navigation roots collect [expired] and drop straight back to the login
 * screen — fixing the "JWT expired forever" loop where a dead session was
 * reloaded on every cold start with no route back to login.
 *
 * No replay on purpose: a stale expiry must never bounce a freshly-logged-in
 * user. Collectors subscribe at app launch (long before any network 401 can
 * return), so the live signal is never missed.
 */
object SessionEvents {
    private val _expired = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val expired: SharedFlow<Unit> = _expired.asSharedFlow()

    /** Fired from the network layer when refresh fails. Safe to call repeatedly. */
    fun notifyExpired() { _expired.tryEmit(Unit) }
}
