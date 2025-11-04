package net.hearnsoft.tcm.compose.data.repository

import androidx.media3.common.MediaItem
import kotlinx.coroutines.flow.Flow
import net.hearnsoft.tcm.compose.data.database.entities.AlbumEntity
import net.hearnsoft.tcm.compose.data.database.entities.ArtistEntity
import net.hearnsoft.tcm.compose.data.database.entities.PlaylistEntity
import net.hearnsoft.tcm.compose.data.database.entities.PlaylistWithSongs
import net.hearnsoft.tcm.compose.data.database.entities.SongEntity
import net.hearnsoft.tcm.compose.data.database.entities.SongWithPlaylists

/**
 * 音乐仓库抽象基类
 * 定义所有音乐数据源的通用接口
 */
abstract class MusicRepository {

    // === 歌曲相关操作 ===
    abstract fun getAllSongs(): Flow<List<SongEntity>>
    abstract suspend fun getSongById(songId: Long): SongEntity?
    abstract suspend fun getSongByMediaStoreId(mediaStoreId: Long): SongEntity?
    abstract fun getSongsByAlbum(albumId: Long): Flow<List<SongEntity>>
    abstract fun getSongsByArtist(artistId: Long): Flow<List<SongEntity>>
    abstract fun getFavoriteSongs(): Flow<List<SongEntity>>

    abstract suspend fun updateFavoriteStatus(
        songId: Long,
        isFavorite: Boolean,
        timestamp: Long = System.currentTimeMillis()
    )
    abstract fun getMostPlayedSongs(limit: Int = 50): Flow<List<SongEntity>>
    abstract fun getRecentlyPlayedSongs(limit: Int = 50): Flow<List<SongEntity>>

    abstract suspend fun insertSong(song: SongEntity): Long
    abstract suspend fun insertSongs(songs: List<SongEntity>): List<Long>
    abstract suspend fun updateSong(song: SongEntity)
    abstract suspend fun deleteSong(song: SongEntity)
    abstract suspend fun deleteAllSongs()
    abstract suspend fun getSongCount(): Int

    abstract suspend fun incrementPlayCount(songId: Long, timestamp: Long = System.currentTimeMillis())
    abstract suspend fun updateFavoriteStatus(songId: Long, isFavorite: Boolean)

    // === 专辑相关操作 ===
    abstract fun getAllAlbums(): Flow<List<AlbumEntity>>
    abstract suspend fun getAlbumById(albumId: Long): AlbumEntity?
    abstract suspend fun getAlbumByMediaStoreId(mediaStoreAlbumId: Long): AlbumEntity?
    abstract suspend fun getAlbumsByAlbumArtist(albumArtist: String): Flow<List<AlbumEntity>>

    abstract suspend fun insertAlbum(album: AlbumEntity): Long
    abstract suspend fun insertAlbums(albums: List<AlbumEntity>): List<Long>
    abstract suspend fun updateAlbum(album: AlbumEntity)
    abstract suspend fun deleteAlbum(album: AlbumEntity)
    abstract suspend fun deleteAllAlbums()

    // === 艺术家相关操作 ===
    abstract fun getAllArtists(): Flow<List<ArtistEntity>>
    abstract suspend fun getArtistById(artistId: Long): ArtistEntity?
    abstract suspend fun getArtistByName(artistName: String): ArtistEntity?

    abstract suspend fun insertArtist(artist: ArtistEntity): Long
    abstract suspend fun insertArtists(artists: List<ArtistEntity>): List<Long>
    abstract suspend fun updateArtist(artist: ArtistEntity)
    abstract suspend fun deleteArtist(artist: ArtistEntity)
    abstract suspend fun deleteAllArtists()

    // === 歌单相关操作 ===
    abstract fun getAllPlaylists(): Flow<List<PlaylistEntity>>
    abstract suspend fun getPlaylistById(playlistId: Long): PlaylistEntity?
    abstract suspend fun getPlaylistByName(playlistName: String): PlaylistEntity?
    abstract suspend fun insertPlaylist(playlist: PlaylistEntity): Long
    abstract suspend fun updatePlaylist(playlist: PlaylistEntity)
    abstract suspend fun deletePlaylist(playlist: PlaylistEntity)
    abstract suspend fun deleteAllPlaylists()
    abstract suspend fun getPlaylistCount(): Int

    // === 歌单与歌曲关联操作 ===
    abstract fun getPlaylistWithSongs(playlistId: Long): Flow<PlaylistWithSongs?>
    abstract fun getSongsInPlaylist(playlistId: Long): Flow<List<SongEntity>>
    abstract fun getFavoriteSongsInPlaylist(playlistId: Long): Flow<List<SongEntity>>
    abstract fun getSongWithPlaylists(songId: Long): Flow<SongWithPlaylists?>
    abstract suspend fun addSongToPlaylist(playlistId: Long, songId: Long, position: Int = 0)
    abstract suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)
    abstract suspend fun isSongInPlaylist(playlistId: Long, songId: Long): Boolean
    abstract suspend fun getSongCountInPlaylist(playlistId: Long): Int
    abstract suspend fun updateSongPosition(playlistId: Long, songId: Long, position: Int)
    abstract suspend fun clearPlaylist(playlistId: Long)

    // === 歌单封面相关 ===
    abstract suspend fun getFirstSongInPlaylist(playlistId: Long): SongEntity?
    abstract suspend fun getLatestFavoriteSong(): SongEntity?

    // === 数据同步操作 ===
    /**
     * 扫描并更新音乐库
     * @param onProgress 进度回调函数
     */
    abstract suspend fun scanAndUpdateLibrary(onProgress: ((String) -> Unit)? = null)

    /**
     * 将 MediaItem 转换为数据库实体
     * 子类可以重写此方法来处理不同来源的数据转换
     */
    abstract suspend fun convertMediaItemToEntities(mediaItem: MediaItem): Triple<SongEntity, AlbumEntity, ArtistEntity>
}