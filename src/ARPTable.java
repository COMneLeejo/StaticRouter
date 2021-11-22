import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class ARPTable {
    HashMap<String, Object[]> cache_table;

    public ARPTable() {
        this.cache_table = new HashMap<>();

        // ARP 테이블 생성 후 타이머 쓰레드 실행
        Thread thread = new Thread(new CacheTimer(this.cache_table));
        thread.start();
    }

    /**
     * 케시 테이블에 <key, value> 값 넣음
     *
     * @param addr  : ip 주소
     * @param value : value[], value 가 될 값
     * @return      : 정상 작동 여부 반환
     */
    public boolean put(String addr, Object[] value) {
        // value[0] : 케시 테이블의 사이즈 (? 왜 필요한가 아직 이해 못함)
        // value[1] : 상대방 mac 주소
        // value[2] : 상태 , incomplete ? complete
        // value[3] : 현재 시간
        // value[4] : 포트 이름 (* 새로 추가 된 정보 *)

        if (addr.equals("")){
            System.out.println("Input address is null");
            return false;
        }

        this.cache_table.put(addr, value);
        return true;
    }

    /**
     * 케시 테이블에서 key에 맞는 value 값 반환
     * @param key   : ip 주소
     * @return      : 키가 존재하는 경우 key 에 맞는 value 반환
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
     *  케시테이블 업데이트
     *  TODO    :  application layer 에서 연동 필요
     */
    public void updateCacheTable() {
        // TODO : Application Layer 에서 연동 필요 (textArea 관련)

        Set keys = cache_table.keySet();

        for (Iterator iterator = keys.iterator(); iterator.hasNext(); ) {
            String key = (String) iterator.next();
            Object[] value = (Object[]) cache_table.get(key);

            if (value[2] == null) {
                // TODO : Trash 값 없애기
                //      : Application Layer 에서 연동 필요
//                ApplicationLayer.arp_textarea.append("       " + key + "\t" + "??????????????\t trash\n");
            } else if (value[2].equals("Incomplete")) {
                // TODO : Port Name 정보 입력 필요
                //      : Application Layer 에서 연동 필요, port Name 에 대해 처리 필요
//                ApplicationLayer.arp_textarea.append("       " + key + "\t" + "??????????????\t incomplete \t " + // value[5] (포트 이름)
                // + "");
            } else {
                byte[] mac_addr_byte = (byte[]) value[1];
                String mac_address_string = macByteArrToString(mac_addr_byte);
                // TODO : Port Name 관련 정보 입력
                //      : Application Layer 에서 연동 필요, port Name 에 대해 처리 필요
//                ApplicationLayer.arp_textarea.append("       " + key + "\t" + mac_address_string + "\t complete\t" + // value[5] (포트 이름)
                // + "");
            }
        }
    }

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
                    if ((key = (String) iterator.next()) != null) {    // key 값 받아옴
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