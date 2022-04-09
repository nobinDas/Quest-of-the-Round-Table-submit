package team1.project.exception;

public class NotFoundException extends Exception{
    private String message;

    public NotFoundException(String message){
        this.message = message;

    }

    public String message(){
        return message;
    }
}