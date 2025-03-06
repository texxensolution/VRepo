package com.spmadrid.vrepo.domain.services

interface WebSocketService {
    suspend fun connect(): Unit
    suspend fun disconnect(): Unit
}