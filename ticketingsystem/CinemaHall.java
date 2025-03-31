package ticketingsystem;

import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CinemaHall
{
    private static final int NO_OF_SEATS = 10;

    private final ReentrantLock cinemalock = new ReentrantLock(true);

    private final Semaphore bookingSemaphore = new Semaphore(1);

    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();


    boolean[] isAvailable = new boolean[NO_OF_SEATS];

    {
        Arrays.fill(isAvailable, true);
    }

    public void printAvailableSeats()
    {
        readWriteLock.readLock().lock();

        try
        {
            System.out.print("Available seats: ");

            for (int i = 0; i < NO_OF_SEATS; i++)
            {
                if (isAvailable[i])
                {
                    System.out.print((i + 1) + " ");
                }
            }

            System.out.println();
        }
        finally
        {
            readWriteLock.readLock().unlock();
        }
    }


    private int[] generateSeatSelection()
    {
        int noOfSeats = ThreadLocalRandom.current().nextInt(1, 6);

        return ThreadLocalRandom.current()
                .ints(1, NO_OF_SEATS + 1)
                .distinct()
                .limit(noOfSeats)
                .toArray();
    }

    private boolean seatsAreAvailable(int[] seats)
    {
        for (int seat : seats)
        {
            if (!isAvailable[seat - 1])
            {
                System.out.println(Thread.currentThread().getName() + " Selected seats not available " + seat);

                return false;
            }
        }

        return true;
    }

    private void bookSeats(int [] seats)
    {
        System.out.print(Thread.currentThread().getName() + " Booked: ");
        for (int seat : seats)
        {
            System.out.print(seat + " ");
            isAvailable[seat - 1] = false;
        }

        System.out.println();
    }

    private void cancelSeats(int [] seats)
    {
        for(int seat: seats)
        {
            isAvailable[seat-1] = true;
        }
    }

    private void attemptBookSeats(int[] seats)
    {
        boolean seatsAvailable = false;

        readWriteLock.writeLock().lock();
        try
        {
            if (seatsAreAvailable(seats))
            {
                seatsAvailable = true;
                bookSeats(seats);
            }
        }
        catch (Exception e)
        {
            if (seatsAvailable)
            {
                cancelSeats(seats);
            }

            Thread.currentThread().interrupt();
        }
        finally
        {
            readWriteLock.writeLock().unlock();
        }
    }


    public synchronized void bookSeatsSynchronized()
    {
        try
        {
            int [] seats = generateSeatSelection();

            attemptBookSeats(seats);

        }
        catch (Exception e)
        {
            System.out.println(Thread.currentThread().getName() + " interrupted - synchronized");

            Thread.currentThread().interrupt();
        }
    }

    public void bookSeatsWithTryLock()
    {
        try
        {
            int [] seats = generateSeatSelection();

            if(cinemalock.tryLock(10,TimeUnit.MILLISECONDS))
            {
                try
                {
                    attemptBookSeats(seats);
                    Thread.sleep(100);
                }
                finally
                {
                    cinemalock.unlock();
                }
            }
            else
            {
                System.out.println(Thread.currentThread().getName() + " unable to acquire lock");
            }
        }
        catch (InterruptedException e)
        {
            System.out.println(Thread.currentThread().getName() + " interrupted - tryLock");
            Thread.currentThread().interrupt();
        }
    }

    public void bookSeatsWithLockInterruptibly()
    {

        try
        {
            int[] seats = generateSeatSelection();

            cinemalock.lockInterruptibly();
            try
            {
                attemptBookSeats(seats);
                Thread.sleep(1000000);
            }
            finally
            {
                cinemalock.unlock();
            }
        }
        catch (InterruptedException e)
        {
            System.out.println( Thread.currentThread().getName() + " interrupted - lockInterruptibly");
            Thread.currentThread().interrupt();
        }
    }


    public void bookSeatsWithSemaphore()
    {
        // if we want to allow a certain number of threads to get into this method concurrently, then semaphore is good
        // it is not re-entrant
        //like try-lock there is tryAcquire here
        try
        {
            int[] seats = generateSeatSelection();

            bookingSemaphore.acquire();

            try
            {
                attemptBookSeats(seats);
            }
            finally
            {
                bookingSemaphore.release();
            }
        }
        catch (InterruptedException e)
        {
            System.out.println("Thread interrupted - semaphore");
            Thread.currentThread().interrupt();
        }
    }





}
