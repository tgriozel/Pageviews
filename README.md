# Pageviews stats microservice

This is a REST microservice built with the Play framework that allows recording pageviews and calculating stats about them.

## API

To record a pageview, issue a ```POST /v1/page``` query with the following body:
```
{
  "user_id": "ae89fe12cecf590b",
  "name": "engineering",
  "timestamp": "2000-12-01T12:00:00.000Z"
}
```


To obtain stats for a given user, ```GET /v1/user/:userid``` will return a payload with the following format:
```
{
    "userId": "ae89fe12cecf590b",
    "daysCount": 7,
    "numberPagesViewed": 9,
    "numberOfDaysActive": 3,
    "secondsSpentOnSite": 360,
    "mostViewedPage": "prices"
}
```
This aggregated data is observed over `daysCount` days, which is a query parameter with default value = 7

## Testing & running

Prerequisites are [sbt](https://www.scala-sbt.org/1.0/docs/Setup.html), [docker](https://docs.docker.com/engine/installation/) and [docker-compose](https://docs.docker.com/compose/install/):
- To run the test suite, run `sbt test`
- To run the app, run `sbt dockerComposeUp`

The HTTP API server will run on `localhost:9000`. The data storage is persistent and done in InfluxDB.
Both the server and the app are containerized, and will be run with the above command.
