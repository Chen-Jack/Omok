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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
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


    public List<WifiP2pDevice> devices = new ArrayList<>();
    String[] device_names;
    WifiP2pDevice[] device_list;

    ClientThread client;
    ServerThread server;
    SendReceive sendReceive;

    Board game_board;

    Button discover_button;
    TextView status;


    static final int PORT = 8890;
    static final int PIECE_PLAYED = 0;
    static final int NEXT_TURN = 1;
    static final int WINNER = 2;
    static final int LOSER = 3;

    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            InetAddress groupOwnerAddress = info.groupOwnerAddress;

            //Once there is a connection established, start the game.
            if (info.groupFormed && info.isGroupOwner) {
                Toast.makeText(getApplicationContext(), "White", Toast.LENGTH_SHORT).show();
                server = new ServerThread();
                server.start();

                GameSession.this.game_board = new Board( GameSession.this, true);
                ViewGroup layout = (ViewGroup)findViewById(R.id.board_space);
                layout.addView(game_board);

            }
            else if (info.groupFormed) {
                Toast.makeText(getApplicationContext(), "Black", Toast.LENGTH_SHORT).show();
                client = new ClientThread(groupOwnerAddress);
                client.start();

                GameSession.this.game_board = new Board( GameSession.this, false);
                ViewGroup layout = (ViewGroup)findViewById(R.id.board_space);
                layout.addView(game_board);
            }
        }
    };

    public void connectTo(WifiP2pDevice connection_device){
        final WifiP2pDevice device = connection_device;
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;

        p2p_manager.connect(GameSession.this.channel, config,
                new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getApplicationContext(), "Connected to " + device.deviceName, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reason) {
//                        Toast.makeText(getApplicationContext(), "Failed to connect", Toast.LENGTH_SHORT).show();
                        Log.i("TEST", "Failed to connect");

                    }
                });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_session);

        initializeActivity();

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

        this.discover_button = findViewById(R.id.discover_btn);
        this.discover_button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GameSession.this.p2p_manager.discoverPeers(GameSession.this.channel,
                                new WifiP2pManager.ActionListener() {
                                    @Override
                                    public void onSuccess() {
                                        Toast.makeText(GameSession.this, "Looking for Opponent", Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onFailure(int reason) {
                                        Toast.makeText(GameSession.this, "Failed to Search", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }
        );


        ((ListView) findViewById(R.id.list_view)).setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                        WifiP2pDevice device = device_list[position];
                        WifiP2pConfig config = new WifiP2pConfig();
                        config.deviceAddress = device.deviceAddress;

                        p2p_manager.connect(GameSession.this.channel, config,
                                new WifiP2pManager.ActionListener() {
                                    @Override
                                    public void onSuccess() {
                                        Toast.makeText(getApplicationContext(), "Connected to " + device_names[position], Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onFailure(int reason) {
                                        Toast.makeText(getApplicationContext(), "Failed to connect", Toast.LENGTH_SHORT).show();

                                    }
                                });
                    }
                }
        );


    }



    public void updateListView(String[] device_names) {

        ArrayAdapter<String> device_name_adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, device_names);
        ListView list_view = findViewById(R.id.list_view);
        list_view.setAdapter(device_name_adapter);
    }


        Handler handler = new Handler(
                new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message msg) {
                        switch (msg.what) {
                            case NEXT_TURN:
                                game_board.startTurn();
                                break;

                            case PIECE_PLAYED:
                                //extract coordinates from msg
                                int spot_x = msg.arg1;
                                int spot_y = msg.arg2;
//                                Board.Spot s = game_board.at(spot_x, spot_y);
                                game_board.updateBoardState(spot_x, spot_y, true);
                                break;
                            case WINNER:
                                Toast.makeText(getApplicationContext(), "Loser", Toast.LENGTH_SHORT).show();
                                //Create alert dialog that you lost
                                AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
                                builder.setMessage("Lost");
                                builder.create();

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
                        bytes = in_stream.read(buffer);
                        if (bytes > 0) {
                            String tempMsg = new String(buffer, 0, bytes);
                            String[] msg_arr = tempMsg.split("\\s+");
                            Integer command = Integer.parseInt(msg_arr[0]);

                            if (command.equals(NEXT_TURN)) {
                                //obtainMessage(int what, Object obj)
                                handler.obtainMessage(NEXT_TURN, tempMsg).sendToTarget();
                            }
                            else if (command.equals(PIECE_PLAYED)){
                                Integer spot_x = Integer.parseInt(msg_arr[1]);
                                Integer spot_y = Integer.parseInt(msg_arr[2]);
//
//                                Log.i("TEST", "Spot x is " + spot_x);
//                                Log.i("TEST" , "Spot y is " + spot_y);

                                //obtainMessage(int what, int arg1, int arg2)
                                handler.obtainMessage(PIECE_PLAYED, spot_x, spot_y, tempMsg).sendToTarget();
                            }
                            else if (command.equals(WINNER)){

                                //obtainMessage(int what, int arg1, int arg2)
                                handler.obtainMessage(WINNER, tempMsg).sendToTarget();
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
    protected void onResume() {
        super.onResume();
        registerReceiver(this.receiver, this.filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(this.receiver);
    }

    @Override
    protected void onDestroy () {
        super.onDestroy();
//            wifi_manager.setWifiEnabled(false);
    }

}


