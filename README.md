[![Newest Release](https://img.shields.io/github/v/release/jordybronowicki37/befit-bot?style=for-the-badge&logo=github&logoColor=fff&labelColor=555&color=94398d)](https://github.com/jordybronowicki37/befit-bot/releases)
[![Newest Version](https://img.shields.io/github/v/tag/jordybronowicki37/befit-bot?style=for-the-badge&logo=github&logoColor=fff&labelColor=555&color=94398d)](https://github.com/jordybronowicki37/befit-bot/tags)
[![GitHub Profile](https://img.shields.io/static/v1.svg?color=94398d&labelColor=555555&logoColor=ffffff&style=for-the-badge&label=jordybronowicki37&message=GitHub&logo=github)](https://github.com/jordybronowicki37)

```
██████╗ ███████╗███████╗██╗████████╗
██╔══██╗██╔════╝██╔════╝██║╚══██╔══╝
██████╔╝█████╗  █████╗  ██║   ██║   
██╔══██╗██╔══╝  ██╔══╝  ██║   ██║   
██████╔╝███████╗██║     ██║   ██║   
╚═════╝ ╚══════╝╚═╝     ╚═╝   ╚═╝   
```

# BeFit gains tracker
This cool bot can help you with improving your fitness. It can track your progress, motivate you, manage your goals and compare your progress to others.

## Discord
### Global Commands
<details>
  <summary>Achievements</summary>
  
  >Format: `/achievements` \
  >Example: \
  >![All exercises command response example](docs/examples/achievementsCommandExample.png)
  
</details>

<details>
  <summary>View all exercises</summary>
  
  >Format: `/exercises view all` \
  >Example: \
  >![All exercises command response example](docs/examples/allExercisesCommandExample.png)
  
</details>

<details>
  <summary>View the exercises you are participating in</summary>
  
  >Format: `/exercises view my` \
  >Example: \
  >![My exercises command response example](docs/examples/myExercisesCommandExample.png)
  
</details>

<details>
  <summary>View extended data of a single exercise</summary>
  
  >Format: `/exercises view one {exercise-name}` \
  >Example: \
  >![One exercise command response example](docs/examples/oneExercisesCommandExample.png)

</details>

<details>
  <summary>Create a new exercise type</summary>
  
  >Format: `/exercises create {name} {measurement-type} {goal-direction}` \
  >Example: \
  >![Create exercise command response example](docs/examples/createExerciseCommandExample.png)

</details>

<details>
  <summary>Create a new goal for yourself</summary>
  
  >Format: `/goals add {exercise-name} {amount}` \
  >Example: \
  >![Create goal command response example](docs/examples/addGoalCommandExample.png)

</details>

<details>
  <summary>Remove an existing goal</summary>
  
  >Format: `/goals remove {goal}`

</details>

<details>
  <summary>View your goals</summary>
  
  >Format: `/goals view` \
  >Example: \
  >![View goals command response example](docs/examples/viewGoalsCommandExample.png)

</details>

<details>
  <summary>Help</summary>
  
  >Format: `/help` \
  >Example: \
  >![Help command response example](docs/examples/helpCommandExample.png)

</details>

<details>
  <summary>Leaderboard</summary>
  
  >Format: `/leaderboard` \
  >Example: \
  >![Leaderboard command response example](docs/examples/leaderboardCommandExample.png)

</details>

<details>
  <summary>Log history</summary>
  
  >Format: `/history` \
  >Example: \
  >![History command response example](docs/examples/historyCommandExample.png)

</details>

<details>
  <summary>Log an exercise</summary>
  
  >Format: `/log {exercise-name} {amount}` \
  >Example: \
  >![Log command response example](docs/examples/logCommandExample.png)

</details>

<details>
  <summary>Retrieve a random motivational quote</summary>
  
  >Format: `/motivation` \
  >Example: \
  >![Motivation command response example](docs/examples/motivationCommandExample.png)

</details>

<details>
  <summary>View your progress on an exercise</summary>
  
  >Format: `/progress {exercise-name} ?{view-mode}` \
  >Example: \
  >![Progress command response example](docs/examples/progressCommandExample.png)

</details>

<details>
  <summary>User stats</summary>

  >Format: `/stats` \
  >Example: \
  >![Stats command response example](docs/examples/statsCommandExample.png)

</details>

### Management Commands
<details>
  <summary>Restart server</summary>

  >Format: `/management restart`

</details>

<details>
  <summary>Refresh guild commands</summary>

  >Format: `/management refresh`

</details>

<details>
  <summary>Add scheduled job</summary>

  >Format: `/management jobs add {channel-id} {job-type} {cron-expression} ?{timezone-id}`

</details>

<details>
  <summary>Remove scheduled job</summary>

  >Format: `/management jobs remove {scheduled-job}`

</details>

## Achievements
There are 20 achievements for you to complete.

<details>
  <summary>Achievements</summary>

  > ![Achievements](docs/achievements.png)
  > 
  > | Icon                                                                          | Title                   | Description                                                                               | Difficulty |
  > |-------------------------------------------------------------------------------|-------------------------|-------------------------------------------------------------------------------------------|------------|
  > | ![Achievement icon](backend/src/main/resources/achievement-icons/icon-03.png) | Heart monitor           | Log an exercise which uses bpm as a measurement.                                          | EASY       |
  > | ![Achievement icon](backend/src/main/resources/achievement-icons/icon-17.png) | Lets get healthy        | Create your first log.                                                                    | EASY       |
  > | ![Achievement icon](backend/src/main/resources/achievement-icons/icon-12.png) | Reach your potential    | Complete a goal.                                                                          | EASY       |
  > | ![Achievement icon](backend/src/main/resources/achievement-icons/icon-19.png) | Cardio enthusiast       | Do any exercise for 30 minutes.                                                           | MEDIUM     |
  > | ![Achievement icon](backend/src/main/resources/achievement-icons/icon-08.png) | Done for today          | Create 10 logs on a single day.                                                           | MEDIUM     |
  > | ![Achievement icon](backend/src/main/resources/achievement-icons/icon-07.png) | Full workout            | Within 24h log an exercise for the following categories: weight, time and distance based. | MEDIUM     |
  > | ![Achievement icon](backend/src/main/resources/achievement-icons/icon-09.png) | Keep on stacking        | Have 5 concurrent logs of a single exercise that keep increasing.                         | MEDIUM     |
  > | ![Achievement icon](backend/src/main/resources/achievement-icons/icon-20.png) | Love to lift            | Lift something weighing more than 50kg for 3 days in a row.                               | MEDIUM     |
  > | ![Achievement icon](backend/src/main/resources/achievement-icons/icon-04.png) | On a roll               | Log an exercise 4 days in a row.                                                          | MEDIUM     |
  > | ![Achievement icon](backend/src/main/resources/achievement-icons/icon-16.png) | The right mindset       | Set 5 goals and complete these within a month.                                            | MEDIUM     |
  > | ![Achievement icon](backend/src/main/resources/achievement-icons/icon-05.png) | Think about your health | Log an exercise that burns 200 calories.                                                  | MEDIUM     |
  > | ![Achievement icon](backend/src/main/resources/achievement-icons/icon-18.png) | Feels like home         | Log an exercise 10 days in a row.                                                         | HARD       |
  > | ![Achievement icon](backend/src/main/resources/achievement-icons/icon-13.png) | Lets go places          | Reach a distance on any exercise of 20km.                                                 | HARD       |
  > | ![Achievement icon](backend/src/main/resources/achievement-icons/icon-14.png) | Show off                | Reach the first place on an exercise leaderboard that has at least 6 participants.        | HARD       |
  > | ![Achievement icon](backend/src/main/resources/achievement-icons/icon-15.png) | The goat                | Create a total of 100 logs.                                                               | HARD       |
  > | ![Achievement icon](backend/src/main/resources/achievement-icons/icon-06.png) | The hulk                | Lift something weighing more than 100kg.                                                  | HARD       |
  > | ![Achievement icon](backend/src/main/resources/achievement-icons/icon-02.png) | Like a marathon         | Reach a distance on any exercise of 42km.                                                 | IMPOSSIBLE |
  > | ![Achievement icon](backend/src/main/resources/achievement-icons/icon-10.png) | Serious dedication      | Create logs each day for an entire month.                                                 | IMPOSSIBLE |
  > | ![Achievement icon](backend/src/main/resources/achievement-icons/icon-01.png) | Bodybuilder             | WIP                                                                                       | UNKNOWN    |
  > | ![Achievement icon](backend/src/main/resources/achievement-icons/icon-11.png) | On the bench            | WIP                                                                                       | UNKNOWN    |

</details>

## Deployment
### Docker
Follow the instructions as mentioned in the [how-to](docs/how-to-run-on-docker.md)

### RaspberryPi
Follow the instructions as mentioned in the [how-to](docs/how-to-run-on-raspberrypi-using-docker.md)
