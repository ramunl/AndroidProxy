package grgr.localproxy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import grgr.localproxy.ui.theme.AndroidProxyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidProxyTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val vm = rememberProxyManagerVM()
                    RootView(vm)
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RootView(vm: ProxyManagerVM) {
    val textStyle = TextStyle.Default.copy(fontSize = 44.sp)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .semantics {
                testTagsAsResourceId = true
            }
        ,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
    ) {

        Text(
            text = "connection: " + vm.proxyState.value.state.toString(),
            modifier = Modifier.semantics {
                testTag = "connectionStateTag"
                contentDescription = "connectionStateDesc"
            },
            style = textStyle
        )
        Text(
            text = "info: " + vm.proxyState.value.info,
            modifier = Modifier,
            style = textStyle
        )
    }

}
