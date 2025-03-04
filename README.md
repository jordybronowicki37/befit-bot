[![Newest Release](https://img.shields.io/github/v/release/jordybronowicki37/befit-bot?style=for-the-badge&logo=github&logoColor=fff&labelColor=555&color=94398d)](https://github.com/jordybronowicki37/befit-bot/releases)
[![Newest Version](https://img.shields.io/github/v/tag/jordybronowicki37/befit-bot?style=for-the-badge&logo=github&logoColor=fff&labelColor=555&color=94398d)](https://github.com/jordybronowicki37/befit-bot/tags)
[![GitHub Profile](https://img.shields.io/static/v1.svg?color=94398d&labelColor=555555&logoColor=ffffff&style=for-the-badge&label=jordybronowicki37&message=GitHub&logo=github)](https://github.com/jordybronowicki37)
[![Docker image](https://img.shields.io/static/v1.svg?color=94398d&labelColor=555555&logoColor=ffffff&style=for-the-badge&label=Docker&message=latest&logo=docker)](https://github.com/jordybronowicki37/befit-bot/pkgs/container/befit)

```
██████╗ ███████╗███████╗██╗████████╗   ██████╗  ██████╗ ████████╗
██╔══██╗██╔════╝██╔════╝██║╚══██╔══╝   ██╔══██╗██╔═══██╗╚══██╔══╝
██████╔╝█████╗  █████╗  ██║   ██║█████╗██████╔╝██║   ██║   ██║   
██╔══██╗██╔══╝  ██╔══╝  ██║   ██║╚════╝██╔══██╗██║   ██║   ██║   
██████╔╝███████╗██║     ██║   ██║      ██████╔╝╚██████╔╝   ██║   
╚═════╝ ╚══════╝╚═╝     ╚═╝   ╚═╝      ╚═════╝  ╚═════╝    ╚═╝   
```

# BeFit gains tracker
This cool bot can help you with improving your fitness. It can track your progress, motivate you, manage your goals and compare your progress to others.

## Discord
### Global Commands
<details>
  <summary>Achievements</summary>
  
  > With this command you can view all of your earned and locked achievements. It can also display the completion percentage of the entire community.
  >
  > Format: `/achievements` \
  > Example: \
  > ![All exercises command response example](https://raw.githubusercontent.com/jordybronowicki37/befit-bot/refs/heads/main/docs/examples/achievementsCommandExample.png)
  
</details>

<details>
  <summary>View all exercises</summary>
  
  > With this command you van view all available exercises. The extended data will also show how many people are 
  > participating and who is in first place.
  >
  > Format: `/exercises view all` \
  > Example: \
  > ![All exercises command response example](https://raw.githubusercontent.com/jordybronowicki37/befit-bot/refs/heads/main/docs/examples/allExercisesCommandExample.png)
  
</details>

<details>
  <summary>View the exercises you are participating in</summary>
  
  > With this command you can view all the exercises you are participating in. The extended data will show the amount 
  > of logs you've made, the goal if you have added any, your personal record and your place in the leaderboard.
  >
  > Format: `/exercises view my` \
  > Example: \
  > ![My exercises command response example](https://raw.githubusercontent.com/jordybronowicki37/befit-bot/refs/heads/main/docs/examples/myExercisesCommandExample.png)
  
</details>

<details>
  <summary>View extended data of a single exercise</summary>
  
  > With this command you can view extended data on a single exercise. It can show global information and statistics,
  > your personal statistics and also a larger leaderboard.
  >
  > Format: `/exercises view one {exercise-name}` \
  > Example: \
  > ![One exercise command response example](https://raw.githubusercontent.com/jordybronowicki37/befit-bot/refs/heads/main/docs/examples/oneExercisesCommandExample.png)

</details>

<details>
  <summary>Create a new exercise type</summary>
  
  > With this command you can create new exercises so that you can track your progress on all of your favorite exercises.
  > Go to [measurement-types](#measurement-types) and [goal-direction](#goal-directions) to see the available options.
  >
  > Format: `/exercises create {name} {measurement-type} {goal-direction}` \
  > Example: \
  > ![Create exercise command response example](https://raw.githubusercontent.com/jordybronowicki37/befit-bot/refs/heads/main/docs/examples/createExerciseCommandExample.png)

</details>

<details>
  <summary>Create a new goal for yourself</summary>
  
  > With this command you can set a goal for a specific exercise to work towards. 
  > See [goal-status](#goal-status) for all possible statuses.
  >
  > Format: `/goals add {exercise-name} {amount}` \
  > Example: \
  > ![Create goal command response example](https://raw.githubusercontent.com/jordybronowicki37/befit-bot/refs/heads/main/docs/examples/goalAddCommandExample.png)

</details>

<details>
  <summary>Cancel an existing goal</summary>
  
  > With this command you can cancel a goal. This will update the goal's status to `CANCELLED`.
  > See [goal-status](#goal-status) for all possible statuses.
  >
  > Format: `/goals cancel {goal}` \
  > Example: \
  > ![Goal cancel command response example](https://raw.githubusercontent.com/jordybronowicki37/befit-bot/refs/heads/main/docs/examples/goalCancelCommandExample.png)

</details>

<details>
  <summary>View your goals</summary>
  
  > With this command you can view all of your active goals.
  > See [goal-status](#goal-status) for all possible statuses.
  >
  > Format: `/goals view` \
  > Example: \
  > ![View goals command response example](https://raw.githubusercontent.com/jordybronowicki37/befit-bot/refs/heads/main/docs/examples/goalsViewCommandExample.png)

</details>

<details>
  <summary>Add a new habit</summary>

> Use this command to add a new habit. See [habit time-ranges](#habit-time-ranges) for all the possible options.
>
> Format: `/habits add {name} {time-range}` \
> Example: \
> ![Habit add command response example](https://raw.githubusercontent.com/jordybronowicki37/befit-bot/refs/heads/main/docs/examples/habitsAddCommandExample.png)

</details>

<details>
  <summary>Check off a habit</summary>

> This action will ask you what habit you have completed in the past time-range. The daily habits will be asked every day, 
> the weekly habits will be asked each sunday and the monthly habits will be asked on the last day of the month.
>
> Note: this is not really a command, instead it is automatically being sent to you via a private channel. \
> Example: \
> ![Habit check command response example](https://raw.githubusercontent.com/jordybronowicki37/befit-bot/refs/heads/main/docs/examples/habitsCheckCommandExample.png)

</details>

<details>
  <summary>View all your habits</summary>

> Use this command to view all your habits, optionally you can filter on a specific time-range. 
> See [habit time-ranges](#habit-time-ranges) for all the possible options.
>
> Format: `/habits view all ?{time-range}` \
> Example: \
> ![Habit view all command response example](https://raw.githubusercontent.com/jordybronowicki37/befit-bot/refs/heads/main/docs/examples/habitsViewAllCommandExample.png)

</details>

<details>
  <summary>View one of your habits</summary>

> Use this command to get a detailed view of one of your habits.
>
> Format: `/habits view one {habit}` \
> Example: \
> ![Habit view one command response example](https://raw.githubusercontent.com/jordybronowicki37/befit-bot/refs/heads/main/docs/examples/habitsViewOneCommandExample.png)

</details>

<details>
  <summary>View your progress on a habit</summary>

> Use this command to generate a chart of your logged habits.
>
> Format: `/habits progress {time-range}` \
> Example: \
> ![Habit daily progress command response example](https://raw.githubusercontent.com/jordybronowicki37/befit-bot/refs/heads/main/docs/examples/habitsProgressDailyCommandExample.png)
> ![Habit weekly progress command response example](https://raw.githubusercontent.com/jordybronowicki37/befit-bot/refs/heads/main/docs/examples/habitsProgressWeeklyCommandExample.png)
> ![Habit monthly progress command response example](https://raw.githubusercontent.com/jordybronowicki37/befit-bot/refs/heads/main/docs/examples/habitsProgressMonthlyCommandExample.png)

</details>

<details>
  <summary>Remove one of your habits</summary>

> Use this command to remove one of your habits.
>
> Format: `/habits remove {habit}`

</details>

<details>
  <summary>Help</summary>
  
  > Use this command to get an overview of the most common and useful commands. You also get some information of the bot.
  >
  > Format: `/help` \
  > Example: \
  > ![Help command response example](https://raw.githubusercontent.com/jordybronowicki37/befit-bot/refs/heads/main/docs/examples/helpCommandExample.png)

</details>

<details>
  <summary>Leaderboard</summary>
  
  > With this command you can view the global leaderboard. This leaderboard is based on the users total xp.
  >
  > Format: `/leaderboard` \
  > Example: \
  > ![Leaderboard command response example](https://raw.githubusercontent.com/jordybronowicki37/befit-bot/refs/heads/main/docs/examples/leaderboardCommandExample.png)

</details>

<details>
  <summary>Log history</summary>
  
  > With this command you can see your entire log history or filter it by an exercise.
  >
  > Format: `/history {exercise-name}` \
  > Example: \
  > ![History command response example](https://raw.githubusercontent.com/jordybronowicki37/befit-bot/refs/heads/main/docs/examples/historyCommandExample.png)

</details>

<details>
  <summary>Log an exercise</summary>
  
  > With this command you can create a log of an exercise. In the response you can see multiple personal statistics 
  > of the exercise, get congratulations on your reached result, see your reached achievements, view the received 
  > amount of experience and finally get some motivation.
  > This is perhaps the most important, used and complex command of the bot.
  >
  > Format: `/log {exercise-name} {amount}` \
  > Example: \
  > ![Log command response example](https://raw.githubusercontent.com/jordybronowicki37/befit-bot/refs/heads/main/docs/examples/logCommandExample.png)

</details>

<details>
  <summary>Retrieve a random motivational quote</summary>
  
  > With this command you can receive a random motivational quote.
  >
  > Format: `/motivation` \
  > Example: \
  > ![Motivation command response example](https://raw.githubusercontent.com/jordybronowicki37/befit-bot/refs/heads/main/docs/examples/motivationCommandExample.png)

</details>

<details>
  <summary>View your progress on an exercise</summary>
  
  > With this command you can get a progress chart of a single exercise. By using the view mode you can specify if you 
  > only want to view your own data or of all participants. To view the progress of an exercise you must have made at 
  > least two logs for that specific exercise.
  >
  > Format: `/progress {exercise-name} ?{view-mode}` \
  > Example: \
  > ![Progress command response example](https://raw.githubusercontent.com/jordybronowicki37/befit-bot/refs/heads/main/docs/examples/progressCommandExample.png)

</details>

<details>
  <summary>Create session</summary>

  > With this command you can create a session. With a session you can group subsequent logs and get an overview of your
  > entire workout. A session will get automatically finished when there is no log created for at least an hour.
  > See [session-status](#session-status) for the possible states that a session can be in.
  >
  > Format: `/sessions create {name}` \
  > Example: \
  > ![Session create command response example](https://raw.githubusercontent.com/jordybronowicki37/befit-bot/refs/heads/main/docs/examples/sessionCreateCommandExample.png)

</details>

<details>
  <summary>View sessions</summary>

  > With this command you can see all of your created sessions.
  > See [session-status](#session-status) for the possible states that a session can be in.
  >
  > Format: `/sessions view all` \
  > Example: \
  > ![Session view all command response example](https://raw.githubusercontent.com/jordybronowicki37/befit-bot/refs/heads/main/docs/examples/sessionsViewAllCommandExample.png)

</details>

<details>
  <summary>View last session</summary>

  > With this command you can view more expanded data on your last session.
  > See [session-status](#session-status) for the possible states that a session can be in.
  >
  > Format: `/sessions view last` \
  > Example: \
  > ![Session view last command response example](https://raw.githubusercontent.com/jordybronowicki37/befit-bot/refs/heads/main/docs/examples/sessionViewOneCommandExample.png)

</details>

<details>
  <summary>View one session</summary>

  > With this command you can view more expanded data on a specific session.
  > See [session-status](#session-status) for the possible states that a session can be in.
  >
  > Format: `/sessions view one {session}` \
  > Example: \
  > ![Session view one command response example](https://raw.githubusercontent.com/jordybronowicki37/befit-bot/refs/heads/main/docs/examples/sessionViewOneCommandExample.png)

</details>

<details>
  <summary>Stop session</summary>

  > With this command you can manually stop a session. This will update the session's status to `STOPPED`.
  > See [session-status](#session-status) for the possible states that a session can be in.
  > 
  > Format: `/sessions stop {session}` \
  > Example: \
  > ![Session stop command response example](https://raw.githubusercontent.com/jordybronowicki37/befit-bot/refs/heads/main/docs/examples/sessionStopCommandExample.png)

</details>

<details>
  <summary>User stats</summary>

  > With this command you can see your own stats or optionally see the stats of one of your friends.
  >
  > Format: `/stats ?{user-id}` \
  > Example: \
  > ![Stats command response example](https://raw.githubusercontent.com/jordybronowicki37/befit-bot/refs/heads/main/docs/examples/statsCommandExample.png)

</details>

### Management Commands
These commands are only available from the configured management server. It is advisable to create a separate server
as a management server so that these management commands will only be visible to the maintainer. Besides this the bot 
needs some free space for the custom emoji's that are used for the achievement icons, and to not fill your personal
server with these visuals, it is advisable to create this separate server.

<details>
  <summary>Restart server</summary>

  > With this command you can as a maintainer restart the server on demand.
  > 
  > Format: `/management restart`

</details>

<details>
  <summary>Refresh guild commands</summary>

  > With this command you can as a maintainer register the discord commands. This is useful if some commands are not
  > working properly or are missing on a server.
  > 
  > Format: `/management refresh`

</details>

<details>
  <summary>Add scheduled job</summary>

  > With this command you can as a maintainer add a scheduled job. These jobs are scheduled on a cron-expression basis.
  > Optionally you can give a `timezone-id`, defaults to UTC.
  > See [job-types](#job-types) to view all the available types of jobs.
  > 
  > Format: `/management jobs add {channel-id} {job-type} {cron-expression} ?{timezone-id}`

</details>

<details>
  <summary>Remove scheduled job</summary>

  > With this command you can as a maintainer remove a scheduled job.
  > 
  > Format: `/management jobs remove {scheduled-job}`

</details>

## Options and statuses
### Goal directions
- Increasing
- Decreasing
### Goal status
- Active
- Cancelled (manually)
- Overwritten (when a goal with the same exercise is created)
- Completed
### Habit time ranges
- Daily
- Weekly
- Monthly
### Job types
- Gym reminder (sends a reminder to go to the gym)
- Motivational message (sends a motivational message)
### Measurement types
- Kilograms
- Grams
- Kilometers
- Meters
- Centimeters
- Kilometers per hour
- Times
- Hours
- Minutes
- Seconds
- Calories
- Beats per minute
- Percentage
### Session status
- Active
- Overwritten (when a new session is started if one is still active)
- Stopped (manually)
- Finished (automatically)

## Achievements
There are 20 achievements for you to complete. Each increasing in difficulty. Are you the one to complete them all?

<details>
  <summary>Achievements</summary>

  > ![Achievements](https://raw.githubusercontent.com/jordybronowicki37/befit-bot/refs/heads/main/docs/achievements.png)
  > 
  > | Icon                                                                                                                                                                    | Title                   | Description                                                                          | Difficulty |
  > |-------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------|--------------------------------------------------------------------------------------|------------|
  > | ![Achievement icon](https://raw.githubusercontent.com/jordybronowicki37/befit-bot/refs/heads/main/backend/src/main/resources/achievement-icons/icon-achievement-03.png) | Heart monitor           | Log an exercise which uses bpm as a measurement.                                     | EASY       |
  > | ![Achievement icon](https://raw.githubusercontent.com/jordybronowicki37/befit-bot/refs/heads/main/backend/src/main/resources/achievement-icons/icon-achievement-17.png) | Let's get healthy       | Create your first log.                                                               | EASY       |
  > | ![Achievement icon](https://raw.githubusercontent.com/jordybronowicki37/befit-bot/refs/heads/main/backend/src/main/resources/achievement-icons/icon-achievement-12.png) | Reach your potential    | Complete a goal.                                                                     | EASY       |
  > | ![Achievement icon](https://raw.githubusercontent.com/jordybronowicki37/befit-bot/refs/heads/main/backend/src/main/resources/achievement-icons/icon-achievement-19.png) | Cardio enthusiast       | Do any exercise for 30 minutes.                                                      | MEDIUM     |
  > | ![Achievement icon](https://raw.githubusercontent.com/jordybronowicki37/befit-bot/refs/heads/main/backend/src/main/resources/achievement-icons/icon-achievement-08.png) | Done for today          | Create 10 logs on a single day.                                                      | MEDIUM     |
  > | ![Achievement icon](https://raw.githubusercontent.com/jordybronowicki37/befit-bot/refs/heads/main/backend/src/main/resources/achievement-icons/icon-achievement-07.png) | Full workout            | Within 24h, log an exercise for the following categories: weight, time and distance. | MEDIUM     |
  > | ![Achievement icon](https://raw.githubusercontent.com/jordybronowicki37/befit-bot/refs/heads/main/backend/src/main/resources/achievement-icons/icon-achievement-09.png) | Keep on stacking        | Have 5 concurrent logs of a single exercise that keep increasing.                    | MEDIUM     |
  > | ![Achievement icon](https://raw.githubusercontent.com/jordybronowicki37/befit-bot/refs/heads/main/backend/src/main/resources/achievement-icons/icon-achievement-20.png) | Love to lift            | Lift something weighing more than 50kg for 3 days in a row.                          | MEDIUM     |
  > | ![Achievement icon](https://raw.githubusercontent.com/jordybronowicki37/befit-bot/refs/heads/main/backend/src/main/resources/achievement-icons/icon-achievement-04.png) | On a roll               | Log an exercise 4 days in a row.                                                     | MEDIUM     |
  > | ![Achievement icon](https://raw.githubusercontent.com/jordybronowicki37/befit-bot/refs/heads/main/backend/src/main/resources/achievement-icons/icon-achievement-11.png) | On the bench            | Add 5 logs to a single session.                                                      | MEDIUM     |
  > | ![Achievement icon](https://raw.githubusercontent.com/jordybronowicki37/befit-bot/refs/heads/main/backend/src/main/resources/achievement-icons/icon-achievement-16.png) | The right mindset       | Set 5 goals and complete these within a month.                                       | MEDIUM     |
  > | ![Achievement icon](https://raw.githubusercontent.com/jordybronowicki37/befit-bot/refs/heads/main/backend/src/main/resources/achievement-icons/icon-achievement-05.png) | Think about your health | Log an exercise that burns 200 calories.                                             | MEDIUM     |
  > | ![Achievement icon](https://raw.githubusercontent.com/jordybronowicki37/befit-bot/refs/heads/main/backend/src/main/resources/achievement-icons/icon-achievement-18.png) | Feels like home         | Log an exercise 10 days in a row.                                                    | HARD       |
  > | ![Achievement icon](https://raw.githubusercontent.com/jordybronowicki37/befit-bot/refs/heads/main/backend/src/main/resources/achievement-icons/icon-achievement-13.png) | Let's go places         | Reach a distance of 20km.                                                            | HARD       |
  > | ![Achievement icon](https://raw.githubusercontent.com/jordybronowicki37/befit-bot/refs/heads/main/backend/src/main/resources/achievement-icons/icon-achievement-14.png) | Show off                | Reach the first place on an exercise leaderboard that has at least 6 participants.   | HARD       |
  > | ![Achievement icon](https://raw.githubusercontent.com/jordybronowicki37/befit-bot/refs/heads/main/backend/src/main/resources/achievement-icons/icon-achievement-15.png) | The goat                | Create a total of 100 logs.                                                          | HARD       |
  > | ![Achievement icon](https://raw.githubusercontent.com/jordybronowicki37/befit-bot/refs/heads/main/backend/src/main/resources/achievement-icons/icon-achievement-06.png) | The hulk                | Lift something weighing more than 100kg.                                             | HARD       |
  > | ![Achievement icon](https://raw.githubusercontent.com/jordybronowicki37/befit-bot/refs/heads/main/backend/src/main/resources/achievement-icons/icon-achievement-01.png) | Bodybuilder             | Add 10 logs to a single session.                                                     | IMPOSSIBLE |
  > | ![Achievement icon](https://raw.githubusercontent.com/jordybronowicki37/befit-bot/refs/heads/main/backend/src/main/resources/achievement-icons/icon-achievement-02.png) | Like a marathon         | Reach a distance of 42km.                                                            | IMPOSSIBLE |
  > | ![Achievement icon](https://raw.githubusercontent.com/jordybronowicki37/befit-bot/refs/heads/main/backend/src/main/resources/achievement-icons/icon-achievement-10.png) | Serious dedication      | Create at least one log each day for an entire month.                                | IMPOSSIBLE |

</details>

## Deployment
### Docker
Follow the instructions as mentioned in the [how-to](https://raw.githubusercontent.com/jordybronowicki37/befit-bot/refs/heads/main/docs/how-to-run-on-docker.md)

### RaspberryPi
Follow the instructions as mentioned in the [how-to](https://raw.githubusercontent.com/jordybronowicki37/befit-bot/refs/heads/main/docs/how-to-run-on-raspberrypi-using-docker.md)
