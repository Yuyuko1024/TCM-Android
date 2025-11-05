package net.hearnsoft.tcm.compose.ui.uicomponent

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.TextButton
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.dialog.BasicDialog
import com.moriafly.salt.ui.dialog.DialogTitle
import com.moriafly.salt.ui.dialog.InputDialog
import com.moriafly.salt.ui.outerPadding
import net.hearnsoft.tcm.compose.R
import net.hearnsoft.tcm.compose.data.database.entities.PlaylistEntity
import net.hearnsoft.tcm.compose.data.database.entities.SongEntity
import net.hearnsoft.tcm.compose.ui.viewmodel.PlaylistViewModel

@UnstableSaltUiApi
@Composable
fun AddToPlaylistDialog(
    modifier: Modifier = Modifier,
    playlistViewModel: PlaylistViewModel = hiltViewModel(),
    selectedSongEntity: SongEntity,
    onDismissRequest: () -> Unit = {},
) {
    val playlists = playlistViewModel.allPlaylists.collectAsState().value

    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    if (showCreatePlaylistDialog) {
        InputDialog(
            title = stringResource(R.string.new_playlist_dialog_title),
            hint = stringResource(R.string.new_playlist_dialog_hint),
            text = newPlaylistName,
            onChange = {
                newPlaylistName = it
            },
            onConfirm = {
                playlistViewModel.createPlaylist(newPlaylistName)
                showCreatePlaylistDialog = false

            },
            onDismissRequest = {
                showCreatePlaylistDialog = false
            }
        )
    }

    BasicDialog(
        onDismissRequest = onDismissRequest
    ) {
        DialogTitle(text = stringResource(R.string.add_to_song_playlist))

        LazyColumn(Modifier.fillMaxWidth()) {
            items(
                items = playlists,
                key = { it.playlistId }
            ) { playlist ->
                PlaylistDialogListItem(
                    playlistEntity = playlist,
                    onClick = {
                        playlistViewModel.addSongToPlaylist(
                            playlistId = playlist.playlistId,
                            songId = selectedSongEntity.songId
                        )
                        onDismissRequest()
                    }
                )
            }
        }

        Row(
            modifier = Modifier.outerPadding()
        ) {
            TextButton(
                onClick = {
                    showCreatePlaylistDialog = true
                },
                modifier = Modifier
                    .weight(1f),
                text = stringResource(R.string.playlist_action_new)
            )
        }

    }
}

@Composable
private fun PlaylistDialogListItem(
    playlistEntity: PlaylistEntity,
    onClick: () -> Unit = { }
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
        .clickable { onClick() }
    ) {
        Text(
            text = playlistEntity.playlistName ?: stringResource(R.string.playlist_name_not_defined),
            style = SaltTheme.textStyles.main,
            color = SaltTheme.colors.text,
            modifier = Modifier.padding(vertical = 2.dp)
        )
        Text(
            text = stringResource(R.string.song_count, playlistEntity.songCount),
            style = SaltTheme.textStyles.sub,
            color = SaltTheme.colors.subText
        )
    }
}

