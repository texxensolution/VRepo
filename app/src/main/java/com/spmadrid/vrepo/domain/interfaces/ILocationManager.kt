package com.spmadrid.vrepo.domain.interfaces

interface ILocationManager {
    suspend fun getLocation()
}