package com.amarchaud.amgraphqlartist.injection


import android.content.Context
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.http.ApolloHttpCache
import com.apollographql.apollo.cache.http.DiskLruHttpCacheStore
import com.apollographql.apollo.subscription.WebSocketSubscriptionTransport
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    companion object {
        // Size in bytes of the cache
        const val size: Long = 1024 * 1024 * 10
    }

    // display log
    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    @Singleton
    @Provides
    fun provideApolloInterface(@ApplicationContext appContext: Context): ApolloClient =
        ApolloClient.builder()
            .serverUrl("https://graphbrainz.herokuapp.com/")
            .httpCache(
                ApolloHttpCache(
                    DiskLruHttpCacheStore(
                        File(appContext.cacheDir, "apolloCache"),
                        size
                    )
                )
            )
            .okHttpClient(okHttpClient)
            .build()
}