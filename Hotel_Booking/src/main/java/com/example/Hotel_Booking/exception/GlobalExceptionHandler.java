package com.example.Hotel_Booking.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.Hotel_Booking.dto.ResponseDTO;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseDTO handleCustom(CustomException ex) {
        return new ResponseDTO(false, ex.getMessage(), null);
    }

//    @ExceptionHandler(Exception.class)
//    public ResponseDTO handleAll(Exception ex) {
//        return new ResponseDTO(false, "Something went wrong", null);
//    }
    
    @ExceptionHandler(Exception.class)
    public ResponseDTO handleAll(Exception ex) {
       // ex.printStackTrace();  // 🔥 सबसे जरूरी
        return new ResponseDTO(false, ex.getMessage(), null);
    }
}