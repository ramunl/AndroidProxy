package uci.localproxy.proxycore.core;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpEntityEnclosingRequest;
import cz.msebera.android.httpclient.HttpHost;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NoHttpResponseException;
import cz.msebera.android.httpclient.auth.AuthScope;
import cz.msebera.android.httpclient.auth.NTCredentials;
import cz.msebera.android.httpclient.client.CredentialsProvider;
import cz.msebera.android.httpclient.client.HttpRequestRetryHandler;
import cz.msebera.android.httpclient.client.methods.HttpDelete;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpHead;
import cz.msebera.android.httpclient.client.methods.HttpOptions;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.client.methods.HttpPut;
import cz.msebera.android.httpclient.client.methods.HttpTrace;
import cz.msebera.android.httpclient.client.methods.HttpUriRequest;
import cz.msebera.android.httpclient.client.methods.RequestBuilder;
import cz.msebera.android.httpclient.impl.client.BasicCredentialsProvider;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import cz.msebera.android.httpclient.impl.client.HttpClientBuilder;
import cz.msebera.android.httpclient.impl.conn.PoolingHttpClientConnectionManager;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.protocol.HttpContext;
import uci.localproxy.proxydata.applicationPackage.ApplicationPackageLocalDataSource;
import uci.localproxy.proxydata.firewallRule.FirewallRule;
import uci.localproxy.proxydata.firewallRule.FirewallRuleLocalDataSource;
import uci.localproxy.proxydata.header.HeaderDataSource;
import uci.localproxy.proxydata.trace.Trace;
import uci.localproxy.proxydata.trace.TraceDataSource;
import uci.localproxy.proxyutil.StringUtils;
import uci.localproxy.proxyutil.network.ClientResolver;
import uci.localproxy.proxyutil.network.ConnectionDescriptor;

/**
 * Created by daniel on 17/04/17.
 */

public class HttpForwarder extends Thread {

    private static List<String> stripHeadersIn = Arrays.asList(
            "Content-Type", "Content-Length", "Proxy-Connection", "Keep-Alive"
    );
    private static List<String> stripHeadersOut = Arrays.asList(
            "Proxy-Authentication", "Proxy-Authorization", "Transfer-Encoding"
    );
    private static List<String> stripHeaders = Arrays.asList(
            "Proxy-Authentication", "Proxy-Authorization", "Transfer-Encoding",
            "Connection", "Content-Type", "Content-Length", "Proxy-Connection", "Keep-Alive",
            "TE", "Trailer", "Upgrade"
    );

    private static String CONNECTION_HEADER = "Connection";

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

//        Log.e(getClass().getName(), "Starting proxy");
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
                .setConnectionManager(manager)
                .setProxy(new HttpHost(this.addr, this.inport))
                .setDefaultCredentialsProvider(credentials)
                .disableRedirectHandling()
                .disableCookieManagement()
//                .disableAuthCaching()
                .disableAutomaticRetries()
                .disableConnectionState()
                .build();

        this.noDelegateClient = HttpClientBuilder.create()
                .setConnectionManager(manager)
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
//        Log.e(getClass().getName(), "Stoping proxy");
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

        private CloseableHttpClient createDelegateClient() {
            CloseableHttpClient client = HttpClientBuilder.create()
//                .setConnectionManager(manager)
                    .setProxy(new HttpHost(HttpForwarder.this.addr, HttpForwarder.this.inport))
                    .setDefaultCredentialsProvider(credentials)
                    .disableRedirectHandling()
                    .disableCookieManagement()
//                .disableAuthCaching()
                    .disableAutomaticRetries()
                    .setRetryHandler(new HttpRequestRetryHandler() {
                        @Override
                        public boolean retryRequest(IOException exception, int executionCount,
                                                    HttpContext context) {
                            if (executionCount > 3) {
//                                LOGGER.warn("Maximum tries reached for client http pool ");
                                return false;
                            }
                            if (exception instanceof NoHttpResponseException) {
//                                LOGGER.warn("No response from server on " + executionCount + " call");
                                return true;
                            }
                            return false;
                        }
                    })
//                    .disableConnectionState()
                    .build();

            return client;
        }


        private CloseableHttpClient createNoDelegateClient() {
            return HttpClientBuilder.create()
//                .setConnectionManager(manager)
                    .disableRedirectHandling()
                    .disableCookieManagement()
                    .disableAutomaticRetries()
//                    .disableConnectionState()
                    .setRetryHandler(new HttpRequestRetryHandler() {
                        @Override
                        public boolean retryRequest(IOException exception, int executionCount,
                                                    HttpContext context) {
                            if (executionCount > 3) {
//                                LOGGER.warn("Maximum tries reached for client http pool ");
                                return false;
                            }
                            if (exception instanceof NoHttpResponseException) {
//                                LOGGER.warn("No response from server on " + executionCount + " call");
                                return true;
                            }
                            return false;
                        }
                    })
                    .build();
        }


        private List<Header> getValidHeaders(Header[] parserHeaders) {
            ArrayList<Header> resultHeaders = new ArrayList<>();
            ArrayList<String> cnnHeaders = new ArrayList<>();
            for (Header h : parserHeaders) {
                if (h.getName().equals(CONNECTION_HEADER)) {
                    String[] connectionHeaders = h.getValue().split(", ");
                    cnnHeaders.addAll(Arrays.asList(connectionHeaders));
                    break;
                }
            }
            for (Header h : parserHeaders) {
                if (stripHeaders.contains(h.getName()) || cnnHeaders.contains(h.getName()))
                    continue;
                resultHeaders.add(h);
            }

            return resultHeaders;
        }

        private List<Header> replaceOrAddHeaders(List<Header> headers){
            HeaderDataSource headerDataSource = HeaderDataSource.newInstance();
            List<uci.localproxy.proxydata.header.Header> headersToAddOrModify = headerDataSource.getAllHeaders();
            if (!headersToAddOrModify.isEmpty()){
                for (uci.localproxy.proxydata.header.Header dataHeader : headersToAddOrModify){
                    boolean contains = false;
                    for (int i = 0; i < headers.size(); i++){
                        Header h = headers.get(i);
                        if (dataHeader.getName().equalsIgnoreCase(h.getName())){
                            headers.set(i, new BasicHeader(h.getName(), dataHeader.getValue()));
                            contains = true;
                        }
                    }
                    if (!contains) headers.add(new BasicHeader(dataHeader.getName(), dataHeader.getValue()));
                }
            }
            return headers;
        }

        public void run() {
            HttpParser parser = null;
            OutputStream os = null;
            long bytes = 0;

            try {
                parser = new HttpParser(this.localSocket.getInputStream());
                boolean validRequest = parser.parse();
                os = this.localSocket.getOutputStream();
                if (!validRequest) {
                    os.write("HTTP/1.1 400 Bad Request".getBytes());
                    os.write("\r\n\n".getBytes());
                    return;
                }
            } catch (Exception e) {
                return;
            }

            ConnectionDescriptor connectionDescriptor = getPackageConnectionDescritor(localSocket.getPort(),
                    localSocket.getInetAddress().getHostAddress());
            String packageNameSource = connectionDescriptor.getNamespace();

            Log.e("Request:", connectionDescriptor.getNamespace() +
                    "(" + connectionDescriptor.getName() + ")" + ": " + parser.getUri());

//            for (Header h : parser.getHeaders()) {
//                Log.e("Header", h.getName() + ":" + h.getValue());
//            }

            //Firewall action
            if (!firewallFilter(packageNameSource, parser.getUri())) {
                try {
                    os.write("HTTP/1.1 403 Forbidden".getBytes());
                    os.write("\r\n".getBytes());
                    os.write("\r\n".getBytes());
                    os.write("<h1>Forbidden by LocalProxy's firewall</h1>".getBytes());
                } catch (Exception e) {
                }
                return;
            }

            if (parser.getMethod().equals("CONNECT")) {
                bytes += resolveConnect(parser, os);
            } else {
                bytes += resolveOtherMethods(parser, os);
            }

            saveTrace(packageNameSource,
                    (connectionDescriptor.getName() != null) ? connectionDescriptor.getName() : Trace.UNKNOWN_APP_NAME,
                    parser.getUri(), bytes);

        }

        private void saveTrace(String packageName, String name, String requestedUrl, long bytesSpent) {
            Trace trace = Trace.newTrace(packageName, name, requestedUrl, bytesSpent, Calendar.getInstance().getTimeInMillis());
            TraceDataSource traceDataSource = TraceDataSource.newInstance();
            traceDataSource.saveTrace(trace);
            traceDataSource.releaseResources();
            traceDataSource = null;
        }

        private long resolveOtherMethods(HttpParser parser, OutputStream os) {
            long bytes = 0;
            InputStream inRemote = null;

            CloseableHttpClient client;
            boolean matches = (bypass != null) && StringUtils.matches(parser.getUri(), bypass);
            if (matches) {
                client = createNoDelegateClient();
//                client = HttpForwarder.this.noDelegateClient;
//                Log.i(getClass().getName(), "url matches bypass " + parser.getUri());
            } else {
                client = createDelegateClient();
//                client = HttpForwarder.this.delegateClient;
//                Log.i(getClass().getName(), "url does not matches bypass " + parser.getUri());
            }
            HttpUriRequest request;
            HttpResponse response;
            try {

//                Log.i(getClass().getName(), parser.getMethod() + " " + parser.getUri());
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

                List<Header> requestHeaders = getValidHeaders(parser.getHeaders());
                List<Header> modifiedHeaders = replaceOrAddHeaders(requestHeaders);
                request.setHeaders(modifiedHeaders.toArray(new Header[modifiedHeaders.size()]));

                response = client.execute(request);
                this.localSocket.shutdownInput();

                os.write(response.getStatusLine().toString().getBytes());
//                Log.e("STATUS-LINE", response.getStatusLine().toString());
                os.write("\r\n".getBytes());

                List<Header> responseHeaders = getValidHeaders(response.getAllHeaders());
                for (Header h : responseHeaders) {
                    os.write((h.toString() + "\r\n").getBytes());
                }

                if (response.getEntity() != null) {
                    os.write("\r\n".getBytes());
                    inRemote = response.getEntity().getContent();
                    bytes += new Piper(inRemote, os).startCopy();
                }

            } catch (Exception e) {
            } finally {
                if (inRemote != null) try {
                    inRemote.close();
                } catch (IOException e) {
                }
                try {
                    os.close();
                } catch (IOException e) {
                }
                try {
                    this.localSocket.close();
                } catch (IOException e) {
                }
                try {
                    client.close();
                } catch (IOException e) {
                }
            }

            return bytes;
        }

        private long resolveConnect(HttpParser parser, OutputStream os) {
//            Log.e(getClass().getName(), "CONNECT " + parser.getUri());
//            for (Header h : parser.getHeaders()){
//                Log.e("Header", h.getName() + " : " + h.getValue());
//            }

            long bytes = 0;
            boolean matches = (bypass != null) && StringUtils.matches(parser.getUri(), bypass);
            if (!matches) {
//                Log.i(getClass().getName(), "url does not matches bypass " + parser.getUri());
                bytes += doConnect(parser, os);
            } else {
//                Log.i(getClass().getName(), "url matches bypass " + parser.getUri());
                bytes += doConnectNoProxy(parser, os);
            }
            return bytes;
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

        //TODO> copiar cabezeras al destino al inicio de la peticion
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

                os.write("HTTP/1.1 200 OK".getBytes());
                os.write("\r\n\r\n".getBytes());

                threadPool.execute(new Piper(parser, outRemote));
                bytes += new Piper(inRemote, os).startCopy();

            } catch (Exception e) {
                Log.e("Error", parser.getMethod() + parser.getUri());
            } finally {
                if (remoteSocket != null) {
                    try {
                        remoteSocket.close();
                    } catch (Exception fe) {
                    }
                }
                try {
                    os.close();
                } catch (IOException e) {
                }
                try {
                    parser.close();
                } catch (IOException e) {
                }
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

                List<Header> requestHeaders = getValidHeaders(parser.getHeaders());
                List<Header> modifiedHeaders = replaceOrAddHeaders(requestHeaders);
                for(Header h : modifiedHeaders){
                    Log.e("Header", h.getName() + " : " + h.getValue());
                }

                remoteSocket = client.tunnel(
                        proxyHost,
                        targetHost,
                        credentials.getCredentials(AuthScope.ANY),
                        modifiedHeaders.toArray(new Header[modifiedHeaders.size()])
                        );

                inRemote = remoteSocket.getInputStream();
                outRemote = remoteSocket.getOutputStream();

                os.write("HTTP/1.1 200 OK".getBytes());
                os.write("\r\n\r\n".getBytes());
                threadPool.execute(new Piper(parser, outRemote));

                bytes += new Piper(inRemote, os).startCopy();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (inRemote != null) inRemote.close();
                } catch (IOException e) {
                }
                try {
                    if (outRemote != null) outRemote.close();
                } catch (IOException e) {
                }
                if (remoteSocket != null) {
                    try {
                        remoteSocket.close();
                    } catch (Exception fe) {
                    }
                }
            }

            return bytes;

        }

    }

}
