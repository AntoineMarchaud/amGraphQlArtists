package com.amarchaud.amgraphqlartist.injection

import com.apollographql.apollo.ApolloClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    /*
    private class AuthorizationInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request().newBuilder()
                .addHeader("Accept-Encoding", "gzip, deflate, br")
                .build()

            return chain.proceed(request)
        }
    }*/

    @Singleton
    @Provides
    fun provideApolloInterface(): ApolloClient =
        ApolloClient.builder()
            .serverUrl("https://graphbrainz.herokuapp.com/")
                /*
            .okHttpClient(OkHttpClient.Builder()
                .addInterceptor(AuthorizationInterceptor())
                .build()
            )*/
            .build()
}