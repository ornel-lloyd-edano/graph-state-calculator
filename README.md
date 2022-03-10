# How To Run
1. sbt docker:publishLocal
2. docker run -p 8080:8080 ornel-state-calculator:1.0
3. In any browser, go to http://localhost:8080/api/topology to check the graph. Initially it will be empty
4. Check ApplicationTest.scala on how to use the api

# Backend Software Engineer

# Assessment: State Calculation

## Background

---

In this assessment you will code a small and simplified piece of StackState's logic pertaining to the calculation of states over Components and their dependencies.

### Components and Depenencies

StackState visualizes a dependency graph of interconnected components. Each component can be connected to multiple other components. Each relation represents a dependency. The target of each relation is a component that the other component depends upon. A dependency can be uni-directional or bi-directional (meaning both components depend on each other).

### States

A component has three types of states (properties of the component). Each of the states has one of the following values, ordered from low to high:

- `no_data` - no state is known
- `clear` - the component is running fine
- `warning` - something is wrong
- `alert` - something went wrong

The three types of state are:

1. The *check* states. Each components has zero, one or multiple of these. (In StackState they represent states that are calculated by running checks on monitoring data, which is outside the scope of this assessment so you can just change their value yourself.) These check states can be expected to change all the time. The check states influence the own state of the component.
2. The *own* state, singular, is the highest/most severe state of the component's check states. It is not affected by anything other then the check states. If the component has no check states the own state is `no_data` . (In StackState the own state represents the state that the component itself reports.) The own state of a component influences the derived state of the component.
3. The *derived* state, singular, is the higher of A) The *own* *state (warning and higher)* or B) the *highest of the derived states of the components it depends on.*

<aside>
ℹ️ `Clear` **state does not propagate, so if the own state of a component is `clear` or `no_data` and its dependent derived states are `clear` or `no_data`, then the derived state is set to `no_data`.

</aside>

### Events

It is important for our users to have traceability of events through the system. For example when an external service reports a higher than normal error rate, StackState might generate an alert check state, which becomes the new own state, which triggers a whole host of derived states to change.

# Simple example

---

Take a dependency graph with two nodes. A component named "db" and a component named "app". The app is dependent on the db. The db has two check states. One of the check states named "CPU load" of the db goes to the warning state. The own state of the db becomes warning. The derived state of the db and app become warning.

## JSON

The dependency graph and events can be expressed in JSON. Here is the complete scenario above, starting from an initial state, set of events and a final state.

### Initial state

```json
{
  "graph": {
    "components": [
      {
        "id": "app",
        "own_state": "no_data",
        "derived_state": "no_data",
        "check_states": {
          "CPU load": "no_data",
          "RAM usage": "no_data"
        },
        "depends_on": ["db"]
      },
      {
        "id": "db",
        "own_state": "no_data",
        "derived_state": "no_data",
        "check_states": {
          "CPU load": "no_data",
          "RAM usage": "no_data"
        },
        "dependency_of": ["app"]
      }
    ]
  }
}
```

### Events

Here are two events that update initial state above:

```json
{
  "events": [
    {
      "timestamp": "1",
      "component": "db",
      "check_state": "CPU load",
      "state": "warning"
    },
    {
      "timestamp": "2",
      "component": "app",
      "check_state": "CPU load",
      "state": "clear"
    }
  ]
}
```

### Final state

The resulting state after the events have been processed can be expressed in the first format.

```json
{
  "graph": {
    "components": [
      {
        "id": "app",
        "own_state": "clear",
        "derived_state": "warning",
        "check_states": {
          "CPU load": "clear",
          "RAM usage": "no_data"
        },
        "depends_on": ["db"]
      },
      {
        "id": "db",
        "own_state": "warning",
        "derived_state": "warning",
        "check_states": {
          "CPU load": "warning",
          "RAM usage": "no_data"
        },
        "dependency_of": ["app"]
      }
    ]
  }
}
```

<aside>
⚠️ Keep in mind: The events are processed in the order of their timestamp

</aside>

# Assignment

---

Implement the above stated domain and its logic so that a particular dependency graph and associated events result in the right dependency graph state.

## Delivery

Your solution should contain:

- a Dockerfile that, when built and run, starts a server that exposes a REST API
- a README file that describes the necessary steps to build, test and run the solution.

The REST API should implement the following calls:

- `POST /api/topology` which takes a `graph` JSON document containing the graph of components. If the graph is readable, the response HTTP code should be `201 Created`.
- `GET /api/topology` Returns the current `graph` as JSON document with all the state events applied to it.
- `POST /api/events` which takes an `events` JSON document containing 1 or more events to apply to the currently loaded component graph.

<aside>
ℹ️ It is possible to call the POST topology endpoint multiple times. Doing so will either add new components and dependencies to the graph or update the existing ones by matching their IDs.

</aside>

<aside>
⚠️ An additional call to the POST events endpoint may contain older event data than the previous call.

</aside>

### Questions

If you have any questions or need clarifications, please send an email to the interviewer.
