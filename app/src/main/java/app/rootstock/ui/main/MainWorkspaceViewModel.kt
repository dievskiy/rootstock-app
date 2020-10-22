package app.rootstock.ui.main

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.rootstock.data.user.User
import app.rootstock.data.user.UserRepository
import kotlinx.coroutines.launch

class MainWorkspaceViewModel @ViewModelInject constructor(private val repository: UserRepository) :
    ViewModel() {

    val user = repository.getUser()
}