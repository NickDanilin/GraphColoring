package edu.spbstu.input;

public class InputValidationException extends Exception {
    public InputValidationException() {
        super();
    }

    public InputValidationException(String message) {
        super(message);
    }

    public InputValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public InputValidationException(Throwable cause) {
        super(cause);
    }
}
