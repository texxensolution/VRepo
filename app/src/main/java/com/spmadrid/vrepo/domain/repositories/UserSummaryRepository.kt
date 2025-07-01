package com.spmadrid.vrepo.domain.repositories

import com.spmadrid.vrepo.domain.dtos.UserSummary

interface UserSummaryRepository {
    suspend fun getUserSummary(): UserSummary?
}