package juja.microservices.gamification.slackbot.dao;

import juja.microservices.gamification.slackbot.model.*;

/**
 * @author Danil Kuznetsov
 */

public interface GamificationRepository {

    String saveAchievement(String url, Achievement achievement);

    String saveDailyAchievement(DailyAchievement daily);

    String saveCodenjoyAchievement(CodenjoyAchievment codenjoy);

    String saveThanksAchievement(ThanksAchievement thanks);

    String saveInterviewAchievement(InterviewAchievement interview);

}
