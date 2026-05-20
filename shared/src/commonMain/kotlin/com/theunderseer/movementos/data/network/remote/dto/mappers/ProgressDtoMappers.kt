import com.theunderseer.movementos.domain.model.ProgressEntry
import com.theunderseer.movementos.domain.model.values.DifficultyLevel
import com.theunderseer.movementos.domain.model.values.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
internal fun ProgressEntryDto.toDomain(): ProgressEntry =
    ProgressEntry(
        id = id,
        sessionId = sessionId,
        completedAt = Instant.parse(completedAt),
        actualDuration = Duration(actualDurationSeconds),
        perceivedDifficulty = DifficultyLevel.valueOf(perceivedDifficulty),
        notes = notes,
    )

@OptIn(ExperimentalTime::class)
internal fun ProgressEntry.toDto(): ProgressEntryDto =
    ProgressEntryDto(
        id = id,
        sessionId = sessionId,
        completedAt = completedAt.toString(),
        actualDurationSeconds = actualDuration.seconds,
        perceivedDifficulty = perceivedDifficulty.name,
        notes = notes,
    )
