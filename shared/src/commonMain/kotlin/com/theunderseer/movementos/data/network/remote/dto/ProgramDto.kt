import com.theunderseer.movementos.data.network.remote.dto.SessionDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ProgramDto(
    val id: String,
    @SerialName("goal_id") val goalId: String,
    val name: String,
    val description: String,
    @SerialName("primary_type") val primaryType: String,
    @SerialName("generated_at") val generatedAt: String,
    @SerialName("is_active") val isActive: Boolean,
    val sessions: List<SessionDto> = emptyList(),
)
