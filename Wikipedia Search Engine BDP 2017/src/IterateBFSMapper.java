

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.AbstractMapWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.conf.Configuration;

import edu.umd.cloud9.collection.wikipedia.language.EnglishWikipediaPage;


public class IterateBFSMapper extends Mapper<IntWritable, EnglishWikipediaPage, IntWritable, EnglishWikipediaPage> {

	private String searchWord; 
	private IntWritable occurences = new IntWritable(1);

	public void map(IntWritable nid, EnglishWikipediaPage page,	Context context) throws IOException, InterruptedException {
		//EnglishWikipediaPage are WikipediaPage objects
		Configuration conf = context.getConfiguration();
		String searchWord = conf.get("searchterm");

	try{
			long id = nid.get();
			int count = 0;
			String wholePage = page.getWikiMarkup().toLowerCase();//stores all text of page in string in wiki format
		  String [] searchWordList = wholePage.split(searchWord.toLowerCase());//splits words into array

			for (int i = 0; i < searchWordList.length-1; i++ ) {
			context.write(nid, page);
		  }
	}
	catch(NullPointerException e){
	  System.out.println("fail");
	}


	}

}
