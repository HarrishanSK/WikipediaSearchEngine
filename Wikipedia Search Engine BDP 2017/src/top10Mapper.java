import java.io.IOException;
import java.util.StringTokenizer;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import java.util.*;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.Calendar;

public class top10Mapper extends Mapper<Object, Text, Text, IntWritable> {

  private IntWritable value;

  public void map(Object key, Text input, Context context) throws IOException, InterruptedException
  {

        System.out.println(input.toString());
  			String [] fields = input.toString().split("@");//split line and put into array with the @ symbol separating them
        if(fields.length == 2){// check that only 2 elements are created so that its a key value pair
    			context.write(new Text(fields[0]),  new IntWritable(Integer.parseInt(fields[1].trim())));//write this to the reducer and make sure that when converting the string into an integer to trim off any whitespaces
        }
  }
}
