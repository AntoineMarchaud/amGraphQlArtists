package com.amarchaud.amgraphqlartist.injection

import com.apollographql.apollo.ApolloClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {
    @Singleton
    @Provides
    fun provideApolloInterface(): ApolloClient =
        ApolloClient.builder()
            .serverUrl("https://graphbrainz.herokuapp.com/")
            .build()
}