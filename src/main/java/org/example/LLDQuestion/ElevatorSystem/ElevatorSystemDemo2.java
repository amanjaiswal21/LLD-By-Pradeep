package org.example.LLDQuestion.ElevatorSystem;

/*
//Functional Requirement
1) System should support multiple elevators
2) system should multiple types of elevation selection strategy
3) system should support both internal and external req
4) system should able to display on each floor

//Non functional requirement
1) code should be extensible
2) no dublicate req

 */


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

class Elevator implements Runnable {

    int id;
    volatile int currentFloor = 0;
    boolean isRunning = false;
    Direction currentDirection = Direction.IDLE;

    TreeSet<Integer> upStops = new TreeSet<>();
    TreeSet<Integer> downStops =
            new TreeSet<>(Collections.reverseOrder());

    List<Observer> observers = new ArrayList<>();

    void addObserver(Observer observer) {
        observers.add(observer);
    }

    void startElevator() {
        this.isRunning = true;

        System.out.println(
                "[START] Elevator-" + id
        );
    }

    synchronized void stopElevator() {
        this.isRunning = false;

        System.out.println(
                "[STOP] Elevator-" + id +
                        " | Current Floor " + currentFloor
        );

        notifyAll();
    }

    synchronized void addRequest(InternalRequest request) {

        System.out.println(
                "[INTERNAL REQUEST] Elevator-" + id +
                        " | Current Floor " + currentFloor +
                        " -> Destination Floor " + request.floorNo
        );

        if (request.floorNo > currentFloor) {

            upStops.add(request.floorNo);

            if (currentDirection == Direction.IDLE) {
                currentDirection = Direction.UP;
            }

        } else if (request.floorNo < currentFloor) {

            downStops.add(request.floorNo);

            if (currentDirection == Direction.IDLE) {
                currentDirection = Direction.DOWN;
            }

        } else {

            System.out.println(
                    "[INFO] Elevator-" + id +
                            " is already at Floor " + currentFloor
            );
        }

        notifyAll();
    }

    synchronized void addRequest(ExternalRequest request) {

        if (request.direction == Direction.IDLE) {
            throw new IllegalArgumentException(
                    "External request direction cannot be IDLE"
            );
        }

        if (currentDirection == Direction.UP) {

            if (request.currentFloorNo >= currentFloor) {
                upStops.add(request.currentFloorNo);
            } else {
                downStops.add(request.currentFloorNo);
            }

        } else if (currentDirection == Direction.DOWN) {

            if (request.currentFloorNo <= currentFloor) {
                downStops.add(request.currentFloorNo);
            } else {
                upStops.add(request.currentFloorNo);
            }

        } else {

            if (request.currentFloorNo > currentFloor) {

                upStops.add(request.currentFloorNo);
                currentDirection = Direction.UP;

            } else if (request.currentFloorNo < currentFloor) {

                downStops.add(request.currentFloorNo);
                currentDirection = Direction.DOWN;

            } else {

                currentDirection = request.direction;
            }
        }

        notifyAll();
    }

    void openDoor() throws InterruptedException {

        System.out.println(
                "[DOOR OPEN] Elevator-" + id +
                        " | Floor " + currentFloor
        );

        Thread.sleep(10000);

        System.out.println(
                "[DOOR CLOSE] Elevator-" + id +
                        " | Floor " + currentFloor
        );
    }

    void step() throws InterruptedException {

        Thread.sleep(2000);

        synchronized (this) {

            if (currentDirection == Direction.UP) {
                currentFloor++;
            }

            if (currentDirection == Direction.DOWN) {
                currentFloor--;
            }
        }

        notifyObserver();
    }

    void notifyObserver() {

        for (Observer observer : observers) {

            observer.notifyFloor(
                    id,
                    currentFloor,
                    currentDirection
            );
        }
    }

    @Override
    public void run() {

        while (isRunning) {

            synchronized (this) {

                while (
                        isRunning &&
                                upStops.isEmpty() &&
                                downStops.isEmpty()
                ) {

                    currentDirection = Direction.IDLE;

                    try {
                        wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }

                if (!isRunning) {
                    return;
                }
            }

            synchronized (this) {

                if (currentDirection == Direction.IDLE) {

                    if (!upStops.isEmpty()) {
                        currentDirection = Direction.UP;

                    } else if (!downStops.isEmpty()) {
                        currentDirection = Direction.DOWN;
                    }
                }
            }

            try {
                step();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            boolean shouldOpenDoor = false;

            synchronized (this) {

                if (
                        currentDirection == Direction.UP &&
                                !upStops.isEmpty()
                ) {

                    if (currentFloor == upStops.first()) {

                        upStops.pollFirst();
                        shouldOpenDoor = true;

                        System.out.println(
                                "[STOP REACHED] Elevator-" + id +
                                        " | Floor " + currentFloor
                        );
                    }

                } else if (
                        currentDirection == Direction.DOWN &&
                                !downStops.isEmpty()
                ) {

                    if (currentFloor == downStops.first()) {

                        downStops.pollFirst();
                        shouldOpenDoor = true;

                        System.out.println(
                                "[STOP REACHED] Elevator-" + id +
                                        " | Floor " + currentFloor
                        );
                    }
                }

                if (
                        currentDirection == Direction.UP &&
                                upStops.isEmpty()
                ) {

                    if (!downStops.isEmpty()) {

                        currentDirection = Direction.DOWN;

                        System.out.println(
                                "[DIRECTION CHANGE] Elevator-" + id +
                                        " | UP -> DOWN"
                        );

                    } else {

                        currentDirection = Direction.IDLE;
                    }

                } else if (
                        currentDirection == Direction.DOWN &&
                                downStops.isEmpty()
                ) {

                    if (!upStops.isEmpty()) {

                        currentDirection = Direction.UP;

                        System.out.println(
                                "[DIRECTION CHANGE] Elevator-" + id +
                                        " | DOWN -> UP"
                        );

                    } else {

                        currentDirection = Direction.IDLE;
                    }
                }
            }

            if (shouldOpenDoor) {

                try {
                    openDoor();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
}

class ElevatorSystem {

    List<Elevator> elevators;
    ElevatorSelectionStrategy strategy;

    public ElevatorSystem(
            ElevatorSelectionStrategy strategy,
            int n
    ) {

        this.elevators = new ArrayList<>();
        this.strategy = strategy;

        for (int i = 1; i <= n; i++) {

            Elevator elevator = new Elevator();

            elevator.id = i;

            Thread thread =
                    new Thread(
                            elevator,
                            "Elevator-" + i
                    );

            elevators.add(elevator);

            elevator.startElevator();

            thread.start();
        }
    }

    public Elevator getElevator(
            ExternalRequest request
    ) {

        Elevator elevator =
                strategy.getElevator(
                        elevators,
                        request
                );

        elevator.addRequest(request);

        System.out.println(
                "[EXTERNAL REQUEST] Floor " +
                        request.currentFloorNo +
                        " | Direction " +
                        request.direction +
                        " -> Assigned Elevator-" +
                        elevator.id
        );

        return elevator;
    }
}

class InternalRequest {
    int floorNo;
}

class ExternalRequest {
    Direction direction;
    int currentFloorNo;
}

interface ElevatorSelectionStrategy {

    Elevator getElevator(
            List<Elevator> elevators,
            ExternalRequest request
    );
}

class nearestElevatorSelectonStrategy
        implements ElevatorSelectionStrategy {

    @Override
    public Elevator getElevator(
            List<Elevator> elevators,
            ExternalRequest request
    ) {

        Elevator best = null;

        int dis = 100000;

        for (Elevator elevator : elevators) {

            int diff =
                    Math.abs(
                            elevator.currentFloor -
                                    request.currentFloorNo
                    );

            if (dis > diff) {

                best = elevator;
                dis = diff;
            }
        }

        return best;
    }
}

enum Direction {
    UP,
    DOWN,
    IDLE
}

interface Observer {

    void notifyFloor(
            int elevatorId,
            int currentFloor,
            Direction direction
    );
}

class Display implements Observer {

    @Override
    public void notifyFloor(
            int elevatorId,
            int currentFloor,
            Direction direction
    ) {

        System.out.println(
                "[DISPLAY] Elevator-" +
                        elevatorId +
                        " | Floor " +
                        currentFloor +
                        " | Direction " +
                        direction
        );
    }
}

public class ElevatorSystemDemo2 {

    public static void main(String[] args)
            throws InterruptedException {

        ElevatorSelectionStrategy strategy =
                new nearestElevatorSelectonStrategy();

        ElevatorSystem elevatorSystem =
                new ElevatorSystem(
                        strategy,
                        3
                );

        for (
                Elevator elevator :
                elevatorSystem.elevators
        ) {

            elevator.addObserver(
                    new Display()
            );
        }

        ExternalRequest request1 =
                new ExternalRequest();

        request1.currentFloorNo = 5;
        request1.direction = Direction.UP;

        Elevator elevator1 =
                elevatorSystem.getElevator(
                        request1
                );

        Thread.sleep(12000);

        InternalRequest internalRequest1 =
                new InternalRequest();

        internalRequest1.floorNo = 8;

        elevator1.addRequest(
                internalRequest1
        );

        Thread.sleep(3000);

        ExternalRequest request2 =
                new ExternalRequest();

        request2.currentFloorNo = 0;
        request2.direction = Direction.UP;

        Elevator elevator2 =
                elevatorSystem.getElevator(
                        request2
                );

        Thread.sleep(3000);

        ExternalRequest request3 =
                new ExternalRequest();

        request3.currentFloorNo = 1;
        request3.direction = Direction.DOWN;

        Elevator elevator3 =
                elevatorSystem.getElevator(
                        request3
                );

        Thread.sleep(50000);

        for (
                Elevator elevator :
                elevatorSystem.elevators
        ) {

            elevator.stopElevator();
        }
    }
}