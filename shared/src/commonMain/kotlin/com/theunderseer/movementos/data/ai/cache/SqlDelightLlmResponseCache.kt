package com.theunderseer.movementos.data.ai.cache

import com.theunderseer.movementos.data.ai.LlmProvider
import com.theunderseer.movementos.data.ai.LlmResponse
import com.theunderseer.movementos.data.ai.TokenUsage
import com.theunderseer.movementos.database.MovementOSDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Persistent LLM response cache backed by SQLDelight.
 *
 * L2 of multi-layer cache (survives app restart). Lazy eviction:
 * expired rows deleted on read miss + scheduled cleanup (future Phase 7 work).
 */
@OptIn(ExperimentalTime::class)
internal class SqlDelightLlmResponseCache(
    private val database: MovementOSDatabase,
    private val dispatcher: CoroutineDispatcher,
    private val clock: Clock = Clock.System,
) : LlmResponseCache {
    private val queries get() = database.llmCacheQueries

    override suspend fun get(key: String): LlmResponse? =
        withContext(dispatcher) {
            val now = clock.now().toEpochMilliseconds()
            queries.selectByKey(cache_key = key, now = now).executeAsOneOrNull()?.let { row ->
                LlmResponse(
                    content = row.content,
                    provider = LlmProvider.valueOf(row.provider),
                    model = row.model,
                    usage =
                        TokenUsage(
                            promptTokens = row.prompt_tokens.toInt(),
                            completionTokens = row.completion_tokens.toInt(),
                            totalTokens = row.total_tokens.toInt(),
                        ),
                )
            }
        }

    override suspend fun put(
        key: String,
        response: LlmResponse,
        ttlMs: Long,
    ) {
        withContext(dispatcher) {
            val now = clock.now().toEpochMilliseconds()
            queries.upsert(
                cache_key = key,
                content = response.content,
                provider = response.provider.name,
                model = response.model,
                prompt_tokens = response.usage.promptTokens.toLong(),
                completion_tokens = response.usage.completionTokens.toLong(),
                total_tokens = response.usage.totalTokens.toLong(),
                cached_at_ms = now,
                expires_at_ms = now + ttlMs,
            )
        }
    }

    override suspend fun clear() {
        withContext(dispatcher) {
            queries.deleteAll()
        }
    }

    override suspend fun size(): Int =
        withContext(dispatcher) {
            queries.count().executeAsOne().toInt()
        }
}
