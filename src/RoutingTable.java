import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class RoutingTable {
	
	
   public static class CustomizedHashMap implements Comparator<Map.Entry<Integer, HashMap<String,Object[]>>> {

      @Override
      public int compare(Entry<Integer, HashMap<String,Object[]>> o1, Entry<Integer, HashMap<String,Object[]>> o2) {
         // TODO Auto-generated method stub
         return -o1.getKey().compareTo(o2.getKey());
      }
   }


   public static Map<Integer,HashMap<String,Object[]>> routing_table;
   public HashMap<String,Object[]> head;

   public static List<Map.Entry<Integer, HashMap<String,Object[]>>> entries;

   public RoutingTable() {
      routing_table = new HashMap<Integer,HashMap<String,Object[]>>();
   }

   public static void add(String key, Object[] value) {

      HashMap<String,Object[]> head = null;
      byte[] netmask = (byte[]) value[1];
      int netnum = computeNetnum(netmask);

      if(routing_table.containsKey(netnum)) {
         head = routing_table.get(netnum);

         head.put(key, value);

      }else {
         routing_table.put(netnum, new HashMap<String,Object[]>());
         head = routing_table.get(netnum);
         head.put(key, value);
         Sorting();

      }
   }

   public static void Sorting() {
      entries = new ArrayList<Map.Entry<Integer, HashMap<String,Object[]>>>(routing_table.entrySet());
      Collections.sort(entries, new CustomizedHashMap());

   }

   public static int computeNetnum(byte[] netmask) {
      int cnt=0;

      for(int i=0;i<4;i++) {
         if((netmask[i]&0xFF) == 255) cnt += 8;
         else {
            int n= (netmask[i]&0xFF);
            while(n!=0) {
               cnt+=n%2;
               n/=2;
            }
         }
      }

      return cnt;
   }

   public Object[] findEntry(byte[] real_destination) {
	  long time = System.currentTimeMillis();
      if(entries == null) return null;

      for(Map.Entry<Integer, HashMap<String,Object[]>> entry : entries) {

         HashMap<String, Object[]> get_map = entry.getValue();
         HashMap.Entry<String, Object[]> node_entry = get_map.entrySet().iterator().next();
         byte[] netmask = (byte[])(node_entry.getValue()[1]);

         byte[] masking_result = new byte[4];
         masking_result[0]=(byte) (real_destination[0]&netmask[0]);
         masking_result[1]=(byte) (real_destination[1]&netmask[1]);
         masking_result[2]=(byte) (real_destination[2]&netmask[2]);
         masking_result[3]=(byte) (real_destination[3]&netmask[3]);

         String masking_result_to_string = (masking_result[0]&0xFF)+"."+(masking_result[1]&0xFF)+"."+(masking_result[2]&0xFF)+"."+(masking_result[3]&0xFF);
         if(get_map.containsKey(masking_result_to_string)) {
        	 long time2 = System.currentTimeMillis();
             System.out.println("hi : " + (time2 - time));
            return get_map.get(masking_result_to_string);
         }

      }
      
      return null;
   }

   public boolean remove(Object[] value) {
      byte[] netmask = (byte[]) value[1];
      int netnum = computeNetnum(netmask);

      HashMap<String,Object[]> head = routing_table.get(netnum);
      if(head==null) return false;

      byte[] value_dest_ip = (byte[]) value[0];
      String value_dest_ip_string = (value_dest_ip[0] & 0xFF) + "." + (value_dest_ip[1] & 0xFF) + "."
            + (value_dest_ip[2] & 0xFF) + "." + (value_dest_ip[3] & 0xFF);

      HashMap<String, Object[]> get_map = head;
      if(get_map.containsKey(value_dest_ip_string)) {
         if(get_map.size()==1) {
            routing_table.remove(netnum);
            Sorting();
         }else {
            get_map.remove(value_dest_ip_string);
         }
         return true;
      }

      return false;
   }



   public static String updateRoutingTable() {
      String print_result = "";

      for(Entry<Integer, HashMap<String,Object[]>> entry : entries) {

         HashMap<String,Object[]> head = entry.getValue();
         HashMap<String,Object[]> map = head;

         for ( String key : head.keySet() ) {
            Object[] value = map.get(key);

            byte[] netmask = (byte[])value[1];
            byte[] gateway_byte = (byte[])value[2];

            String destIP_String = key;
            String mask_string = "";
            String gateway_string = "";

            for (int j = 0; j < 3; j++) {
               mask_string = mask_string + (netmask[j]&0xFF)+".";
               gateway_string = gateway_string + (gateway_byte[j]&0xFF)+".";
            }
            mask_string = mask_string + (netmask[3]&0xFF);
            gateway_string = gateway_string + (gateway_byte[3]&0xFF);

            String flag_string = "";
            String interface_string = value[6] + "";

            if ((boolean) value[3]) {
               flag_string += "U";
            }
            if ((boolean) value[4]) {
               flag_string += "G";
            }
            if ((boolean) value[5]) {
               flag_string += "H";
            }

            print_result = print_result + "    " +destIP_String + "    " + mask_string + "      " + gateway_string + "         " + flag_string
                  + "        " + interface_string + "\n";
         }
      }
      return print_result;
   }


}