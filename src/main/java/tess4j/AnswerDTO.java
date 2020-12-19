package tess4j;

/**
 * Class which contains the structure of the output.
 */


public class AnswerDTO {
	
	public String dataType; // each image has a dataType (ex: radio button, textbox, etc.)
	
	public int sequence; //the question number,
		
	public String answers; //the answers to the question.
	
	
	public AnswerDTO(String dataType, int sequence, String answers) 
	{
        this.dataType = dataType;
        this.sequence = sequence;
        this.answers = answers;
         
    }
	
	
	/**
	 * This method gets the textual representation of the AnswerDTO object.
	 * return String the String representation of the object.
	 */

	public String toString()
	{
		
		return "Question#: " + sequence + "\n" + "Data Type: " + dataType +  "\n" + "Answer Options: " + answers;
	}

}
