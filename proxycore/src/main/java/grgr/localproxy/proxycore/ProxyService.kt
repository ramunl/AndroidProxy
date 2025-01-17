package grgr.localproxy.proxycore

import ProxyConnectionState
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import grgr.localproxy.proxycore.core.HttpForwarder
import grgr.localproxy.proxyutil.LOG_TAG
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ProxyService : Service() {
    private var user: String? = ""

    //    private ServerTask s;
    private var proxyThread: HttpForwarder? = null
    private val NOTIFICATION = 1337
    private var executor: ExecutorService? = null
    override fun onCreate() {
        proxyErrInfoState.value = "Service created"
        Log.d(LOG_TAG, "Service created")
        proxyConnectionState.value = ProxyConnectionState.CONNECTING
        startForeground(NOTIFICATION, notifyIt())
        executor = Executors.newSingleThreadExecutor()
        super.onCreate()
    }

    override fun onBind(arg0: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        Log.d(LOG_TAG, "onDestroy")
        executor?.shutdown()
        proxyThread?.halt()
        if (proxyThread != null) {
            //if (set_global_proxy) {
            //Toast.makeText(this, getString(R.string.OnNoProxy), Toast.LENGTH_LONG).show();
            /* try {
                    WifiProxyChanger.clearProxySettings(this);
                } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | InvocationTargetException |
                        NoSuchFieldException | IllegalAccessException | NullWifiConfigurationException | ApiNotSupportedException e) {
                    e.printStackTrace();
                }*/
            //  }
        }
        proxyErrInfoState.value = "Service destroyed"
        Log.d(LOG_TAG, "Service destroyed")
        proxyConnectionState.value = ProxyConnectionState.DISCONNECTED
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(LOG_TAG, "onStartCommand ${intent.extras}")
        if (intent.extras == null) {
            Log.d(LOG_TAG, "Error starting service")
        } else {
            Log.d(LOG_TAG, "Starting service..")
            user = intent.getStringExtra("user")!!
            val pass = intent.getStringExtra("pass")!!
            val server = intent.getStringExtra("server")!!
            val inPort = Integer.valueOf(intent.getStringExtra("inputport")!!)!!
            val outPort = Integer.valueOf(intent.getStringExtra("outputport")!!)!!
            // set_global_proxy = intent.getBooleanExtra("set_global_proxy", true);
            val bypass = intent.getStringExtra("bypass")!!
            val domain = intent.getStringExtra("domain") ?: ""
            proxyErrInfoState.value = "Starting service.. \n" +
                    "user: $user \n" +
                    "server: $server \n" +
                    "pass: $pass \n" +
                    "ip: $inPort \n" +
                    "op: $outPort \n" +
                    "bypass: $bypass \n" +
                    "domain: $domain "
            try {
                proxyThread = HttpForwarder(
                    server, inPort, user!!, pass, outPort, true, bypass,
                    domain, applicationContext
                )
                executor!!.execute(proxyThread)
               // proxyConnectionState.value = ProxyConnectionState.CONNECTED
                //notifyit()

                //configuring wifi settings
                /*try {
                    if (set_global_proxy) {
                      //  Toast.makeText(this, getString(R.string.OnProxy), Toast.LENGTH_LONG).show();
                        WifiProxyChanger.changeWifiStaticProxySettings("127.0.0.1", outputport, this);
                    }
                } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | InvocationTargetException |
                        NoSuchFieldException | IllegalAccessException | NullWifiConfigurationException | ApiNotSupportedException e) {
                    e.printStackTrace();
                }*/
            } catch (e: IOException) {
                Log.e(LOG_TAG, "Starting service error: ${e.localizedMessage}")
                proxyConnectionState.value = ProxyConnectionState.FAILED
                proxyErrInfoState.value = e.localizedMessage ?: e.stackTraceToString()
            }
            Log.d(LOG_TAG, "Starting service: ok!")
        }


        //START_REDELIVER_INTENT permite que si el sistema mata el servicio entonces cuando intenta reiniciarlo envia el mismo Intent que se envio para
        //iniciarlo por primera vez
        return START_REDELIVER_INTENT
    }

    private fun notifyIt(): Notification {
        /*
         * Este método asegura que el servicio permanece en el área de notificación
		 * */

        //Intent i = new Intent(this, ProxyActivity.class);
        // i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        // PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, 0);
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Notification.Builder(
            this,
            CHANNEL_ONE_ID
        ) else Notification.Builder(this)
        builder.setContentTitle("proxy is set") // .setSmallIcon(R.mipmap.ic_launcher5)
            // .setContentText(getApplicationContext().getString(R.string.excuting_proxy_service_notification) + " " + user)
            .setWhen(System.currentTimeMillis())
        // .setContentIntent(contentIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ONE_ID,
                CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.enableLights(true)
            //notificationChannel.setLightColor(getColor(R.color.colorPrimary));
            notificationChannel.setShowBadge(true)
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(notificationChannel)
        }
        val notification: Notification = builder.build()
        notification.flags = notification.flags or Notification.FLAG_NO_CLEAR
        return notification
    } //This from <SandroProxy proyect>/projects/SandroProxyPlugin/src/org/sandroproxy/plugin/gui/MainActivity.java

    //    private static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ProxyService.class.getName());
    /*
    private void ipTablesForTransparentProxy(boolean activate) {
        int processId = getApplicationInfo().uid;
        String excludedUid = String.valueOf(processId);
        String action = (activate) ? "A" : "D";
        String chainName = "spplugin";
        String chainName1 = "sppluginOutput";
        List<String> rules = new ArrayList<String>();

        String r = "iptables -t nat -" + action + " OUTPUT -p 6 -d 10.0.0.1 -j RETURN";
        String redirectRule = "iptables -t nat -" + action + " OUTPUT -p 6 --dport 80 -m owner ! --uid-owner " + excludedUid + " -j REDIRECT --to-port 8080 ";
        String redirectRule2 = "iptables -t nat -" + action + " OUTPUT -p 6 --dport 443 -m owner ! --uid-owner " + excludedUid + " -j REDIRECT --to-port 8080 ";
        String redirectRule3 = "iptables -t nat -" + action + " OUTPUT -p 6 --dport 5228 -m owner ! --uid-owner " + excludedUid + " -j REDIRECT --to-port 8080 ";

        try {
            Command command0 = new Command(4, r) {
                @Override
                public void commandOutput(int id, String line) {
                    super.commandOutput(id, line);
//                    Log.e("command output", line);
                }
            };
            Command command = new Command(0, redirectRule);
            Command command1 = new Command(1, redirectRule2);
            Command command2 = new Command(1, redirectRule3);
            Shell shell = getShell(true);
            shell.add(command0);
            shell.add(command);
            shell.add(command1);
            shell.add(command2);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (RootDeniedException e) {
            e.printStackTrace();
        }
//        String redirectRule2 = "iptables -t nat -" + action + " OUTPUT -p 6 --dport 0:65535 -m owner ! --uid-owner " + excludedUid + " -j REDIRECT --to-port 8080 ";
//        rules.add(redirectRule2);

//        String rule0 = "iptables -" + action + " POSTROUTING -t nat -j MASQUERADE";
//        String rule1 = "iptables -t nat -" + action + " OUTPUT -m owner --uid-owner " + excludedUid  + " -j ACCEPT";
//        String rule2 = "iptables -t nat -" + action + " OUTPUT -p tcp --dport 80 -j REDIRECT --to-port 8080";
//        String rule3 = "iptables -t nat -" + action + " OUTPUT -p tcp --dport 443 -j REDIRECT --to-port 8080";
////        String rule3 = "iptables -t nat -" + action + " OUTPUT -p tcp --dport 80 -j DNAT --to :8080";
//        rules.add(rule0);
//        rules.add(rule1);
//        rules.add(rule2);
//        rules.add(rule3);


//        if (activate){
//            action = "A";
//            String createChainRule = "iptables --new " + chainName; rules.add(createChainRule);
//            String createNatChainRule = "iptables -t nat --new " + chainName; rules.add(createNatChainRule);
//            String createNatChainRule1 = "iptables -t nat --new " + chainName1; rules.add(createNatChainRule1);
//        }else{
//            action = "D";
//            String dettachChainRule = "iptables -D INPUT -j " + chainName; rules.add(dettachChainRule);
//            String dettachNatChainRule = "iptables -t nat -D PREROUTING -j " + chainName; rules.add(dettachNatChainRule);
//            String dettachNatChainRule1 = "iptables -t nat -D OUTPUT -j " + chainName1; rules.add(dettachNatChainRule1);
//        }
//

//        Process p;
//        try {
//            p = Runtime.getRuntime().exec(new String[]{"su", "-c", "sh"});
//
//            DataOutputStream stdin = new DataOutputStream(p.getOutputStream());
//            DataInputStream stdout = new DataInputStream(p.getInputStream());
//            InputStream stderr = p.getErrorStream();
//
//            for (String rule : rules) {
////                logger.finest(rule);
//                stdin.writeBytes(rule + "\n");
//                stdin.writeBytes("echo $?\n");
//                Thread.sleep(100);
//                byte[] buffer = new byte[4096];
//                int read = 0;
//                String out = new String();
//                String err = new String();
//                while (true) {
//                    read = stdout.read(buffer);
//                    out += new String(buffer, 0, read);
//                    if (read < 4096) {
//                        break;
//                    }
//                }
//                while (stderr.available() > 0) {
//                    read = stderr.read(buffer);
//                    err += new String(buffer, 0, read);
//                    if (read < 4096) {
//                        break;
//                    }
//                }
//                if (out != null && out.trim().length() > 0) Log.d(getClass().getName(), out);
//                if (err != null && err.trim().length() > 0) Log.d(getClass().getName(), err);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
////            logger.finest("Error executing rules: " + e.getMessage());
//        }
    }
    */
    companion object {
        fun onFailed(e: Exception) {
            Log.d(LOG_TAG, "onFailed $e")
            proxyErrInfoState.value = e.localizedMessage ?: e.stackTraceToString()
            proxyConnectionState.value = ProxyConnectionState.FAILED
        }

        const val CHANNEL_ONE_ID = "grgr.localproxy.proxycore.proxyservice"
        const val CHANNEL_ONE_NAME = "Proxy Service"
        val proxyConnectionState = mutableStateOf(ProxyConnectionState.IDLE)
        val proxyErrInfoState = mutableStateOf("no errors")
    }
}