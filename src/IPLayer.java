
import java.util.ArrayList;

public class IPLayer implements BaseLayer {
    public int number_of_upper_layer = 0;
    public int number_of_under_layer = 0;
    public String present_layer_name = null;
    public BaseLayer under_layer = null;
    public ArrayList<BaseLayer> array_of_upper_layer = new ArrayList<BaseLayer>();
    public ArrayList<BaseLayer> array_of_under_layer = new ArrayList<BaseLayer>();

    public final static int size_of_ip_header = 20;             //IP 헤더의 크기
    public final static int pos_of_ip_src_from_ip_header = 0;  //IP 헤더에서 src IP의 시작위치
    public final static int pos_of_ip_dst_from_ip_header = 4;  //IP 헤더에서 dst IP의 시작위치
    public final static int size_of_ip_addr = 4;               //IP 주소 크기

    public LayerManager layer_manager;
    public RoutingTable routing_table;
    public IPLayer another_ip_layer;
    public String port_name;

    public void setRouter(RoutingTable routing_table) {
        this.routing_table = routing_table;
    }

    public void setPort(String port_name) {
        this.port_name = port_name;
    }

    public void setAnotherIPLayer(IPLayer another_ip_layer) {
        this.another_ip_layer = another_ip_layer;
    }

    private class IPLayer_Header {

        byte[] ip_version_len;
        byte[] ip_service_type;
        byte[] ip_packet_len;
        byte[] ip_datagram_ID;
        byte[] ip_offset;
        byte[] ip_ttl;
        byte[] ip_protocol;
        byte[] ip_checksum;
        byte[] ip_src_addr;		// IP address of source
        byte[] ip_dst_addr;		// IP address of destination
        byte[] ip_data;			// data

        public IPLayer_Header(){
            this.ip_version_len = new byte[1];
            this.ip_service_type = new byte[1];
            this.ip_packet_len = new byte[2];
            this.ip_datagram_ID = new byte[2];
            this.ip_offset = new byte[2];
            this.ip_ttl = new byte[2];
            this.ip_protocol = new byte[1];
            this.ip_checksum = new byte[2];
            this.ip_src_addr = new byte[size_of_ip_addr];
            this.ip_dst_addr = new byte[size_of_ip_addr];
            this.ip_data = null;
        }
    }

    IPLayer_Header ip_header = new IPLayer_Header();

    public IPLayer(String layer_name) {
        present_layer_name = layer_name;
        ip_header = new IPLayer_Header();
    }

    public void setIPSrcAddress(byte[] src_address) {
        for (int i = 0; i < size_of_ip_addr; i++)
            ip_header.ip_src_addr[i] = src_address[i];
    }

    public void setIPDstAddress(byte[] dst_address) {
        for (int i = 0; i < size_of_ip_addr; i++)
            ip_header.ip_dst_addr[i] = dst_address[i];
    }

    public byte[] objToByte(IPLayer_Header header, byte[] input, int length) {
        byte[] buf = new byte[length + size_of_ip_header];

        buf[0] = header.ip_version_len[0];
        buf[1] = header.ip_service_type[0];
        buf[2] = header.ip_packet_len[0];
        buf[3] = header.ip_packet_len[1];
        buf[4] = header.ip_datagram_ID[0];
        buf[5] = header.ip_datagram_ID[1];
        buf[6] = header.ip_offset[0];
        buf[7] = header.ip_offset[1];
        buf[8] = header.ip_ttl[0];
        buf[9] = header.ip_protocol[0];
        buf[10] = header.ip_checksum[0];
        buf[11] = header.ip_checksum[1];

        for (int i = 0; i < size_of_ip_addr; i++) {
            buf[12 + i] = header.ip_src_addr[i];
            buf[16 + i] = header.ip_dst_addr[i];
        }
        for (int i = 0; i < length; i++) {
            buf[size_of_ip_header + i] = input[i];
        }
        return buf;
    }

    public byte[] removeIPHeader(byte[] input, int length) {

        byte[] return_data = new byte[length - size_of_ip_header];
        for(int i = 0; i < length - size_of_ip_header; i++) {
            return_data[i] = input[i + size_of_ip_header];
        }
        return return_data;
    }

    /**
     * 받은 데이터에서 ip header를 제거하여 AppLayer로 전송
     *
     * @param input 받은 데이터
     * @return 전송 결과
     */
    public synchronized boolean receive(byte[] input) {

        byte[] data = removeIPHeader(input, input.length);

        if(areSrcIpAndMyAddrTheSame(input)) return false;

        //dstIP와 내 IP가 같다 = 나에게 온 패킷
        if(areDstIpAndMyAddrTheSame(input)) {
            this.getUpperLayer(0).receive(data);
            return true;
        } else {
            byte[] dst_ip = new byte[4];//input 확인하고 srcPos값 확인
            System.arraycopy(input, 16, dst_ip, 0, size_of_ip_addr);

            Object[] value = routing_table.findEntry(dst_ip);
            if (value == null) return false;

           byte[] net;
            if ((boolean) value[4]) //flag G is selected
                net = (byte[]) value[2];
            else
                net = (byte[]) dst_ip;

            byte[] bytes = objToByte(ip_header, input, input.length);

            ((ARPLayer)another_ip_layer.getUnderLayer(0)).send(new byte[6], another_ip_layer.ip_header.ip_src_addr, new byte[6], dst_ip, bytes, another_ip_layer.port_name);

        }
        return false;
    }

    public boolean areDstIpAndMyAddrTheSame(byte[] input) {
        for(int i = 0; i < size_of_ip_addr; i++)
            if(input[i + pos_of_ip_dst_from_ip_header] != ip_header.ip_src_addr[i]) return false;
        return true;
    }

    public boolean areSrcIpAndMyAddrTheSame(byte[] input) {
        for(int i = 0; i < size_of_ip_addr; i++)
            if(input[i + pos_of_ip_src_from_ip_header] != ip_header.ip_src_addr[i]) return false;
        return true;
    }

    @Override
    public String getLayerName() {
        return present_layer_name;
    }

    @Override
    public BaseLayer getUnderLayer() {
        return null;
    }

    @Override
    public BaseLayer getUpperLayer(int index) {
        if (index < 0 || index > number_of_upper_layer)
            return null;
        return array_of_under_layer.get(index);
    }

    @Override
    public void setUnderLayer(BaseLayer under_layer) {
        if (under_layer == null) return;
        this.array_of_upper_layer.add(number_of_under_layer++, under_layer);
    }

    public void setUpperLayer(BaseLayer upper_layer) {
        if (upper_layer == null) return;
        this.array_of_under_layer.add(number_of_upper_layer++, upper_layer);

    }
    @Override
    public void setUpperUnderLayer(BaseLayer upper_under_layer) {
        this.setUpperLayer(upper_under_layer);
        this.setUnderLayer(upper_under_layer);
    }

    @Override
    public BaseLayer getUnderLayer(int index) {
        if (index < 0 || index > number_of_under_layer) return null;
        return array_of_upper_layer.get(index);
    }
}