


import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;

public class NILayer implements BaseLayer {

    public int n_upper_layer_count = 0;
    public String p_layer_name = null;
    public BaseLayer p_under_layer = null;
    public ArrayList<BaseLayer> p_upper_layer = new ArrayList<BaseLayer>();
    private Receive_Thread thread = null;
    int m_i_num_adapter;
    public Pcap m_adapter_object;
    public PcapIf device;
    public List<PcapIf> m_adapter_list;
    StringBuilder errbuf = new StringBuilder();

    public NILayer(String pName) {
        // super(pName);
        p_layer_name = pName;

        m_adapter_list = new ArrayList<PcapIf>();
        m_i_num_adapter = 0;
        setAdapterList();
    }

    public void packetStartDriver() {
        int snaplen = 64 * 1024; // Capture all packets, no trucation
        int flags = Pcap.MODE_PROMISCUOUS; // capture all packets
        int timeout = 10 * 1000; // 10 seconds in millis
        m_adapter_object = Pcap.openLive(m_adapter_list.get(m_i_num_adapter).getName(), snaplen, flags, timeout, errbuf);
    }
    //실제 기기를 열어주는 기능을 하는 것으로 snaplen는 패킷당 저장할 바이스 수, 실제 datalink계층부터 패킷의 크기를 계산하여 원하는 부분만을 얻어오면 되는 것입니다.
    //헤더정보만을 보고싶은데 쓸데없이 데이터까지 받을 필요는 없겠죠. 데이터까지 보고싶으면 snaplen를 크게 하면 됩니다.
    //PROMISCUOUS는 1이며 네트웍 디바이스에 오는 모든 패킷을 받겠다는 의미입니다.
    //이 모드를 자세하게 설명하면 Ethernet은 모든 패킷이 broadcasting되며 일단 모든 네트웍 디바이스는 동일 네트웍내의 다른 호스트의 패킷도 일단 접하게 됩니다.
    //그러나, 네트웍 디바이스는 기본적으로 자신의 패킷만을 받게끔 되어있습니다. 그러므로 다른 호스트의 패킷은 버리게 되는 것입니다.
    //그러나 promiscuous모드로 디바이스 모드를 바꾸게 되면 모든 패킷을 받아들이게 되는 것입니다. 모든 네트워크 모니터링 프로그램들은 모두 이 모드를 사용하게 됩니다.
    //세 번째 인자는 패킷이 버퍼로 전달될 때 바로 전달되는 것이 아니라 위에서 명시한 시간을 넘겼을 때나 버퍼가 다 채워졌을 때 응용프로그램으로 전달되는 것입니다.
    //https://wiki.kldp.org/KoreanDoc/html/Libpcap-KLDP/function.html

    public PcapIf getAdapterObject(int iIndex) {
        return m_adapter_list.get(iIndex);
    }

    public List<PcapIf> getAdapterList(){
        return this.m_adapter_list;
    }

    public void setAdapterNumber(int iNum) {
        System.out.println("initialized");
        m_i_num_adapter = iNum;
        packetStartDriver();
        receive();
    }


    public void setAdapterList() {
        int r = Pcap.findAllDevs(m_adapter_list, errbuf);   // Bring All Network Adapter list of Host PC
        System.out.println("Number of I/F : "+m_adapter_list.size());
        if (r == Pcap.NOT_OK || m_adapter_list.isEmpty()) { // Error if there are no Network Adapter
            System.err.printf("Can't read list of devices, error is %s", errbuf.toString());
        }
    }

    public boolean send(byte[] input, int length) {
//        System.out.println("send from NI");
//        printSrcMac(input);
        ByteBuffer buf = ByteBuffer.wrap(input);
        if (m_adapter_object.sendPacket(buf) != Pcap.OK) {
            System.err.println(m_adapter_object.getErr());
            return false;
        }
        System.out.println("send packet to host!!");

        System.out.println("total packet length : " + input.length + "\n\n");
        return true;
    }

    // debug mac addr print
    public void printSrcMac(byte[] input){
        System.out.println("debug print NI src mac addr");
        for(int i = 0; i < 6; i++){
            System.out.print(String.format("%X ", input[6+i]));
        }
        System.out.println();
    }

    public boolean  receive() {

        if(thread != null) {
            return false;
        }
        else {
            thread = new Receive_Thread(m_adapter_object, (EthernetLayer)this.getUpperLayer(0));
            Thread obj = new Thread(thread);
            obj.start();
            count++;
            System.out.println("[NI] " + count + " : received from NI");
            return false;
        }
    }

    int count = 0;

    @Override
    public void setUnderLayer(BaseLayer pUnderLayer) {
        // TODO Auto-generated method stub
        if (pUnderLayer == null)
            return;
        p_under_layer = pUnderLayer;
    }

    @Override
    public void setUpperLayer(BaseLayer pUpperLayer) {
        // TODO Auto-generated method stub
        if (pUpperLayer == null)
            return;
        this.p_upper_layer.add(n_upper_layer_count++, pUpperLayer);
    }

    @Override
    public String getLayerName() {
        // TODO Auto-generated method stub
        return p_layer_name;
    }

    @Override
    public BaseLayer getUnderLayer() {
        if (p_under_layer == null)
            return null;
        return p_under_layer;
    }

    @Override
    public BaseLayer getUpperLayer(int nindex) {
        // TODO Auto-generated method stub
        if (nindex < 0 || nindex > n_upper_layer_count || n_upper_layer_count < 0)
            return null;
        return p_upper_layer.get(nindex);
    }

    @Override
    public void setUpperUnderLayer(BaseLayer pUULayer) {
        this.setUpperLayer(pUULayer);
        pUULayer.setUnderLayer(this);
    }

    @Override
    public BaseLayer getUnderLayer(int nindex) {
        // TODO Auto-generated method stub
        return null;
    }
}

class Receive_Thread implements Runnable {
    byte[] data;
    Pcap adapter_object;
    BaseLayer upper_layer;

    public Receive_Thread(Pcap m_adapter_object, BaseLayer m_upper_layer) {
        // TODO Auto-generated constructor stub
        adapter_object = m_adapter_object;
        upper_layer = m_upper_layer;
    }

    @Override
    public void run() {
        while (true) {
            PcapPacketHandler<String> jpacketHandler = new PcapPacketHandler<String>() {
                public void nextPacket(PcapPacket packet, String user) {
                    data = packet.getByteArray(0, packet.size());
//                    System.out.println("running");
                    upper_layer.receive(data);
                }
            };

            adapter_object.loop(100000, jpacketHandler, "");
        }
    }
}