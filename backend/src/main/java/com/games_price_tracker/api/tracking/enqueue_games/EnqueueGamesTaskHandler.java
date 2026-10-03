package com.games_price_tracker.api.tracking.enqueue_games;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.games_price_tracker.api.game.GameService;
import com.games_price_tracker.api.steam.config.SteamApiProperties;
import com.games_price_tracker.api.tracking.enqueue_games.enums.CancelEnqueueResult;
import com.games_price_tracker.api.tracking.enqueue_games.enums.StartEnqueueResult;
import com.games_price_tracker.api.tracking.fetch_appdetails.FetchAppDetailsTasksHandler;

@Component
public class EnqueueGamesTaskHandler {
    private final TaskScheduler taskScheduler;
    private final EnqueueGamesNeedingPriceUpdate task;
    private final Duration delayBetweenRequests;
    private final Duration minIntervalGamePriceUpdate;
    private final int maxPagesPerEnqueue; 
    private final Logger log = LoggerFactory.getLogger(EnqueueGamesTaskHandler.class);
    private ScheduledFuture<?> currentTaskScheduled;

    public EnqueueGamesTaskHandler(GameService gameService, FetchAppDetailsTasksHandler fetchAppDetailsTasksHandler, TaskScheduler taskScheduler, SteamApiProperties steamApiProperties, @Value("${price.min-interval-update}") Duration minIntervalGamePriceUpdate){
        this.taskScheduler = taskScheduler;
        this.task = new EnqueueGamesNeedingPriceUpdate(gameService, fetchAppDetailsTasksHandler, this, steamApiProperties);
        this.delayBetweenRequests = steamApiProperties.getAppdetails().getDelayBetweenRequests();
        this.minIntervalGamePriceUpdate = minIntervalGamePriceUpdate;
        this.maxPagesPerEnqueue = steamApiProperties.getAppdetails().getMaxPagesPerEnqueue();
    }

    public StartEnqueueResult start(int gamesPerRequest){
        if(currentTaskScheduled != null){
            log.error("Can't start enqueue because there is already one scheduled");
            return StartEnqueueResult.ENQUEUE_ALREADY_SCHEDULED;
        }

        task.setGamesPerRequest(gamesPerRequest);
        currentTaskScheduled = taskScheduler.schedule(task, Instant.now());
        return StartEnqueueResult.STARTED;
    }

    public CancelEnqueueResult cancel(){
        if(currentTaskScheduled == null){
            log.error("Cancel enqueue failed because no enqueue is scheduled");
            return CancelEnqueueResult.NO_ENQUEUE_SCHEDULED;
        }

        boolean canceled = currentTaskScheduled.cancel(false);
        if(!canceled){
            log.error("Current enqueue couldn't be canceled");
            return CancelEnqueueResult.CANCEL_FAILED;
        }

        currentTaskScheduled = null;
        log.info("Enqueue canceled");
        return CancelEnqueueResult.CANCELED;
    }

    public void nextExecution(boolean allGamesChecked){
        Instant schedulingTime;

        if(allGamesChecked){
            task.resetActualPage();

            ZonedDateTime dateEnqueue = ZonedDateTime
                .now(ZoneId.of("America/Argentina/Buenos_Aires"))
                .plus(minIntervalGamePriceUpdate.plusDays(1))
                .withHour(6)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

            schedulingTime = dateEnqueue.toInstant();

            log.info("Enqueue completed. Next: {}", dateEnqueue.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        }else{
            long delayNextExecution = delayBetweenRequests.getSeconds()*maxPagesPerEnqueue+10;
            schedulingTime = Instant.now().plus(Duration.ofSeconds(delayNextExecution));
        }

        if(currentTaskScheduled == null) return;

        log.info("Scheduling next enqueue task");
        currentTaskScheduled = taskScheduler.schedule(task, schedulingTime);
    }
}
    
