package android_final.jack.omok;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;


public class WifiDirectBroadcastReceiver extends BroadcastReceiver {
    WifiP2pManager manager;
    WifiP2pManager.Channel channel;
    GameSession main_activity;

    public WifiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, GameSession activity){
        this.manager = manager;
        this.channel = channel;
        this.main_activity = activity;
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();

        if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            //When the state of a connect has changed
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED){
            }
            else if(state == WifiP2pManager.WIFI_P2P_STATE_DISABLED) {
            }

        }
        else if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)){
            //when the list of connections has changed
            if(this.manager != null){
                this.manager.requestPeers(this.channel,
                        new WifiP2pManager.PeerListListener() {
                            @Override
                            public void onPeersAvailable(WifiP2pDeviceList peers) {
                                if(peers.getDeviceList().size() == 0){
                                    Toast.makeText(context, "No Opponents Found", Toast.LENGTH_SHORT);
                                }
                                else if(!peers.getDeviceList().equals(main_activity.devices)){
//                                else{
//                                    List<WifiP2pDevice> discovery_list = new ArrayList<>();
//                                    discovery_list.addAll(peers.getDeviceList());
//                                    for(WifiP2pDevice dev : discovery_list) {
//                                        main_activity.connectTo(dev);
//                                    }

                                    main_activity.devices.clear();;
                                    main_activity.devices.addAll(peers.getDeviceList());

                                    main_activity.device_names = new String[peers.getDeviceList().size()];
                                    main_activity.device_list = new WifiP2pDevice[peers.getDeviceList().size()];
                                    int index = 0;
                                    for(WifiP2pDevice device : peers.getDeviceList()){
                                        main_activity.device_names[index] = device.deviceName;
                                        main_activity.device_list[index] = device;
                                        index += 1;
                                    }



                                    main_activity.updateListView(main_activity.device_names);


                                }
                            }
                        }
                );

            }

        }
        else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){
            //A change in connections
            if(this.manager == null){
                return;
            }

            NetworkInfo info = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if(info.isConnected()){
                manager.requestConnectionInfo(channel, main_activity.connectionInfoListener);
            }
            else{
                Toast.makeText( main_activity, "Connection Lost", Toast.LENGTH_SHORT).show();
            }
        }

        else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)){
            //When the status of this device's wifi state has been changed
        }
    }
}
