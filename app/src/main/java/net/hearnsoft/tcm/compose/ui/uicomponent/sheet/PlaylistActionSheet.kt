package net.hearnsoft.tcm.compose.ui.uicomponent.sheet

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.moriafly.salt.ui.Item
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.dialog.InputDialog
import com.moriafly.salt.ui.dialog.YesNoDialog
import net.hearnsoft.tcm.compose.R
import net.hearnsoft.tcm.compose.data.database.entities.PlaylistEntity
import net.hearnsoft.tcm.compose.ui.viewmodel.PlaylistViewModel

@UnstableSaltUiApi
@ExperimentalMaterial3Api
@Composable
fun PlaylistActionSheet(
    playlist: PlaylistEntity?,
    playlistViewModel: PlaylistViewModel,
    onDismissRequest: () -> Unit = {},
) {
    var showEditTitleDialog by remember { mutableStateOf(false) }
    var titleText by remember { mutableStateOf(playlist?.playlistName ?: "") }

    var showEditContentDialog by remember { mutableStateOf(false) }
    var contentText by remember { mutableStateOf(playlist?.description ?: "") }

    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showEditTitleDialog) {
        playlist?.let {
            InputDialog(
                title = stringResource(R.string.playlist_action_edit_title),
                text = titleText,
                onChange = {
                    titleText = it
                },
                onConfirm = {
                    val updatedPlaylist = it.copy(playlistName = titleText)
                    playlistViewModel.updatePlaylist(updatedPlaylist)
                    showEditTitleDialog = false
                    onDismissRequest()
                },
                onDismissRequest = {
                    showEditTitleDialog = false
                    onDismissRequest()
                }
            )
        }
    }

    if (showEditContentDialog) {
        playlist?.let {
            InputDialog(
                title = stringResource(R.string.playlist_action_edit_description),
                text = contentText,
                onChange = {
                    contentText = it
                },
                onConfirm = {
                    val updatedPlaylist = it.copy(description = contentText)
                    playlistViewModel.updatePlaylist(updatedPlaylist)
                    showEditContentDialog = false
                    onDismissRequest()
                },
                onDismissRequest = {
                    showEditContentDialog = false
                    onDismissRequest()
                }
            )
        }
    }

    if (showDeleteDialog) {
        playlist?.let {
            YesNoDialog(
                title = stringResource(R.string.playlist_action_delete),
                content = it.playlistName,
                onConfirm = {
                    playlistViewModel.deletePlaylist(it)
                    showDeleteDialog = false
                    onDismissRequest()
                },
                onDismissRequest = {
                    showDeleteDialog = false
                    onDismissRequest()
                }
            )
        }
    }

    BottomSheetDialog(
        modifier = Modifier,
        onDismissRequest = onDismissRequest,
    ) {
        RoundedColumn {
            Item(
                text = stringResource(R.string.playlist_action_edit_title),
                onClick = {
                    showEditTitleDialog = true
                }
            )
            Item(
                text = stringResource(R.string.playlist_action_edit_description),
                onClick = {
                    showEditContentDialog = true
                }
            )
            Item(
                text = stringResource(R.string.playlist_action_delete),
                onClick = {
                    showDeleteDialog = true
                }
            )
        }
    }
}