package org.example.LLDQuestion.ElevatorSystem;

/*
--------------------------------------------------------------------------------
LLD: ElevatorSystem
--------------------------------------------------------------------------------

Functional Requirement
-> user should be able to send the request to system (req can be internal (inside the elevator) or external (outside the elevator)
processed by same req)
-> system should support multiple no of  elevators
-> system will be able to pick best elevator for that req


Non Functional Requirement
-> no duplicate request
-> it should be extensible (in future we can add multiple selection strategy for elevator)
-> system should uses proper safety measures (while moving lift we cannot open door from internal buttons)
-> Fault isolation : elevator should not interfere with other elevator


Core Entity
->Elevator
->ElevatorState
->ElevatorDirection
->ElevatorSystem
->ElevatorSelectionStrategy : nearestElevatorSelectionStrategy
->DoorState

 */

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

enum Direction {
    UP, DOWN, IDLE
}

enum ElevatorState {
    MOVING, STOPPED, MAINTENANCE, IDLE
}

enum DoorState {
    OPEN, CLOSED
}

class Request {
    int floor;
    Direction direction;

    public Request(int floor, Direction direction) {
        this.floor = floor;
        this.direction = direction;
    }
}


interface ElevatorSelectionStrategy {
    Elevator getElevator(Request request, List<Elevator> elevators);
}


class nearestElevatorSelectionStrategy implements ElevatorSelectionStrategy {

    @Override
    public Elevator getElevator(Request request, List<Elevator> elevators) {
        Elevator best = null;
        int bestScore = Integer.MAX_VALUE;
        for (Elevator elevator : elevators) {
            if (elevator.elevatorState == ElevatorState.MAINTENANCE) continue;
            int currentFloor = elevator.currentFloor;
            int distance = Math.abs(currentFloor - request.floor);
            int score;

            if (elevator.elevatorState == ElevatorState.IDLE || isMovingTowardRequest(elevator, request))
                score = distance;

            else
                score = distance * 2;

            if (bestScore > score) {
                bestScore = score;
                best = elevator;
            }

        }
        return best;
    }

    boolean isMovingTowardRequest(Elevator elevator, Request request) {
        if (elevator.direction == Direction.UP) {
            return request.direction == Direction.UP && elevator.currentFloor < request.floor;
        }
        if (elevator.direction == Direction.DOWN) {
            return request.direction == Direction.DOWN
                    && elevator.currentFloor >= request.floor;
        }
        return false;
    }
}

class Elevator implements Runnable {
    int id;
    Direction direction;
    ElevatorState elevatorState;
    DoorState doorState;
    int currentFloor;
    TreeSet<Integer> upStops;
    TreeSet<Integer> downStops;
    boolean running = false;
    Lock lock;

    public Elevator(int id, int startingFloor) {
        this.id = id;
        this.currentFloor = startingFloor;
        upStops = new TreeSet<>();
        downStops = new TreeSet<>(Comparator.reverseOrder());
        direction = Direction.IDLE;
        elevatorState = ElevatorState.IDLE;
        doorState = DoorState.CLOSED;
        lock = new ReentrantLock();
    }


    @Override
    public void run() {
        while (running) {
            step();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    void step() {
        lock.lock();
        try {
            if (direction == Direction.UP) {
                if (upStops.isEmpty()) {
                    direction = downStops.isEmpty() ? Direction.IDLE : Direction.DOWN;
                    elevatorState = downStops.isEmpty() ? ElevatorState.IDLE : ElevatorState.MOVING;
                    return;
                }
                if (currentFloor == upStops.first()) {
                    upStops.pollFirst();
                    openAndCloseDoors();
                }
                currentFloor++;
            } else if (direction == Direction.DOWN) {
                if (downStops.isEmpty()) {
                    direction = upStops.isEmpty() ? Direction.IDLE : Direction.UP;
                    elevatorState = upStops.isEmpty() ? ElevatorState.IDLE : ElevatorState.MOVING;
                    return;
                }
                if (currentFloor == downStops.first()) {
                    downStops.pollFirst();
                    openAndCloseDoors();
                }
                currentFloor--;
            } else {
                elevatorState = ElevatorState.IDLE;
            }
        } finally {
            lock.unlock();
        }
    }

    private void openAndCloseDoors() {
        elevatorState = ElevatorState.STOPPED;
        doorState = DoorState.OPEN;
        System.out.println("Elevator " + id + " arrived at floor " + currentFloor + ", doors OPEN");
        doorState = DoorState.CLOSED;
        System.out.println("Elevator " + id + " doors CLOSED at floor " + currentFloor);
    }

    void addRequest(Request request) {
        lock.lock();
        try {
            if (currentFloor < request.floor) {
                upStops.add(request.floor);
                if (direction == Direction.IDLE) {
                    direction = Direction.UP;
                }
            } else if (request.floor < currentFloor) {
                downStops.add(request.floor);
                if (direction == Direction.IDLE) {
                    direction = Direction.DOWN;
                }
            } else {
                openAndCloseDoors();
            }
        } finally {
            lock.unlock();
        }
    }

    void start() {
        running = true;
    }

}

class ELevetorSystem {
    List<Elevator> elevators;
    ElevatorSelectionStrategy elevatorSelectionStrategy;
    List<Thread> threads;

    public ELevetorSystem(ElevatorSelectionStrategy strategy, int noOfElevator) {
        this.elevatorSelectionStrategy = strategy;
        elevators = new ArrayList<>();
        threads = new ArrayList<>();
        for (int i = 0; i < noOfElevator; i++) {
            Elevator elevator = new Elevator(i, 0);
            elevator.start();
            Thread thread = new Thread(elevator);
            elevators.add(elevator);
            threads.add(thread);
            thread.start();
        }
    }


    public void addRequest(Request request) {
        Elevator elevator = elevatorSelectionStrategy.getElevator(request, elevators);
        elevator.addRequest(request);
        System.out.println("Assigned request for floor " + request.floor + " to elevator " + elevator.id);
    }
}


public class ElevatorSystemDemo {
    public static void main(String[] args) {

        ElevatorSelectionStrategy elevatorSelectionStrategy = new nearestElevatorSelectionStrategy();
        ELevetorSystem eLevetorSystem = new ELevetorSystem(elevatorSelectionStrategy, 3);
        Request req1 = new Request(3, Direction.UP);
        Request req2 = new Request(1, Direction.DOWN);
        Request req3 = new Request(6, Direction.DOWN);

        eLevetorSystem.addRequest(req1);
        eLevetorSystem.addRequest(req2);
        eLevetorSystem.addRequest(req3);
    }
}
