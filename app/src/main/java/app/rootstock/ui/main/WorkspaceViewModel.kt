package app.rootstock.ui.main

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import app.rootstock.adapters.WorkspaceEventHandler
import app.rootstock.data.channel.Channel
import app.rootstock.data.network.ResponseResult
import app.rootstock.data.prefs.CacheClass
import app.rootstock.data.prefs.SharedPrefsController
import app.rootstock.data.result.Event
import app.rootstock.data.user.UserRepository
import app.rootstock.data.workspace.Workspace
import app.rootstock.data.workspace.WorkspaceWithChildren
import app.rootstock.ui.channels.ChannelRepository
import app.rootstock.ui.workspace.WorkspaceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


sealed class WorkspaceEvent {
    class OpenWorkspace(val workspaceId: String) : WorkspaceEvent()
    object NoUser : WorkspaceEvent()
    object Error : WorkspaceEvent()
    object NavigateToRoot : WorkspaceEvent()
    class Backdrop(val close: Boolean) : WorkspaceEvent()
}

enum class ChannelEvent {
    EDIT_OPEN, EDIT_EXIT, UPDATE_FAILED
}

enum class PagerEvent {
    PAGER_SCROLLED
}


@ExperimentalCoroutinesApi
class WorkspaceViewModel @ViewModelInject constructor(
    private val userRepository: UserRepository,
    private val workspaceRepository: WorkspaceRepository,
    private val channelRepository: ChannelRepository,
    private val spController: SharedPrefsController,
) :
    ViewModel(), WorkspaceEventHandler {

    private val _workspace = MutableLiveData<WorkspaceWithChildren>()

    private val _isAtRoot = MutableLiveData<Boolean>()
    val isAtRoot: LiveData<Boolean> get() = _isAtRoot

    var hasSwiped: Boolean = false

    private val _favouriteShowed = MutableLiveData(false)
    val favouriteShowed: LiveData<Boolean> get() = _favouriteShowed

    val workspace: LiveData<WorkspaceWithChildren>
        get() = _workspace

    val workspacesChildren: LiveData<List<Workspace>> = _workspace.map { workspaceWithChildren ->
        workspaceWithChildren ?: return@map listOf()
        workspaceWithChildren.children
    }

    val channels: LiveData<MutableList<Channel>>
        get() = _channels

    private val _channels: MutableLiveData<MutableList<Channel>> = MutableLiveData(mutableListOf())

    private val _eventWorkspace = MutableLiveData<Event<WorkspaceEvent>>()
    val eventWorkspace: LiveData<Event<WorkspaceEvent>> get() = _eventWorkspace

    private val _eventChannel = MutableLiveData<Event<ChannelEvent>>()
    val eventEdit: LiveData<Event<ChannelEvent>> get() = _eventChannel

    private val _pagerPosition = MutableLiveData<Int>()
    val pagerPosition: LiveData<Int> get() = _pagerPosition

    private val _pagerScrolled = MutableLiveData<Event<PagerEvent>>()
    val pagerScrolled: LiveData<Event<PagerEvent>> get() = _pagerScrolled

    fun editDialogOpened() {
        _eventChannel.value = Event(ChannelEvent.EDIT_OPEN)
    }

    fun editChannelStop() {
        _eventChannel.value = Event(ChannelEvent.EDIT_EXIT)
    }


    fun loadWorkspace(workspaceId: String?) {
        _workspace.value = null
        var id = workspaceId
        viewModelScope.launch {
            if (workspaceId == null) {
                id = userRepository.getUserId()

                if (id == null) {
                    _eventWorkspace.postValue(Event(WorkspaceEvent.NoUser))
                    return@launch
                }

            }
            id?.let { wsId ->
                val update = spController.shouldUpdateCache(CacheClass.Workspace(wsId))
                workspaceRepository.getWorkspace(wsId, update)
                    .collect {
                        when (it) {
                            is ResponseResult.Success -> {
                                it.data ?: return@collect
                                _workspace.value =
                                    it.data.apply { children.sortedBy { child -> child.createdAt } }
                                _channels.value = it.data.channels.toMutableList().apply {
                                    sortBy { channel -> channel.lastUpdate }
                                }.asReversed()
                                spController.updateCacheSettings(CacheClass.Workspace(wsId), false)
                            }
                            is ResponseResult.Error -> {
                                _eventWorkspace.postValue(Event(WorkspaceEvent.Error))
                            }
                        }
                    }
            }
        }
    }

    override fun workspaceClicked(workspaceId: String) {
        pageScrolled()
        // set to null to avoid blinking workspaces
        _workspace.value = null
        _eventWorkspace.value = Event(WorkspaceEvent.OpenWorkspace(workspaceId))
    }

    fun changeFab(position: Int) {
        hasSwiped = true
        _pagerPosition.value = position
    }

    fun navigateToRoot() {
        _eventWorkspace.value = Event(WorkspaceEvent.NavigateToRoot)
    }

    fun updateChannel(channel: Channel) {
        if (channel.isValid()) {
            val oldValue = _channels.value
            var index: Int? = null
            // if new data is valid, update locally and send request to the server
            _channels.value = _channels.value?.apply {
                val oldChannel = find { channel.channelId == it.channelId }
                // return if there were no changes
                if (oldChannel == channel) return
                // otherwise replace old channel with new one
                oldChannel?.let { c ->
                    indexOf(c).let {
                        index = it
                        this[it] = channel
                    }
                }
            }
            viewModelScope.launch {
                when (val c = channelRepository.updateChannel(channel).first()) {
                    is ResponseResult.Success -> {
                        _channels.value = _channels.value?.apply {
                            index?.let {
                                if (c.data != null)
                                    this[it] = c.data
                            }
                        }
                    }
                    is ResponseResult.Error -> {
                        _channels.value = oldValue
                    }
                }
            }
        } else {
            _eventChannel.value = Event(ChannelEvent.UPDATE_FAILED)
        }
    }

    fun deleteChannel(channelId: Long) {
        val oldValue = _channels.value ?: return
        val wsId = workspace.value?.workspaceId ?: return

        _channels.value = _channels.value?.apply {
            val c = find { it.channelId == channelId }
            remove(c)
        }
        viewModelScope.launch {
            when (channelRepository.deleteChannel(channelId, wsId).first()) {
                is ResponseResult.Success -> {
                }
                is ResponseResult.Error -> {
                    _channels.value = oldValue
                }
            }
        }
    }

    fun pageScrolled() {
        _pagerScrolled.value = Event(PagerEvent.PAGER_SCROLLED)
    }

    fun setRoot(atRoot: Boolean) {
        _isAtRoot.value = atRoot
    }

    fun addChannel(channel: Channel) {
        _channels.value = _channels.value?.apply {
            add(0, channel)
        }
    }

    fun deleteWorkspace(wsId: String) {
        val oldValue = _workspace.value ?: return
        _workspace.value = _workspace.value?.apply {
            children.apply {
                val w = find { it.workspaceId == wsId }
                remove(w)
            }
        }
        viewModelScope.launch {
            when (workspaceRepository.deleteWorkspace(wsId, oldValue.workspaceId).first()) {
                is ResponseResult.Success -> {
                }
                is ResponseResult.Error -> {
                    // in case of error, return to old workspace value
                    _workspace.value = oldValue
                }
            }
        }
    }

    fun resetPager() {
        hasSwiped = false
        _pagerPosition.value = 0
    }

    fun showFavourite() {
        _favouriteShowed.value = true
    }

    fun toggleBackdrop(close: Boolean) {
        _eventWorkspace.value = Event(WorkspaceEvent.Backdrop(close))
    }

    fun createWorkspace(workspace: Workspace) {
        _workspace.value = _workspace.value?.apply {
            children.add(workspace)
        }
    }

    fun updateWorkspace(newWorkspace: Workspace) {
        val oldValue = _workspace.value ?: return
        var index: Int? = null
        _workspace.value = _workspace.value?.apply {
            val oldWorkspace =
                children.find { it.workspaceId == newWorkspace.workspaceId } ?: return@apply
            if (oldWorkspace == newWorkspace) return@apply
            children.indexOf(oldWorkspace).let {
                children[it] = newWorkspace
                index = it
            }
        }
        viewModelScope.launch {
            when (val result =
                workspaceRepository.updateWorkspace(newWorkspace, oldValue.workspaceId).first()) {
                is ResponseResult.Success -> {
                    _workspace.value = _workspace.value?.apply {
                        index?.let {
                            if (result.data != null)
                                children[it] = result.data
                        }
                    }
                }
                is ResponseResult.Error -> {
                    _workspace.value = oldValue
                }
            }
        }

    }


}