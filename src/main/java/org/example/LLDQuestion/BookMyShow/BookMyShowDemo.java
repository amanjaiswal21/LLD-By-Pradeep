package org.example.LLDQuestion.BookMyShow;

/*
-------------------------------------------------------------------------------------------
-------------------------------------------------------------------------------------------
Book My Show
-------------------------------------------------------------------------------------------

Function Requirement
1) user should be able to select current city
2) user should be able to search movies
3) based on search we suggest list<theatre> available in that current location
4) user will select particular theatre and particular show
5) user will select seat and make payment
6) system should book the seat for that particular user and genarate the ticket

Non-Functional Requirement
1) thread safety-> no two user book the same ticket
2) use solid principle

Entity
User
City-> ENUM
Theatre
Movie
Seat
ShowSeat
Show

 */

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

enum SeatStatus{
    BOOKED,AVAILABLE,HOLD
}


class User{
    int userId;

}
enum City{
    LUCKNOW,DELHI,BANGALORE
}

class Movie{
    String movieName;
    Duration movieTime;
}

class Show{
    int showId;
    Movie movie;
    List<ShowSeat>seats;
    LocalDateTime showTime;

}

class ShowSeat{
    int seatId;
    SeatStatus status;
    double price;
}

class Screen{
    int screenId;
    List<Show>shows;
}


class Theatre{
    int theatreId;
    City city;
    List<Screen>screens;

}

class BookingSystem{
    Map<City,Theatre>cityTheatreMap;
    List<Theatre>theatres;

}

class Ticket{

}

class TheatreController{
    List<Theatre> getTheatre(City city){
        return null;
    }

    List<Show> getAllShows(Theatre theatre){
        return null;
    }

    boolean selectSeats(List<ShowSeat>seats,int showId, int userId){
        return false;
    }


}


public class BookMyShowDemo {
    public static void main(String[] args) {

    }
}
