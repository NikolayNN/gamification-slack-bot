package juja.microservices.gamification.slackbot.service;

import juja.microservices.gamification.slackbot.model.DTO.UserDTO;

import java.util.List;
import java.util.Set;

/**
 * @author Artem
 * @author Nikolay Horushko
 */
public interface UserService {

    List<UserDTO> findUsersBySlackNames(List<String> slackNames);

    Set<UserDTO> findUsersByUuids(Set<String> uuids);
}
