package com.theunderseer.movementos.data.ai.prompt.program

import co.touchlab.kermit.Logger
import com.theunderseer.movementos.data.ai.LlmClient
import com.theunderseer.movementos.data.ai.prompt.PromptBuilder
import com.theunderseer.movementos.data.ai.prompt.PromptConfig
import com.theunderseer.movementos.data.ai.prompt.ResponseParser
import com.theunderseer.movementos.data.network.ApiResult
import com.theunderseer.movementos.domain.model.Program
import com.theunderseer.movementos.domain.model.UserGoal
import com.theunderseer.movementos.domain.usecase.ProgramGenerator
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Production implementation of [ProgramGenerator] backed by LLM.
 *
 * Pipeline:
 * 1. Build prompt from template + goal context
 * 2. Call LLM via [LlmClient] (orchestrator handles provider routing)
 * 3. Parse JSON response to [ProgramGenerationResponse]
 * 4. Map response to domain [Program]
 * 5. On any failure, throw — domain use case wraps in Result, falls back to deterministic generator
 *
 * Logger emits prompt/response/error events for observability (Phase 3 issue 3).
 */
@OptIn(ExperimentalTime::class)
internal class ProgramGenerator(
    private val llmClient: LlmClient,
    private val promptBuilder: PromptBuilder = PromptBuilder(),
    private val responseParser: ResponseParser = ResponseParser(),
    private val clock: Clock = Clock.System,
    private val logger: Logger = Logger.withTag("LlmProgramGenerator"),
) : ProgramGenerator {
    override suspend fun generate(goal: UserGoal): Program {
        val request =
            promptBuilder.build(
                template = ProgramGenerationPrompt.template,
                context = ProgramGenerationPrompt.contextFrom(goal),
                config = PromptConfig.STRUCTURED_JSON,
            )

        logger.d { "Generating program for goal=${goal.id}" }

        return when (val apiResult = llmClient.complete(request)) {
            is ApiResult.Success -> {
                logger.d { "LLM responded via ${apiResult.data.provider}, tokens=${apiResult.data.usage.totalTokens}" }
                parseAndMap(apiResult.data.content, goal)
            }

            is ApiResult.Error -> {
                logger.w { "LLM call failed: $apiResult" }
                error("LLM unavailable: $apiResult")
            }
        }
    }

    private fun parseAndMap(
        content: String,
        goal: UserGoal,
    ): Program {
        val parsed =
            responseParser
                .parse(content, ProgramGenerationResponse.serializer())
                .getOrElse { throwable ->
                    logger.w(throwable) { "Failed to parse response; raw: $content" }
                    error("Invalid LLM response: ${throwable.message}")
                }

        val mapped =
            parsed
                .toDomain(goal = goal, generatedAt = clock.now())
                .getOrElse { throwable ->
                    val validationError = throwable.toValidationError()
                    logger.w { "Failed to map response: $validationError" }
                    error("Invalid program structure: $validationError")
                }

        return mapped
    }
}
