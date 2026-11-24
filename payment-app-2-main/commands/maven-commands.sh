mvn clean install failsafe:integration-test

# this one doesn't invoke integration tests because the goal is not explicitly declared
mvn clean install