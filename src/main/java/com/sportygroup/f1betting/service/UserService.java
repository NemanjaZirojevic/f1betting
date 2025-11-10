package com.sportygroup.f1betting.service;

import com.sportygroup.f1betting.entity.User;
import com.sportygroup.f1betting.exceptions.OutOfBalanceException;
import com.sportygroup.f1betting.exceptions.UserNotFoundException;
import com.sportygroup.f1betting.model.PlaceBetRequest;
import com.sportygroup.f1betting.repository.UserRepository;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {

  private final UserRepository userRepository;


  public User checkUserBalance(PlaceBetRequest req) {
    var user = userRepository.findById(req.userId())
        .orElseThrow(() -> new UserNotFoundException("User with id: %s not found".formatted(req.userId())));

    if (user.getBalance() < req.amount()) {
      throw new OutOfBalanceException("Insufficient funds for this bet");
    }
    return user;
  }

  public List<User> findAllById(Set<Long> userIds) {
    return userRepository.findAllById( userIds);
  }

  public void saveAll(List<User> users) {
    userRepository.saveAll(users);
  }

}
