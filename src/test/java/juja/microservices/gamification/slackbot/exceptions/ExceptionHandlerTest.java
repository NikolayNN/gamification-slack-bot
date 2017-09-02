package juja.microservices.gamification.slackbot.exceptions;

import juja.microservices.gamification.slackbot.controller.GamificationSlackCommandController;
import juja.microservices.gamification.slackbot.model.DTO.UserDTO;
import juja.microservices.gamification.slackbot.service.GamificationService;
import juja.microservices.utils.SlackUrlUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.inject.Inject;
import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Danil Kuznetsov
 * @author Nikolay Horushko
 */
@RunWith(SpringRunner.class)
@WebMvcTest(GamificationSlackCommandController.class)
public class ExceptionHandlerTest {

    @Inject
    private MockMvc mvc;

    @MockBean
    private GamificationService gamificationService;

    @Value("${gamification.slackbot.rest.api.version}")
    private String gamificationSlackbotRestApiVersion;
    @Value("${gamification.slackbot.commandsUrl}")
    private String gamificationSlackbotCommandsUrl;
    @Value("${gamification.slackbot.endpoint.daily}")
    private String gamificationSlackbotDailyUrl;
    @Value("${gamification.slackbot.endpoint.thanks}")
    private String gamificationSlackbotThanksUrl;
    @Value("${gamification.slackbot.endpoint.codenjoy}")
    private String gamificationSlackbotCodenjoyUrl;
    @Value("${gamification.slackbot.endpoint.interview}")
    private String gamificationSlackbotInterviewUrl;

    private String gamificationSlackbotFullDailyUrl;
    private String gamificationSlackbotFullCodenjoyUrl;

    @Before
    public void setup() {

        gamificationSlackbotFullDailyUrl = "/" + gamificationSlackbotRestApiVersion + gamificationSlackbotCommandsUrl +
                gamificationSlackbotDailyUrl;
        gamificationSlackbotFullCodenjoyUrl = "/" + gamificationSlackbotRestApiVersion +
                gamificationSlackbotCommandsUrl + gamificationSlackbotCodenjoyUrl;
    }

    @Test
    public void shouldHandleGamificationAPIError() throws Exception {

        final String DAILY_COMMAND_TEXT = "daily description text";

        ApiError apiError = new ApiError(
                400, "GMF-F5-D2",
                "You cannot give more than one thanks for day to one person",
                "The reason of the exception is 'Thanks' achievement",
                "Something went wrong",
                Collections.emptyList()
        );

        when(gamificationService.sendDailyAchievement(any(String.class), any(String.class)))
                .thenThrow(new GamificationExchangeException(apiError, new RuntimeException("exception")));

        mvc.perform(MockMvcRequestBuilders.post(SlackUrlUtils.getUrlTemplate(gamificationSlackbotFullDailyUrl),
                SlackUrlUtils.getUriVars("slashCommandToken", "/daily", DAILY_COMMAND_TEXT))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("You cannot give more than one thanks for day to one person"));
    }

    @Test
    public void shouldHandleUserAPIError() throws Exception {

        final String DAILY_COMMAND_TEXT = "daily description text";

        ApiError apiError = new ApiError(
                400, "USF-F1-D1",
                "User not found",
                "User not found",
                "Something went wrong",
                Collections.emptyList()
        );

        when(gamificationService.sendDailyAchievement(any(), any())).
                thenThrow(new UserExchangeException(apiError, new RuntimeException("exception")));

        mvc.perform(MockMvcRequestBuilders.post(SlackUrlUtils.getUrlTemplate(gamificationSlackbotFullDailyUrl),
                SlackUrlUtils.getUriVars("slashCommandToken", "/daily", DAILY_COMMAND_TEXT))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("User not found"));
    }

    @Test
    public void shouldHandleWrongCommandException() throws Exception {

        final String COMMAND_TEXT = "@slack1 -2th @slack2 -3th @slack3";

        when(gamificationService.sendCodenjoyAchievement(any(String.class), any(String.class))).
                thenThrow(new WrongCommandFormatException("Wrong command exception"));

        mvc.perform(MockMvcRequestBuilders.post(SlackUrlUtils.getUrlTemplate(gamificationSlackbotFullCodenjoyUrl),
                SlackUrlUtils.getUriVars("slashCommandToken", "/codenjoy", COMMAND_TEXT))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Wrong command exception"));
    }

    @Test
    public void shouldHandleAllOtherException() throws Exception {

        final String COMMAND_TEXT = "daily report";

        when(gamificationService.sendDailyAchievement(any(String.class), any(String.class))).
                thenThrow(new RuntimeException("Runtime exception"));

        mvc.perform(MockMvcRequestBuilders.post(SlackUrlUtils.getUrlTemplate(gamificationSlackbotFullDailyUrl),
                SlackUrlUtils.getUriVars("slashCommandToken", "/daily", COMMAND_TEXT))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Runtime exception"));
    }
}