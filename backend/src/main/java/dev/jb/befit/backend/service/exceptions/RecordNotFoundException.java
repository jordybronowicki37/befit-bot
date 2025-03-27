package dev.jb.befit.backend.service.exceptions;

public class RecordNotFoundException extends MyException {
    public RecordNotFoundException() {
        super("Record was not found");
    }
}
