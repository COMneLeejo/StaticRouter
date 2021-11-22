import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class ARPTable {
    HashMap<String, Object[]> cache_table;

    public ARPTable() {
        this.cache_table = new HashMap<>();

        // ARP ���̺� ���� �� Ÿ�̸� ������ ����
        Thread thread = new Thread(new CacheTimer(this.cache_table));
        thread.start();
    }

    /**
     * �ɽ� ���̺� <key, value> �� ����
     *
     * @param addr  : ip �ּ�
     * @param value : value[], value �� �� ��
     * @return      : ���� �۵� ���� ��ȯ
     */
    public boolean put(String addr, Object[] value) {
        // value[0] : �ɽ� ���̺��� ������ (? �� �ʿ��Ѱ� ���� ���� ����)
        // value[1] : ���� mac �ּ�
        // value[2] : ���� , incomplete ? complete
        // value[3] : ���� �ð�
        // value[4] : ��Ʈ �̸� (* ���� �߰� �� ���� *)

        if (addr.equals("")){
            System.out.println("Input address is null");
            return false;
        }

        this.cache_table.put(addr, value);
        return true;
    }

    /**
     * �ɽ� ���̺��� key�� �´� value �� ��ȯ
     * @param key   : ip �ּ�
     * @return      : Ű�� �����ϴ� ��� key �� �´� value ��ȯ
     */
    public Object get(String key){
        if (this.cache_table.containsKey(key)) {
            return this.cache_table.get(key);
        }else{
            System.out.println("No such key exist in table");
            return null;
        }
    }

    /**
     *  �ɽ����̺� ������Ʈ
     *  TODO    :  application layer ���� ���� �ʿ�
     */
    public void updateCacheTable() {
        // TODO : Application Layer ���� ���� �ʿ� (textArea ����)

        Set keys = cache_table.keySet();

        for (Iterator iterator = keys.iterator(); iterator.hasNext(); ) {
            String key = (String) iterator.next();
            Object[] value = (Object[]) cache_table.get(key);

            if (value[2] == null) {
                // TODO : Trash �� ���ֱ�
                //      : Application Layer ���� ���� �ʿ�
//                ApplicationLayer.arp_textarea.append("       " + key + "\t" + "??????????????\t trash\n");
            } else if (value[2].equals("Incomplete")) {
                // TODO : Port Name ���� �Է� �ʿ�
                //      : Application Layer ���� ���� �ʿ�, port Name �� ���� ó�� �ʿ�
//                ApplicationLayer.arp_textarea.append("       " + key + "\t" + "??????????????\t incomplete \t " + // value[5] (��Ʈ �̸�)
                // + "");
            } else {
                byte[] mac_addr_byte = (byte[]) value[1];
                String mac_address_string = macByteArrToString(mac_addr_byte);
                // TODO : Port Name ���� ���� �Է�
                //      : Application Layer ���� ���� �ʿ�, port Name �� ���� ó�� �ʿ�
//                ApplicationLayer.arp_textarea.append("       " + key + "\t" + mac_address_string + "\t complete\t" + // value[5] (��Ʈ �̸�)
                // + "");
            }
        }
    }

    /**
     * byte ���� mac �ּ� ���ڿ��� ��ȯ
     *
     * @param mac_byte_arr byte �迭���� mac �ּ�
     * @return String ������ mac wnth
     */
    public String macByteArrToString(byte[] mac_byte_arr) {
        return String.format("%X:", mac_byte_arr[0]) + String.format("%X:", mac_byte_arr[1])
                + String.format("%X:", mac_byte_arr[2]) + String.format("%X:", mac_byte_arr[3])
                + String.format("%X:", mac_byte_arr[4]) + String.format("%X", mac_byte_arr[5]);
    }

    private class CacheTimer implements Runnable {
        HashMap<String, Object[]> cache_table;
        final int INCOMPLETE_TIME_LIMIT = 3;
        final int COMPLETE_TIME_LIMIT = 20;

        public CacheTimer(HashMap<String, Object[]> _cache_table) {
            this.cache_table = _cache_table;
        }

        @Override
        public void run() {
            while (true) {
                Set key_set = this.cache_table.keySet();
                ArrayList<String> delete_key = new ArrayList<>();

                for (Iterator iterator = key_set.iterator(); iterator.hasNext(); ) {
                    String key = "";
                    if ((key = (String) iterator.next()) != null) {    // key �� �޾ƿ�
                        Object[] value = this.cache_table.get(key);

                        if (((String) value[2]).equals("Incomplete") &&
                                (System.currentTimeMillis() - (long) value[3]) / 60000 >= INCOMPLETE_TIME_LIMIT) {
                            delete_key.add(key);
                        }

                        if (((String) value[2]).equals("Complete") &&
                                (System.currentTimeMillis() - (long) value[3]) / 60000 >= COMPLETE_TIME_LIMIT) {
                            delete_key.add(key);
                        }
                    }
                }

                for (String del_key : delete_key) {
                    this.cache_table.remove(del_key);
                }
                
                updateCacheTable();

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}