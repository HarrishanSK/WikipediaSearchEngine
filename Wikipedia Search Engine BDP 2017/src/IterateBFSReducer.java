

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import edu.umd.cloud9.collection.wikipedia.language.EnglishWikipediaPage;

public class IterateBFSReducer extends Reducer<IntWritable, EnglishWikipediaPage, IntWritable, EnglishWikipediaPage > {

    private IntWritable result = new IntWritable();

	public void reduce(IntWritable key,  Iterable<EnglishWikipediaPage> values, Context context)
													throws IOException, InterruptedException {
    context.write(key, values.iterator().next());
	}

}
