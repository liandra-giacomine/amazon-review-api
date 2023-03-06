# Amazon Review API

The purpose of this API is to process Amazon reviews in order to provide useful statistics, such as the best rated product in a given time range.

<img width="671" alt="Screenshot 2023-03-05 at 21 05 27" src="https://user-images.githubusercontent.com/91252116/222985961-80839837-a40b-4b7d-ad0c-5f8e58ef8da1.png">

As can be seen in the image above, this API has a dependency on the persistence microservice so please ensure to have both running locally.

## Prerequisites

- SBT
- Java 11+
- Mongo 3.6+

## Build and run

This service runs on port 8081. You can compile and run it with the following command `sbt compile run`

### Testing

- To run unit tests use `sbt test`
- To run integration tests use `sbt it:test`

### Libraries

- http4s: Automatically streams requests using fs2
- fs2: Functional streaming
- Guava: Performance improvement for computationally intensive queries

### Requests

```http
POST /amazon/best-rated HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "start": "01.01.2010",
  "end": "31.12.2020",
  "limit": 2,
  "min_number_reviews": 2
}
```

Note: This request is validated in the API.

#### Response:
- 200: Successful response with a response in the following json format:
```json
[
  {
    "asin": "B000JQ0JNS",
    "average_rating": 4.5
  },
  {
    "asin": "B000NI7RW8",
    "average_rating": 3.666666666666666666666666666666667
  }
]
```
- 400: Bad request with the problem stated in the body
- 500: Internal server error

