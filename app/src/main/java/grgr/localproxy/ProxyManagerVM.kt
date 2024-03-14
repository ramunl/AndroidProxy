package grgr.localproxy

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import grgr.localproxy.proxycore.ProxyService
import grgr.localproxy.proxyutil.LOG_TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn


interface ProxyManagerVM {

    val proxyState: MutableState<ProxyDebugInfo>
}

@Composable
fun rememberProxyManagerVM(): ProxyManagerVM {
    val scope = rememberCoroutineScope()
    return remember { ProxyManagerVMDefault(scope) }

}

class ProxyManagerVMDefault(private val scope: CoroutineScope) : ProxyManagerVM {
    override val proxyState = mutableStateOf(ProxyDebugInfo())

    init {
        snapshotFlow { ProxyService.proxyConnectionState.value }
            .combine(snapshotFlow { ProxyService.proxyErrInfoState.value }) { state, info ->
                Log.d(LOG_TAG, "ConnectionState changed: $state $info")
                proxyState.value = ProxyDebugInfo(state, info)
            }.launchIn(scope)
    }

}