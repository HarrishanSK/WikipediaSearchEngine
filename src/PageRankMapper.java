
import java.util.*;
import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.conf.Configuration;

import edu.umd.cloud9.collection.wikipedia.language.EnglishWikipediaPage;


public class PageRankMapper extends Mapper<IntWritable, EnglishWikipediaPage, Text, IntWritable> {

  private String searchWord;// = "aldous";
	private IntWritable finalPriority = new IntWritable(0);

	public void map(IntWritable nid, EnglishWikipediaPage page,	Context context) throws IOException, InterruptedException
  {
    Configuration conf = context.getConfiguration();
		searchWord = conf.get("searchterm2");
    System.out.println("Search word is :"+searchWord);
    int priority = 0;
    try{
    searchWord = formatSearchTerm(searchWord);//formats searchTerm to be used in this mapper
		String wholePage = page.getWikiMarkup();//stores whole page as string in wiki format
		String [] searchWordList = wholePage.split(searchWord);
		String [] wordsInPage = wholePage.split("\\s+");
		int totalWords = wordsInPage.length;

    int priorityForElements = ObtainPriorityForElements(searchWord, page);
    priority += priorityForElements;

    //KEYWORD DENSITY STUFF
    int priorityForKD = ObtainKdPriority(searchWord, wholePage);
    priority += priorityForKD;

    //check if page is spam from title and page text
    priority = priority * spamCapture(page); //returns 1 or 0
        String title = page.getTitle();
		context.write(new Text(title+"@"), new IntWritable(priority));
    }
    catch(Exception e){
      System.out.println("fail");
    }
	}


//----------------------------------------harrishan-----------------------------
public int ObtainKdPriority(String searchWord, String wholePage)  throws IOException, InterruptedException
{
  int priority = 0;
  String [] wordsInPage = wholePage.split("\\s+");
  int totalWords = wordsInPage.length;

  //Arrays containing lowerbound, optimal value and upper bound KD values for different types of search terms
  double[] charactersKDR = {4.759,20.091,46.662} ;
  double[] vowelsKDR = {25.110,44.104,64.017} ;
  double[] singleTermKDR = {0.0395,3.144,8.36} ;
  double[] multiTermKDR = {0.0568,1.008,5.587} ;

  int frequencyOfWord = countOccurances(searchWord, wholePage);
  double KD = calculateKeywordDensity(searchWord, frequencyOfWord, totalWords);//(occ* 1.0) / wordsInPage.length; //calculateKeywordDensity(searchWord, occ, wordsInPage.length);

  //Only one of the following flags can be on at a time, (ie - equal to 1 at a time)
  int multiFlag = checkMultiTerm(searchWord);// check if the the searchTerm is multi-term
  int singleFlag = checkSingleTerm(searchWord);// check if the the searchTerm is single-term
  int characterFlag = checkCharacters(searchWord); // check if the the searchTerm is a non vowel character
  int vowelFlag = checkVowel(searchWord);//check if the the searchTerm is a vowel

  //calculate priority using KD
  double p = 0.0;
  if( multiFlag == 1)
  {
    p += calculatePriority(KD, multiTermKDR);
    p += spamFilter(KD, multiTermKDR[0], multiTermKDR[2]);
  }
  else if ( singleFlag == 1)
  {
    p += calculatePriority(KD, singleTermKDR);
    p += spamFilter(KD, singleTermKDR[0], singleTermKDR[2]);
  }
  else if ( characterFlag == 1)
  {
    p += calculatePriority(KD, charactersKDR);
    p += spamFilter(KD, charactersKDR[0], charactersKDR[2]);
  }
  else if ( vowelFlag == 1)
  {
    p += calculatePriority(KD, vowelsKDR);
    p += spamFilter(KD, vowelsKDR[0], vowelsKDR[2]);
  }
  int pint = (int) p;
  priority += pint;
  return priority;
}

public int ObtainPriorityForElements(String searchWord, EnglishWikipediaPage page ) throws IOException, InterruptedException
 {
  int priority = 0;
  String wholePage = page.getWikiMarkup();
  String[] contributorsArray = wholePage.split("<contributors>");
   int numOfContributions = contributorsArray.length;
   numOfContributions = numOfContributions * 35;
   priority = priority + numOfContributions;
  String title = page.getTitle();
      System.out.println(title);

      //checks if title contains search word as seperate word  in title
      if( countWordsInStringAndKillWhitespace(title) > 1)
      {
       String[] titleWord = title.toLowerCase().split("\\s+");
        for(String tword : titleWord)
        {
         if(tword.equals(searchWord))
         {
          priority = priority + 5000;
         }
       }
      }
        //only one of these per search term as wikipedia has unique titles
    if(searchWord.equals(formatSearchTerm(title)))
    {
          priority = priority + 50000;//give highest priority as this would be a special wiki home page for keyword
    }

  int frequencyOfWord = countOccurances(searchWord, wholePage);
  priority = priority + frequencyOfWord;
  priority = priority + boldAndItalicPriority(wholePage);
  priority = priority + headerPriority(wholePage);
  priority = priority + linkPriority(wholePage);

 return priority;

}


public int countOccurances(String findSearchWord, String wholepage) throws NullPointerException, IOException, InterruptedException
   {
    int counter = 0;
    int finalIndex = 0;
    while(finalIndex != -1)
          {
              finalIndex = wholepage.indexOf(findSearchWord,finalIndex);
               if(finalIndex != -1)
                 {
                      counter ++;
                      finalIndex += findSearchWord.length();
                 }
          }
            return counter;
}


public String formatSearchTerm(String searchWord)
{
  searchWord = searchWord.toLowerCase();//converts string to lowercase
  searchWord = searchWord.trim();// trim white space before and after string
  return searchWord;
}
//Generic Method to calculate Keyword Desnity for specific searchTerm types------------------------------------------------------------------------------
public double calculateKeywordDensity(String searchWord, int frequencyOfWord, int totalWords)throws NullPointerException, IOException, InterruptedException
{
    double keywordDensity = 0;
    double countNumInPhrase = countWordsInStringAndKillWhitespace(searchWord);//counts words in string and kills all white space
    System.out.println("//////YOO WORDS IN STRING/// : " + countNumInPhrase);
    if( countNumInPhrase == 0.0){ countNumInPhrase = 1.0;} //if no spaces then search phrase is 1 word
    keywordDensity = ((frequencyOfWord * countNumInPhrase) / totalWords)*100;

    return keywordDensity;
}

public static int countWordsInStringAndKillWhitespace(String str)
{
    int onWord = 1;//always start on a word due to trim command at top
    int wordCount = 1;
    int ret = 1; //value to be returned to call
      for (int i = 0; i < str.length(); i++) {
          if (Character.isWhitespace(str.charAt(i))) {// if on white
              if( onWord == 1) { wordCount++;} //track when on word
                    onWord = 0;//no longer on a word, now on white
                  }//end first if
              else{ //if we are on word
                  onWord = 1;
                  }
                }//end for loop

    ret = wordCount; // if count is 0 then we need to return 1
    if(ret == 0) { ret = 1; }
    return ret;
}


//CHECK SEARCH TERM METHODS START---------------------------------------------------------------------------
public static int checkCharacters(String searchTerm)
{
    String[] charactersArray = {"b","c","d","f","g","h","j","k","l","m","n","p","q","r","s","t","v","w","x","y","z"};// all letters minus vowels
    int ret = 0;
    for(String charac : charactersArray)
      {
        if( searchTerm.equals(charac))
          {
            ret = 1; //if search term is a single character from the charactersArray
          }
      }
    return ret;
}

public static int checkVowel(String searchTerm)
          {
             String[] vowelsArray = {"a","e","i","o","u"};// all letters minus vowels
            int ret = 0;
              for(String vowel : vowelsArray)
              {
                if( searchTerm.equals(vowel))// compares all vowels to search Term
                {
                  ret = 1; //if search term is a single character from the charactersArray
                }
              }

              return ret;
}

public static int checkSingleTerm(String searchTerm)
          {
            int charCount = 0;// counts chars in word
            int onWord = 1;//flag which indicates pointer is on word or white
            int wordCount = 1;// counts number of words
            int ret = 0; //value to be returned to call
           for (int i = 0; i < searchTerm.length(); i++) {
             if (Character.isWhitespace(searchTerm.charAt(i))) {// if on white
               if( onWord == 1) { wordCount++;} //track when on word
               onWord = 0;//no longer on a word, now on white
             }
             else if(!(Character.isWhitespace(searchTerm.charAt(i)))){ //if we are on char of word
             onWord = 1;
             charCount++;
             }
           }//end for loop


           if(charCount > 1 && wordCount == 1)//Make sure its not a single char and make sure word count is 1
           {
             ret = 1;
           }

           return ret;
}

public static int checkMultiTerm(String searchTerm)
          {
            int charCount = 0;// counts chars in word
            int onWord = 1;//flag which indicates pointer is on word or white
            int wordCount = 1;// counts number of words
            int ret = 0; //value to be returned to call
           for (int i = 0; i < searchTerm.length(); i++) {
             if (Character.isWhitespace(searchTerm.charAt(i))) {// if on white
               if( onWord == 1) { wordCount++;} //track when on word
               onWord = 0;//no longer on a word, now on white
             }
             else if(!(Character.isWhitespace(searchTerm.charAt(i)))){ //if we are on char of word
             onWord = 1;
             charCount++;
             }
           }//end for loop


           if(charCount > 1 && wordCount > 1)
           // if searchTerm is bigger than 1 character and has more than 1 word
           {
             ret = 1;
           }

           return ret;
}

//CHECK SEARCH TERM METHODS END---------------------------------------------------------------------------
//SPAM FILTERING METHODS START----------------------------------------------------------------------------
public int spamCapture(EnglishWikipediaPage page){ //checks spam keywords in wikipage
  int priority = 1;
  String title = page.getTitle();
  if(title.toLowerCase().contains("spam/"))
  {
    priority = 0;
  }

  //if search term doesnt contain file: or wikipedia:
   if(!(searchWord.contains("file:")) || !(searchWord.contains("wikipedia:")))
   { //..and the page starts with it...
     if(title.toLowerCase().contains("file:") || title.toLowerCase().contains("wikipedia:"))
     {
         priority = 0;//give no priority..
     }
   }

   return priority;
}

public double spamFilter(double kd, double minKD, double maxKD)throws NullPointerException, IOException, InterruptedException
        {
          double p = 0.0;
          if( kd > maxKD )
          {
            //kd of key is too high -> flag as spam
            p = -1000.0;//minus max priority
          }
          else if( kd < minKD){
            //kd is too low however may still have slight relevence so reduce less priority
            p = -100.0;
          }

          return p;
}
//SPAM FILTERING METHODS END----------------------------------------------------------------------------

//CALCULATE PRIORITY USING KEYWORD DENSITY START--------------------------------------------------------
public static double calculatePriority(double kd, double[] rangeList)
        {
          double p = 0;
          int k = 500;
          double upperbound = rangeList[2];
          double lowerbound = rangeList[0];
          double optimalValue = rangeList[1];
          double absDiff = 1; //absolute difference
          if(kd < upperbound && kd > lowerbound)
          {
                //get absolute difference between kd and Optimal value
                if(kd > optimalValue)
                {
                  absDiff = kd - optimalValue;
                }
                else{
                  absDiff = optimalValue - kd;
                }
                  // smaller the absDiff -->the closer to OV, -> higher the priority
                  p = k / absDiff; //500 is calculated using lowest bound from all types


          }
          else{
            //page gets minimal boost in priority
            p = (k/10) / Math.abs(kd - optimalValue);
          }


          return p;
}
//CALCULATE PRIORITY USING KEYWORD DENSITY END------------------------------------------------------------

//----------------------------------------harrishan-----------------------------

public int genericPriority(String symbol, String wholePage){
        int charLength = symbol.length();//get length os symbols e.g. ''' = 3
        String character = symbol.substring(0,1);//get first character of string e.g. '' produces '
    //    int Occurances = countOccurances(character, wholePage); //character = "=", counts number of occurances of char in wholePage
        List<Integer> positions = arrayIndexOfString(character, wholePage); //character = "=", calculates position of each occurances of char in wholePage

        List<Integer> Size = new ArrayList();//array list for length of symbols
        List<Integer> startPosition = new ArrayList();//positions and shit
        for(int i = 0; i < positions.size(); i++){
            int c = checkRepeats(positions.get(i), wholePage, character); //character = "="
            if(c == 0){//means that num has already been counted

            }
            else{
                if(c == charLength){//if c = charLength, e.g. ''' 3, this is less efficient, be better to make a header method and a
                    Size.add(c);
                    startPosition.add(positions.get(i));
                }
            }
        }
        System.out.println("Size "+Size.size());

        //need to work out range of sizes e.g. if it's 2-3 (bold & italics) or 2-6 (for headers)

        List<String> Array = new ArrayList();//make a array for each range value
        int count = 0;
        for(int i = 0; i < Size.size()/2; i = i+2){//in 2's because everything comes in pair e.g. ''' bold ''' or == Header ==
            if(Size.get(i) == 2){                       //make if statement for each range value
                int start = startPosition.get(i)+2;
                int end = startPosition.get(i+1);
                Array.add(wholePage.substring(start,end));
            }
            count++;
        }

        //for

        return 0;
}

public int boldAndItalicPriority(String wholePage){

      int p = 0;
      List<Integer> positions = arrayIndexOfString("'", wholePage); //character "=", calculates positions of each occurances of character in wholePage

      List<Integer> Size = new ArrayList();//array list for adjacent character repition sizes
      List<Integer> startPosition = new ArrayList();//stores positions of each occurance of characters
      for(int i = 0; i < positions.size(); i++){
          int c = checkRepeats(positions.get(i), wholePage, "'"); //method returns the size of each adjacent character repition
          if(c == 0){//when c = 0, that position has already been included
            //do nothing
          }
          else{
              if(c != 1){//don't add when c = 1, as that is just an apostrophe
                  Size.add(c);
                  startPosition.add(positions.get(i));//size and startPosition correspond to each other startPosition(i) and size(i) represent same character
              }
          }
      }


      List<String> iArray = new ArrayList();//italic
      List<String> bArray = new ArrayList();//bold
      List<String> bAndIArray = new ArrayList();//bold & italic
      int count = 0;
      for(int i = 0; i < Size.size()/2; i = i+2){//increment by 2 as everything comes in pair e.g. ''' bold ''' or == Header ==
          if(Size.get(i) == 2){                       //make if statement for each range value
              int start = startPosition.get(i)+2;//increment 2 index positins as don't want to add mark-up characters in substring
              int end = startPosition.get(i+1);
              iArray.add(wholePage.substring(start,end));//gets string bewteen mark-up pair. e.g. '' this is added into array ''
          }
          else if(Size.get(i) == 3){
              int start = startPosition.get(i)+3;//i+3 index positins as don't want to add mark-up characters in substring
              int end = startPosition.get(i+1);//i+1 so as wiki-mark up comes in pair and next positin in array is the corresponding pair
              bArray.add(wholePage.substring(start,end));//^same as above
          }
          else if(Size.get(i) == 5){
              int start = startPosition.get(i)+5;
              int end = startPosition.get(i+1);
              bAndIArray.add(wholePage.substring(start,end));//^same as above
          }
          count++;
      }

      for(int i = 0; i < iArray.size(); i++){//search each substring of page for the word
          if(iArray.get(i).toLowerCase().contains(searchWord.toLowerCase())){//toLowerCase to ignore alphabet cases
              p = p + 55;//italics
          }
      }

      for(int i = 0; i < bArray.size(); i++){//^same as above
          if(bArray.get(i).toLowerCase().contains(searchWord.toLowerCase())){
              p = p + 58;//bold
          }
      }

      for(int i = 0; i < bAndIArray.size(); i++){//^same as above
          if(bAndIArray.get(i).toLowerCase().contains(searchWord.toLowerCase())){
              p = p +113;//bold and italic
          }
      }

      return p;//return priority of bold, italic and both for page
}

public int headerPriority(String wholePage){
      int p = 0;
      List<Integer> positions = arrayIndexOfString("=", wholePage); //character = "=", calculates position of each occurances of char in wholePage

      List<Integer> Size = new ArrayList();//array list for adjacent character repition sizes
      List<Integer> startPosition = new ArrayList();//stores positions of each occurance of characters
      for(int i = 0; i < positions.size(); i++){
          int c = checkRepeats(positions.get(i), wholePage, "="); //character = "="
          if(c == 0){//when c = 0, that position has already been included

          }
          else{
              if(c != 1){//don't add when c = 1, as that is just a title
                  Size.add(c);
                  startPosition.add(positions.get(i));
              }
          }
        }


      List<String> h2Array = new ArrayList();//or make a array for each range value
      List<String> h3Array = new ArrayList();
      List<String> h4Array = new ArrayList();
      List<String> h5Array = new ArrayList();
      List<String> h6Array = new ArrayList();
      int count = 0;
      for(int i = 0; i < Size.size()/2; i = i+2){//in 2's because everything comes in pair e.g. ''' bold ''' or == Header ==
          if(Size.get(i) == 2){                        //make if statement for each range value
              int start = startPosition.get(i)+2;
              int end = startPosition.get(i+1);
              h2Array.add(wholePage.substring(start,end));
          }
          else if(Size.get(i) == 3){
              int start = startPosition.get(i)+3;//increment 3 index positins as don't want to add mark-up characters in substring e.g. don't include "==="
              int end = startPosition.get(i+1);//i+1 so as wiki-mark up comes in pair and next positin in array is the corresponding pair
              h3Array.add(wholePage.substring(start,end));//gets string bewteen mark-up pair. e.g. == this is added into array ==
          }
          else if(Size.get(i) == 4){
              int start = startPosition.get(i)+4;
              int end = startPosition.get(i+1);
              h4Array.add(wholePage.substring(start,end));
          }
          else if(Size.get(i) == 5){
              int start = startPosition.get(i)+5;
              int end = startPosition.get(i+1);
              h5Array.add(wholePage.substring(start,end));
          }
          else if(Size.get(i) == 6){
              int start = startPosition.get(i)+6;
              int end = startPosition.get(i+1);
              h6Array.add(wholePage.substring(start,end));
          }
          count++;
      }

      for(int i = 0; i < h2Array.size(); i++){
          if(h2Array.get(i).toLowerCase().contains(searchWord.toLowerCase())){
              p = p + 340;//header2
          }
      }

      for(int i = 0; i < h3Array.size(); i++){
          if(h3Array.get(i).toLowerCase().contains(searchWord.toLowerCase())){
              p = p + 220;//header3
          }
      }

      for(int i = 0; i < h4Array.size(); i++){
          if(h4Array.get(i).toLowerCase().contains(searchWord.toLowerCase())){
              p = p + 160;//header4
          }
      }

      for(int i = 0; i < h5Array.size(); i++){
          if(h5Array.get(i).toLowerCase().contains(searchWord.toLowerCase())){
              p = p + 100;//header5
          }
      }

      for(int i = 0; i < h6Array.size(); i++){
          if(h6Array.get(i).toLowerCase().contains(searchWord.toLowerCase())){
              p = p + 60;//header6
          }
      }
      return p;
}

public int linkPriority(String wholePage) throws IOException, InterruptedException
  {
      int p = 0;
      List<Integer> positions = arrayIndexOfString("[", wholePage); //character = "[", calculates position of each occurances of char in wholePage

      List<Integer> SizeL = new ArrayList();//array list for length of symbols
      List<Integer> startPosition1 = new ArrayList();//positions and shit

      List<Integer> SizeLL = new ArrayList();//array list for length of symbols
      List<Integer> startPosition2 = new ArrayList();//positions and shit
      for(int i = 0; i < positions.size(); i++){
          int c = checkRepeats(positions.get(i), wholePage, "["); //character = "="
          if(c == 0){//means that num has already been counted

          }
          else{
              if(c == 1){//if c = charLength, e.g. ''' 3, this is less efficient, be better to make a header method and a
                  SizeL.add(c);
                  startPosition1.add(positions.get(i));
              }
              else if(c == 2){//if c = charLength, e.g. ''' 3, this is less efficient, be better to make a header method and a
                  SizeLL.add(c);
                  startPosition2.add(positions.get(i));
              }
          }
        }

      List<Integer> positions2 = arrayIndexOfString("]", wholePage);

      List<Integer> SizeL2 = new ArrayList();//array list for length of symbols
      List<Integer> endPosition1 = new ArrayList();//positions and shit

      List<Integer> SizeLL2 = new ArrayList();//array list for length of symbols
      List<Integer> endPosition2 = new ArrayList();//positions and shit
      for(int i = 0; i < positions2.size(); i++){
          int c = checkRepeats(positions2.get(i), wholePage, "]"); //character = "="
          if(c == 0){//means that num has already been counted

          }
          else{
              if(c == 1){//if c = charLength, e.g. ''' 3, this is less efficient, be better to make a header method and a
                  SizeL2.add(c);
                  endPosition1.add(positions2.get(i));
              }
              else if(c == 2){//if c = charLength, e.g. ''' 3, this is less efficient, be better to make a header method and a
                  SizeLL2.add(c);
                  endPosition2.add(positions2.get(i));
              }
          }
      }


      List<String> ilArray = new ArrayList();
      List<String> elArray = new ArrayList();


      if(startPosition1.size() == endPosition1.size()){//checks if there are the same number of open and close brackets, if not, error on page (internal)
          System.out.println("[] Same Size");
          for(int i = 0; i < startPosition1.size(); i++){

              int l = i;
              int j = i+1;
              if(j < startPosition1.size() && startPosition1.get(j)<endPosition1.get(i)){//if start bracket position i+1 < end bracket postion i, then looks like [ [ ] ]
                  while(j < startPosition1.size() && startPosition1.get(j)<endPosition1.get(i)){//for every mis matched open and closing bracket increment j
                      j++;
                  }
                  for(int k = j-1; k >= l; k--){//for loop to run, j and i in opposite directions e.g. 12 pairs with 10, 11 pairs with 11 & 10 pairs with 12
                      int start = startPosition1.get(k)+1;//index of start position

                      int end = endPosition1.get(i);//index of end position
                      i++;

                      ilArray.add(wholePage.substring(start,end));//substring bewteen end and start position
                  }
                  i--;
              }
              else if(startPosition1.get(i)>endPosition1.get(i)){//if start position is bigger then end poisition i, something wrong with data
                  System.out.println("error retrieving link");
              }
              else{//data already in perfect order, add substring into array
                  int start = startPosition1.get(i)+1;
                  int end = endPosition1.get(i);
                  ilArray.add(wholePage.substring(start,end));
              }
          }
      }
      else{
          System.out.println("[] Not Same Size");//do not process if num of opening characters does not equal ending characters
      }

      if(startPosition2.size() == endPosition2.size()){//checks if there are the same number of open and close brackets, if not, error on page (external)
          System.out.println("[[]] Same Size");
          for(int i = 0; i < startPosition2.size(); i++){
              int l = i;
              int j = i+1;

              if(j < startPosition2.size() && startPosition2.get(j)<endPosition2.get(i)){//if start bracket position i+1 < end bracket postion i, then looks like [ [ ] ]
                  while(j < startPosition2.size() && startPosition2.get(j)<endPosition2.get(i)){//for every mis matched open and closing bracket increment j

                      j++;
                  }
                  for(int k = j-1; k >= l; k--){//for loop to run, j and i in opposite directions e.g. 12 pairs with 10, 11 pairs with 11 & 10 pairs with 12
                      int start = startPosition2.get(k)+2;//index of start position
                      int end = endPosition2.get(i);//index of end position
                      i++;

                      elArray.add(wholePage.substring(start,end));//substring bewteen end and start position
                  }
                  i--;
              }
              if(startPosition2.get(i)>endPosition2.get(i)){//if start position is bigger then end poisition i, something wrong with data
                  System.out.println("error retrieving link");
              }
              else{//data already in perfect order, add substring into array
                  int start = startPosition2.get(i)+2;
                  int end = endPosition2.get(i);
                  elArray.add(wholePage.substring(start,end));
              }
          }
      }
      else{
          System.out.println("[[]] Not Same Size");//do not process if num of opening characters does not equal ending characters
      }


      for(int i = 0; i < ilArray.size(); i++){//used to print
          if(ilArray.get(i).toLowerCase().contains(searchWord.toLowerCase())){
              p = p + 140;//internal link
          }
      }

      for(int i = 0; i < elArray.size(); i++){//used to print
          if(elArray.get(i).toLowerCase().contains(searchWord.toLowerCase())){
              p = p + 20;//external link
              int repeats = countOccurances("|",elArray.get(i));//check for number of sybol, if more than 2, it is an image caption
              if(repeats >= 2 ){//for image captions
                  p = p + 100;
              }
          }
      }

      return p;
}


public List<Integer> arrayIndexOfString(String findSearchWord, String wholepage) { // returns array giving position of every occurance in the string
	      int lastIndex = 0;
        List<Integer> result = new ArrayList<Integer>();

	      while(lastIndex != -1) {

	         lastIndex = wholepage.indexOf(findSearchWord,lastIndex);//returns position of occurance in string or -1 if no more occurances

    		   if(lastIndex != -1){//run if statement as long as lastIndex does not
        	     result.add(lastIndex);//add position of index into arraylist
               lastIndex += 1;
           }
    		}
    		return result;
}

public int checkRepeats(int position, String wholepage, String findSearchWord) { // returns integer representing repititions of each character adjacent to each other, eg if === then 3,
          int num = 0;
          char SW = findSearchWord.charAt(0);
  	      if(position == 0 || wholepage.charAt(position-1) != (SW)){
  		        while(position < wholepage.length() && wholepage.charAt(position) == SW ){
                num = num + 1;//increment num, if repeat found
                position++;//increment position to check for next loop
              }
              return num;//retrun repitions as size
  	      }
  	      else{
  		        return 0;//returns 0 if position before is the same character (done at start of method), avoids counting size of each row of characters more than once
  	      }
 }
}
