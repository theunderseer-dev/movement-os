import com.theunderseer.movementos.domain.model.UserGoal
import com.theunderseer.movementos.domain.model.values.Duration
import com.theunderseer.movementos.domain.model.values.MovementType
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
internal fun UserGoalDto.toDomain(): UserGoal =
    UserGoal(
        id = id,
        description = description,
        focus = MovementType.valueOf(focus),
        sessionsPerWeek = sessionsPerWeek,
        timePerSession = Duration(timePerSessionSeconds),
        createdAt = Instant.parse(createdAt),
    )

@OptIn(ExperimentalTime::class)
internal fun UserGoal.toDto(): UserGoalDto =
    UserGoalDto(
        id = id,
        description = description,
        focus = focus.name,
        sessionsPerWeek = sessionsPerWeek,
        timePerSessionSeconds = timePerSession.seconds,
        createdAt = createdAt.toString(),
    )
