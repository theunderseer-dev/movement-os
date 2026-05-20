import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ProgressEntryDto(
    val id: String,
    @SerialName("session_id") val sessionId: String,
    @SerialName("completed_at") val completedAt: String,
    @SerialName("actual_duration_seconds") val actualDurationSeconds: Int,
    @SerialName("perceived_difficulty") val perceivedDifficulty: String,
    val notes: String? = null,
)
