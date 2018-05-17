package android_final.jack.omok;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class GameSession extends AppCompatActivity {
    WifiManager wifi_manager;
    WifiP2pManager p2p_manager;
    WifiP2pManager.Channel channel;
    WifiDirectBroadcastReceiver receiver;

    IntentFilter filter;

    Button discover_button;
    EditText input_field;
    Button send_button;

    public List<WifiP2pDevice> devices = new ArrayList<>();
    String[] device_names;
    WifiP2pDevice[] device_list;

    ClientThread client;
    ServerThread server;
    SendReceive sendReceive;

    static final int PORT = 8890;
    static final int MESSAGE_READ = 1;
    static final int TEST_COMMAND = 2;

    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            InetAddress groupOwnerAddress = info.groupOwnerAddress;

            if (info.groupFormed && info.isGroupOwner) {
                Toast.makeText(getApplicationContext(), "Host", Toast.LENGTH_SHORT).show();
                server = new ServerThread();
                server.start();
            } else if (info.groupFormed) {
                Toast.makeText(getApplicationContext(), "Client", Toast.LENGTH_SHORT).show();
                client = new ClientThread(groupOwnerAddress);
                client.start();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_session);

        initializeActivity();

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(this.receiver, this.filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(this.receiver);
    }

    private void initializeActivity() {
        this.wifi_manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        this.p2p_manager = (WifiP2pManager) getApplicationContext().getSystemService(Context.WIFI_P2P_SERVICE);
        this.channel = this.p2p_manager.initialize(this, getMainLooper(), null);
        this.receiver = new WifiDirectBroadcastReceiver(this.p2p_manager, this.channel, this);

        this.filter = new IntentFilter();
        this.filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        this.filter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        this.filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        this.filter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

//        this.discover_button = findViewById(R.id.discover);
//        this.input_field = findViewById(R.id.input);
//        this.send_button = findViewById(R.id.send);
//
//
//        wifi_manager.setWifiEnabled(true);
//
//        this.send_button.setOnClickListener(
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        String val = MainActivity.this.input_field.getText().toString();
//                        Log.i("TEST", "Sent a message " + val);
//                        Log.i("TEST", "Send Recieve is " + sendReceive.toString());
//                        sendReceive.write(val.getBytes());
//
//                    }
//                }
//        );

//
//        this.discover_button.setOnClickListener(
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        MainActivity.this.p2p_manager.discoverPeers(MainActivity.this.channel,
//                                new WifiP2pManager.ActionListener() {
//                                    @Override
//                                    public void onSuccess() {
//                                        Toast.makeText(MainActivity.this, "Discovery Started", Toast.LENGTH_SHORT).show();
//                                    }
//
//                                    @Override
//                                    public void onFailure(int reason) {
//                                        Toast.makeText(MainActivity.this, "Failed to start discovery", Toast.LENGTH_SHORT).show();
//                                    }
//                                });
//                    }
//                }
//        );

//        ((ListView) findViewById(R.id.list)).setOnItemClickListener(
//                new AdapterView.OnItemClickListener() {
//                    @Override
//                    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
//                        WifiP2pDevice device = device_list[position];
//                        WifiP2pConfig config = new WifiP2pConfig();
//                        config.deviceAddress = device.deviceAddress;
//
//                        p2p_manager.connect(MainActivity.this.channel, config,
//                                new WifiP2pManager.ActionListener() {
//                                    @Override
//                                    public void onSuccess() {
//                                        Toast.makeText(getApplicationContext(), "Connected to " + device_names[position], Toast.LENGTH_SHORT).show();
//                                    }
//
//                                    @Override
//                                    public void onFailure(int reason) {
//                                        Toast.makeText(getApplicationContext(), "Failed to connect", Toast.LENGTH_SHORT).show();
//
//                                    }
//                                });
//                    }
//                }
//        );


    }


//
//    public void updateListView(String[] device_names) {
//
//        ArrayAdapter<String> device_name_adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, device_names);
//        ListView list_view = findViewById(R.id.list);
//        list_view.setAdapter(device_name_adapter);
//    }


        Handler handler = new Handler(
                new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message msg) {
                        switch (msg.what) {
                            case MESSAGE_READ:
//                            byte[] readBuff = (byte[]) msg.obj;
//                            String tempMsg = new String(readBuff, 0, msg.arg1);

                                String tempMsg = (msg.obj).toString();
//                            ((Button) findViewById(R.id.send)).setText(tempMsg.toString());
                                Toast.makeText(getApplicationContext(), tempMsg, Toast.LENGTH_SHORT).show();
                                break;
                            case TEST_COMMAND:
                                Toast.makeText(getApplicationContext(), "test command", Toast.LENGTH_SHORT).show();
//                            ((Button) findViewById(R.id.send)).setText("Test");
                                break;
                        }

                        return true;
                    }
                }
        );

        public class ServerThread extends Thread {
            Socket socket;
            ServerSocket serverSocket;

            @Override
            public void run() {
                try {
                    serverSocket = new ServerSocket(PORT);
                    socket = serverSocket.accept();
                    sendReceive = new SendReceive(socket);
                    sendReceive.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }

        public class ClientThread extends Thread {
            Socket socket;
            String hostAddress;

            public ClientThread(InetAddress address) {
                hostAddress = address.getHostAddress();
                socket = new Socket();

            }

            @Override
            public void run() {
                try {
                    socket.connect(new InetSocketAddress(hostAddress, PORT), 500);
                    sendReceive = new SendReceive(socket);
                    sendReceive.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        public class SendReceive extends Thread {
            Socket socket;
            InputStream in_stream;
            OutputStream out_stream;

            public SendReceive(Socket skt) {
                socket = skt;
                try {
                    in_stream = skt.getInputStream();
                    out_stream = skt.getOutputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            //Acts as the receiving end of the sendRecieve. Delegates recieving message to the handler
            @Override
            public void run() {
                byte[] buffer = new byte[1024];
                int bytes;

                while (socket != null) {
                    try {
                        //Reads FROM inputstream and puts it in the buffer. Returns the size of the input
                        bytes = in_stream.read(buffer);
                        if (bytes > 0) {
                            String tempMsg = new String(buffer, 0, bytes);
                            if (tempMsg.equals("test")) {
                                handler.obtainMessage(TEST_COMMAND, tempMsg).sendToTarget();
                            } else {
                                handler.obtainMessage(MESSAGE_READ, tempMsg).sendToTarget();
                            }
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }

            //The send for sendRecieve. writes to the output stream, which is the inputstream for the
            //other end of the socket connection
            public void write(byte[] bytes) {
                try {
                    out_stream.write(bytes);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }


        }

    @Override
    protected void onDestroy () {
        super.onDestroy();
//            wifi_manager.setWifiEnabled(false);
    }

}


