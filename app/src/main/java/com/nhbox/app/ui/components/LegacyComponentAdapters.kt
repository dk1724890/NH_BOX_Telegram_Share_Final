package com.nhbox.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nhbox.app.data.model.CatalogItem
import com.nhbox.app.ui.theme.NhCardBg
import com.nhbox.app.ui.theme.NhCardBorder
import com.nhbox.app.ui.theme.NhLightGray
import com.nhbox.app.ui.theme.NhRed
import com.nhbox.app.ui.theme.NhWhite

@Composable
fun FilterPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (selected) NhRed else NhCardBg,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, if (selected) NhRed else NhCardBorder),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            color = if (selected) NhWhite else NhLightGray,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
        )
    }
}

@Composable
fun MovieBoxLogo(
    iconSize: Dp = 28.dp,
    fontSizeSp: Int = 20,
    subtitle: String? = null
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = NhWhite, fontWeight = FontWeight.ExtraBold)) {
                        append("NH ")
                    }
                    withStyle(SpanStyle(color = NhRed, fontWeight = FontWeight.ExtraBold)) {
                        append("BOX")
                    }
                },
                fontSize = fontSizeSp.sp
            )
        }
        if (!subtitle.isNullOrBlank()) {
            Text(text = subtitle, color = NhLightGray, fontSize = 11.sp)
        }
    }
}

@Composable
fun PosterImageWithFallback(
    imageUrl: String,
    title: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    RtdbAsyncImage(
        source = imageUrl,
        contentDescription = title,
        modifier = modifier,
        contentScale = contentScale
    )
}

@Composable
fun PosterCard(
    item: CatalogItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    PosterCard(
        title = item.title,
        posterSource = item.posterUrl.ifBlank { item.backdropUrl },
        releaseYear = item.releaseYear.toString(),
        rating = item.rating,
        onClick = onClick,
        modifier = modifier
    )
}
