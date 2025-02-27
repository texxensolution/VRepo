package com.spmadrid.vrepo.data.services

import com.spmadrid.vrepo.domain.repositories.ServerInfoRepository
import com.spmadrid.vrepo.domain.services.ServerInfoService
import javax.inject.Inject

class ServerInfoServiceImpl @Inject constructor(
    private val serverInfoRepository: ServerInfoRepository
)  : ServerInfoService{
    override suspend fun isServerRunning(): Boolean {
        return serverInfoRepository.isServerRunning()
    }
}