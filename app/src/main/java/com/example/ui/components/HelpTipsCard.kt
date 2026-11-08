package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HelpTipsCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "دليل السرعة الفائقة والخصوصية المطلقة",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            TipItem(
                icon = Icons.Default.Shield,
                iconColor = Color(0xFF10B981),
                title = "خصوصية تامة 100% بدون خوادم خارجية",
                description = "يتم نقل الملفات مباشرة من جهازك إلى حاسوبك عبر بروتوكول HTTP المحلي على شبكة Wi-Fi فقط دون المرور بأي سيرفر سحابي أو اتصال بالإنترنت."
            )

            TipItem(
                icon = Icons.Default.Speed,
                iconColor = Color(0xFF00D2FF),
                title = "أقصى سرعة نقل لاسلكية (30 - 100+ MB/s)",
                description = "لتحقيق سرعات نقل خارقة، تأكد من اتصال الهاتف والحاسوب بنطاق 5GHz على راوتر Wi-Fi أو استخدم وضع نقطة الاتصال (Hotspot)."
            )

            TipItem(
                icon = Icons.Default.WifiTethering,
                iconColor = Color(0xFFF59E0B),
                title = "وضع نقطة الاتصال (Hotspot) بدون راوتر",
                description = "إذا كنت في الخارج أو بدون شبكة Wi-Fi، فقط فعّل 'نقطة اتصال الهواتف المحمولة' في هاتفك واجعل الحاسوب يتصل بها، وسيعمل النقل بأقصى سرعة بدون استهلاك بيانات الشريحة!"
            )

            TipItem(
                icon = Icons.Default.PermMedia,
                iconColor = Color(0xFF8B5CF6),
                title = "دعم كامل لجميع الصيغ والأحجام",
                description = "يدعم التطبيق الصور فائقة الدقة، مقاطع الفيديو 4K، المستندات، ملفات ZIP والبرامج بدون أي قيود على الحجم أو ضغط الجودة."
            )
        }
    }
}

@Composable
private fun TipItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(iconColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}
