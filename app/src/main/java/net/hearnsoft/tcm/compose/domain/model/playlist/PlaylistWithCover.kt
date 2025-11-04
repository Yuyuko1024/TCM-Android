package net.hearnsoft.tcm.compose.domain.model.playlist

import android.net.Uri
import net.hearnsoft.tcm.compose.data.database.entities.PlaylistEntity

/**
 * 歌单及其封面信息
 */
data class PlaylistWithCover(
    val playlist: PlaylistEntity,
    val coverArtworkUri: Uri? = null,
    val songCount: Int = 0
)