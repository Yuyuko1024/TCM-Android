package net.hearnsoft.tcm.compose.ui.screens

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.placeholder
import com.moriafly.salt.ui.Button
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
import net.hearnsoft.tcm.compose.R
import net.hearnsoft.tcm.compose.data.database.entities.SongEntity
import net.hearnsoft.tcm.compose.ui.uicomponent.listitem.MusicListItem
import net.hearnsoft.tcm.compose.ui.uicomponent.sheet.SongActionSheetDialog
import net.hearnsoft.tcm.compose.ui.viewmodel.PlayerViewModel
import net.hearnsoft.tcm.compose.ui.viewmodel.PlaylistViewModel

@SuppressLint("LocalContextGetResourceValueCall")
@UnstableApi
@ExperimentalFoundationApi
@ExperimentalMaterial3Api
@Composable
@UnstableSaltUiApi
fun PlaylistScreen(
    modifier: Modifier = Modifier,
    playlistId: Long,
    navController: NavHostController,
    playerViewModel: PlayerViewModel,
    playlistViewModel: PlaylistViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // === 基础状态 ===
    val isConnected by playerViewModel.isConnected.collectAsState()
    val isLoading by playlistViewModel.isLoading.collectAsState()

    // === 数据源状态 ===
    val favoriteSongs by playerViewModel.favoriteSongs.collectAsState()
    val playlistSongs by playlistViewModel.currentPlaylistSongs.collectAsState()
    val currentPlaylist by playlistViewModel.currentPlaylist.collectAsState()
    val currentPlaying by playerViewModel.currentMediaItem.collectAsState()

    // === 统一的歌曲列表（根据 playlistId 选择数据源） ===
    val songs: List<SongEntity> by remember(playlistId) {
        derivedStateOf {
            if (playlistId == 0L) {
                favoriteSongs
            } else {
                playlistSongs
            }
        }
    }

    // === 歌单标题（用于显示） ===
    val playlistTitle: String by remember(playlistId, currentPlaylist) {
        derivedStateOf {
            if (playlistId == 0L) {
                context.getString(R.string.playlist_favorite)
            } else {
                currentPlaylist?.playlistName ?: context.getString(R.string.playlist_name_not_defined)
            }
        }
    }

    val playlistDescription: String? by remember(playlistId, currentPlaylist) {
        derivedStateOf {
            if (playlistId == 0L) {
                null
            } else {
                currentPlaylist?.description
            }
        }
    }

    // === 数据加载逻辑 ===
    LaunchedEffect(playlistId) {
        if (playlistId != 0L) {
            playlistViewModel.selectPlaylist(playlistId)
        }
    }

    var showActionDialog by remember { mutableStateOf(false) }
    var selectedSong by remember { mutableStateOf<SongEntity?>(null) }

    if (showActionDialog) {
        selectedSong?.let {
            SongActionSheetDialog(
                onDismissRequest = {
                    showActionDialog = false
                },
                playerViewModel = playerViewModel,
                songEntity = it,
                navController = navController
            )
        }
    }

    // === UI 渲染 ===
    Box(modifier.fillMaxSize()) {
        LazyColumn(
            modifier = modifier.fillMaxSize().padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                PlaylistHeader(
                    playlistTitle = playlistTitle,
                    playlistDescription = playlistDescription,
                    songCount = songs.size,
                    artworkUri = songs.firstOrNull()?.artworkUri,
                    onPlayAllClick = {
                        playerViewModel.setAndPlayPlaylist(
                            songs = songs,
                            startIndex = 0
                        )
                    }
                )
            }
            items(
                items = songs,
                key = { it.songId }
            ) { song ->
                MusicListItem(
                    songEntity = song,
                    currentPlaying = currentPlaying,
                    onClick = {
                        playerViewModel.setAndPlayPlaylist(
                            songs = songs,
                            startIndex = songs.indexOfFirst { it.songId == song.songId }
                        )
                    },
                    onActionClick = {
                        showActionDialog = true
                        selectedSong = song
                    }
                )
            }
        }
    }

}

@Composable
fun PlaylistHeader(
    modifier: Modifier = Modifier,
    artworkUri: Uri? = null,
    playlistTitle: String,
    playlistDescription: String?,
    songCount: Int,
    onPlayAllClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 16.dp)
    ) {

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(artworkUri)
                .crossfade(true)
                .placeholder(R.drawable.ic_album_24px)
                .build(),
            contentDescription = playlistDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(8.dp))
                .align(Alignment.CenterVertically)
        )

        Column(
            modifier = Modifier
                .padding(start = 16.dp)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                text = playlistTitle,
                style = SaltTheme.textStyles.main,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = SaltTheme.colors.text,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = playlistDescription?: stringResource(R.string.playlist_default_description),
                style = SaltTheme.textStyles.sub,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = SaltTheme.colors.subText,
                modifier = Modifier.padding(bottom = 2.dp)
            )
            Text(
                text = stringResource(R.string.song_count, songCount),
                style = SaltTheme.textStyles.sub,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = SaltTheme.colors.subText,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Button(
                text = stringResource(R.string.play_all),
                onClick = {
                    onPlayAllClick()
                },
                modifier = Modifier
                    .align(Alignment.Start)
            )
        }
    }
}