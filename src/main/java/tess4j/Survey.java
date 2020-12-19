package tess4j;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import net.sourceforge.tess4j.*;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;


import java.util.*;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.json.JSONObject;

/**
 * @author Eshan Wadhwa
 */

/**
 * I created a program which scans images of surveys and creates a survey out of them. I use the Tesseract API to get the text from the image as 
 * a string. Then, I apply my own logic to perform text analysis on that string to obtain the different questions and answer choices 
 */


public class Survey {

	public static String surveyName; //name of the survey

	public static String surveyDesc = ""; //description of the survey

	public static ArrayList<String> questionName = new ArrayList<String>(); //arraylist with the name of the question

	public static ArrayList<String> answerName = new ArrayList<String>(); //arralist with the answers.

	public static ArrayList<String> answerName2 = new ArrayList<String>(); //new arraylist with correct answers.

	public static LinkedHashMap<String, AnswerDTO> map = new LinkedHashMap<String, AnswerDTO>(); //the map which will store vital information.

	public static ArrayList<String> strNew = new ArrayList<String>();
	
	public static boolean alpha;


	/**
	 * The start point of my application.
	 * @param args 
	 */
	public static void main(String[] args) throws IOException
	{

		String result = null; // String which extracts text from image.

		File file = new File("/Users/eshan/Downloads/tess4j-master/src/test/resources/test-data/image1 copy.png"); //file/image to run program on.

		ITesseract instance = new Tesseract();

		instance.setDatapath("/Users/eshan/Downloads/tess4j-master/src/main/resources/tessdata");

		instance.setTessVariable("user_defined_dpi", "300");

		instance.setTessVariable("debug_file", "/dev/null");

		try
		{
			result = instance.doOCR(file); //calls the Tesseract API
			//System.out.println(result);
		}
		catch (TesseractException e)
		{
			System.err.println(e.getMessage());
		}

		result = result.replace(":", "?"); 


		try (InputStream modelIn = new FileInputStream("en-sent.bin")) {
			SentenceModel model = new SentenceModel(modelIn);

			SentenceDetectorME sentenceDetector = new SentenceDetectorME(model);

			String sentences[] = sentenceDetector.sentDetect(result);

			getNameDesc(result);

			getQuestionAnswer(sentences);
			
			//perform text analysis below.


			String[] answer = GetStringArray(answerName);


			String temp = "";
			
			String temp2 = "";
			
			String temp3 = "";
			
			String temp4 = "";

			for(int i = 0; i < answer.length; i++)
			{

				if(answer.length == 1)
				{
					answerName2.add(answer[0]);
					break;
				}

				if(!(i + 1 < answer.length)) {
					
					temp4 = temp;
					
					if(!answer[answer.length-1].contains("?"))
					{
						temp2 = answer[answer.length-1];
					}
					
					if(temp4.contains(",Text"))
					{
					temp4 = "";
					}
					
					
					temp3 = temp4 + temp2;


				}
				if(answer[i].contains("?"))
				{
					answerName2.remove(answer[i]);
				}

				else if(answer[i].startsWith(","))
				{


					answerName2.add(temp);



					temp = "";
					temp += answer[i] + " ";



				}


				else
				{


					temp+=answer[i] + " ";
				}


			}


			answerName2.add(temp3);



			ArrayList<String> answerName3 = new ArrayList<String>();


			for(String s : answerName2) {
				if(s != null && s.length() > 0) {
					answerName3.add(s);
				}
			}



			String[] question = GetStringArray(questionName);

			String[] answer2 = GetStringArray(answerName3);

			AnswerDTO answerDTO = new AnswerDTO(null, 0 , null);

			String dataType = "";

			for(String s : question)
			{
				s.trim();
			}

			for(String s : answer2)
			{
				s.trim();
			}

			String lQuestion = "";
			AnswerDTO lAnswer = new AnswerDTO(null, 0 , null);
		

			if(question.length == 1)
			{
				
				if(answer2[0].contains("Text")) {
					dataType = "Textbox,";
				}
				else
				{
					dataType = "Dropdown,";
				}
				
				answerDTO =  new AnswerDTO(dataType, 1, answer2[0]);
				map.put(question[0], answerDTO);


			}

			for(int i = 0; i < question.length-1; i++)
			{

				int AWords = countWordsUsingSplit(answer2[i]);

				String last = question[question.length-1];


				if(question[i+1].equals(last))
				{
					if(answer2[i+1].contains("Text")) {
						dataType = "Textbox,";
					}
					else
					{
						dataType = "Dropdown,";
					}
					
					answerDTO =  new AnswerDTO(dataType, i+2, answer2[i+1]);
					//map.put(question[i+1], answerDTO);

					lQuestion = question[i+1];
					lAnswer = answerDTO;


				}
				

				if(question[i].contains("date") || question[i].contains("Date"))
				{
					dataType = "Date,";
				
					answer2[i] = ",Text";
				
				}
				else if(question[i].contains("description") || question[i].contains("Description"))
				{
					dataType = "Text Area,";

				}
				else if(answer2[i].contains("Text"))
				{
					dataType = "Textbox,";
				}
				else if(AWords == 2 )
				{
					dataType = "RadioButton";
				}
				else
				{
					dataType = "Dropdown,";
				}
				
				
				
				
				
				answerDTO =  new AnswerDTO(dataType, i+1, answer2[i]);

				{
					map.put(question[i], answerDTO);


				}

			}


			if(question.length > 1)
			{
				map.put(lQuestion, lAnswer);

			}

			
			int j = 1;
	
						
			for (String i : map.keySet()) 
			{
				
				
				 
				  System.out.println("Question " + j + ": " + i + "\n" + "\n" + map.get(i));
				 
				  
				  System.out.println("\n");
				  
				  j++;
				}
			
	

		}
		
		//call the KloudGin API to store the info in the database and create a survey.
		
		String resource = "https://goldenbuilddev.kloudgin.com/kloudgin-fsmapi/user/authenticate?tenant=Goldenbuild&user=wadhavaeshan";
		
		String jsonInputString = "{\n" + "\"input\": ,\r\n" +
    	        "    \"surveyDescription\": surveyDesc,\r\n" +
    	        "    \"surveyName\": surveyName,\r\n" +
    	        "    \"activityTypeNamesList\": \"Test\",\r\n" +
    	        "    \"createdBy\": \"admin\",\r\n" +
    	        "    \"assetCategory\": \"userDefined\",\r\n" +
    	        "    \"questions\": map,\r\n" +"\n}";
		
		
		 

		JSONObject jo = new JSONObject(map);

	  
	      String jsonText = jo.toString();
	      
	      System.out.println(jsonText);
	      
	
     
		
		try {
			callPOSTOrPUTAPI(resource, jsonInputString, "POST", "JSESSIONID=0CDE6CE8B10CC0AB8BE6E4E2B0C587D8");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	/**
	 * This method gets the survey name and description from the image.
	 * @param String result the string which contains the text from the image
	 */
	

	public static void getNameDesc(String result) throws IOException
	{
		BufferedReader bufReader = new BufferedReader(new StringReader(result));
		Scanner scanner = new Scanner(result);


		String line=null;
		while((line=bufReader.readLine()) != null)
		{
			surveyName = line;
			break;
		}
	
		while (scanner.hasNextLine()) {
			String line2 = scanner.nextLine();
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  
			if(line2.equals(surveyName))
			{
				continue;

			}
			if(line2.contains("?"))
			{
				break;
			}
			else 
			{
				surveyDesc += line2;
				break; 
			}

		}

		scanner.close();
	}
	
	/**
	 * This method gets the questions and answers from the image.
	 * @param String[] result the string[] which contains the text from the image
	 */
	

	public static void getQuestionAnswer(String[] result) throws IOException
	{

		String stringArray[] = result;
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < stringArray.length; i++) {
			sb.append(stringArray[i]);
		}
		
		String str = Arrays.toString(stringArray);

		str = str.replace(surveyName, "");
		str = str.replace(surveyDesc, "");
		str = str.replace("\n\n", "\n");
		str = str.replace("[", "");
		str = str.replace("]", "");



		String[] str3 = str.split("(?<=\\?)|\\[|(?<=\\.)"); 


		

		List<String> list = new ArrayList<String>();

		for(String s : str3) {
			if(s != null && s.length() > 0) {
				list.add(s);
			}
		}

		str3 = list.toArray(new String[list.size()]);



			//get rid of leadding numbers

		for (int i = 0; i < str3.length; i++) {

			str3[i] = str3[i].replaceFirst(",","");
	
			str3[i] = str3[i].replaceFirst("[-]","");


			str3[i] = str3[i].replaceFirst("[.]","");
			str3[i] = str3[i].trim();



		}

	


		
		strNew = removeDuplicates(strNew); 


		
		String[] strFinal = str3;
		
		
		
		for(int i = 0; i < strFinal.length; i ++)
		{
			if(strFinal[i].contains("?"))
			{
				break;
			}
			else
			{
				strFinal[i] = "";
			}
		}
		
		
		for(int i = 0; i < strFinal.length; i ++)
		{
			if(strFinal[i].contains(surveyDesc))
			{
				strFinal[i] = strFinal[i].replaceAll(surveyDesc, "");
			}
		}


		for (int i = 0; i < strFinal.length; i++) {


			strFinal[i] = strFinal[i].trim();



		}


		List<String> list2 = new ArrayList<String>();

		for(String s : strFinal) {
			if(s != null && s.length() > 0) {
				list2.add(s);
			}
		}

		strFinal = list2.toArray(new String[list2.size()]);





		for(int i = 0; i < strFinal.length; i ++)
		{
			if(strFinal[i].contains("?"))
			{
				questionName.add(strFinal[i]);


			}
		}

		String last = strFinal[strFinal.length-1];


		for(int i = 0; i < strFinal.length-1; i++)
		{
			if(strFinal[i].contains("?") && strFinal[i+1].contains("?"))
			{
				strFinal  = insert(strFinal,"Text",i+1);
				i++;

			}


			if(strFinal[i+1].equals(last) && strFinal[i+1].contains("?"))

			{
				strFinal  = insert(strFinal,"Text",i+2);
			}

		}



		for(int i = 0; i<strFinal.length-1; i++)
		{
			if(!strFinal[i+1].contains("?") && strFinal[i].contains("?"))
			{
				strFinal[i+1] = "," + strFinal[i+1];
			}
		}





		List<String> list3 = new ArrayList<String>();

		for(String s : strFinal) {
			if(s != null && s.length() > 0) {
				list3.add(s);
			}
		}


		if(list3.size() == 2)
		{
			answerName.add(strFinal[1]);
		}


		ListIterator<String> listIterator = list3.listIterator();



		String previous=listIterator.next();
		String current=listIterator.next();
		while(listIterator.hasNext()){
			String next=listIterator.next();
			
			if(questionName.size() == 2)
			{
				answerName.add(current);
				previous=current;
				current=next;
				continue;

			}


			if(!current.contains("?") || !previous.contains("?"))
			{
				answerName.add(current);
			}

			if(current.equals(last))
			{
				answerName.add(",Text");
			}




			previous=current;
			current=next;
		}
		
		
		if(list3.size() != 2)
		{
			answerName.add(strFinal[strFinal.length-1]);

		}
		


	}
	
	/**
	 * This method gets the String array from the ArrayList.
	 * @param String ArrayList arr which contains information.
	 * @return String [] from the String ArrayList.
	 */
	


	public static String[] GetStringArray(ArrayList<String> arr)
	{

		String str[] = new String[arr.size()];

		for (int j = 0; j < arr.size(); j++) {

			str[j] = arr.get(j);
		}

		return str;
	}

	/**
	 * This method removes duplicates from the String ArrayList.
	 * @param String ArrayList list the ArrayList to remove duplicates from.
	 * @return String ArrayList with removed duplicates.
	 */
	

	public static <String> ArrayList<String> removeDuplicates(ArrayList<String> list)
	{

		ArrayList<String> newList = new ArrayList<String>();

		for (String element : list) {

		
			if (!newList.equals(element)) {

				newList.add(element);
			}
		}

		return newList;
	}
	
	

	/**
	 * This method inserts an element at an index.
	 * @param String Array a the Array to add an element.
	 * @param String key what to add.
	 * @param Int index the index where to add.
	 * @return String Array new array with added element.
	 */
	private static String[] insert(String[] a, String key, int index) {
		String[] result = new String[a.length + 1];

		System.arraycopy(a, 0, result, 0, index);
		result[index] = key;
		System.arraycopy(a, index, result, index + 1, a.length - index);

		return result;
	}
	
	/**
	 * This method counts the number of words an answer has.
	 * @param String input the String to count.
	 * @return Int number of words answer has.
	 */
	

	public static int countWordsUsingSplit(String input) {
		if (input == null || input.isEmpty())
		{ return 0;
		}
		String[] words = input.split("\\s+");
		return words.length;
	}
	
	/**
	 * This method checks if a character is a letter or digit.
	 * @param Char input_char the character to check.
	 */
	
	public static void charCheck(char input_char) 
    { 
        // CHECKING FOR ALPHABET 
        if ((input_char >= 65 && input_char <= 90) 
            || (input_char >= 97 && input_char <= 122)) 
        {
        	alpha = true; 

        }
  
        
        // CHECKING FOR DIGITS 
        else if (input_char >= 48 && input_char <= 57) 
        	alpha = true; 
  
        else
        	alpha = false; 
           
    } 
	
	public static JsonObject callPOSTOrPUTAPI(String resource, String jsonPayload, String requestMethod, String sessionId) throws Exception {

		JsonObject opJson= null;
		try{
		Long startTime = System.currentTimeMillis();
		System.out.println(  "Calling - "+resource);
		System.out.println(   "Payload : "+jsonPayload);
		URL url = new URL(resource);
		HttpURLConnection connection = (HttpURLConnection)url.openConnection(); 
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setRequestMethod(requestMethod);
		connection.setRequestProperty("Accept", "application/json");
		connection.setInstanceFollowRedirects(false);
		connection.setRequestProperty("Content-Type", "application/json"); 
		connection.setRequestProperty("charset", "utf-8");
		if(jsonPayload != null)
		connection.setRequestProperty("Content-Length", "" + Integer.toString(jsonPayload.getBytes().length));
		if(sessionId!=null){
		connection.setRequestProperty("Cookie", "JSESSIONID="+sessionId);
		}

		if(jsonPayload != null){
		if(!"DELETE".equalsIgnoreCase(requestMethod)){
		connection.getOutputStream().write(jsonPayload.getBytes("utf-8"));

		connection.getOutputStream().flush();
		}
		}
		StringBuilder sb = new StringBuilder(); 

		int HttpResult =connection.getResponseCode(); 
		String output = "";

		if(HttpResult ==HttpURLConnection.HTTP_OK){

		BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(),"utf-8"));  

		String line = null;  

		while ((line = br.readLine()) != null) {  
		sb.append(line + "\n");  
		}  

		br.close();  
		output = sb.toString();
//		    TenantLogger.info(tenantId, ""+sb.toString());  

		}else{
		System.out.println( connection.getResponseMessage());  
		}  
		// TenantLogger.info(tenantId, "JSON Response Code from "+resource+" - " + HttpResult + "- Response  : "+output);
		Long endTime = System.currentTimeMillis();
		Long totalTime=endTime-startTime;
		System.out.println(  "Time taken for the API call "+resource+" - "+ (totalTime) + "ms");
		opJson = createJsonFromString(output);
		
		} catch (Exception e) {
		System.out.println("Exception occurred in callPOSTOrPUTAPI" + e.getMessage());
		e.printStackTrace();
		throw new Exception("Exception occurred in callPOSTOrPUTAPI" + e.getMessage());
		} 

		return opJson;
		}
	
	public static JsonObject createJsonFromString(String jsonString) throws Exception {
		if (jsonString == null || jsonString.isEmpty())  {
		throw new Exception("Cannot create object from empty string");
		}

		JsonObject jsonObject = null;
		try {
		JsonReader reader = Json.createReader(new StringReader(jsonString));
		jsonObject = reader.readObject();
		} catch (JsonException e) {
		throw new Exception("JsonParsingException:" + e.toString());
		}

		return jsonObject;
		}


}
