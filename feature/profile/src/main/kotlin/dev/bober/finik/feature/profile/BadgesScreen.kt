package dev.bober.finik.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.bober.finik.core.designsystem.component.BackHeader
import dev.bober.finik.core.designsystem.component.FinikProgressBar
import dev.bober.finik.core.designsystem.component.FinikIcons
import dev.bober.finik.core.designsystem.theme.FinikColor
import dev.bober.finik.core.designsystem.theme.FinikTheme
import dev.bober.finik.core.designsystem.theme.nunito
import dev.bober.finik.core.model.Badge
import dev.bober.finik.core.model.SampleData

/** «Достижения»: список из шести наград с прогрессом; выполненные — золотые. */
@Composable
internal fun BadgesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    badges: List<Badge> = SampleData.badges,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FinikColor.Background)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BackHeader(title = "Достижения", onBack = onBack)
        badges.forEach { badge -> BadgeRow(badge = badge) }
    }
}

@Composable
private fun BadgeRow(badge: Badge) {
    val shape = RoundedCornerShape(16.dp)
    val color = if (badge.isDone) FinikColor.Gold else FinikColor.GreenBadge
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (badge.isDone) FinikColor.GoldCard else FinikColor.Surface)
            .border(1.dp, if (badge.isDone) FinikColor.GoldBorder else FinikColor.BorderSoft, shape)
            .padding(horizontal = 13.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color, if (badge.isDone) CircleShape else RoundedCornerShape(11.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = badgeIcon(badge),
                contentDescription = null,
                modifier = Modifier.size(25.dp),
                tint = MaterialTheme.colorScheme.onPrimary,
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = badge.name, style = nunito(14.5), color = FinikColor.InkBadge)
            Text(text = badge.note, style = nunito(12, FontWeight.SemiBold), color = FinikColor.Text50)
            FinikProgressBar(progress = badge.percent / 100f, color = color, height = 7.dp, track = FinikColor.IconButton)
        }
        Text(text = "${badge.percent}%", style = nunito(12), color = FinikColor.Text50)
    }
}

private fun badgeIcon(badge: Badge): ImageVector {
    val key = "${badge.slug} ${badge.name}".lowercase()
    return when {
        "план" in key || "plan" in key -> FinikIcons.BadgePlan
        "двадцать" in key || "копил" in key || "percent" in key || "save" in key -> FinikIcons.BadgeSave
        "скид" in key || "discount" in key -> FinikIcons.BadgeDiscount
        "полпути" in key || "половин" in key || "half" in key -> FinikIcons.BadgeHalfway
        "долг" in key || "перерасход" in key || "debt" in key -> FinikIcons.BadgeDebtFree
        "мудр" in key || "wise" in key -> FinikIcons.BadgeWise
        else -> FinikIcons.BadgeOther
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 900)
@Composable
private fun BadgesScreenPreview() {
    FinikTheme { BadgesScreen(onBack = {}) }
}
