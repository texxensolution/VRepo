package com.spmadrid.vrepo.domain.services

import android.app.Activity

interface AuthenticationService {
    fun openLarkSSO(activity: Activity)
    fun initialize(activity: Activity)
}