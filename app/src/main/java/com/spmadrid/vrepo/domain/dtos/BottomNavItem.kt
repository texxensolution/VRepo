package com.spmadrid.vrepo.domain.dtos

import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Camera
import compose.icons.fontawesomeicons.solid.Search


sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object Home : BottomNavItem("home", FontAwesomeIcons.Solid.Camera, "ALPR")
    object Conduction : BottomNavItem("conduction", FontAwesomeIcons.Solid.Search, "Manual Search")
}