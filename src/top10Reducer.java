import java.io.IOException;
import java.util.Iterator;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import java.util.*;

public class top10Reducer extends Reducer<Text, IntWritable, Text, IntWritable> {

    private Map<Text,IntWritable> titlePriorityMap = new HashMap();// this creates a new hashMap object to store all the keys(titles) and values(priority)

    public void reduce(Text key, Iterable<IntWritable> values, Context context)
      throws IOException, InterruptedException {
        int sum = 0;
        for (IntWritable value : values) {// iterates through all the values, without this it will display all the results instead of 10
            sum=value.get();//assigns the integer in the value object to the integer sum
        }
        titlePriorityMap.put(new Text(key), new IntWritable(sum));// puts this value into the hastagsCountMap which is then sorted in the cleanup by the call to the sortbyValues method
    }

    protected void cleanup(Context context) throws IOException, InterruptedException {
        Map<Text,IntWritable> sortedTitlePriorityMap = sortByValues(titlePriorityMap);// get the sorted hashMap
        int counter = 0;
        for (Text key: sortedTitlePriorityMap.keySet()) {
            if (counter ++ == 10) {// only display the top 10
                break;
            }
            context.write(key, sortedTitlePriorityMap.get(key)); //write this top 10 entries to context
        }
    }

    public static <K extends Comparable, V extends Comparable> Map<K, V> sortByValues(Map<K, V> map) {//method to sort the values in descending order with largest value at top
        List<Map.Entry<K, V>> entries = new LinkedList<Map.Entry<K, V>>(map.entrySet());//setting it to a list

        Collections.sort(entries, new Comparator<Map.Entry<K, V>>() {//call to sort method

            @Override // override because I use my own custom comparing method
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return o2.getValue().compareTo(o1.getValue());//where the sorting happens by checking the first value with the next value returns either 0 (if equal), -ve (if first object less than second) and +ve if greater
            }
        });
        Map<K, V> sortedTitlePriorityMap = new LinkedHashMap<K, V>();//creating a new LinkedHashMap object to place the sorted key value pairs into

        for (Map.Entry<K, V> entry : entries) {
            sortedTitlePriorityMap.put(entry.getKey(), entry.getValue());// for all the entries it is now adding that to the sortedTitlePriorityMap from the titlePriorityMap
        }
        return sortedTitlePriorityMap;// return this hashMap back to wherever this method was called
    }


}
