# BeFit
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

  >![Achievements](docs/achievements.png)

</details>

## Deployment
### Docker
Follow the instructions as mentioned in the [how-to](docs/how-to-run-on-docker.md)

### RaspberryPi
Follow the instructions as mentioned in the [how-to](docs/how-to-run-on-raspberrypi-using-docker.md)
