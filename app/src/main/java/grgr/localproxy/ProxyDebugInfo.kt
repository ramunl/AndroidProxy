package grgr.localproxy

import ProxyConnectionState


data class ProxyDebugInfo(val state: ProxyConnectionState = ProxyConnectionState.IDLE , val info: String = "")
