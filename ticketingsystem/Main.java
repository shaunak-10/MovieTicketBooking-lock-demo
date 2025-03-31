package ticketingsystem;


public class Main
{
    public static void main(String[] args) throws InterruptedException {
        CinemaHall inox = new CinemaHall();

        inox.printAvailableSeats();

            Thread t1 = new Thread(inox::bookSeatsSynchronized);

            Thread t2 = new Thread(inox::bookSeatsSynchronized);

        t1.start();

        t2.start();


        t1.join();

        t2.join();

        inox.printAvailableSeats();
    }
}
