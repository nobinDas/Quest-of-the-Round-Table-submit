package team1.project.exception;

public class InvalidParamException extends Exception{

    private String message;

    public InvalidParamException(String message){
        this.message = message;

    }

    public String message(){
        return message;
    }
}
