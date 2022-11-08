
# am

    force-stop [--user <USER_ID> | all | current] <PACKAGE>
      Completely stop the given application package.

    stop-app [--user <USER_ID> | all | current] <PACKAGE>
      Stop an app and all of its services.  Unlike `force-stop` this does
      not cancel the app's scheduled alarms and jobs.
