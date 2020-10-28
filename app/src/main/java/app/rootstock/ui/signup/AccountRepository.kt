package app.rootstock.ui.signup

import app.rootstock.api.UserSignUpService
import app.rootstock.data.token.TokenNetwork
import app.rootstock.data.user.User
import app.rootstock.data.user.UserWithPassword
import app.rootstock.ui.login.LogInLoader
import retrofit2.Response
import javax.inject.Inject

//
//fun <T, A> performGetOperation(databaseQuery: () -> LiveData<T>,
//                               networkCall: suspend () -> Resource<A>,
//                               saveCallResult: suspend (A) -> Unit): LiveData<Resource<T>> =
//    liveData(Dispatchers.IO) {
//        emit(Resource.loading())
//        val source = databaseQuery.invoke().map { Resource.success(it) }
//        emitSource(source)
//
//        val responseStatus = networkCall.invoke()
//        if (responseStatus.status == SUCCESS) {
//            saveCallResult(responseStatus.data!!)
//
//        } else if (responseStatus.status == ERROR) {
//            emit(Resource.error(responseStatus.message!!))
//            emitSource(source)
//        }
//    }

class SignUpLoader @Inject constructor(private val signUpService: UserSignUpService) {

    suspend fun register(user: SignUpUser): Response<User> {
        return signUpService.createUser(user)
    }

}

class AccountRepository @Inject constructor(
    private val signUpLoader: SignUpLoader,
    private val logInLoader: LogInLoader
) {

    suspend fun register(signUpUser: SignUpUser): Response<User> {
        return signUpLoader.register(signUpUser)
    }

    suspend fun authenticate(user: UserWithPassword): Response<TokenNetwork> {
        return logInLoader.logIn(user)
    }
}