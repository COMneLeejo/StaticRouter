import java.util.ArrayList;
import java.util.Arrays;

public class EthernetLayer implements BaseLayer {
    public int n_upper_layer_count = 0;
    public String p_layer_name = null;
    public BaseLayer p_under_layer = null;
    public ArrayList<BaseLayer> p_upper_layer = new ArrayList<BaseLayer>();
    public final static int HEADER_SIZE = 14;
    private static byte[] arp_mac_dstaddr = null;

    public byte[] chat_file_dstaddr;
    public byte[] port_ethernet_addr = new byte[6];

    String port_name = "";

    public void setPort(String p) {
        this.port_name = p;
    }

    private class _ETHERNET_ADDR {
        private byte[] addr = new byte[6];

        public _ETHERNET_ADDR() {
            this.addr[0] = (byte) 0x00;
            this.addr[1] = (byte) 0x00;
            this.addr[2] = (byte) 0x00;
            this.addr[3] = (byte) 0x00;
            this.addr[4] = (byte) 0x00;
            this.addr[5] = (byte) 0x00;
        }
    }

    private class _ETHERNET_HEADER {
        _ETHERNET_ADDR enet_dstaddr;
        _ETHERNET_ADDR enet_srcaddr;
        byte[] enet_type;
        byte[] enet_data;

        public _ETHERNET_HEADER() {
            this.enet_dstaddr = new _ETHERNET_ADDR();
            this.enet_srcaddr = new _ETHERNET_ADDR();
            this.enet_type = new byte[2];
            this.enet_data = null;
        }
    }

    _ETHERNET_HEADER m_sHeader = new _ETHERNET_HEADER();

    public EthernetLayer(String pName) {
        // super(pName);
        // TODO Auto-generated constructor stub
        p_layer_name = pName;

    }

    public void setPort_ethernet_addr(byte[] portAddress) {
        // TODO Auto-generated method stub
        port_ethernet_addr = portAddress;

    }

    public void setEnetSrcAddress(byte[] srcAddress) {
        // TODO Auto-generated method stub
        m_sHeader.enet_srcaddr.addr[0] = srcAddress[0];
        m_sHeader.enet_srcaddr.addr[1] = srcAddress[1];
        m_sHeader.enet_srcaddr.addr[2] = srcAddress[2];
        m_sHeader.enet_srcaddr.addr[3] = srcAddress[3];
        m_sHeader.enet_srcaddr.addr[4] = srcAddress[4];
        m_sHeader.enet_srcaddr.addr[5] = srcAddress[5];

    }

    public void setEnetDstAddress(byte[] dstAddress) {
        // TODO Auto-generated method stub
        m_sHeader.enet_dstaddr.addr[0] = dstAddress[0];
        m_sHeader.enet_dstaddr.addr[1] = dstAddress[1];
        m_sHeader.enet_dstaddr.addr[2] = dstAddress[2];
        m_sHeader.enet_dstaddr.addr[3] = dstAddress[3];
        m_sHeader.enet_dstaddr.addr[4] = dstAddress[4];
        m_sHeader.enet_dstaddr.addr[5] = dstAddress[5];

    }

    public static void setMacAddrDstAddr(byte[] dstAddress) {
        arp_mac_dstaddr = dstAddress;
    }

    public byte[] ObjToByteDATA(_ETHERNET_HEADER Header, byte[] input, int length) {
        byte[] buf = new byte[length + HEADER_SIZE];

        buf[0] = Header.enet_dstaddr.addr[0];
        buf[1] = Header.enet_dstaddr.addr[1];
        buf[2] = Header.enet_dstaddr.addr[2];
        buf[3] = Header.enet_dstaddr.addr[3];
        buf[4] = Header.enet_dstaddr.addr[4];
        buf[5] = Header.enet_dstaddr.addr[5];

//        System.out.println("dst mac addr");
//        System.out.println(macByteArrToString(Header.enet_dstaddr.addr));

        buf[6] = Header.enet_srcaddr.addr[0];
        buf[7] = Header.enet_srcaddr.addr[1];
        buf[8] = Header.enet_srcaddr.addr[2];
        buf[9] = Header.enet_srcaddr.addr[3];
        buf[10] = Header.enet_srcaddr.addr[4];
        buf[11] = Header.enet_srcaddr.addr[5];

//        System.out.println("src mac addr");
//        System.out.println(macByteArrToString(Header.enet_srcaddr.addr));

        buf[12] = Header.enet_type[0];
        buf[13] = Header.enet_type[1];

        for (int i = 0; i < length; i++) {
            buf[HEADER_SIZE + i] = input[i];
        }
        return buf;
    }

    /**
     * ?????? ?????????(ARP Layer)?????? ?????? ??????????????? ????????? ?????? ?????? ?????????(NI Layer)??? ????????? ?????????
     *
     * @param input  ?????? ?????????????????? ?????? data
     * @param length ?????? ?????????????????? ?????? data??? ??????
     * @return boolean??????
     */
    public boolean send(byte[] input, int length, byte[] dstAddr) {
        m_sHeader.enet_data = input;

        if (m_sHeader.enet_data != null && m_sHeader.enet_data.length > 1500) {
            if(m_sHeader.enet_data != null ) {
                System.out.println("[Eth] ARP data input it is NULL!!!");
            }

            System.out.println("ETH error");
            return false;
        }

        byte[] frame;                           //(Header + input)?????? frame
        byte[] src_addr = new byte[6];
        byte[] dst_addr = new byte[6];          //????????? mac??????

        if(dstAddr == null){
            System.out.println(port_name + ": [Eth] dstArr is null... ARP is setting");
            m_sHeader.enet_type[0] = (byte) 0x08;
            m_sHeader.enet_type[1] = (byte) 0x06;   //?????? ???????????? ??????(ARP)
//            src_addr = selectSrcAddress(input);
            dst_addr = selectDstAddress(input);     //input?????? ????????? mac????????? ?????????
        }else {
            System.out.println(port_name + ": [Eth] this is Ping!!");
            m_sHeader.enet_type[0] = (byte) 0x08;
            m_sHeader.enet_type[1] = (byte) 0x00;   //?????? ???????????? ??????(IP): data(ping)??? ?????? ???
//            src_addr = port_ethernet_addr;
            dst_addr = dstAddr;
        }

        System.out.println(port_name + ": [Eth] dstArr is " + macByteArrToString(dst_addr) );
//        setEnetSrcAddress(src_addr);
        setEnetDstAddress(dst_addr);            //Header??? ????????? mac?????? ??????

//        System.out.println("src mac addr from ETH");
//        System.out.println(macByteArrToString(src_addr));
//        System.out.println("dst mac addr from ETH");
//        System.out.println(macByteArrToString(dst_addr));

        frame = ObjToByteDATA(m_sHeader, input, length);
        System.out.println(port_name + ": Eth --> NI");

        ((NILayer) getUnderLayer()).send(frame, length + HEADER_SIZE);      //NILayer??? send??????

        return true;
    }


    public byte[] selectSrcAddress(byte[] input) {
        byte[] src_addr = new byte[6];                      //????????? mac??????
        System.arraycopy(input, 8, src_addr, 0, 6);
        return src_addr;
    }

    /**
     * input?????? byte ?????? ?????????(dst) mac????????? ???????????? ?????????
     *
     * @param input ?????? ?????????????????? ?????? data
     * @return byte????????? ??????
     */
    public byte[] selectDstAddress(byte[] input) {
        byte[] dst_addr = new byte[6];                      //????????? mac??????
        if (input[6] == 0x00 && input[7] == 0x01) {         //ARP????????? ?????? Broadcast
            System.out.println("Ethernet Send: ARP Request");
            Arrays.fill(dst_addr, (byte) 0xff);
        } else if (input[6] == 0x00 && input[7] == 0x02) {  //ARP????????? ??????
            System.out.println("Ethernet Send: ARP Reply");
            System.arraycopy(input, 18, dst_addr, 0, 6);
        }
        return dst_addr;
    }

    /**
     * byte ?????? mac ?????? ???????????? ??????
     *
     * @param mac_byte_arr byte ???????????? mac ??????
     * @return String ????????? mac wnth
     */
    public String macByteArrToString(byte[] mac_byte_arr) {
        return String.format("%X:", mac_byte_arr[0]) + String.format("%X:", mac_byte_arr[1])
                + String.format("%X:", mac_byte_arr[2]) + String.format("%X:", mac_byte_arr[3])
                + String.format("%X:", mac_byte_arr[4]) + String.format("%X", mac_byte_arr[5]);
    }

    /**
     * ?????? ??????(NI Layer)???????????? ?????? ??????????????? Ethernet Header??? ?????? ???????????? ???????????? ?????????
     *
     * @param input  ?????? ?????????????????? ?????? data
     * @param length ?????? ?????????????????? ?????? data??? ??????
     * @return
     */
    public byte[] removeCappHeader(byte[] input, int length) {
        byte[] rebuf = new byte[length - HEADER_SIZE];
        m_sHeader.enet_data = new byte[length - HEADER_SIZE];
        System.arraycopy(input, HEADER_SIZE, rebuf, 0, length - HEADER_SIZE);
        return rebuf;
    }

    int count = 1;

    /**
     * ?????? ?????????(NI Layer)?????? ?????? ??????????????? ????????? ?????? ?????? ?????????(ARP Layer)??? ????????? ?????????
     *
     * @param input ?????? ?????????????????? ?????? data
     * @return boolean??????
     */
    public boolean receive(byte[] input) {
        byte[] data;

        System.out.println("\n\n" +"[Eth]" + count + " : "+ port_name + ": recieved from NI");
        count++;
        System.out.println(port_name + " : recieved dest mac addr");
        System.out.println(macByteArrToString(Arrays.copyOfRange(input, 0, 6)));
        System.out.println(port_name + " : recieved src mac addr");
        System.out.println(macByteArrToString(Arrays.copyOfRange(input, 6, 12)));

        if (!isSrcMyAddress(input)) {       //????????? ?????? ???????????? ??????
            if (isBrodcastAddress(input) || isDstMyAddress(input) ) {       //????????? mac????????? broAddr????????? ????????? ????????????

                data = removeCappHeader(input, input.length);

                if (input[12] == (byte)0x08 && input[13] == (byte)0x06){    //ARP

                    System.out.println(port_name + " : MAC --> ARP");
                    System.out.println(port_name + ": recieved dest mac addr");
                    System.out.println(macByteArrToString(Arrays.copyOfRange(input, 0, 6)));
                    System.out.println(port_name + " : recieved src mac addr");
                    System.out.println(macByteArrToString(Arrays.copyOfRange(input, 6, 12)));

                    ((ARPLayer)this.getUpperLayer(0)).receive(data);
                }else if (input[12] == 0x08 && input[13] == 0x00) {                      //IP, ?????? ARP??? ????????? ?????? ????????? header??? ????????? ??????????????????
                    System.out.println(port_name + " : MAC --> IP");
                    System.out.println(port_name + ": recieved dest mac addr");
                    System.out.println(macByteArrToString(Arrays.copyOfRange(input, 0, 6)));
                    System.out.println(port_name + " : recieved src mac addr");
                    System.out.println(macByteArrToString(Arrays.copyOfRange(input, 6, 12)));

                    ((IPLayer)this.getUpperLayer(1)).receive(data);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * ???????????? mac????????? port_ethernet_addr??? ????????? ???????????? ?????????
     *
     * @param add ???????????? input data
     * @return boolean??????
     */
    public boolean isPortEthernetAddress(byte[] add) {
        for (int i = 0; i < 6; i++) {
            if (add[i + 6] != port_ethernet_addr[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * ????????? mac????????? ????????? mac????????? ????????? ???????????? ?????????
     *
     * @param add ???????????? input data
     * @return boolean??????
     */
    public boolean isSrcMyAddress(byte[] add) {
        for (int i = 0; i < 6; i++) {
            if (add[i + 6] != m_sHeader.enet_srcaddr.addr[i])
                return false;
        }
        return true;
    }

    /**
     * ????????? mac????????? ????????? mac????????? ????????? ???????????? ?????????
     *
     * @param add ???????????? input data
     * @return boolean??????
     */
    public boolean isDstMyAddress(byte[] add) {
        for (int i = 0; i < 6; i++) {
            if (add[i] != m_sHeader.enet_srcaddr.addr[i])
                return false;
        }
        return true;
    }

    /**
     * ????????? mac????????? Broadcast??????(ff:ff:ff:ff:ff:ff)??? ????????? ???????????? ?????????
     *
     * @param add ???????????? input data
     * @return boolean??????
     */
    public boolean isBrodcastAddress(byte[] add) {
        for (int i = 0; i < 6; i++) {
            if (add[i] != (byte) 0xff)
                return false;
        }
        return true;
    }

    @Override
    public void setUnderLayer(BaseLayer pUnder_layer) {
        if (pUnder_layer == null)
            return;
        p_under_layer = pUnder_layer;
    }

    @Override
    public void setUpperLayer(BaseLayer pUpper_layer) {
        if (pUpper_layer == null)
            return;
        this.p_upper_layer.add(n_upper_layer_count++, pUpper_layer);
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
        return null;
    }
}