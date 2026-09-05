package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MomentBadge(
    momentType: String,
    modifier: Modifier = Modifier
) {
    val (label, icon, color, bg) = when (momentType.lowercase()) {
        "viral_hook", "hook" -> Quad("VIRAL HOOK", Icons.Default.Bolt, Color(0xFFE65100), Color(0xFFFFE0B2))
        "epic_climax", "climax", "goal_scored" -> Quad("EPIC CLIMAX", Icons.Default.LocalFireDepartment, Color(0xFFC2185B), Color(0xFFF8BBD0))
        "key_takeaway", "insight", "wisdom" -> Quad("KEY INSIGHT", Icons.Default.Lightbulb, Color(0xFF00796B), Color(0xFFB2DFDB))
        "hilarious_moment", "funny", "laugh" -> Quad("HILARIOUS", Icons.Default.Face, Color(0xFFF57F17), Color(0xFFFFF9C4))
        "action_highlight", "action", "chance_created" -> Quad("ACTION", Icons.Default.ElectricBolt, Color(0xFF512DA8), Color(0xFFD1C4E9))
        "skill_moment", "highlight" -> Quad("TOP MOMENT", Icons.Default.EmojiEvents, Color(0xFF00838F), Color(0xFFB2EBF2))
        else -> Quad("VIRAL SHORT", Icons.Default.AutoAwesome, Color(0xFF6750A4), Color(0xFFEADDFF))
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(bg)
            .border(0.8.dp, color.copy(alpha = 0.25f), CircleShape)
            .padding(horizontal = 9.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = color,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp
            )
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
