

import javax.swing.*;
import java.util.*;

public class ARPLayer implements BaseLayer {
    public int n_upper_layer_count = 0;    // number - 상위 레이어의 수
    public String p_layer_name = null;    // present - 레이어 이름
    public BaseLayer p_under_layer = null;  // present - 하위 레이어
    public ArrayList<BaseLayer> p_upper_layer_list = new ArrayList<>();    // 상위 레이어 저장 리스트


    // TODO : PortName 위한 변수 지정 필요
    String portName;


    byte[] sender_mac_addr = null;
    byte[] sender_ip_addr = null;
    byte[] target_mac_addr = null;
    byte[] target_ip_addr = null;

    public static byte[] host_mac_addr = new byte[6]; // 자신 (host) 의 mac 주소 저장하는 공간
    public static byte[] host_ip_addr = new byte[4];

    public void setPort(String p) {
        this.portName = p;
    }

    public void setHostMacAddr(byte[] addr) {
        host_mac_addr[0] = addr[0];
        host_mac_addr[1] = addr[1];
        host_mac_addr[2] = addr[2];
        host_mac_addr[3] = addr[3];
        host_mac_addr[4] = addr[4];
        host_mac_addr[5] = addr[5];
    }

    public void setHostIpAddr(byte[] addr) {
        host_ip_addr[0] = addr[0];
        host_ip_addr[1] = addr[1];
        host_ip_addr[2] = addr[2];
        host_ip_addr[3] = addr[3];
    }

    public static final int ARP_HEADER_LEN = 28;    // 28 byte 의 헤더 length
    ARPHeader arp_header = new ARPHeader();

    // 생성자 정의
    public ARPLayer(String layer_name) {
        this.p_layer_name = layer_name;
    }

    // ARP 헤더 정보 정의
    private class ARPHeader {
        byte[] hard_type;
        byte[] prot_type;
        byte[] hard_size;
        byte[] prot_size;
        byte[] op_code;
        ARPMacAddr sender_mac_addr;
        ARPIpAddr sender_ip_addr;
        ARPMacAddr target_mac_addr;
        ARPIpAddr target_ip_addr;

        public ARPHeader() {
            hard_type = new byte[2];
            prot_type = new byte[2];
            hard_size = new byte[1];
            prot_size = new byte[1];
            op_code = new byte[2];
            sender_mac_addr = new ARPMacAddr();
            sender_ip_addr = new ARPIpAddr();
            target_mac_addr = new ARPMacAddr();
            target_ip_addr = new ARPIpAddr();
        }

    }

    private class ARPMacAddr {
        // ARP 의 mac 주소 저장하는 inner class
        // 총 48 bit (6 byte)의 정보 저장
        private byte[] mac = new byte[6];

        public ARPMacAddr() {
            mac[0] = (byte) 0x00;
            mac[1] = (byte) 0x00;
            mac[2] = (byte) 0x00;
            mac[3] = (byte) 0x00;
            mac[4] = (byte) 0x00;
            mac[5] = (byte) 0x00;
        }
    }

    private class ARPIpAddr {
        // ARP 의 ip 주소 저장하는 inner class
        // 총 32 bit (4 byte)의 정보 저장
        private byte[] ip = new byte[4];

        public ARPIpAddr() {
            ip[0] = (byte) 0x00;
            ip[1] = (byte) 0x00;
            ip[2] = (byte) 0x00;
            ip[3] = (byte) 0x00;
        }
    }

    /**
     * 상위 레이어(IP Layer)에서 받은 데이터에서 헤더를 붙혀 하위 레이어(ethernet layer)로 보내는 메소드
     *
     * @param _sender_mac_addr 전송자의 mac 주소
     * @param _sender_ip_addr  전송자의 ip 주소
     * @param _target_mac_addr 목적지의 mac 주소
     * @param _target_ip_addr  목적지의 ip 주소
     * @param ip_bytes         ip 레이어에서 넘어오는 패킷
     * @param portName         포트 이름 정보
     * @return boolean 타입
     */
    public boolean send(byte[] _sender_mac_addr, byte[] _sender_ip_addr, byte[] _target_mac_addr,
                        byte[] _target_ip_addr, byte[] ip_bytes, String port_name) {
        // hard_type -> 1 로 고정
        // prot_type -> 0x0800 로 고정
        // hard_size -> 6, prot_size -> 4 (byte) 로 고정

        // TODO :
        //      : 1. 다른 IP 레이어에서 넘어온 IP와 MAC 주소 ARP 테이블에서 확인 --> 없는 경우 업데이트, Port Name도 함께 케시 업데이트
        //      : 2. 이후 send
        //      : opcode가 pingtest에 영향이 있을까 ? 현재 request 기준으로 구현됨 아닌 경우 opcode 0x0001일때만 update하고
        //      : 나머지 opcode는 무시하고 send

        String target_ip_string = ipByteArrToString(_target_ip_addr);
        Object[] value = new Object[5];

//        this.portName = port_name;
        //(1) cache table 우선 확인
        if (ApplicationLayer.arp_table.containsKey(target_ip_string)) {
            if (((String)((Object[])ApplicationLayer.arp_table.get(target_ip_string))[2]).equals("Complete")) {
                // 이미 테이블에 존재하는 key인 경우
                // ARP 헤더를 붙히지 않고 바로 Ethernet 레이어에게 보내준다
                System.out.println(this.portName + " :[ARP] this is ping!! to Eth ");

                //value = Arrays.copyOf(cache_table.get(target_ip_string), cache_table.get(target_ip_string).length);
                byte[] target_mac = ((byte[])((ApplicationLayer.arp_table.get(target_ip_string)))[1]);
                ((EthernetLayer)(this.getUnderLayer())).send(ip_bytes, ip_bytes.length, target_mac);
                return true;
            }
        } else {
            // TODO : port 정보 넣어주자
            // 이 외의 경우는 모두 "Incomplete" 상태
            // value[0]: 현재 테이블의 크기, value[1]: 상대방 mac 주소, value[2]: 상태, value[3]: 현재 시간, value[4]: 포트 이름
            System.out.println(this.portName + " : [ARP] this is ARP!!");
            value[0] = ApplicationLayer.arp_table.size() + 1;  // ??
            value[1] = _target_mac_addr;        // 전달 받은 타겟의 mac 주소 -> new bye[6] 형태
            value[2] = "Incomplete";
            value[3] = System.currentTimeMillis();
            value[4] = portName;


            // basic arp 이므로 케시 테이블 업데이트
//        if (!ipByteArrToString(_sender_ip_addr).equals(ipByteArrToString(_target_ip_addr))) {
//            System.out.println("No ip in cache table!! update!!");
//            ApplicationLayer.arp_table.put(target_ip_string, value);
//            ApplicationLayer.arp_table.updateCacheTable();
//        }

//        if (!ipByteArrToString(_sender_ip_addr).equals(ipByteArrToString(_target_ip_addr))) {
//        if (!ipByteArrToString(_sender_ip_addr).equals(ipByteArrToString(_target_ip_addr))) {
            System.out.println("No ip in cache table!! update!!");
            ApplicationLayer.arp_table.put(target_ip_string, value);
            ApplicationLayer.arp_table.updateCacheTable();
//        }

            // 다른 헤더 정보 입력 --> ARP request를 위해 ARP 헤더를 붙힌다.
            arp_header.hard_type[0] = (byte) 0x00;
            arp_header.hard_type[1] = (byte) 0x01;

            arp_header.prot_type[0] = (byte) 0x08;
            arp_header.prot_type[1] = (byte) 0x00;

            arp_header.hard_size[0] = (byte) 0x06;
            arp_header.prot_size[0] = (byte) 0x04;

            arp_header.op_code = new byte[]{0x00, 0x01};    // request : 0x00 01

            arp_header.sender_mac_addr.mac = host_mac_addr;
            arp_header.sender_ip_addr.ip = _sender_ip_addr;
            arp_header.target_mac_addr.mac = _target_mac_addr;
            arp_header.target_ip_addr.ip = _target_ip_addr;

            byte[] bytes = objToByte(arp_header);

            System.out.println(this.portName + " ARP --> Eth with ARP request");

            ((EthernetLayer) (this.getUnderLayer())).send(bytes, bytes.length, null);

            return true;
        }
        return false;
    }

    private int byte2ToInt(byte value1, byte value2) {
        return (int) ((value1 << 8) | (value2));
    }

    @Override
    public boolean receive(byte[] input) {
        // TODO : cacheTable에서 정보 update 시 port 정보 추가

        if (input == null) {
            return false;
        }
        Object[] value = new Object[5];
        byte[] opcode = new byte[2];
        System.arraycopy(input, 6, opcode, 0, 2);

        System.out.println(portName + " : [ARP] opcode : " + byte2ToInt(opcode[0], opcode[1]));

        String[] arp_request_array = this.arpRequest(input);
        String sender_mac = arp_request_array[0];
        String sender_ip = arp_request_array[1];
        String target_mac = arp_request_array[2];
        String target_ip = arp_request_array[3];

        if (opcode[0] == 0x00 && opcode[1] == 0x02) {
            System.out.println(portName + " : [ARP] this is ARP Reply!!.. table will change");
//
//            if (! ApplicationLayer.arp_table.containsKey(sender_ip)) {
//                //cache_table에 존재하지 않을 경우
//                value[0] =  ApplicationLayer.arp_table.size();
//                value[1] = this.sender_mac_addr;
//                value[2] = "Complete";
//                value[3] = System.currentTimeMillis();
//                value[4] = portName;
//            } else {
            if(ApplicationLayer.arp_table.cache_table.containsKey(sender_ip)){
                //cache_table에 존재하는 경우
                System.out.println("[ARP] found ip in cacheTable !!");
                Object[] temp = (Object[])ApplicationLayer.arp_table.get(sender_ip);


                value[0] = temp[0];
                value[1] = this.sender_mac_addr;
                value[2] = "Complete";
                value[3] = System.currentTimeMillis();
                value[4] = temp[4];

                ApplicationLayer.arp_table.put(sender_ip, value);
                ApplicationLayer.arp_table.updateCacheTable();
                return true;
            }
        }
        return true;
    }
    /**
     * receive함수에서 input이 들어오면 opcode, sender, target의 ip, mac 주소를 string값으로 반환하는 메소드
     * 전역변수(target_mac_addr, target_ip_addr, sender_mac_addr, sender_mac_ip) 초기화 진행
     *
     * @param input ethernet header를 제외한 ARP Request/Reply
     * @return [0] : senderMac
     * [1] : senderIp
     * [2] : targetMac
     * [3] : targetIp
     */
    public String[] arpRequest(byte[] input) {
        String[] arp_request_array = new String[4];

        byte[] sender_mac = new byte[6];
        byte[] sender_ip = new byte[4];
        byte[] target_mac = new byte[6];
        byte[] target_ip = new byte[4];

        System.arraycopy(input, 8, sender_mac, 0, 6);
        System.arraycopy(input, 14, sender_ip, 0, 4);
        System.arraycopy(input, 18, target_mac, 0, 6);
        System.arraycopy(input, 24, target_ip, 0, 4);

        //target, sender의 mac, ip 주소를 byte로 저장한 전역변수 초기화
        this.target_mac_addr = target_mac;
        this.target_ip_addr = target_ip;
        this.sender_mac_addr = sender_mac;
        this.sender_ip_addr = sender_ip;

        ////target, sender의 mac, ip 주소를 string으로 변환하여 배열로 저장
        arp_request_array[0] = this.macByteArrToString(sender_mac);
        arp_request_array[1] = this.ipByteArrToString(sender_ip);
        arp_request_array[2] = this.macByteArrToString(target_mac);
        arp_request_array[3] = this.ipByteArrToString(target_ip);

        return arp_request_array;

    }

    public byte[] objToByte(ARPHeader _arp_header) {
        byte[] header = new byte[ARP_HEADER_LEN];

        header[0] = _arp_header.hard_type[0];
        header[1] = _arp_header.hard_type[1];
        header[2] = _arp_header.prot_type[0];
        header[3] = _arp_header.prot_type[1];
        header[4] = _arp_header.hard_size[0];
        header[5] = _arp_header.prot_size[0];
        header[6] = _arp_header.op_code[0];
        header[7] = _arp_header.op_code[1];

        for (int i = 0; i < 6; i++) {
            // mac 주소
            header[i + 8] = _arp_header.sender_mac_addr.mac[i];
            header[i + 18] = _arp_header.target_mac_addr.mac[i];
        }

        for (int i = 0; i < 4; i++) {
            // ip 주소
            header[i + 14] = _arp_header.sender_ip_addr.ip[i];
            header[i + 24] = _arp_header.target_ip_addr.ip[i];
        }

        return header;
    }

//    /**
//     * 케시 테이블 업데이트
//     */
//    public void updateCacheTable() {
//        // TODO: PORT 정보 넣어주자
//
//        ApplicationLayer.arp_textarea.setText("");
//
//        Set keys = cache_table.keySet();
//        for (Iterator iterator = keys.iterator(); iterator.hasNext(); ) {
//            String key = (String) iterator.next();
//            Object[] value = (Object[]) cache_table.get(key);
//
//            if (value[2] == null) {
//                // TODO: Trash 값 없애기
////                ApplicationLayer.arp_textarea.append("       " + key + "\t" + "??????????????\t trash\n");
//            } else if (value[2].equals("Incomplete")) {
//                // TODO: Port Name 정보 입력 필요
//                ApplicationLayer.arp_textarea.append("       " + key + "\t" + "??????????????\t incomplete \t " + // value[5] (포트 이름) + ""
//                        "portName\n");
//            } else {
//                byte[] mac_addr_byte = (byte[]) value[1];
//                String mac_address_string = macByteArrToString(mac_addr_byte);
//                // TODO: Port Name 관련 정보 입력
//                ApplicationLayer.arp_textarea.append("       " + key + "\t" + mac_address_string + "\t complete\t" + // value[5] (포트 이름) + ""
//                        " portName\n");
//            }
//        }
//    }

    /**
     * byte 형태 mac 주소 문자열로 반환
     *
     * @param mac_byte_arr byte 배열형의 mac 주소
     * @return String 형태의 mac wnth
     */
    public String macByteArrToString(byte[] mac_byte_arr) {
        return String.format("%X:", mac_byte_arr[0]) + String.format("%X:", mac_byte_arr[1])
                + String.format("%X:", mac_byte_arr[2]) + String.format("%X:", mac_byte_arr[3])
                + String.format("%X:", mac_byte_arr[4]) + String.format("%X", mac_byte_arr[5]);
    }

    /**
     * byte 형태 ip 주소 문자열로 반환
     *
     * @param ip_byte_arr byte 배열형의 ip 주소
     * @return String 형태의 ip 주소
     */
    public String ipByteArrToString(byte[] ip_byte_arr) {
        return (ip_byte_arr[0] & 0xFF) + "." + (ip_byte_arr[1] & 0xFF) + "."
                + (ip_byte_arr[2] & 0xFF) + "." + (ip_byte_arr[3] & 0xFF);
    }


    /**
     * 케시 테이블 목록의 시간 확인 위한 스레드 상속 받은 클래스
     */
//    class CacheTimer implements Runnable {
//        HashMap<String, Object[]> cache_table;
//        final int INCOMPLETE_TIME_LIMIT = 3;
//        final int COMPLETE_TIME_LIMIT = 20;
//
//        public CacheTimer(HashMap<String, Object[]> _cache_table) {
//            this.cache_table = _cache_table;
//        }
//
//        @Override
//        public void run() {
//            while (true) {
//                Set key_set = this.cache_table.keySet();
//                ArrayList<String> delete_key = new ArrayList<>();
//
//                for (Iterator iterator = key_set.iterator(); iterator.hasNext(); ) {
//                    String key = "";
//                    if ((key = (String) iterator.next()) != null) {    // key 값 받아옴
//                        Object[] value = this.cache_table.get(key);
//
//                        if (((String) value[2]).equals("Incomplete") &&
//                                (System.currentTimeMillis() - (long) value[3]) / 60000 >= INCOMPLETE_TIME_LIMIT) {
//                            delete_key.add(key);
//                        }
//
//                        if (((String) value[2]).equals("Complete") &&
//                                (System.currentTimeMillis() - (long) value[3]) / 60000 >= COMPLETE_TIME_LIMIT) {
//                            delete_key.add(key);
//                        }
//                    }
//                }
//
//                for (String del_key : delete_key) {
//                    this.cache_table.remove(del_key);
//                }
//
//                // TODO : 케시 테이블 업데이트 메소드 구현
//                updateCacheTable();
//
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }

    @Override
    public String getLayerName() {
        return p_layer_name;
    }

    @Override
    public BaseLayer getUnderLayer() {
        if (p_under_layer == null) {
            return null;
        }
        return p_under_layer;
    }

    @Override
    public BaseLayer getUnderLayer(int nindex) {
        return null;
    }

    @Override
    public BaseLayer getUpperLayer(int nindex) {
        if (nindex < 0 || nindex > n_upper_layer_count || n_upper_layer_count < 0) {
            return null;
        }
        return p_upper_layer_list.get(nindex);
    }

    @Override
    public void setUnderLayer(BaseLayer pUnderLayer) {
        if (pUnderLayer == null) {
            return;
        }
        this.p_under_layer = pUnderLayer;
    }

    @Override
    public void setUpperLayer(BaseLayer pUpperLayer) {
        if (pUpperLayer == null) {
            return;
        }
        this.p_upper_layer_list.add(n_upper_layer_count++, pUpperLayer);
    }

    @Override
    public void setUnderUpperLayer(BaseLayer pUULayer) {
        BaseLayer.super.setUnderUpperLayer(pUULayer);
    }


    @Override
    public void setUpperUnderLayer(BaseLayer pUULayer) {
        this.setUpperLayer(pUULayer);
        pUULayer.setUnderLayer(this);
    }
}
