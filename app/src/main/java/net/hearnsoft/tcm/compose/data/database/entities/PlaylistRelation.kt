package net.hearnsoft.tcm.compose.data.database.entities

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

/**
 * 歌单及其包含的歌曲
 */
data class PlaylistWithSongs(
    @Embedded val playlist: PlaylistEntity,
    @Relation(
        parentColumn = "playlist_id",
        entityColumn = "song_id",
        associateBy = Junction(PlaylistSongCrossRef::class)
    )
    val songs: List<SongEntity>
)

/**
 * 歌曲及其所属的歌单
 */
data class SongWithPlaylists(
    @Embedded val song: SongEntity,
    @Relation(
        parentColumn = "song_id",
        entityColumn = "playlist_id",
        associateBy = Junction(PlaylistSongCrossRef::class)
    )
    val playlists: List<PlaylistEntity>
)