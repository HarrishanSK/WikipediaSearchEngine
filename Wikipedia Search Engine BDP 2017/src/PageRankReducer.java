

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Reducer;

import edu.umd.cloud9.collection.wikipedia.language.EnglishWikipediaPage;


public class PageRankReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

	private IntWritable result = new IntWritable();

	public void reduce(Text title, Iterable<IntWritable> values,	Context context) throws IOException, InterruptedException {


		//context.write(nid, values.iterator().next());
		int sum = 0;
        for (IntWritable value : values) {
            sum = sum + value.get();
        }
               result.set(sum);

        context.write(title, result);


	}

}
