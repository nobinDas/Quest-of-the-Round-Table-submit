package team1.project.exception;

public class InvalidGameException extends Exception{
    private String message;

    public InvalidGameException(String message){
        this.message = message;

    }

    public String message(){
        return message;
    }
}
