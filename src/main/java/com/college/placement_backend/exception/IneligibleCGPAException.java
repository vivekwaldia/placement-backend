package com.college.placement_backend.exception;

public class IneligibleCGPAException extends RuntimeException{
    public IneligibleCGPAException(String message){
        super(message);
    }
}