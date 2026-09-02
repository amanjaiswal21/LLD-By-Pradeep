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

import java.awt.print.Book;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

enum SeatStatus {
    BOOKED, AVAILABLE, HOLD
}

enum PaymetStatus {
    SUCCESS, FAILED, IN_PROGRESS
}


class User {
    int userId;

}

enum City {
    LUCKNOW, DELHI, BANGALORE
}

enum SeatCategory {
    PREMIUM, VIP
}


class Movie {
    String movieName;
    Duration movieTime;
}

class Show {
    int showId;
    Movie movie;
    LocalDateTime startTime;
    Map<Integer, SeatStatus> seatStatusMap;
    Map<Integer, ReentrantLock> seatLocks;

    public boolean lockSeat(List<Integer> seats) {
        List<Integer> sorted = new ArrayList<>(seats);
        Collections.sort(sorted);
        List<ReentrantLock> acquiredLocks = new ArrayList<>();

        //accqire the lock
        try {
            for (int seatId : sorted) {
                ReentrantLock lock = seatLocks.get(seatId);
                lock.lock();
                acquiredLocks.add(lock);
            }

            // check seat status

            for (int seatId : sorted) {
                SeatStatus status = seatStatusMap.get(seatId);
                if (status != SeatStatus.AVAILABLE)
                    return false;
            }
            for (int seatId : sorted) {
                seatStatusMap.put(seatId, SeatStatus.HOLD);
            }
            return true;
        } finally {
            for (ReentrantLock lock : acquiredLocks) {
                lock.unlock();
            }
        }
    }

    public void confirmSeats(List<Integer> seatIds) {
        for (int seatId : seatIds) {
            seatStatusMap.put(seatId, SeatStatus.BOOKED);
        }
    }

    public void releaseSeats(List<Integer> seatIds) {
        for (int seatId : seatIds) {
            seatStatusMap.put(seatId, SeatStatus.AVAILABLE);
        }
    }
}


class Seat {
    int seatId;
    SeatCategory seatCategory;
}

class Screen {
    int screenId;
    List<Seat> seats;
    Map<LocalDate, List<Show>> showsByDate;
}


class Theatre {
    int theatreId;
    City city;
    List<Screen> screens;

}

class TheatreService {
    public Map<City, List<Theatre>> cityTheatreMap;

    List<Theatre> getTheatreByCity(City city) {
        return cityTheatreMap.get(city);
    }

    List<Show> getShowsByDate(Theatre theatre, LocalDate showDate) {
        List<Show> shows = new ArrayList<>();
        for (Screen screen : theatre.screens) {
            for (Show show : screen.showsByDate.get(showDate)) {
                shows.add(show);
            }
        }
        if (shows.isEmpty()) {
            throw new RuntimeException("No shows is available at selected date");
        }
        return shows;
    }

}

class TheatreController {

    public TheatreService theatreService;

    public TheatreController(TheatreService theatreService) {
        this.theatreService = theatreService;
    }

    List<Theatre> getTheatre(City city) {
        return theatreService.getTheatreByCity(city);
    }

    List<Show> getAllShowsByDate(Theatre theatre, LocalDate showDate) {
        return theatreService.getShowsByDate(theatre, showDate);

    }
}
    class Booking {
        public UUID id;
        public User user;
        public Show show;
        public List<Integer> seats;
        public Payment payment;

        public Booking(User user, Show show, List seats, Payment payment){
            this.user=user;
            this.show=show;
            this.seats=seats;
            this.payment=payment;
        }
    }

    class Payment {
        public UUID payementId;
        public PaymetStatus paymetStatus;

        public Payment(PaymetStatus status){
            payementId=new UUID(11,22);
            this.paymetStatus=status;
        }
    }

    class BookingService {
      Map<UUID,Booking>bookings;

      public Booking createBooking(User user, List<Integer>seats,Show show){
          if (!show.lockSeat(seats)) {
              throw new RuntimeException("Seat unavailable");
          }
          Payment payment= new Payment(PaymetStatus.SUCCESS);

          if(payment.paymetStatus==PaymetStatus.SUCCESS){
              show.confirmSeats(seats);
              Booking booking =  new Booking(user, show, seats, payment);
              bookings.put(booking.id, booking);
              return booking;
          }
          else{
              show.releaseSeats(seats);
              throw new RuntimeException("Payment failed");
          }

      }
    }


class BookingController {
    public BookingService bookingService;

    public BookingController(BookingService bookingService){
        this.bookingService=bookingService;
    }

    public Booking createBooking(User user, List<Integer>seats,Show show){
       return bookingService.createBooking(user,seats,show);
    }

}


public class BookMyShowDemo {
    public static void main(String[] args) {

    }
}
