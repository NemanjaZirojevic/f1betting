package com.sportygroup.f1betting.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DuplicateBetException extends RuntimeException {
  private String message;
}
