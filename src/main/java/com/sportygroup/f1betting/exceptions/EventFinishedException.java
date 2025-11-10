package com.sportygroup.f1betting.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class EventFinishedException extends RuntimeException{
 private String message;
}
