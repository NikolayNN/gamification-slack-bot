package juja.microservices.gamification.slackbot.dao;

import juja.microservices.gamification.slackbot.exceptions.UserExchangeException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * @author Artem
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class RestUserRepositoryTest {

    @Inject
    private UserRepository userRepository;

    @Inject
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @Value("${user.baseURL}")
    private String urlBase;
    @Value("${endpoint.userSearch}")
    private String urlGetUser;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setup() {
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();
    }

    @Test
    public void shouldReturnUserWhenSendUserDataToRemoteUserService2() {
        //given
        mockServer.expect(requestTo(urlBase + urlGetUser))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(content().string("{\"slackNames\":[\"@bob\"]}"))
                .andRespond(withSuccess("[{\"uuid\":\"AAAA123\",\"slack\":\"@bob\"}]", MediaType.APPLICATION_JSON_UTF8));
        //when
        String result = userRepository.findUuidUserBySlack("@bob");
        // then
        mockServer.verify();
        assertEquals(result, "AAAA123");
    }

    @Test
    public void shouldAddDogToTheSlackNameIfSlackNameHasNotIt() {
        //given
        mockServer.expect(requestTo(urlBase + urlGetUser))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(content().string("{\"slackNames\":[\"@bob\"]}"))
                .andRespond(withSuccess("[{\"uuid\":\"AAAA123\",\"slack\":\"@bob\"}]", MediaType.APPLICATION_JSON_UTF8));
        //when
        String result = userRepository.findUuidUserBySlack("bob");
        // then
        mockServer.verify();
        assertEquals(result, "AAAA123");
    }

    @Test
    public void shouldThrowExceptionWhenFindUserUuidBySlackToRemoteUserServiceThrowException() {
        // given
        mockServer.expect(requestTo(urlBase + urlGetUser))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withBadRequest().body("{\"httpStatus\":400,\"internalErrorCode\":1," +
                        "\"clientMessage\":\"Oops something went wrong :(\"," +
                        "\"developerMessage\":\"General exception for this service\"," +
                        "\"exceptionMessage\":\"very big and scare error\",\"detailErrors\":[]}"));
        //then
        thrown.expect(UserExchangeException.class);
        thrown.expectMessage(containsString("Oops something went wrong :("));
        //when
        userRepository.findUuidUserBySlack("@user");
    }
}