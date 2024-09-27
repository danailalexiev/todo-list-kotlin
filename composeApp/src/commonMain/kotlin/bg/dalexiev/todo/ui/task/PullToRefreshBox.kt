package bg.dalexiev.todo.ui.task

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullToRefreshBox(
    modifier: Modifier = Modifier,
    isRefreshing: Boolean,
    onRefresh: () -> Unit = {},
    content: @Composable () -> Unit = {}
) {
    val pullToRefreshState = rememberPullToRefreshState()
    if (pullToRefreshState.isRefreshing) {
        val currentOnRefresh by rememberUpdatedState(onRefresh)
        LaunchedEffect(Unit) {
            currentOnRefresh()
        }
    }

    if (isRefreshing) {
        pullToRefreshState.startRefresh()
    } else {
        pullToRefreshState.endRefresh()
    }

    Box(modifier = modifier.nestedScroll(pullToRefreshState.nestedScrollConnection)) {
        content()
        PullToRefreshContainer(
            modifier = Modifier.align(Alignment.TopCenter),
            state = pullToRefreshState
        )
    }
}