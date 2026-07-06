
[![Build branches](https://github.com/costa-alex/tp2intervals/actions/workflows/docker.yml/badge.svg)](https://github.com/costa-alex/tp2intervals/actions/workflows/docker.yml)
[![release](https://img.shields.io/github/release/costa-alex/tp2intervals)](https://github.com/costa-alex/tp2intervals/releases/latest)
<!-- 
[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/E1E6W6XZP)
-->

# Third Party to Intervals.icu

App to sync workouts between TrainingPeaks, TrainerRoad and Intervals.icu. It's a selfhosted app that runs on Docker and includes a mobile friendly user-interface.

**Only for educational purposes**

<img src="https://github.com/costa-alex/tp2intervals/blob/main/docs/TP2I_mobile.JPG?raw=true" width="25%"> <img src="https://github.com/costa-alex/tp2intervals/blob/main/docs/TP2TR.JPG?raw=true" width="25%">

* [List of features](#list-of-features)
* [Configuration](#configuration)
    + [Intervals.icu](#intervalsicu)
    + [TrainingPeaks](#trainingpeaks)
    + [TrainerRoad](#trainerroad)
* [How to run the app](#how-to-run-the-app)
    + [Docker](#docker)
* [FAQ](#faq)
    + [General](#general)
    + [Info regarding scheduling for the next day with TrainingPeaks free account](#info-regarding-scheduling-for-the-next-day-with-trainingpeaks-free-account)
* [Troubleshooting](#troubleshooting)
    + [How to get logs](#how-to-get-logs)
    + [How to record HAR file](#how-to-record-har-file)

**Docker image location ⚠️**

**Image url: `ghcr.io/costa-alex/tp2intervals:latest`**

## List of features

**TrainingPeaks**

Athlete account
* Sync planned workouts in calendar between Intervals.icu and TrainingPeaks (for today and tomorrow with free TP account)
* Copy whole training plan from TrainingPeaks
* Create training plan or workout folder on Intervals.icu from planned workouts on TrainingPeaks

Coach account
* Copy whole training plan and workout library from TrainingPeaks

**TrainerRoad**
* Sync planned workouts in calendar from TrainerRoad to TrainingPeaks or Intervals.icu
    + Syncs planned TSS value
    + Automatically sets the Training Peaks activity type as Ride and subtype as Virtual Bike 
* Copy workouts from TrainerRoad library to Intervals
* Create training plan or workout folder on Intervals.icu from planned workouts on TrainerRoad

Automatically schedule workouts for today, by checking your calendar every 20 minutes.
To clear up scheduled jobs just restart the application.


## Configuration
Before using the application you need to configure access to platforms.
Access to Intervals.icu is required, access to other platforms is optional.

After you gathered all required configuration, you can click Confirm button.
If everything is fine, you will be redirected to the home page.

If your configuration is wrong. You will see an error that there is no access to particular platform.
Check all your values and save configuration again.

### Intervals.icu
Copy API key and Athlete Id from [Settings page](https://intervals.icu/settings) in Developer Settings section on Intervals.icu web page.

### TrainingPeaks
To use TrainingPeaks copy all cookies from request `https://tpapi.trainingpeaks.com/users/v3/token` and put it on Configuration page.
The app automatically will remove redundant parts and only require cookie will remain. Follow guide below how to do that.

The app requires `Production_tpAuth` cookie (key and value, smth like `Production_tpAuth=very_long_string`).
Another guide is [available here](https://forum.intervals.icu/t/implemented-push-workout-to-wahoo/783/87)

<img src="https://github.com/freekode/tp2intervals/blob/main/docs/tp_guide.png?raw=true">

### TrainerRoad
Configuration is very similar to TrainingPeaks. Copy all cookies from request `https://tpapi.trainingpeaks.com/users/v3/token` and put it on Configuration page.
The app automatically will remove redundant parts and only require cookie will remain. Follow guide below how to do that.

Cookie `SharedTrainerRoadAuth` (key and value, smth like `SharedTrainerRoadAuth=very_long_string`) is required for the app.

<img src="https://github.com/freekode/tp2intervals/blob/main/docs/tr_guide.png?raw=true">

Be aware, Firefox cuts long strings in Dev Tool window. Copy cookie value with right click -> Copy Value.

## How to run the app
### Docker
There is a Docker image built with every release

```yaml
version: "3.9"
services:
  tp2intervals:
    image: ghcr.io/costa-alex/tp2intervals:latest
    container_name: tp2intervals
    restart: unless-stopped
    ports:
      - "8098:8080"
    volumes:
      - ./data:/data
```

## FAQ

### General
* Ramp steps in TrainerRoad are not supported
* More info you can find on the forum https://forum.intervals.icu/t/tp2intervals-copy-trainingpeaks-and-trainerroad-workouts-plans-to-intervals/63375

### Info regarding scheduling for the next day with TrainingPeaks free account
Officially if you have a free TP account, you can't plan workouts for future dates, but practically you can.
You can plan a workout for the next day relative to TrainingPeaks server local time. The server is in UTC-6 time zone. Let's check some examples:

Example 1. Your TZ is UTC+2 and current local date time 20.05.2024 06:00. It means at this moment TP server local date time is 19.05.2024 22:00.
Therefore, you can plan workouts for 20.05.2024. But you can't plan workouts for 21.05.2024, you can do it in 2 hours, because in 2 hours server local time will be 20.05.2024 00:00.

Example 2. Your TZ is UTC+12, current local date time 20.05.2024 18:00. TP server local date time is 20.05.2024 00:00. At this moment, you can plan workouts for 21.05.2024.

Visible time difference with [worldtimebuddy](https://www.worldtimebuddy.com/?pl=1&lid=206,100,756135,2193733&h=206&hf=0)

## Troubleshooting
To identify the problems with any platform, logs from the users helps very much.

Gather logs from [guide below](#how-to-get-logs). And in case of TrainerRoad platform try to [record HAR file](#how-to-record-har-file). Send the files directly to me.

#### How to get logs
1. Go to Configuration
2. In General section check Debug Mode, click Confirm
3. Reproduce your issue
4. Find log file according to your system

* JAR: ./tp2intervals.log

#### How to record HAR file
1. Open new tab in your browser
2. Open dev tools, check Preserve log (Firefox Cog -> Persist Logs)

   <img src="https://github.com/freekode/tp2intervals/blob/main/docs/har-1.png?raw=true" width="70%">
3. Next steps are very important, as they simulate what the app does.
   Open TrainerRoad page, open workout library, find any workout, open workout page (the page where you have chart with workout steps, description, alternatives, etc.)
4. In dev tools, click Export HAR (Firefox - Cog -> Save All as HAR), save the file and send it to me

   <img src="https://github.com/freekode/tp2intervals/blob/main/docs/har-2.png?raw=true" width="70%">

