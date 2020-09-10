

import java.util.Arrays;
import javax.swing.*;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;

import edu.umd.cloud9.collection.wikipedia.language.EnglishWikipediaPage;

public class IterateBFS {


    public static boolean runXmlParsing(String[] input, String output, String searchterm) throws Exception {
      //input[0] = "/data/enwiki-latest.block";
      output = "newOut";
    //searchterm = JOptionPane.showInputDialog(null, "Enter search term: ");
    	Job job = Job.getInstance(new Configuration());
      Configuration conf = job.getConfiguration();
    	conf.set("mapreduce.child.java.opts", "-Xmx2048m");
      conf.set("searchterm", searchterm);

    	job.setNumReduceTasks(1);
    	job.setJarByClass(IterateBFS.class);
    	Path outputPath = new Path(output);

    	// Input -> Mapper -> Map
    	FileInputFormat.setInputPaths(job, StringUtils.join(input, ","));
    	job.setInputFormatClass(SequenceFileInputFormat.class);
    	job.setMapperClass(IterateBFSMapper.class);//DON'T TOUCH
    	//job.setMapOutputKeyClass(SequenceFileOutputFormat.class);

    	// Map -> Reducer -> Output
    	FileOutputFormat.setOutputPath(job, outputPath);
    	job.setOutputFormatClass(SequenceFileOutputFormat.class);//
    	job.setOutputKeyClass(IntWritable.class);//Text
    	job.setOutputValueClass(EnglishWikipediaPage.class);//Changed from EnglishWikipediaPageIntWritable
    	job.setReducerClass(IterateBFSReducer.class);

    	outputPath.getFileSystem(conf).delete(outputPath, true);

    	return job.waitForCompletion(true);
    }

    public static boolean pageRank(String[] input, String output, String searchterm) throws Exception {

      Job job = Job.getInstance(new Configuration());
      Configuration conf = job.getConfiguration();
    	conf.set("mapreduce.child.java.opts", "-Xmx2048m");
      conf.set("searchterm2", searchterm);

    	job.setNumReduceTasks(1);
    	job.setJarByClass(IterateBFS.class);
    	Path outputPath = new Path(output);

    	// Input -> Mapper -> Map
    	FileInputFormat.setInputPaths(job, StringUtils.join(input, ","));
    	job.setInputFormatClass(SequenceFileInputFormat.class);
    	job.setMapperClass(PageRankMapper.class);//DON'T TOUCH
    	//job.setMapOutputKeyClass(SequenceFileOutputFormat.class);

    	// Map -> Reducer -> Output
    	FileOutputFormat.setOutputPath(job, outputPath);
    	//job.setOutputFormatClass(SequenceFileOutputFormat.class);//
    	job.setOutputKeyClass(Text.class);//Text
    	job.setOutputValueClass(IntWritable.class);//Changed from EnglishWikipediaPageIntWritable
    	job.setReducerClass(PageRankReducer.class);

    	outputPath.getFileSystem(conf).delete(outputPath, true);

    	return job.waitForCompletion(true);

    }

    public static boolean runRankSorter(String[] input, String output) throws Exception {

      Configuration conf = new Configuration();

  		Job job = new Job(conf);
  		job.setJarByClass(IterateBFS.class);
  		job.setMapperClass(top10Mapper.class);
  		job.setReducerClass(top10Reducer.class);
  		job.setMapOutputKeyClass(Text.class);
  		job.setMapOutputValueClass(IntWritable.class);

  		job.setNumReduceTasks(1);
  		Path outputPath = new Path(output);
  		FileInputFormat.setInputPaths(job, StringUtils.join(input, ","));
  		FileOutputFormat.setOutputPath(job, outputPath);
  		outputPath.getFileSystem(conf).delete(outputPath, true);
  		return job.waitForCompletion(true);
    }

	public static void runJob(String[] input, String output, String searchterm) throws Exception {
    /*for (int i = 0; i < input.length; i++) {
         System.out.print(input[i]+",");
    }
    System.out.println();
    System.out.println("1");
    System.out.println(output);*/
    System.out.println(searchterm);
		boolean test = runXmlParsing(input, output, searchterm);
		if(test){
		  System.out.println("Nice");
		}
		else{
		  return;
		}

    String[] inPath = {"newOut"};//out
    String outPath = "newOut2";//out2

    System.out.println(searchterm);
		boolean test2 = pageRank(inPath, outPath, searchterm);
		if(test2){
		  System.out.println("Nice2");
		}
		else{
		  return;
		}

    inPath[0] = "newOut2";//out2
    outPath = "newOut3";//out3

		boolean test3 = runRankSorter(inPath, outPath);
		if(test3){
		  System.out.println("Nice3");
		}
		else{
		  return;
		}

	}

	public static void main(String[] args) throws Exception {

    String s = "";
    for(int i = 2; i < args.length; i++){
      if(i == 2){
        s = s + args[i];
      }
      else{
        s = s + " " + args[i];
      }
    }

    runJob(Arrays.copyOfRange(args, 0, 1), args[1], s);
    /*runJob(Arrays.copyOfRange(args, 0, args.length - 2),
        args[args.length - 2], args[args.length-1]);*/


	}

}
