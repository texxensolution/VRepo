package com.spmadrid.vrepo.domain.services

import android.app.Activity

interface AuthenticationService {
    fun openLarkSSO()
    fun initialize(activity: Activity)
}