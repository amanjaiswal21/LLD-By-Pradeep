package org.example.designPattern.Behavioural.ChainOfResponsibility;


import java.util.logging.Handler;

class Request {
    int priority;
    public Request(int priority){
        this.priority=priority;
    }
}

abstract class IHandler {
    IHandler nextHandler;

    public IHandler setNextHandler(IHandler nextHandler) {
        this.nextHandler = nextHandler;
        return nextHandler;
    }

    public IHandler getNextHandler() {
        return nextHandler;
    }

    abstract boolean isAllowed(Request request);

    abstract void processRequest(Request request);
}

class Level1Handler extends IHandler {

    @Override
    boolean isAllowed(Request request) {
        return request.priority > 10;
    }

    @Override
    void processRequest(Request request) {
        if (isAllowed(request)) {
            System.out.println("Req handle by 1 handler");
        } else {
            getNextHandler().processRequest(request);
        }
    }
}

class Level2Handler extends IHandler {

    @Override
    boolean isAllowed(Request request) {
        return request.priority > 5 && request.priority <= 10;
    }

    @Override
    void processRequest(Request request) {
        if (isAllowed(request))
            System.out.println("Req handle by 2 handler");
        else {
            getNextHandler().processRequest(request);
        }
    }
}

class Level3Handler extends IHandler {

    @Override
    boolean isAllowed(Request request) {
        if (request.priority >= 1 && request.priority <= 5) {
            return true;
        }
        return false;
    }

    @Override
    void processRequest(Request request) {
        if (isAllowed(request))
            System.out.println("Req handle by 3 handler");
        else {
            getNextHandler().processRequest(request);
        }
    }
}

public class ChainOfResponsibilityDemo {
    public static void main(String[] args) {
        Level1Handler level1Handler=new Level1Handler();
        Level2Handler level2Handler=new Level2Handler();
        Level3Handler level3Handler=new Level3Handler();



        level1Handler.setNextHandler(level2Handler).setNextHandler(level3Handler).setNextHandler(null);


        Request req=new Request(1);

        level1Handler.processRequest(req);
    }
}

