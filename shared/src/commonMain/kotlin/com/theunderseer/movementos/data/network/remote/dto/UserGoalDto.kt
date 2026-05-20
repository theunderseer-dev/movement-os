import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class UserGoalDto(
    val id: String,
    val description: String,
    val focus: String,
    @SerialName("sessions_per_week") val sessionsPerWeek: Int,
    @SerialName("time_per_session_seconds") val timePerSessionSeconds: Int,
    @SerialName("created_at") val createdAt: String,
)
