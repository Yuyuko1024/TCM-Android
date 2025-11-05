package net.hearnsoft.tcm.compose.ui.uicomponent.listitem

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toDrawable
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.placeholder
import com.moriafly.salt.ui.Icon
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import net.hearnsoft.tcm.compose.R

// 这个是歌单用的歌单列表组件
// 和上面的那个PlayerPlaylistItem不是一个东西
@Composable
fun PlaylistListItem(
    modifier: Modifier = Modifier,
    playlistName: String,
    songCount: Int = 0,
    artworkUri: Uri? = null,
    isFavoritePlaylist: Boolean = false,
    onClick: () -> Unit = { },
    onActionClick: () -> Unit = { }
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Box(Modifier.size(50.dp).align(Alignment.CenterVertically)) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(artworkUri ?: R.drawable.ic_nav_music.toDrawable())
                    .crossfade(true)
                    .crossfade(1000)
                    .placeholder(R.drawable.ic_nav_music)
                    .build(),
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(4.dp)),
                contentDescription = "Album Art",
                contentScale = ContentScale.Crop
            )
            if (isFavoritePlaylist) {
                Box(Modifier.matchParentSize()
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.DarkGray.copy(alpha = 0.4f)))
                Icon(
                    painter = painterResource(R.drawable.ic_favorite),
                    contentDescription = "Favorite Playlist",
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(4.dp)
                        .size(24.dp)
                )
            }
        }
        Spacer(Modifier.size(4.dp))
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 2.dp)
                .weight(1f)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                text = playlistName ?: stringResource(R.string.playlist_name_not_defined),
                style = SaltTheme.textStyles.main,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = SaltTheme.colors.text
            )
            Text(
                text = stringResource(R.string.song_count, songCount),
                style = SaltTheme.textStyles.sub,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = SaltTheme.colors.subText
            )
        }

        if (isFavoritePlaylist.not()) {
            IconButton(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(8.dp),
                onClick = { onActionClick() }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_more_vert_24px),
                    contentDescription = "More Options",
                )
            }
        }
    }
}