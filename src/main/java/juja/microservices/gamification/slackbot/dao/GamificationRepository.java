package juja.microservices.gamification.slackbot.dao;

import juja.microservices.gamification.slackbot.model.DailyAchievement;

/**
 * @author Danil Kuznetsov
 */

public interface GamificationRepository {
    String saveDailyAchievement(DailyAchievement daily);
}
