package com.capstone.toma.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.capstone.toma.home.HomeScreen
import com.capstone.toma.storage.RecipeStorageScreen
import kotlinx.coroutines.launch

private enum class TomaScreen(
    val label: String,
    val subtitle: String,
    val icon: ImageVector
) {
    Home("홈", "분석과 추천을 확인합니다", Icons.Default.Home),
    Storage("저장소", "저장한 레시피를 관리합니다", Icons.Default.BookmarkBorder),
}

@Composable
fun TomaApp() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentScreen by rememberSaveable { mutableStateOf(TomaScreen.Home) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            TomaDrawer(
                currentScreen = currentScreen,
                onSelect = { destination ->
                    currentScreen = destination
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = TomaCream
        ) {
            Crossfade(
                targetState = currentScreen,
                label = "screen-crossfade"
            ) { destination ->
                when (destination) {
                    TomaScreen.Home -> HomeScreen(
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onOpenStorage = { currentScreen = TomaScreen.Storage }
                    )

                    TomaScreen.Storage -> RecipeStorageScreen(
                        onMenuClick = { scope.launch { drawerState.open() } }
                    )
                }
            }
        }
    }
}

@Composable
private fun TomaDrawer(
    currentScreen: TomaScreen,
    onSelect: (TomaScreen) -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier
            .width(300.dp)
            .fillMaxHeight(),
        drawerContainerColor = TomaCard,
        drawerShape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 28.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(TomaTomato, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.RestaurantMenu,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "To-ma",
                        color = TomaBrown,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "메뉴에서 저장소로 이동할 수 있어요",
                        color = TomaMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            TomaScreen.entries.forEach { item ->
                NavigationDrawerItem(
                    label = {
                        Column {
                            Text(
                                text = item.label,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = item.subtitle,
                                color = TomaMuted,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    },
                    selected = currentScreen == item,
                    onClick = { onSelect(item) },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label
                        )
                    },
                    modifier = Modifier.padding(vertical = 4.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = TomaChip,
                        selectedTextColor = TomaInk,
                        selectedIconColor = TomaTomato,
                        unselectedContainerColor = TomaCard,
                        unselectedTextColor = TomaInk,
                        unselectedIconColor = TomaMuted
                    )
                )
            }
        }
    }
}

@Composable
fun TomaBrandBar(
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(TomaTomato, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.RestaurantMenu,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "To-ma",
                color = TomaBrown,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        IconButton(
            onClick = onMenuClick,
            modifier = Modifier.background(TomaCard, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "메뉴 열기",
                tint = TomaInk
            )
        }
    }
}
