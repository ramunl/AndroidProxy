package grgr.localproxy.proxycore.core

import android.content.Context
import android.util.Log
import com.google.common.base.Strings
import cz.msebera.android.httpclient.Header
import cz.msebera.android.httpclient.HttpEntityEnclosingRequest
import cz.msebera.android.httpclient.HttpHost
import cz.msebera.android.httpclient.HttpResponse
import cz.msebera.android.httpclient.NoHttpResponseException
import cz.msebera.android.httpclient.auth.AuthScope
import cz.msebera.android.httpclient.auth.NTCredentials
import cz.msebera.android.httpclient.client.CredentialsProvider
import cz.msebera.android.httpclient.client.HttpRequestRetryHandler
import cz.msebera.android.httpclient.client.methods.HttpDelete
import cz.msebera.android.httpclient.client.methods.HttpGet
import cz.msebera.android.httpclient.client.methods.HttpHead
import cz.msebera.android.httpclient.client.methods.HttpOptions
import cz.msebera.android.httpclient.client.methods.HttpPost
import cz.msebera.android.httpclient.client.methods.HttpPut
import cz.msebera.android.httpclient.client.methods.HttpTrace
import cz.msebera.android.httpclient.client.methods.HttpUriRequest
import cz.msebera.android.httpclient.client.methods.RequestBuilder
import cz.msebera.android.httpclient.impl.client.BasicCredentialsProvider
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient
import cz.msebera.android.httpclient.impl.client.HttpClientBuilder
import cz.msebera.android.httpclient.impl.conn.PoolingHttpClientConnectionManager
import cz.msebera.android.httpclient.message.BasicHeader
import grgr.localproxy.proxycore.ProxyService
import grgr.localproxy.proxydata.applicationPackage.ApplicationPackageLocalDataSource
import grgr.localproxy.proxydata.firewallRule.FirewallRuleLocalDataSource
import grgr.localproxy.proxydata.header.HeaderDataSource
import grgr.localproxy.proxydata.trace.Trace
import grgr.localproxy.proxydata.trace.TraceDataSource
import grgr.localproxy.proxyutil.StringUtils
import grgr.localproxy.proxyutil.network.ClientResolver
import grgr.localproxy.proxyutil.network.ConnectionDescriptor
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.UnknownHostException
import java.util.Arrays
import java.util.Calendar
import java.util.concurrent.Executors

/**
 * Created by daniel on 17/04/17.
 */
class HttpForwarder(
    private val addr: String, private val inport: Int, private val user: String,
    private val pass: String, outport: Int, onlyLocal: Boolean,
    private val bypass: String?, private val domain: String, context: Context?
) : Thread() {
    private var ssocket: ServerSocket? = null
    private val manager: PoolingHttpClientConnectionManager
    private val threadPool = Executors.newCachedThreadPool()
    private var delegateClient: CloseableHttpClient? = null
    private var noDelegateClient: CloseableHttpClient? = null
    var running = true
    private var credentials: CredentialsProvider? = null
    private val clientResolver: ClientResolver

    init {
        if (onlyLocal) {
            ssocket = ServerSocket(
                outport, 0,
                InetAddress.getByName("127.0.0.1")
            )
        } else {
            ssocket = ServerSocket(outport)
        }
        manager = PoolingHttpClientConnectionManager()
        manager.defaultMaxPerRoute = 20
        manager.maxTotal = 200
        credentials = BasicCredentialsProvider()
        clientResolver = ClientResolver(context)

//        Log.e(getClass().getName(), "Starting proxy");
    }

    override fun run() {
        try {
            //NTCredentials extends from UsernamePasswordCredential which means that can resolve
            //Basic, Digest and NTLM authentication schemes. The field of domain act like an realm,
            //it can be null and it will works correctly
            credentials!!.setCredentials(
                AuthScope(AuthScope.ANY),
                NTCredentials(
                    user, pass, InetAddress.getLocalHost().hostName,
                    if (Strings.isNullOrEmpty(domain)) null else domain
                )
            )
            ProxyService.proxyConnectionState.value = ProxyConnectionState.CONNECTED
        } catch (e: UnknownHostException) {
            ProxyService.onFailed(e)
        }
        delegateClient = HttpClientBuilder.create()
            .setConnectionManager(manager)
            .setProxy(HttpHost(addr, inport))
            .setDefaultCredentialsProvider(credentials)
            .disableRedirectHandling()
            .disableCookieManagement() //                .disableAuthCaching()
            .disableAutomaticRetries()
            .disableConnectionState()
            .build()
        noDelegateClient = HttpClientBuilder.create()
            .setConnectionManager(manager)
            .disableRedirectHandling()
            .disableCookieManagement()
            .disableAutomaticRetries()
            .disableConnectionState()
            .build()
        while (running) {
            try {
//                if (interrupted()) {
//                    Log.e(getClass().getName(), "The proxy task was interrupted");
//                }
                val s = ssocket!!.accept()
                threadPool.execute(Handler(s))
            } catch (e: IOException) {
                ProxyService.onFailed(e)
            }
        }
    }

    fun halt() {
//        Log.e(getClass().getName(), "Stoping proxy");
        running = false
        manager.shutdown()
        threadPool.shutdownNow()
        try {
            delegateClient!!.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        try {
            noDelegateClient!!.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        try {
            close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    fun close() {
        ssocket!!.close()
    }

    internal inner class Handler    //ByteBuffer buffer = ByteBuffer.allocate(8192);
        (var localSocket: Socket) : Runnable {
        private fun createDelegateClient(): CloseableHttpClient {
            return HttpClientBuilder.create() //                .setConnectionManager(manager)
                .setProxy(HttpHost(addr, inport))
                .setDefaultCredentialsProvider(credentials)
                .disableRedirectHandling()
                .disableCookieManagement() //                .disableAuthCaching()
                .disableAutomaticRetries()
                .setRetryHandler(HttpRequestRetryHandler { exception, executionCount, context ->
                    if (executionCount > 3) {
//                                LOGGER.warn("Maximum tries reached for client http pool ");
                        return@HttpRequestRetryHandler false
                    }
                    exception is NoHttpResponseException
                }) //                    .disableConnectionState()
                .build()
        }

        private fun createNoDelegateClient(): CloseableHttpClient {
            return HttpClientBuilder.create() //                .setConnectionManager(manager)
                .disableRedirectHandling()
                .disableCookieManagement()
                .disableAutomaticRetries() //                    .disableConnectionState()
                .setRetryHandler(HttpRequestRetryHandler { exception, executionCount, context ->
                    if (executionCount > 3) {
//                                LOGGER.warn("Maximum tries reached for client http pool ");
                        return@HttpRequestRetryHandler false
                    }
                    exception is NoHttpResponseException
                })
                .build()
        }

        private fun getValidHeaders(parserHeaders: Array<Header>): MutableList<Header> {
            val resultHeaders = ArrayList<Header>()
            val cnnHeaders = ArrayList<String>()
            for (h in parserHeaders) {
                if (h.name == CONNECTION_HEADER) {
                    val connectionHeaders =
                        h.value.split(", ".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                    cnnHeaders.addAll(Arrays.asList(*connectionHeaders))
                    break
                }
            }
            for (h in parserHeaders) {
                if (stripHeaders.contains(h.name) || cnnHeaders.contains(h.name)) continue
                resultHeaders.add(h)
            }
            return resultHeaders
        }

        private fun replaceOrAddHeaders(headers: MutableList<Header>): List<Header> {
            val headerDataSource = HeaderDataSource.newInstance()
            val headersToAddOrModify = headerDataSource.allHeaders
            if (!headersToAddOrModify.isEmpty()) {
                for (dataHeader in headersToAddOrModify) {
                    var contains = false
                    for (i in headers.indices) {
                        val h = headers[i]
                        if (dataHeader.name.equals(h.name, ignoreCase = true)) {
                            headers[i] = BasicHeader(h.name, dataHeader.value)
                            contains = true
                        }
                    }
                    if (!contains) headers.add(BasicHeader(dataHeader.name, dataHeader.value))
                }
            }
            return headers
        }

        override fun run() {
            var parser: HttpParser? = null
            var os: OutputStream? = null
            var bytes: Long = 0
            try {
                parser = HttpParser(localSocket.getInputStream())
                val validRequest = parser.parse()
                os = localSocket.getOutputStream()
                if (!validRequest) {
                    os.write("HTTP/1.1 400 Bad Request".toByteArray())
                    os.write("\r\n\n".toByteArray())
                    return
                }
            } catch (e: Exception) {
                return
            }
            val connectionDescriptor = getPackageConnectionDescritor(
                localSocket.port,
                localSocket.inetAddress.hostAddress
            )
            val packageNameSource = connectionDescriptor.namespace
            Log.e(
                "Request:", connectionDescriptor.namespace +
                        "(" + connectionDescriptor.name + ")" + ": " + parser.getUri()
            )

//            for (Header h : parser.getHeaders()) {
//                Log.e("Header", h.getName() + ":" + h.getValue());
//            }

            //Firewall action
            if (!firewallFilter(packageNameSource, parser.getUri())) {
                try {
                    os.write("HTTP/1.1 403 Forbidden".toByteArray())
                    os.write("\r\n".toByteArray())
                    os.write("\r\n".toByteArray())
                    os.write("<h1>Forbidden by LocalProxy's firewall</h1>".toByteArray())
                } catch (e: Exception) {
                }
                return
            }
            bytes += if (parser.getMethod() == "CONNECT") {
                resolveConnect(parser, os)
            } else {
                resolveOtherMethods(parser, os)
            }
            saveTrace(
                packageNameSource,
                if (connectionDescriptor.name != null) connectionDescriptor.name else Trace.UNKNOWN_APP_NAME,
                parser.getUri(), bytes
            )
        }

        private fun saveTrace(
            packageName: String,
            name: String,
            requestedUrl: String,
            bytesSpent: Long
        ) {
            val trace = Trace.newTrace(
                packageName,
                name,
                requestedUrl,
                bytesSpent,
                Calendar.getInstance().timeInMillis
            )
            var traceDataSource = TraceDataSource.newInstance()
            traceDataSource!!.saveTrace(trace)
            traceDataSource.releaseResources()
            traceDataSource = null
        }

        private fun resolveOtherMethods(parser: HttpParser, os: OutputStream?): Long {
            var bytes: Long = 0
            var inRemote: InputStream? = null
            val client: CloseableHttpClient
            val matches = bypass != null && StringUtils.matches(parser.getUri(), bypass)
            client = if (matches) {
                createNoDelegateClient()
                //                client = HttpForwarder.this.noDelegateClient;
//                Log.i(getClass().getName(), "url matches bypass " + parser.getUri());
            } else {
                createDelegateClient()
                //                client = HttpForwarder.this.delegateClient;
//                Log.i(getClass().getName(), "url does not matches bypass " + parser.getUri());
            }
            val request: HttpUriRequest
            val response: HttpResponse
            try {

//                Log.i(getClass().getName(), parser.getMethod() + " " + parser.getUri());
                request = if (parser.getMethod() == "GET") {
                    HttpGet(parser.getUri())
                } else if (parser.getMethod() == "POST") {
                    HttpPost(parser.getUri())
                } else if (parser.getMethod() == "HEAD") {
                    HttpHead(parser.getUri())
                } else if (parser.getMethod() == "PUT") {
                    HttpPut(parser.getUri())
                } else if (parser.getMethod() == "DELETE") {
                    HttpDelete(parser.getUri())
                } else if (parser.getMethod() == "OPTIONS") {
                    HttpOptions(parser.getUri())
                } else if (parser.getMethod() == "TRACE") {
                    HttpTrace(parser.getUri())
                } else {
                    RequestBuilder.create(parser.getMethod())
                        .setUri(parser.getUri())
                        .build()
                }
                if (request is HttpEntityEnclosingRequest) {
                    val request1 = request as HttpEntityEnclosingRequest
                    request1.entity = StreamingRequestEntity(parser)
                }
                val requestHeaders = getValidHeaders(parser.getHeaders())
                val modifiedHeaders = replaceOrAddHeaders(requestHeaders)
                request.setHeaders(modifiedHeaders.toTypedArray())
                response = client.execute(request)
                localSocket.shutdownInput()
                os!!.write(response.getStatusLine().toString().toByteArray())
                //                Log.e("STATUS-LINE", response.getStatusLine().toString());
                os.write("\r\n".toByteArray())
                val responseHeaders: List<Header> = getValidHeaders(response.getAllHeaders())
                for (h in responseHeaders) {
                    os.write(
                        """$h""".toByteArray()
                    )
                }
                if (response.getEntity() != null) {
                    os.write("\r\n".toByteArray())
                    inRemote = response.getEntity().content
                    bytes += Piper(inRemote, os).startCopy()
                }
            } catch (e: Exception) {
            } finally {
                if (inRemote != null) try {
                    inRemote.close()
                } catch (e: IOException) {
                }
                try {
                    os!!.close()
                } catch (e: IOException) {
                }
                try {
                    localSocket.close()
                } catch (e: IOException) {
                }
                try {
                    client.close()
                } catch (e: IOException) {
                }
            }
            return bytes
        }

        private fun resolveConnect(parser: HttpParser, os: OutputStream?): Long {
//            Log.e(getClass().getName(), "CONNECT " + parser.getUri());
//            for (Header h : parser.getHeaders()){
//                Log.e("Header", h.getName() + " : " + h.getValue());
//            }
            var bytes: Long = 0
            val matches = bypass != null && StringUtils.matches(parser.getUri(), bypass)
            bytes += if (!matches) {
//                Log.i(getClass().getName(), "url does not matches bypass " + parser.getUri());
                doConnect(parser, os)
            } else {
//                Log.i(getClass().getName(), "url matches bypass " + parser.getUri());
                doConnectNoProxy(parser, os)
            }
            return bytes
        }

        @Throws(IOException::class)
        private fun printResponse(response: HttpResponse) {
            var line: String?
            val bf = BufferedReader(InputStreamReader(response.entity.content))
            while (bf.readLine().also { line = it } != null) {
                Log.e("InputStream", line!!)
            }
        }

        fun firewallFilter(packageNameSource: String, uri: String?): Boolean {
            var firewallRuleLocalDataSource = FirewallRuleLocalDataSource.newInstance()
            return try {
                for (firewallRule in firewallRuleLocalDataSource!!.activeFirewallRules) {
                    if (firewallRule.applicationPackageName == ApplicationPackageLocalDataSource.ALL_APPLICATION_PACKAGES_STRING && StringUtils.matches(
                            uri,
                            firewallRule.rule
                        ) || packageNameSource == firewallRule.applicationPackageName && StringUtils.matches(
                            uri,
                            firewallRule.rule
                        )
                    ) {
//                        Log.i(getClass().getName(), packageNameSource + " : " + uri + " blocked by firewall");
                        return false
                    }
                }

//                Log.i(getClass().getName(), packageNameSource + " : " + uri + " pass the firewall");
                true
            } finally {
                firewallRuleLocalDataSource!!.releaseResources()
                firewallRuleLocalDataSource = null
            }
        }

        private fun getPackageConnectionDescritor(
            localPort: Int,
            localAddress: String
        ): ConnectionDescriptor {
            return clientResolver.getClientDescriptor(localPort, localAddress)
        }

        //TODO> copiar cabezeras al destino al inicio de la peticion
        private fun doConnectNoProxy(parser: HttpParser, os: OutputStream?): Long {
            var bytes: Long = 0
            val uri = parser.getUri().split(":".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            var remoteSocket: Socket? = null
            var inRemote: InputStream? = null
            var outRemote: OutputStream? = null
            try {
                remoteSocket = Socket(uri[0], uri[1].toInt())
                inRemote = remoteSocket.getInputStream()
                outRemote = remoteSocket.getOutputStream()
                os!!.write("HTTP/1.1 200 OK".toByteArray())
                os.write("\r\n\r\n".toByteArray())
                threadPool.execute(Piper(parser, outRemote))
                bytes += Piper(inRemote, os).startCopy()
            } catch (e: Exception) {
                Log.e("Error", parser.getMethod() + parser.getUri())
            } finally {
                if (remoteSocket != null) {
                    try {
                        remoteSocket.close()
                    } catch (fe: Exception) {
                    }
                }
                try {
                    os!!.close()
                } catch (e: IOException) {
                }
                try {
                    parser.close()
                } catch (e: IOException) {
                }
            }
            return bytes
        }

        private fun doConnect(parser: HttpParser, os: OutputStream?): Long {
            var bytes: Long = 0
            val uri = parser.getUri().split(":".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            var remoteSocket: Socket? = null
            var inRemote: InputStream? = null
            var outRemote: OutputStream? = null
            try {
//                BufferedReader i = new BufferedReader(
//                        new InputStreamReader(parser));
//                String line = null;
//                while ((line = i.readLine()) != null) {
//                    Log.e("InputStream", line);
//                }
                val client = AdaptedProxyClient()
                val proxyHost = HttpHost(addr, inport)
                val targetHost = HttpHost(uri[0], uri[1].toInt())
                val requestHeaders = getValidHeaders(parser.getHeaders())
                val modifiedHeaders = replaceOrAddHeaders(requestHeaders)
                for (h in modifiedHeaders) {
                    Log.e("Header", h.name + " : " + h.value)
                }
                remoteSocket = client.tunnel(
                    proxyHost,
                    targetHost,
                    credentials!!.getCredentials(AuthScope.ANY),
                    modifiedHeaders.toTypedArray()
                )
                inRemote = remoteSocket.getInputStream()
                outRemote = remoteSocket.getOutputStream()
                os!!.write("HTTP/1.1 200 OK".toByteArray())
                os.write("\r\n\r\n".toByteArray())
                threadPool.execute(Piper(parser, outRemote))
                bytes += Piper(inRemote, os).startCopy()
            } catch (e: Exception) {
                ProxyService.onFailed(e)
            } finally {
                try {
                    inRemote?.close()
                } catch (e: IOException) {
                }
                try {
                    outRemote?.close()
                } catch (e: IOException) {
                }
                if (remoteSocket != null) {
                    try {
                        remoteSocket.close()
                    } catch (fe: Exception) {
                    }
                }
            }
            return bytes
        }
    }

    companion object {
        private val stripHeadersIn: List<String> = mutableListOf(
            "Content-Type", "Content-Length", "Proxy-Connection", "Keep-Alive"
        )
        private val stripHeadersOut: List<String> = mutableListOf(
            "Proxy-Authentication", "Proxy-Authorization", "Transfer-Encoding"
        )
        private val stripHeaders: List<String> = mutableListOf(
            "Proxy-Authentication", "Proxy-Authorization", "Transfer-Encoding",
            "Connection", "Content-Type", "Content-Length", "Proxy-Connection", "Keep-Alive",
            "TE", "Trailer", "Upgrade"
        )
        private const val CONNECTION_HEADER = "Connection"
    }
}