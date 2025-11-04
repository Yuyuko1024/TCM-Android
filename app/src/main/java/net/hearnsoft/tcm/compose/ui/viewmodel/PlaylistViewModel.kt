package net.hearnsoft.tcm.compose.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import net.hearnsoft.tcm.compose.data.database.entities.PlaylistEntity
import net.hearnsoft.tcm.compose.data.database.entities.SongEntity
import net.hearnsoft.tcm.compose.data.repository.MusicRepository
import net.hearnsoft.tcm.compose.domain.model.playlist.PlaylistWithCover
import net.hearnsoft.tcm.compose.utils.Logger
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val musicRepository: MusicRepository
) : ViewModel() {
    private val TAG = "PlaylistViewModel"

    // === 歌单列表 ===
    private val _allPlaylists = MutableStateFlow<List<PlaylistEntity>>(emptyList())
    val allPlaylists: StateFlow<List<PlaylistEntity>> = _allPlaylists.asStateFlow()

    // === 当前选中的歌单 ===
    private val _currentPlaylist = MutableStateFlow<PlaylistEntity?>(null)
    val currentPlaylist: StateFlow<PlaylistEntity?> = _currentPlaylist.asStateFlow()

    // === 当前歌单的歌曲 ===
    private val _currentPlaylistSongs = MutableStateFlow<List<SongEntity>>(emptyList())
    val currentPlaylistSongs: StateFlow<List<SongEntity>> = _currentPlaylistSongs.asStateFlow()

    // === 歌单封面数据 ===
    private val _playlistsWithCovers = MutableStateFlow<List<PlaylistWithCover>>(emptyList())
    val playlistsWithCovers: StateFlow<List<PlaylistWithCover>> = _playlistsWithCovers.asStateFlow()

    // === 喜欢的歌曲封面 ===
    private val _favoriteCoverUri = MutableStateFlow<Uri?>(null)
    val favoriteCoverUri: StateFlow<Uri?> = _favoriteCoverUri.asStateFlow()

    // === UI 状态 ===
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadAllPlaylists()
        observeFavoriteCover()
    }

    // === 歌单基本操作 ===

    /**
     * 加载所有歌单及其封面
     */
    fun loadAllPlaylists() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                musicRepository.getAllPlaylists().collectLatest { playlists ->
                    _allPlaylists.value = playlists
                    Logger.debug(TAG, "加载了 ${playlists.size} 个歌单")

                    // 加载每个歌单的封面
                    loadPlaylistCovers(playlists)
                }
            } catch (e: Exception) {
                Logger.err(TAG, "加载歌单失败: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 加载歌单封面
     */
    private fun loadPlaylistCovers(playlists: List<PlaylistEntity>) {
        viewModelScope.launch {
            try {
                val playlistsWithCovers = playlists.map { playlist ->
                    val firstSong = musicRepository.getFirstSongInPlaylist(playlist.playlistId)
                    PlaylistWithCover(
                        playlist = playlist,
                        coverArtworkUri = firstSong?.artworkUri,
                        songCount = playlist.songCount
                    )
                }
                _playlistsWithCovers.value = playlistsWithCovers
                Logger.debug(TAG, "加载了 ${playlistsWithCovers.size} 个歌单封面")
            } catch (e: Exception) {
                Logger.err(TAG, "加载歌单封面失败: ${e.message}")
            }
        }
    }

    /**
     * 持续观察喜欢的歌曲封面变化
     */
    private fun observeFavoriteCover() {
        viewModelScope.launch {
            try {
                musicRepository.getFavoriteSongs().collectLatest { favoriteSongs ->
                    // 获取最新添加的喜欢歌曲的封面
                    _favoriteCoverUri.value = favoriteSongs.firstOrNull()?.artworkUri
                    Logger.debug(TAG, "更新喜欢的歌曲封面: ${_favoriteCoverUri.value}")
                }
            } catch (e: Exception) {
                Logger.err(TAG, "观察喜欢的歌曲封面失败: ${e.message}")
            }
        }
    }

    /**
     * 加载喜欢的歌曲封面（用于显示"我喜欢的音乐"封面）
     */
    fun loadFavoriteCover() {
        viewModelScope.launch {
            try {
                val latestFavoriteSong = musicRepository.getLatestFavoriteSong()
                _favoriteCoverUri.value = latestFavoriteSong?.artworkUri
                Logger.debug(TAG, "加载喜欢的歌曲封面: ${latestFavoriteSong?.artworkUri}")
            } catch (e: Exception) {
                Logger.err(TAG, "加载喜欢的歌曲封面失败: ${e.message}")
            }
        }
    }

    /**
     * 刷新歌单封面（在歌曲添加/移除后调用）
     */
    fun refreshPlaylistCover(playlistId: Long) {
        viewModelScope.launch {
            try {
                if (playlistId == 0L) {
                    // 刷新喜欢的歌曲封面
                    loadFavoriteCover()
                } else {
                    // 刷新指定歌单的封面
                    val firstSong = musicRepository.getFirstSongInPlaylist(playlistId)
                    val updatedList = _playlistsWithCovers.value.map { playlistWithCover ->
                        if (playlistWithCover.playlist.playlistId == playlistId) {
                            playlistWithCover.copy(coverArtworkUri = firstSong?.artworkUri)
                        } else {
                            playlistWithCover
                        }
                    }
                    _playlistsWithCovers.value = updatedList
                }
            } catch (e: Exception) {
                Logger.err(TAG, "刷新歌单封面失败: ${e.message}")
            }
        }
    }

    /**
     * 创建新歌单
     */
    fun createPlaylist(name: String, description: String? = null): Long {
        var playlistId = -1L
        viewModelScope.launch {
            try {
                val playlist = PlaylistEntity(
                    playlistName = name,
                    description = description
                )
                playlistId = musicRepository.insertPlaylist(playlist)
                Logger.debug(TAG, "创建歌单成功: $name, ID: $playlistId")
            } catch (e: Exception) {
                Logger.err(TAG, "创建歌单失败: ${e.message}")
            }
        }
        return playlistId
    }

    /**
     * 更新歌单信息
     */
    fun updatePlaylist(playlist: PlaylistEntity) {
        viewModelScope.launch {
            try {
                musicRepository.updatePlaylist(playlist)

                // 刷新封面
                refreshPlaylistCover(playlist.playlistId)
                Logger.debug(TAG, "更新歌单成功: ${playlist.playlistName}")
            } catch (e: Exception) {
                Logger.err(TAG, "更新歌单失败: ${e.message}")
            }
        }
    }

    /**
     * 删除歌单
     */
    fun deletePlaylist(playlist: PlaylistEntity) {
        viewModelScope.launch {
            try {
                musicRepository.deletePlaylist(playlist)
                Logger.debug(TAG, "删除歌单成功: ${playlist.playlistName}")

                // 如果删除的是当前选中的歌单，清空选中状态
                if (_currentPlaylist.value?.playlistId == playlist.playlistId) {
                    _currentPlaylist.value = null
                    _currentPlaylistSongs.value = emptyList()
                    _playlistsWithCovers.value = emptyList()
                }
            } catch (e: Exception) {
                Logger.err(TAG, "删除歌单失败: ${e.message}")
            }
        }
    }

    // === 歌单与歌曲关联操作 ===

    /**
     * 选择歌单并加载其歌曲
     */
    fun selectPlaylist(playlistId: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // 加载歌单信息
                val playlist = musicRepository.getPlaylistById(playlistId)
                _currentPlaylist.value = playlist

                // 加载歌单的所有歌曲
                musicRepository.getSongsInPlaylist(playlistId).collectLatest { songs ->
                    _currentPlaylistSongs.value = songs
                    Logger.debug(TAG, "加载歌单歌曲: ${songs.size} 首")
                }
            } catch (e: Exception) {
                Logger.err(TAG, "选择歌单失败: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 添加歌曲到歌单
     */
    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            try {
                if (musicRepository.isSongInPlaylist(playlistId, songId)) {
                    Logger.warn(TAG, "歌曲已在歌单中")
                    return@launch
                }

                val position = musicRepository.getSongCountInPlaylist(playlistId)
                musicRepository.addSongToPlaylist(playlistId, songId, position)
                Logger.debug(TAG, "添加歌曲到歌单成功")

                // 刷新封面
                refreshPlaylistCover(playlistId)
            } catch (e: Exception) {
                Logger.err(TAG, "添加歌曲到歌单失败: ${e.message}")
            }
        }
    }

    /**
     * 批量添加歌曲到歌单
     */
    fun addSongsToPlaylist(playlistId: Long, songIds: List<Long>) {
        viewModelScope.launch {
            try {
                var position = musicRepository.getSongCountInPlaylist(playlistId)

                songIds.forEach { songId ->
                    if (!musicRepository.isSongInPlaylist(playlistId, songId)) {
                        musicRepository.addSongToPlaylist(playlistId, songId, position++)
                    }
                }

                Logger.debug(TAG, "批量添加 ${songIds.size} 首歌曲到歌单")
            } catch (e: Exception) {
                Logger.err(TAG, "批量添加歌曲失败: ${e.message}")
            }
        }
    }

    /**
     * 从歌单移除歌曲
     */
    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            try {
                musicRepository.removeSongFromPlaylist(playlistId, songId)
                Logger.debug(TAG, "从歌单移除歌曲成功")

                // 刷新封面
                refreshPlaylistCover(playlistId)
            } catch (e: Exception) {
                Logger.err(TAG, "从歌单移除歌曲失败: ${e.message}")
            }
        }
    }

    /**
     * 清空歌单（移除所有歌曲）
     */
    fun clearPlaylist(playlistId: Long) {
        viewModelScope.launch {
            try {
                musicRepository.clearPlaylist(playlistId)
                Logger.debug(TAG, "清空歌单成功")

                // 如果清空的是当前选中的歌单，清空歌曲列表
                if (_currentPlaylist.value?.playlistId == playlistId) {
                    _currentPlaylistSongs.value = emptyList()
                }
            } catch (e: Exception) {
                Logger.err(TAG, "清空歌单失败: ${e.message}")
            }
        }
    }

    /**
     * 更新歌曲在歌单中的位置（用于拖拽排序）
     */
    fun updateSongPosition(playlistId: Long, songId: Long, newPosition: Int) {
        viewModelScope.launch {
            try {
                musicRepository.updateSongPosition(playlistId, songId, newPosition)
                Logger.debug(TAG, "更新歌曲位置成功")
            } catch (e: Exception) {
                Logger.err(TAG, "更新歌曲位置失败: ${e.message}")
            }
        }
    }

    /**
     * 检查歌曲是否在歌单中
     */
    suspend fun isSongInPlaylist(playlistId: Long, songId: Long): Boolean {
        return try {
            musicRepository.isSongInPlaylist(playlistId, songId)
        } catch (e: Exception) {
            Logger.err(TAG, "检查歌曲是否在歌单中失败: ${e.message}")
            false
        }
    }

    /**
     * 获取歌曲所属的歌单列表
     */
    fun getPlaylistsForSong(songId: Long): StateFlow<List<PlaylistEntity>> {
        val playlists = MutableStateFlow<List<PlaylistEntity>>(emptyList())
        viewModelScope.launch {
            try {
                musicRepository.getSongWithPlaylists(songId).collectLatest { songWithPlaylists ->
                    playlists.value = songWithPlaylists?.playlists ?: emptyList()
                }
            } catch (e: Exception) {
                Logger.err(TAG, "获取歌曲的歌单列表失败: ${e.message}")
            }
        }
        return playlists.asStateFlow()
    }

}