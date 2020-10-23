package app.rootstock.di.modules


import android.content.Context
import app.rootstock.api.UserLogInService
import app.rootstock.api.UserSignUpService
import app.rootstock.data.db.AppDatabase
import app.rootstock.data.network.LiveDataCallAdapterFactory
import app.rootstock.data.token.TokenDao
import app.rootstock.data.user.UserDao
import app.rootstock.data.user.UserRepository
import app.rootstock.ui.login.LogInLoader
import app.rootstock.ui.signup.AccountRepository
import app.rootstock.ui.signup.SignUpLoader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
class AppModule {

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    fun provideUserDao(appDatabase: AppDatabase): UserDao {
        return appDatabase.userDao()
    }

    @Provides
    fun provideTokenDao(appDatabase: AppDatabase): TokenDao {
        return appDatabase.tokenDao()
    }

    @Singleton
    @Provides
    fun provideRegisterRepository(
        signUpLoader: SignUpLoader,
        logInLoader: LogInLoader
    ): AccountRepository {
        return AccountRepository(signUpLoader, logInLoader)
    }

    @Provides
    fun provideSignUpLoader(signUpService: UserSignUpService): SignUpLoader {
        return SignUpLoader(signUpService)
    }

    @Singleton
    @Provides
    fun provideUserRepository(userDao: UserDao, tokenDao: TokenDao): UserRepository {
        return UserRepository(userDao = userDao, tokenDao = tokenDao)
    }

//    @Singleton
//    @Provides
//    fun provideRetrofit(): Retrofit =
//        Retrofit.Builder()
//            .baseUrl("http://0.0.0.0:8080/")
//            .build()

    @Singleton
    @Provides
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("http://192.168.43.116:8000")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(LiveDataCallAdapterFactory())
            .client(client)
            .build()

    @Provides
    fun getClient(): OkHttpClient =
        OkHttpClient.Builder()
            .callTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .connectTimeout(15, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val requestBuilder: Request.Builder = chain.request().newBuilder()
                requestBuilder.header("Content-Type", "application/json")
                chain.proceed(requestBuilder.build())
            }
            .build()

    @Provides
    fun provideUserSignUp(retrofit: Retrofit): UserSignUpService {
        return retrofit.create(UserSignUpService::class.java)
    }

    @Provides
    fun provideUserLogInService(retrofit: Retrofit): UserLogInService {
        return retrofit.create(UserLogInService::class.java)
    }
}