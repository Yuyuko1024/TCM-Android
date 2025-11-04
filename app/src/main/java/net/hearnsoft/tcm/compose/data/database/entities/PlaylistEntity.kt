package net.hearnsoft.tcm.compose.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "playlists",
    indices = [
        Index(value = ["playlist_id"], unique = true),
        Index(value = ["playlist_name"])
    ]
)
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "playlist_id")
    val playlistId: Long = 0,

    @ColumnInfo(name = "playlist_name")
    val playlistName: String,

    @ColumnInfo(name = "song_count")
    val songCount: Int = 0,

    @ColumnInfo(name = "description")
    val description: String? = null
)