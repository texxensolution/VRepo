package com.spmadrid.vrepo.domain.services

interface ServerInfoService {
    suspend fun isServerRunning(): Boolean
}