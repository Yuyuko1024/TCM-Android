package net.hearnsoft.tcm.compose.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import net.hearnsoft.tcm.compose.data.database.entities.PlaylistEntity
import net.hearnsoft.tcm.compose.data.database.entities.PlaylistSongCrossRef
import net.hearnsoft.tcm.compose.data.database.entities.PlaylistWithSongs
import net.hearnsoft.tcm.compose.data.database.entities.SongEntity
import net.hearnsoft.tcm.compose.data.database.entities.SongWithPlaylists

@Dao
interface PlaylistDao {

    // === 歌单基本操作 ===
    @Query("SELECT * FROM playlists ORDER BY playlist_name ASC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE playlist_id = :playlistId")
    suspend fun getPlaylistById(playlistId: Long): PlaylistEntity?

    @Query("SELECT * FROM playlists WHERE playlist_name = :name")
    suspend fun getPlaylistByName(name: String): PlaylistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists")
    suspend fun deleteAllPlaylists()

    @Query("SELECT COUNT(*) FROM playlists")
    suspend fun getPlaylistCount(): Int

    // === 歌单与歌曲关联操作 ===

    /**
     * 获取歌单及其包含的所有歌曲
     */
    @Transaction
    @Query("SELECT * FROM playlists WHERE playlist_id = :playlistId")
    fun getPlaylistWithSongs(playlistId: Long): Flow<PlaylistWithSongs?>

    /**
     * 获取歌单的第一首歌曲（按添加时间倒序，即最新添加的）
     */
    @Query("""
        SELECT songs.* FROM songs
        INNER JOIN playlist_song_cross_ref ON songs.song_id = playlist_song_cross_ref.song_id
        WHERE playlist_song_cross_ref.playlist_id = :playlistId
        ORDER BY playlist_song_cross_ref.date_added DESC
        LIMIT 1
    """)
    suspend fun getFirstSongInPlaylist(playlistId: Long): SongEntity?

    /**
     * 获取歌单内的歌曲(按添加时间倒序排序)
     */
    @Query("""
        SELECT songs.* FROM songs
        INNER JOIN playlist_song_cross_ref ON songs.song_id = playlist_song_cross_ref.song_id
        WHERE playlist_song_cross_ref.playlist_id = :playlistId
        ORDER BY playlist_song_cross_ref.date_added DESC
    """)
    fun getSongsInPlaylist(playlistId: Long): Flow<List<SongEntity>>

    /**
     * 获取歌单内喜欢的歌曲(按添加时间倒序)
     */
    @Query("""
        SELECT songs.* FROM songs
        INNER JOIN playlist_song_cross_ref ON songs.song_id = playlist_song_cross_ref.song_id
        WHERE playlist_song_cross_ref.playlist_id = :playlistId
        AND songs.is_favorite = 1
        ORDER BY playlist_song_cross_ref.date_added DESC
    """)
    fun getFavoriteSongsInPlaylist(playlistId: Long): Flow<List<SongEntity>>

    /**
     * 获取歌曲所属的所有歌单
     */
    @Transaction
    @Query("SELECT * FROM songs WHERE song_id = :songId")
    fun getSongWithPlaylists(songId: Long): Flow<SongWithPlaylists?>

    /**
     * 添加歌曲到歌单
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSongToPlaylist(crossRef: PlaylistSongCrossRef): Long

    /**
     * 从歌单移除歌曲
     */
    @Query("DELETE FROM playlist_song_cross_ref WHERE playlist_id = :playlistId AND song_id = :songId")
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)

    /**
     * 检查歌曲是否在歌单中
     */
    @Query("SELECT COUNT(*) FROM playlist_song_cross_ref WHERE playlist_id = :playlistId AND song_id = :songId")
    suspend fun isSongInPlaylist(playlistId: Long, songId: Long): Int

    /**
     * 获取歌单中歌曲的数量
     */
    @Query("SELECT COUNT(*) FROM playlist_song_cross_ref WHERE playlist_id = :playlistId")
    suspend fun getSongCountInPlaylist(playlistId: Long): Int

    /**
     * 更新歌曲在歌单中的位置
     */
    @Query("UPDATE playlist_song_cross_ref SET position = :position WHERE playlist_id = :playlistId AND song_id = :songId")
    suspend fun updateSongPosition(playlistId: Long, songId: Long, position: Int)

    /**
     * 清空歌单（移除所有歌曲）
     */
    @Query("DELETE FROM playlist_song_cross_ref WHERE playlist_id = :playlistId")
    suspend fun clearPlaylist(playlistId: Long)
}