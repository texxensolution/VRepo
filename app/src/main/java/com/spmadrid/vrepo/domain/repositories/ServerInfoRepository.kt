package com.spmadrid.vrepo.domain.repositories

interface ServerInfoRepository {
    suspend fun isServerRunning(): Boolean
}