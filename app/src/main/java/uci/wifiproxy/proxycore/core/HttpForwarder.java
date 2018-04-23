package uci.wifiproxy.proxycore.core;

import android.content.Context;
import android.util.Log;

import com.google.common.base.Strings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HeaderIterator;
import cz.msebera.android.httpclient.HttpConnectionFactory;
import cz.msebera.android.httpclient.HttpEntityEnclosingRequest;
import cz.msebera.android.httpclient.HttpHost;
import cz.msebera.android.httpclient.HttpRequest;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.auth.AuthScope;
import cz.msebera.android.httpclient.auth.NTCredentials;
import cz.msebera.android.httpclient.client.CredentialsProvider;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.config.RequestConfig;
import cz.msebera.android.httpclient.client.methods.HttpDelete;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpHead;
import cz.msebera.android.httpclient.client.methods.HttpOptions;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.client.methods.HttpPut;
import cz.msebera.android.httpclient.client.methods.HttpTrace;
import cz.msebera.android.httpclient.client.methods.HttpUriRequest;
import cz.msebera.android.httpclient.client.methods.RequestBuilder;
import cz.msebera.android.httpclient.config.ConnectionConfig;
import cz.msebera.android.httpclient.impl.client.BasicCredentialsProvider;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import cz.msebera.android.httpclient.impl.client.HttpClientBuilder;
import cz.msebera.android.httpclient.impl.client.ProxyClient;
import cz.msebera.android.httpclient.impl.conn.ManagedHttpClientConnectionFactory;
import cz.msebera.android.httpclient.impl.conn.PoolingHttpClientConnectionManager;
import cz.msebera.android.httpclient.impl.io.DefaultHttpRequestParser;
import cz.msebera.android.httpclient.impl.io.HttpRequestParser;
import cz.msebera.android.httpclient.impl.io.HttpTransportMetricsImpl;
import cz.msebera.android.httpclient.impl.io.SessionInputBufferImpl;
import cz.msebera.android.httpclient.io.HttpMessageParser;
import cz.msebera.android.httpclient.message.BasicHttpRequest;
import cz.msebera.android.httpclient.params.CoreProtocolPNames;
import uci.wifiproxy.data.applicationPackage.ApplicationPackageLocalDataSource;
import uci.wifiproxy.data.firewallRule.FirewallRule;
import uci.wifiproxy.data.firewallRule.FirewallRuleLocalDataSource;
import uci.wifiproxy.data.trace.Trace;
import uci.wifiproxy.data.trace.TraceDataSource;
import uci.wifiproxy.util.StringUtils;
import uci.wifiproxy.util.network.ClientResolver;
import uci.wifiproxy.util.network.ConnectionDescriptor;

/**
 * Created by daniel on 17/04/17.
 */

public class HttpForwarder extends Thread {

    private static List<String> stripHeadersIn = Arrays.asList(
            "Content-Type", "Content-Length", "Proxy-Connection"
    );
    private static List<String> stripHeadersOut = Arrays.asList(
            "Proxy-Authentication", "Proxy-Authorization", "Transfer-Encoding"
    );

    private ServerSocket ssocket;
    private PoolingHttpClientConnectionManager manager;
    private ExecutorService threadPool = Executors.newCachedThreadPool();
    private CloseableHttpClient delegateClient;
    private CloseableHttpClient noDelegateClient;

    private final int inport;
    private final String addr;
    private final String user;
    private final String pass;
    private final String bypass;
    private final String domain;

    public boolean running = true;

    private CredentialsProvider credentials = null;

    private ClientResolver clientResolver;

    public HttpForwarder(String addr, int inport, String user,
                         String pass, int outport, boolean onlyLocal,
                         String bypass, String domain, Context context) throws IOException {
        this.addr = addr;
        this.inport = inport;
        this.user = user;
        this.pass = pass;
        this.bypass = bypass;
        this.domain = domain;

        if (onlyLocal) {
            this.ssocket = new ServerSocket(outport, 0,
                    InetAddress.getByName("127.0.0.1"));
        } else {
            this.ssocket = new ServerSocket(outport);
        }

        manager = new PoolingHttpClientConnectionManager();
        manager.setDefaultMaxPerRoute(20);
        manager.setMaxTotal(200);

        credentials = new BasicCredentialsProvider();

        clientResolver = new ClientResolver(context);

        Log.e(getClass().getName(), "Starting proxy");
    }

    public void run() {
        try {
            //NTCredentials extends from UsernamePasswordCredential which means that can resolve
            //Basic, Digest and NTLM authentication schemes. The field of domain act like an realm,
            //it can be null and it will works correctly
            credentials.setCredentials(new AuthScope(AuthScope.ANY),
                    new NTCredentials(this.user, this.pass, InetAddress.getLocalHost().getHostName(),
                            (Strings.isNullOrEmpty(domain) ? null : domain)
                    )
            );
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        this.delegateClient = HttpClientBuilder.create()
//                .setConnectionManager(manager)
                .setProxy(new HttpHost(this.addr, this.inport))
                .setDefaultCredentialsProvider(credentials)
                .disableRedirectHandling()
                .disableCookieManagement()
//                .disableAuthCaching()
                .disableAutomaticRetries()
                .disableConnectionState()
                .build();

        this.noDelegateClient = HttpClientBuilder.create()
//                .setConnectionManager(manager)
                .disableRedirectHandling()
                .disableCookieManagement()
                .disableAutomaticRetries()
                .disableConnectionState()
                .build();

        while (running) {
            try {
//                if (interrupted()) {
//                    Log.e(getClass().getName(), "The proxy task was interrupted");
//                }
                Socket s = this.ssocket.accept();
                this.threadPool.execute(new HttpForwarder.Handler(s));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void halt() {
        Log.e(getClass().getName(), "Stoping proxy");
        running = false;

        manager.shutdown();
        threadPool.shutdownNow();
        try {
            this.delegateClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            this.noDelegateClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void close() throws IOException {
        this.ssocket.close();
    }

    class Handler implements Runnable {

        Socket localSocket;
        //ByteBuffer buffer = ByteBuffer.allocate(8192);

        public Handler(Socket localSocket) {
            this.localSocket = localSocket;
        }


        public void run() {
            HttpParser parser = null;
            OutputStream os = null;
            long bytes = 0;
            try {
//                HttpTransportMetricsImpl metrics = new HttpTransportMetricsImpl();
//                SessionInputBufferImpl inbuffer = new SessionInputBufferImpl(metrics, 8 * 1024);
//                inbuffer.bind(this.localSocket.getInputStream());
//                HttpMessageParser<HttpRequest> requestParser = new DefaultHttpRequestParser(
//                        inbuffer);
//                HttpClient client;
//
//                HttpRequest request = requestParser.parse();
//
//                boolean matches = (bypass != null) && StringUtils.matches(request.getRequestLine().getUri(), bypass);
//                if (matches) {
//                    client = HttpForwarder.this.noDelegateClient;
//                    Log.i(getClass().getName(), "url matches bypass " + request.getRequestLine().getUri());
//                } else {
//                    client = HttpForwarder.this.delegateClient;
//                    Log.i(getClass().getName(), "url does not matches bypass " + request.getRequestLine().getUri());
//                }
//
//                HttpResponse response = client.execute(null, request);
//                os = localSocket.getOutputStream();
//                os.write(response.getStatusLine().toString().getBytes());
////                Log.e("STATUS-LINE", response.getStatusLine().toString());
//                os.write("\r\n".getBytes());
//                if (response.getEntity() != null) {
//                    InputStream inRemote = response.getEntity().getContent();
//                    bytes += new Piper(inRemote, os).startCopy();
//                    inRemote.close();
//                }


                parser = parseInputStream(this.localSocket.getInputStream());
                os = this.localSocket.getOutputStream();

                ConnectionDescriptor connectionDescriptor = getPackageConnectionDescritor(localSocket.getPort(),
                        localSocket.getInetAddress().getHostAddress());
                String packageNameSource = connectionDescriptor.getNamespace();

                Log.e("Request:", connectionDescriptor.getNamespace() +
                        "(" + connectionDescriptor.getName() + ")" + ": " + parser.getUri());

                //Firewall action
                if (!firewallFilter(packageNameSource, parser.getUri())) {
                    try {
                        os.write("HTTP/1.1 403 Forbidden".getBytes());
                        os.write("\r\n".getBytes());
                        os.write("\r\n".getBytes());
                        os.write("<h1>Forbidden by WifiProxy's firewall</h1>".getBytes());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;
                }

                //changing headers
//                Header [] headers = parser.getHeaders();
//                for (int i = 0; i < headers.length; i++){
//                    Header h = headers[i];
//                    if (h.getName().equalsIgnoreCase("user-agent")){
//                        Log.e("yes", "yes");
//                        headers[i] = new BasicHeader("User-agent", "firefox");
//                    }
//                }

                if (parser.getMethod().equals("CONNECT")) {
                    bytes += resolveConnect(parser, os);
                } else {
                    bytes += resolveOtherMethods(parser, os);
                }

                saveTrace(packageNameSource,
                        (connectionDescriptor.getName() != null) ? connectionDescriptor.getName() : Trace.UNKNOWN_APP_NAME,
                        parser.getUri(), bytes);

                Log.e("bytes:", packageNameSource + ": " + bytes + "");

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (parser != null) parser.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    if (os != null) os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    this.localSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                parser = null;
                os = null;
            }
        }

        private void saveTrace(String packageName, String name, String requestedUrl, long bytesSpent) {
            Trace trace = Trace.newTrace(packageName, name, requestedUrl, bytesSpent, Calendar.getInstance().getTimeInMillis());
            TraceDataSource traceDataSource = TraceDataSource.newInstance();
            traceDataSource.saveTrace(trace);
            traceDataSource.releaseResources();
            traceDataSource = null;
        }

//        private long resolveOtherMethods1(HttpParser parser, OutputStream os) throws IOException {
//            long bytes = 0;
//            HttpClientContext clientContext = HttpClientContext.create();
//            clientContext.setCredentialsProvider(credentials);
//            PlainConnectionSocketFactory sf = PlainConnectionSocketFactory.getSocketFactory();
//            InetSocketAddress remoteAddress = new InetSocketAddress(
//                    InetAddress.getByAddress(new byte[]{10,0,0,1}), HttpForwarder.this.inport);
//            Socket socket = sf.connectSocket(1000, null, new HttpHost("/"),
//                    remoteAddress, null, clientContext);
//
//            InputStream inRemote = socket.getInputStream();
//            OutputStream outRemote = socket.getOutputStream();
//            os.write("HTTP/1.1 200 Connection established".getBytes());
//            os.write("\r\n\r\n".getBytes());
//            threadPool.execute(new Piper(parser, outRemote));
//            bytes += new Piper(inRemote, os).startCopy();
//            return bytes;
//        }

        private long resolveOtherMethods(HttpParser parser, OutputStream os) {
            long bytes = 0;
            InputStream inRemote = null;

            HttpClient client;
            boolean matches = (bypass != null) && StringUtils.matches(parser.getUri(), bypass);
            if (matches) {
                client = HttpForwarder.this.noDelegateClient;
                Log.i(getClass().getName(), "url matches bypass " + parser.getUri());
            } else {
                client = HttpForwarder.this.delegateClient;
                Log.i(getClass().getName(), "url does not matches bypass " + parser.getUri());
            }
            HttpUriRequest request;
            HttpResponse response;
            try {

                Log.i(getClass().getName(), parser.getMethod() + " " + parser.getUri());
                if (parser.getMethod().equals("GET")) {
                    request = new HttpGet(parser.getUri());
                } else if (parser.getMethod().equals("POST")) {
                    request = new HttpPost(parser.getUri());
                } else if (parser.getMethod().equals("HEAD")) {
                    request = new HttpHead(parser.getUri());
                } else if (parser.getMethod().equals("PUT")) {
                    request = new HttpPut(parser.getUri());
                } else if (parser.getMethod().equals("DELETE")) {
                    request = new HttpDelete(parser.getUri());
                } else if (parser.getMethod().equals("OPTIONS")) {
                    request = new HttpOptions(parser.getUri());
                } else if (parser.getMethod().equals("TRACE")) {
                    request = new HttpTrace(parser.getUri());
                } else {
                    request = RequestBuilder.create(parser.getMethod())
                            .setUri(parser.getUri())
                            .build();
                }

                if (request instanceof HttpEntityEnclosingRequest) {
                    HttpEntityEnclosingRequest request1 = (HttpEntityEnclosingRequest) request;
                    request1.setEntity(new StreamingRequestEntity(parser));
                }

                Header[] headers = parser.getHeaders();
                for (Header h : headers) {
                    if (stripHeadersIn.contains(h.getName())) continue;
                    request.addHeader(h);
                }

                response = client.execute(request);

                os.write(response.getStatusLine().toString().getBytes());
//                Log.e("STATUS-LINE", response.getStatusLine().toString());
                os.write("\r\n".getBytes());

                HeaderIterator it = response.headerIterator();
                while (it.hasNext()) {
                    Header h = (Header) it.next();
                    if (stripHeadersOut.contains(h.getName())) continue;
                    os.write((h.toString() + "\r\n").getBytes());
                }

                os.write("\r\n".getBytes());

                if (response.getEntity() != null) {
                    inRemote = response.getEntity().getContent();
                    bytes += new Piper(inRemote, os).startCopy();
                    inRemote.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (inRemote != null) inRemote.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                inRemote = null;
                client = null;
                request = null;
                response = null;
            }

            return bytes;
        }

        private long resolveConnect(HttpParser parser, OutputStream os) {
            Log.e(getClass().getName(), "CONNECT " + parser.getUri());
//            for (Header h : parser.getHeaders()){
//                Log.e("Header", h.getName() + " : " + h.getValue());
//            }

            long bytes = 0;
            boolean matches = (bypass != null) && StringUtils.matches(parser.getUri(), bypass);
            if (!matches) {
                Log.i(getClass().getName(), "url does not matches bypass " + parser.getUri());
                bytes += doConnect(parser, os);
            } else {
                Log.i(getClass().getName(), "url matches bypass " + parser.getUri());
                bytes += doConnectNoProxy(parser, os);
            }
            return bytes;
        }


        private HttpParser parseInputStream(InputStream is) throws ParseException, IOException {
            HttpParser parser = new HttpParser(is);
            try {
                for (int i = 0; i < 100 && !parser.parse(); i++) {
//                    Log.e("parser", 1 + "");
                }
//                parser.parse();
//                while (!parser.parse()) {
//                    Log.e("parser", "parse");
//                }
            } catch (IOException e) {
                parser.close();
            }
            return parser;
        }

        private void printResponse(HttpResponse response) throws IOException {
            String line;
            BufferedReader bf = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            while ((line = bf.readLine()) != null) {
                Log.e("InputStream", line);
            }

        }


        public boolean firewallFilter(String packageNameSource, String uri) {
            FirewallRuleLocalDataSource firewallRuleLocalDataSource = FirewallRuleLocalDataSource.newInstance();
            try {
                for (FirewallRule firewallRule : firewallRuleLocalDataSource.getActiveFirewallRules()) {
                    if (
                            (firewallRule.getApplicationPackageName().equals(ApplicationPackageLocalDataSource.ALL_APPLICATION_PACKAGES_STRING)
                                    && StringUtils.matches(uri, firewallRule.getRule()))
                                    || (packageNameSource.equals(firewallRule.getApplicationPackageName())
                                    && StringUtils.matches(uri, firewallRule.getRule()))
                            ) {
//                        Log.i(getClass().getName(), packageNameSource + " : " + uri + " blocked by firewall");
                        return false;
                    }
                }

//                Log.i(getClass().getName(), packageNameSource + " : " + uri + " pass the firewall");
                return true;
            } finally {
                firewallRuleLocalDataSource.releaseResources();
                firewallRuleLocalDataSource = null;
            }
        }

        private ConnectionDescriptor getPackageConnectionDescritor(int localPort, String localAddress) {
            ConnectionDescriptor connectionDescriptor = clientResolver.getClientDescriptor(localPort, localAddress);
            return connectionDescriptor;
        }

        long doConnectNoProxy(HttpParser parser, OutputStream os) {
            long bytes = 0;
            String[] uri = parser.getUri().split(":");
            Socket remoteSocket = null;
            InputStream inRemote = null;
            OutputStream outRemote = null;

            try {
                remoteSocket = new Socket(uri[0], Integer.parseInt(uri[1]));
                inRemote = remoteSocket.getInputStream();
                outRemote = remoteSocket.getOutputStream();

                os.write("HTTP/1.1 200 Connection established".getBytes());
                os.write("\r\n\r\n".getBytes());
                threadPool.execute(new Piper(parser, outRemote));

                bytes += new Piper(inRemote, os).startCopy();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (inRemote != null) inRemote.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    if (outRemote != null) outRemote.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (remoteSocket != null) {
                    try {
                        remoteSocket.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                remoteSocket = null;
                inRemote = null;
                outRemote = null;
            }

            return bytes;

        }

        long doConnect(HttpParser parser, OutputStream os) {
            long bytes = 0;
            String[] uri = parser.getUri().split(":");
            Socket remoteSocket = null;
            InputStream inRemote = null;
            OutputStream outRemote = null;

            try {
//                BufferedReader i = new BufferedReader(
//                        new InputStreamReader(parser));
//                String line = null;
//                while ((line = i.readLine()) != null) {
//                    Log.e("InputStream", line);
//                }

                AdaptedProxyClient client = new AdaptedProxyClient();
                HttpHost proxyHost = new HttpHost(addr, inport);
                HttpHost targetHost = new HttpHost(uri[0], Integer.parseInt(uri[1]));

                List<Header> headers = new ArrayList<>();
                for (Header h : parser.getHeaders()){
                    if(!stripHeadersIn.contains(h.getName()))
                        headers.add(h);
                }

                Header[] arr = new Header[headers.size()];
                arr = headers.toArray(arr);

                remoteSocket = client.tunnel(
                        proxyHost,
                        targetHost,
                        credentials.getCredentials(AuthScope.ANY),
                        arr
                        );

                inRemote = remoteSocket.getInputStream();
                outRemote = remoteSocket.getOutputStream();

                os.write("HTTP/1.1 200 Connection established".getBytes());
                os.write("\r\n\r\n".getBytes());
                threadPool.execute(new Piper(parser, outRemote));

                bytes += new Piper(inRemote, os).startCopy();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (inRemote != null) inRemote.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    if (outRemote != null) outRemote.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (remoteSocket != null) {
                    try {
                        remoteSocket.close();
                    } catch (Exception fe) {
                        fe.printStackTrace();
                    }
                }
                remoteSocket = null;
                inRemote = null;
                outRemote = null;
            }

            return bytes;

        }

    }

}
