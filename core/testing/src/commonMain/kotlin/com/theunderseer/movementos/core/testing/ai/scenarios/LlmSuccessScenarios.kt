package com.theunderseer.movementos.core.testing.ai.scenarios

import com.theunderseer.movementos.core.testing.ai.FakeLlmClient
import com.theunderseer.movementos.core.testing.ai.fixtures.ProgramGenerationFixtures
import com.theunderseer.movementos.data.ai.LlmProvider
import com.theunderseer.movementos.data.ai.LlmResponse
import com.theunderseer.movementos.data.network.ApiResult

object LlmSuccessScenarios {
    val validProgramFromGemini: ApiResult.Success<LlmResponse> =
        FakeLlmClient.successResponse(
            content = ProgramGenerationFixtures.validProgramJson,
            provider = LlmProvider.GEMINI,
        )

    val validProgramFromAnthropic: ApiResult.Success<LlmResponse> =
        FakeLlmClient.successResponse(
            content = ProgramGenerationFixtures.validProgramJson,
            provider = LlmProvider.ANTHROPIC,
        )

    val validProgramWithMarkdown: ApiResult.Success<LlmResponse> =
        FakeLlmClient.successResponse(
            content = ProgramGenerationFixtures.validProgramJsonWithMarkdown,
        )
}
