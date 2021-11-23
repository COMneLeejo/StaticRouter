import java.awt.Color;
import java.awt.Container;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;

import org.jnetpcap.PcapIf;

public class ApplicationLayer extends JFrame implements BaseLayer {
    public int number_of_upper_layer = 0;
    public String present_layer_name = null;
    public BaseLayer under_layer = null;
    public ArrayList<BaseLayer> array_of_upper_layer = new ArrayList<BaseLayer>();
    public static ARPTable arp_table;
    public static RoutingTable routing_table;

    private static LayerManager m_layer_mgr = new LayerManager();
    public static boolean exist = false;

    String path;
    String[] interface_name = { "Port1", "Port2" };
    String interface0 = interface_name[0];

    int selected_index;
    static int adapter_number = 0;

    Container content_pane;

    static JTextArea arp_textarea;
    static JTextArea ip_src_address;
    static JTextArea ethernet_src_address1 = new JTextArea();
    static JTextArea ethernet_src_address2 = new JTextArea();
    JTextArea destination_textarea;
    JTextArea netmask_textarea;
    JTextArea gateway_textarea;
    JTextArea routing_area;

    JButton all_item_delete_button;
    JButton arp_send_button;
    JButton item_delete_button;
    JButton my_info_setting_button;
    JButton setting_Button;

    JLabel nic_title;
    JLabel destination_title;
    JLabel gateway_title;
    JLabel netmask_title;
    JLabel flag_title;
    JLabel interface_title;

    JCheckBox flag_up;
    JCheckBox flag_gateway;
    JCheckBox flag_host;

    static JComboBox<String> nic_combo_box1;
    static JComboBox<String> nic_combo_box2;
    JComboBox str_combo;
    JComboBox<String> select_host;
    JComboBox<String> select_combo;

    int index1;
    int index2;

    FileDialog fd;

    public static void main(String[] args) throws IOException {
        arp_table = new ARPTable();
        routing_table = new RoutingTable();

        m_layer_mgr.addLayer(new NILayer("NI"));
        m_layer_mgr.addLayer(new NILayer("NI2"));
        m_layer_mgr.addLayer(new EthernetLayer("Ethernet"));
        m_layer_mgr.addLayer(new EthernetLayer("Ethernet2"));
        m_layer_mgr.addLayer(new ARPLayer("ARP2"));
        m_layer_mgr.addLayer(new ARPLayer("ARP"));
        m_layer_mgr.addLayer(new IPLayer("IP"));
        m_layer_mgr.addLayer(new IPLayer("IP2"));
        m_layer_mgr.addLayer(new ApplicationLayer("GUI"));

//        arp_table = new ARPTable((ARPLayer) m_layer_mgr.getLayer("ARP"), (ARPLayer) m_layer_mgr.getLayer("ARP2"),(ApplicationLayer) m_layer_mgr.getLayer("GUI") );
//        ((ARPLayer)m_layer_mgr.getLayer("ARP")).setArpTable(arp_table,(ApplicationLayer) m_layer_mgr.getLayer("GUI"));
//        ((ARPLayer)m_layer_mgr.getLayer("ARP2")).setArpTable(arp_table,(ApplicationLayer) m_layer_mgr.getLayer("GUI"));

        m_layer_mgr.connectLayers("NI ( +Ethernet ( +ARP ( +IP ( +GUI ) ) +IP ( +GUI ) ) ) ^GUI ( -IP ( -ARP ( -Ethernet ( -NI ) ) -Ethernet ( -NI ) ) )  ^NI2 ( +Ethernet2 ( +ARP2 ( +IP2 ( +GUI ) ) +IP2 ( +GUI ) ) ) ^GUI ( -IP2 ( -ARP2 ( -Ethernet2 ( -NI2 ) ) -Ethernet2 ( -NI2 ) ) )");
        // m_layer_mgr.connectLayers("NI ( +Ethernet ( +ARP +IP ( + GUI ) ) ) ^GUI ( -IP ( -ARP ( -Ethernet ( -NI ) ) ) ) ^NI2 ( +Ethernet2 ( +ARP2 +IP2 ( + GUI ) ) ) ^GUI ( -IP2 ( -ARP2 ( -Ethernet2 ( -NI2 ) ) ) )");

        // 친구 ip설정해주는 부분(우린 라우팅테이블 써야함)
        // ((IPLayer) m_layer_mgr.getLayer("IP")).friendIpset(((IPLayer) m_layer_mgr.getLayer("IP2")));
        // ((IPLayer) m_layer_mgr.getLayer("IP2")).friendIpset(((IPLayer) m_layer_mgr.getLayer("IP")));
        ((IPLayer) m_layer_mgr.getLayer("IP")).setRouter(routing_table);
        ((IPLayer) m_layer_mgr.getLayer("IP2")).setRouter(routing_table);
        ((IPLayer) m_layer_mgr.getLayer("IP")).setAnotherIPLayer((IPLayer) m_layer_mgr.getLayer("IP2"));
        ((IPLayer) m_layer_mgr.getLayer("IP2")).setAnotherIPLayer((IPLayer) m_layer_mgr.getLayer("IP"));
    }

    public ApplicationLayer(String pName) {
        present_layer_name = pName;

        exist = true;

        setTitle("Router");
        setBounds(250, 250, 980, 590);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        content_pane = this.getContentPane();
        getContentPane().setLayout(null);

        /**
         * ARP
         */
        // layer
        JPanel arp_cache_title = new JPanel();
        arp_cache_title.setBounds(14, 12, 458, 420);
        arp_cache_title.setBorder(new TitledBorder(null, "ARP Cache", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        arp_cache_title.setLayout(null);
        content_pane.add(arp_cache_title);

        // ARP결과 입력창
        arp_textarea = new JTextArea();
        arp_textarea.setEditable(false);
        arp_textarea.setBounds(14, 24, 430, 320);
        arp_cache_title.add(arp_textarea);

        // arp 테이블에서 원하는 주소 하나만 지우는 버튼
        item_delete_button = new JButton("Item Delete");
        item_delete_button.setBounds(14, 365, 430, 35);
        arp_cache_title.add(item_delete_button);
        item_delete_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                String delete_ip = JOptionPane.showInputDialog("Item's IP Address");
                if (delete_ip != null) {
                    if (arp_table.cache_table.containsKey(delete_ip)) {
                        Object[] value = arp_table.cache_table.get(delete_ip);
                        if (System.currentTimeMillis() - (long) value[3] / 1000 > 1) {
                            arp_table.cache_table.remove(delete_ip);
                            arp_table.updateCacheTable();
                        }
                    }
                }
            }
        });

        /**
         * Static Routing Table
         */
        // layout
        JPanel routing_panel = new JPanel();
        routing_panel.setToolTipText("Static Routing Table");
        routing_panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Static Routing Table", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        routing_panel.setBounds(486, 12, 466, 420);
        getContentPane().add(routing_panel);
        routing_panel.setLayout(null);

        // ARP Cache textarea
        routing_area = new JTextArea();
        routing_area.setEditable(false);
        routing_area.setBounds(14, 30, 430, 173);
        routing_panel.add(routing_area);

        // destination
        destination_title = new JLabel("Destination");
        destination_title.setBounds(110, 207, 90, 30);
        routing_panel.add(destination_title);
        destination_textarea = new JTextArea();
        destination_textarea.setBounds(200, 210, 180, 20);
        destination_textarea.setEnabled(true);
        routing_panel.add(destination_textarea);

        // netmask
        netmask_title = new JLabel("Netmask");
        netmask_title.setBounds(110, 237, 90, 30);
        routing_panel.add(netmask_title);
        netmask_textarea = new JTextArea();
        netmask_textarea.setBounds(200, 245, 180, 20);
        netmask_textarea.setEnabled(true);
        routing_panel.add(netmask_textarea);

        // gateway
        gateway_title = new JLabel("Gateway");
        gateway_title.setBounds(110, 270, 90, 30);
        routing_panel.add(gateway_title);
        gateway_textarea = new JTextArea();
        gateway_textarea.setBounds(200, 275, 180, 20);
        gateway_textarea.setEnabled(true);
        routing_panel.add(gateway_textarea);

        // flag
        flag_title = new JLabel("Flag");
        flag_title.setBounds(110, 300, 90, 30);
        routing_panel.add(flag_title);

        flag_up = new JCheckBox("UP");
        flag_up.setBounds(200, 300, 50, 30);
        routing_panel.add(flag_up);

        flag_gateway = new JCheckBox("Gateway");
        flag_gateway.setBounds(260, 300, 80, 30);
        routing_panel.add(flag_gateway);

        flag_host = new JCheckBox("Host");
        flag_host.setBounds(350, 300, 60, 30);
        routing_panel.add(flag_host);

        // interface
        interface_title = new JLabel("Interface");
        interface_title.setBounds(110, 330, 90, 30);
        routing_panel.add(interface_title);

        select_combo = new JComboBox<String>(interface_name);
        select_combo.setBounds(200, 335, 170, 20);
        select_combo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                interface0 = interface_name[select_combo.getSelectedIndex()];
            }
        });
        routing_panel.add(select_combo);

        // Static Routing에 추가
        JButton btnAdd = new JButton("Add");
        btnAdd.setBounds(42, 365, 165, 35);
        routing_panel.add(btnAdd);
        btnAdd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                StringTokenizer st = new StringTokenizer(destination_textarea.getText(), ".");

                byte[] destination = new byte[4];
                for (int i = 0; i < 4; i++) {
                    String ss = st.nextToken();
                    int s = Integer.parseInt(ss);
                    destination[i] = (byte) (s & 0xFF);
                }

                st = new StringTokenizer(netmask_textarea.getText(), ".");

                byte[] netmask = new byte[4];
                for (int i = 0; i < 4; i++) {
                    String ss = st.nextToken();
                    int s = Integer.parseInt(ss);
                    netmask[i] = (byte) (s & 0xFF);
                }

                st = new StringTokenizer(gateway_textarea.getText(), ".");

                byte[] gateway = new byte[4];
                for (int i = 0; i < 4; i++) {
                    String ss = st.nextToken();
                    int s = Integer.parseInt(ss);
                    gateway[i] = (byte) (s & 0xFF);
                }

                String interface_num = interface0;
                Object[] value = new Object[7];
                value[0] = destination;
                value[1] = netmask;
                value[2] = gateway;
                value[3] = flag_up.isSelected();
                value[4] = flag_gateway.isSelected();
                value[5] = flag_host.isSelected();
                value[6] = interface_num;

                routing_table.add(destination_textarea.getText(), value);
                routing_area.setText(routing_table.updateRoutingTable());
            }
        });

        // Static Routing테이블 삭제 버튼
        JButton arp_delete_button = new JButton("Delete");
        arp_delete_button.setBounds(249, 365, 165, 35);
        routing_panel.add(arp_delete_button);
        arp_delete_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                String delete_ip = JOptionPane.showInputDialog("Item's IP Address");
                if (delete_ip != null) {
                    StringTokenizer st = new StringTokenizer(delete_ip,".");
                    byte[] ip_address = new byte[4];
                    for (int i = 0; i < 4; i++) {
                        String ss = st.nextToken();
                        int s = Integer.parseInt(ss);
                        ip_address[i] = (byte) (s & 0xFF);
                    }

                    String netmask = JOptionPane.showInputDialog("Item's netMask Address");
                    st = new StringTokenizer(netmask, ".");
                    byte[] net_mask_byte = new byte[4];
                    for (int i = 0; i < 4; i++) {
                        String ss = st.nextToken();
                        int s = Integer.parseInt(ss);
                        net_mask_byte[i] = (byte) (s & 0xFF);
                    }
                    Object[] remove_value = new Object[2];
                    remove_value[0]=ip_address;
                    remove_value[1]=net_mask_byte;
                    routing_table.remove(remove_value);
                    routing_area.setText(routing_table.updateRoutingTable());
                }
            }
        });

        /*
         * setting NIC
         */
        JPanel settingPanel = new JPanel();
        settingPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "setting", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        settingPanel.setBounds(14, 435, 930, 50);
        content_pane.add(settingPanel);
        settingPanel.setLayout(null);

        JLabel choice1 = new JLabel("NIC select 1: ");
        choice1.setBounds(80, 20, 170, 20);
        settingPanel.add(choice1);

        nic_combo_box1 = new JComboBox();
        nic_combo_box1.setBounds(170, 20, 165, 20);
        settingPanel.add(nic_combo_box1);

        for (int i = 0; ((NILayer) m_layer_mgr.getLayer("NI")).getAdapterList().size() > i; i++) {
            PcapIf pcapIf = ((NILayer) m_layer_mgr.getLayer("NI")).getAdapterObject(i);
            nic_combo_box1.addItem(pcapIf.getName());
        }

        nic_combo_box1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox jcombo = (JComboBox) e.getSource();
                adapter_number = jcombo.getSelectedIndex();
                System.out.println("Index: " + adapter_number);
                try {
                    ethernet_src_address1.setText("");
                    ethernet_src_address1.append(getMacAddress(((NILayer) m_layer_mgr.getLayer("NI"))
                            .getAdapterObject(adapter_number).getHardwareAddress()));
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });


        JLabel choice2 = new JLabel("NIC select 2: ");
        choice2.setBounds(390, 20, 170, 20);
        settingPanel.add(choice2);

        nic_combo_box2 = new JComboBox();
        nic_combo_box2.setBounds(490, 20, 165, 20);
        settingPanel.add(nic_combo_box2);

        for (int i = 0; ((NILayer) m_layer_mgr.getLayer("NI2")).getAdapterList().size() > i; i++) {
            PcapIf pcapIf = ((NILayer) m_layer_mgr.getLayer("NI2")).getAdapterObject(i);
            nic_combo_box2.addItem(pcapIf.getName());
        }

        nic_combo_box2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox jcombo = (JComboBox) e.getSource();
                adapter_number = jcombo.getSelectedIndex();
                System.out.println("Index: " + adapter_number);
                try {
                    ethernet_src_address2.setText("");
                    ethernet_src_address2.append(getMacAddress(((NILayer) m_layer_mgr.getLayer("NI2"))
                            .getAdapterObject(adapter_number).getHardwareAddress()));
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        setting_Button = new JButton("setting");// setting
        setting_Button.setBounds(720, 20, 80, 20);
        setting_Button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (setting_Button.getText() == "setting") {
                    index1 = nic_combo_box1.getSelectedIndex();
                    index2 = nic_combo_box2.getSelectedIndex();

                    try {
                        byte[] mac0 = ((NILayer) m_layer_mgr.getLayer("NI")).m_adapter_list.get(index1).getHardwareAddress();
                        byte[] mac1 = ((NILayer) m_layer_mgr.getLayer("NI2")).m_adapter_list.get(index2).getHardwareAddress();

                        final StringBuilder ethernet_addrbuf1 = new StringBuilder();
                        for(byte b:mac0) {
                            if(ethernet_addrbuf1.length()!=0) ethernet_addrbuf1.append(":");
                            if(b>=0 && b<16) ethernet_addrbuf1.append('0');
                            ethernet_addrbuf1.append(Integer.toHexString((b<0)? b+256:b).toUpperCase());
                        }

                        final StringBuilder ethernet_addrbuf2 = new StringBuilder();
                        for(byte b:mac1) {
                            if(ethernet_addrbuf2.length()!=0) ethernet_addrbuf2.append(":");
                            if(b>=0 && b<16) ethernet_addrbuf2.append('0');
                            ethernet_addrbuf2.append(Integer.toHexString((b<0)? b+256:b).toUpperCase());
                        }

                        byte[] ip_src_address1 = ((((NILayer)m_layer_mgr.getLayer("NI")).m_adapter_list.get(index1).getAddresses()).get(0)).getAddr().getData();
                        final StringBuilder ip_addrbuf0 = new StringBuilder();
                        for(byte b:ip_src_address1) {
                            if(ip_addrbuf0.length()!=0) ip_addrbuf0.append(".");
                            ip_addrbuf0.append(b&0xff);
                        }

                        byte[] ip_src_address2 = ((((NILayer)m_layer_mgr.getLayer("NI")).m_adapter_list.get(index2).getAddresses()).get(0)).getAddr().getData();
                        final StringBuilder ip_addrbuf1 = new StringBuilder();
                        for(byte b:ip_src_address2) {
                            if(ip_addrbuf1.length()!=0) ip_addrbuf1.append(".");
                            ip_addrbuf1.append(b&0xff);
                        }

                        System.out.println("NIC1: "+ip_addrbuf0.toString()+" // "+ethernet_addrbuf1.toString());
                        System.out.println("NIC2: "+ip_addrbuf1.toString()+" // "+ethernet_addrbuf2.toString());
                        /*IP Address 설정*/
                        ((IPLayer)m_layer_mgr.getLayer("IP")).setIPSrcAddress(ip_src_address1);
                        ((IPLayer)m_layer_mgr.getLayer("IP2")).setIPSrcAddress(ip_src_address2);
                        /*IP Port 설정*/
                        ((IPLayer)m_layer_mgr.getLayer("IP")).setPort("Port1");
                        ((IPLayer)m_layer_mgr.getLayer("IP2")).setPort("Port2");

                        /*ARP Address 설정*/
//                        ((ARPLayer)m_layer_mgr.getLayer("ARP")).setIPAddrSrcAddr(ip_src_address1);
//                        ((ARPLayer)m_layer_mgr.getLayer("ARP2")).setIPAddrSrcAddr(ip_src_address2);

                        ((ARPLayer) m_layer_mgr.getLayer("ARP")).setHostMacAddr(mac0);
                        ((ARPLayer) m_layer_mgr.getLayer("ARP2")).setHostMacAddr(mac1);

                        /*Ethernet Mac 주소 설정*/
                        ((EthernetLayer) m_layer_mgr.getLayer("Ethernet")).setEnetSrcAddress(mac0);
                        ((EthernetLayer) m_layer_mgr.getLayer("Ethernet2")).setEnetSrcAddress(mac1);

                        /*Receive 실행*/
                        ((NILayer) m_layer_mgr.getLayer("NI")).setAdapterNumber(index1);
                        ((NILayer) m_layer_mgr.getLayer("NI2")).setAdapterNumber(index2);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }

                    nic_combo_box1.setEnabled(false);
                    nic_combo_box2.setEnabled(false);
                    setting_Button.setText("reset");
                } else {
                    nic_combo_box1.setEnabled(true);
                    nic_combo_box2.setEnabled(true);
                    setting_Button.setText("setting");
                }
            }
        });
        settingPanel.add(setting_Button);

        /*
         * Close Menu
         */
        JMenu mn_new_menu = new JMenu("close menu");
        mn_new_menu.setBounds(-206, 226, 375, 183);
        routing_panel.add(mn_new_menu);

        JButton close_button = new JButton("close");
        close_button.setBounds(16, 503, 165, 35);
        getContentPane().add(close_button);
        close_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                exist = false;
                dispose();
            }
        });

        setVisible(true);
    }


    public String getMacAddress(byte[] byte_mac_address) {

        String mac_address = "";
        for (int i = 0; i < 6; i++) {
            mac_address += String.format("%02X%s", byte_mac_address[i], (i < mac_address.length() - 1) ? "" : "");
            if (i != 5) {
                mac_address += "-";
            }
        }

        System.out.println("present MAC address: " + mac_address);
        return mac_address;
    }

    public boolean receive(byte[] input) {
        byte[] data = input;

        return false;
    }


    @Override
    public void setUnderLayer(BaseLayer under_layer) {
        // TODO Auto-generated method stub
        if (under_layer == null)
            return;
        this.under_layer = under_layer;
    }

    @Override
    public void setUpperLayer(BaseLayer upper_layer) {
        // TODO Auto-generated method stub
        if (upper_layer == null)
            return;
        this.array_of_upper_layer.add(number_of_upper_layer++, upper_layer);
        // number_of_upper_layer++;
    }

    @Override
    public String getLayerName() {
        // TODO Auto-generated method stub
        return present_layer_name;
    }

    @Override
    public BaseLayer getUnderLayer() {
        // TODO Auto-generated method stub
        if (under_layer == null)
            return null;
        return under_layer;
    }

    @Override
    public BaseLayer getUpperLayer(int nindex) {
        // TODO Auto-generated method stub
        if (nindex < 0 || nindex > number_of_upper_layer || number_of_upper_layer < 0)
            return null;
        return array_of_upper_layer.get(nindex);
    }

    @Override
    public void setUpperUnderLayer(BaseLayer uu_layer) {
        this.setUpperLayer(uu_layer);
        uu_layer.setUnderLayer(this);

    }

    @Override
    public BaseLayer getUnderLayer(int nindex) {
        return null;
    }
}