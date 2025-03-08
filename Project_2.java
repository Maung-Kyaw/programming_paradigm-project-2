/*Trinnaya Damrongpatharawat 6581147
Jinjutha Yolsirivat 6581053
Kyaw Zin Thant 6581178
Chartwut Piriyapanyaporn 6481227
Phurilap Kitlertpaisan 6680251*/

package Project2_6581147;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

class Place {
    private final String name;
    private int visitorCount= 0;

    public Place(String name) {
        this.name = name;
    }

    public synchronized void addVisitors(int count) {
        visitorCount+= count;
    }

    public String getName() { return name; }
    public int getVisitorCount() { return visitorCount; }
}

class Tour {
    private final String name;
    private final int capacity;
    private int currentCustomers = 0;
    private int totalCustomers = 0;

    public Tour(String name, int capacity) {
        this.name = name;
        this.capacity = capacity;
    }

    public int getCapacity() { return capacity; }

    public synchronized boolean addCustomers(int count) {
        if (currentCustomers + count <= capacity) {
            currentCustomers += count;
            totalCustomers += count;
            return true;
        }
        return false;
    }

    public synchronized int takeCustomers() {
        int customers = currentCustomers;
        currentCustomers = 0;
        return customers;
    }

    public int getTotalCustomers() { return totalCustomers; }
    public int getCurrentCustomers() {return currentCustomers;}
    public String getName() { return name; }
}

class DayCounter {
    private int day = 0;

    public synchronized void incrementDay() {
        day++;
        notifyAll();
    }

    public synchronized void waitForDay(int currentDay) throws InterruptedException {
        while (day < currentDay) {
            wait();
        }
    }

    public int getCurrentDay() {
        return day;
    }
}

class AgencyThread extends Thread {
    private final List<Tour> tours;
    private final int maxDailyArrival;
    private int remainingCustomers = 0;
    private Random random = new Random(); 
    private DayCounter dayCounter;
    private CyclicBarrier arrivalBarrier; 
    private CyclicBarrier barrier;

    public AgencyThread(String name, List<Tour> tours, int maxDailyArrival, DayCounter dayCounter, CyclicBarrier arrivalBarrier, CyclicBarrier barrier) {
        super(name);
        this.tours = tours;
        this.maxDailyArrival = maxDailyArrival;
        this.dayCounter = dayCounter;
        this.arrivalBarrier = arrivalBarrier;
        this.barrier = barrier;
    }

    public void run() {
        for (int day = 1; day <= Project2_6581147.days; day++) {
            try {
                dayCounter.waitForDay(day); 

                int arrivals = random.nextInt(maxDailyArrival + 1);
                remainingCustomers += arrivals;
                System.out.printf("%22s >> new arrival = %2d %39s = %3d\n", Thread.currentThread().getName(), 
                        arrivals, "remaining customers", remainingCustomers);
                
                arrivalBarrier.await();

                if (remainingCustomers > 0) {
                    Tour tour = tours.get(random.nextInt(tours.size()));
                    synchronized(tour){
                        int send;
                        if((remainingCustomers+tour.getCurrentCustomers())>tour.getCapacity()){
                            send= tour.getCapacity()-tour.getCurrentCustomers();
                        }else{
                            send= remainingCustomers;
                        }
                        if (tour.addCustomers(send)) {
                            remainingCustomers -= send;
                            System.out.printf("%22s >> send %2d customers to %-15s seats taken = %2d\n", Thread.currentThread().getName(), send, tour.getName(), tour.getCurrentCustomers());
                        }
                        
                    }
                }

                
                try{
                    barrier.await();
                }
                catch(InterruptedException | BrokenBarrierException e ){
                    System.err.println(e.getClass().getName());
                }
            } catch (Exception e) {
                System.err.println(e.getClass().getName());
            }
        }
    }
}

class OperatorThread extends Thread {
    private final Tour tour;
    private final List<Place> places;
    private Random random = new Random();
    private DayCounter dayCounter;
    private CyclicBarrier barrier; 

    public OperatorThread(Tour tour, List<Place> places, DayCounter dayCounter, CyclicBarrier barrier) {
        super("OperatorThread_" + tour.getName());
        this.tour = tour;
        this.places = places;
        this.dayCounter = dayCounter;
        this.barrier = barrier;
    }

    public void run() {
        for (int day = 1; day <= Project2_6581147.days; day++) {
            try {
                dayCounter.waitForDay(day);
                
                try{
                    barrier.await();
                }
                catch(InterruptedException | BrokenBarrierException e ){
                    System.err.println(e.getClass().getName());
                }

                Place place = places.get(random.nextInt(places.size()));
                synchronized(place){
                    int customers = tour.takeCustomers();
                    if (customers > 0) {
                        place.addVisitors(customers);
                        System.out.printf("%22s >> take %2d customers to %-15s visitor count = %3d\n", 
                                Thread.currentThread().getName(), 
                                customers, place.getName(), 
                                place.getVisitorCount());
                    } else {
                        System.out.printf("%22s >> no customer\n", Thread.currentThread().getName());
                    }
                    //System.out.printf("%22s >>\n",Thread.currentThread().getName());
                }
                
            } catch (Exception e) {
                System.err.println(e.getClass().getName());
            }
        }
    }
}

public class Project2_6581147 {
    public static int days = 0;
    
    public static void main(String[] args) {
        Scanner input= new Scanner(System.in);
        String fileName="config_1.txt";
        Scanner scan=null;
        
        while(scan==null){
            try {
                scan = new Scanner(new File("src/main/java/Project2_6581147/"+fileName));

                int agencyCount = 0, maxArrival = 0, tourCount = 0, tourCapacity = 0, placeCount = 0;

                while (scan.hasNextLine()) {
                    String line = scan.nextLine();
                    String[] cols = line.split(",");

                    switch (cols[0].trim()) {
                        case "days":
                            days = Integer.parseInt(cols[1].trim());
                            break;
                        case "agency_num_arrival":
                            agencyCount = Integer.parseInt(cols[1].trim());
                            maxArrival = Integer.parseInt(cols[2].trim());
                            break;
                        case "tour_num_capacity":
                            tourCount = Integer.parseInt(cols[1].trim());
                            tourCapacity = Integer.parseInt(cols[2].trim());
                            break;
                        case "place_num":
                            placeCount = Integer.parseInt(cols[1].trim());
                            break;
                    }
                }

                System.out.printf("%22s >> ================ Parameters ================\n\n",Thread.currentThread().getName());
                System.out.printf("%22s >> %-18s = %d\n", Thread.currentThread().getName() ,"Days of simulation", days);
                System.out.printf("%22s >> %-18s = %d\n", Thread.currentThread().getName(), "Max arrival", maxArrival);
                
                ArrayList<Tour> tours = new ArrayList<>();
                for (int i = 0; i < tourCount; i++) {
                    tours.add(new Tour("Tour_" + i, tourCapacity));
                }

                ArrayList<Place> places = new ArrayList<>();
                for (int i = 0; i < placeCount; i++) {
                    places.add(new Place("Place_" + i));
                }

                DayCounter dayCounter = new DayCounter();
                CyclicBarrier arrivalBarrier = new CyclicBarrier(agencyCount);
                CyclicBarrier sendBarrier = new CyclicBarrier(agencyCount + tourCount);

                ArrayList<AgencyThread> agencies = new ArrayList<>();
                for (int i = 0; i < agencyCount; i++) {
                    AgencyThread agency = new AgencyThread("AgencyThread_" + i, tours, maxArrival, dayCounter, arrivalBarrier, sendBarrier);
                    agencies.add(agency);
                    agency.start();
                }
                System.out.printf("%22s >> %-18s = [",Thread.currentThread().getName(), "AgencyThread");
                for (AgencyThread agency : agencies) {
                    System.out.printf(" %s ", agency.getName());
                }
                System.out.printf("]\n");
                
                System.out.printf("%22s >> %-18s = %d\n", Thread.currentThread().getName(), "Tour capacity", tourCapacity);
                System.out.printf("%22s >> %-18s = [",Thread.currentThread().getName(), "OperatorThread");

                ArrayList<OperatorThread> operators = new ArrayList<>();
                for (Tour tour : tours) {
                    OperatorThread operator = new OperatorThread(tour, places, dayCounter, sendBarrier);
                    operators.add(operator);
                    operator.start();
                    System.out.printf(" %s ", tour.getName());
                }
                System.out.printf("]\n");
                
                System.out.printf("%22s >> %-18s = [",Thread.currentThread().getName(), "Places");
                for (Place place : places) {
                    System.out.printf(" %s ",place.getName());
                }
                System.out.printf("]\n");

                for (int day = 1; day <= days; day++) {
                    System.out.println();
                    System.out.printf("%22s >> ============================================\n",Thread.currentThread().getName());
                    System.out.printf("%22s >> Day %d\n",Thread.currentThread().getName(),day);
                    System.out.println();
                    dayCounter.incrementDay(); 
                    Thread.sleep(100); 
                }

                for (AgencyThread agency : agencies) {
                    agency.join();
                }
                for (OperatorThread operator : operators) {
                    operator.join();
                }
                System.out.println();
                System.out.printf("%22s >> ============================================\n",Thread.currentThread().getName());
                System.out.println();
                
                System.out.printf("%22s >> Summary\n",Thread.currentThread().getName());
                tours.sort(Comparator.comparing(Tour::getTotalCustomers).reversed().thenComparing(Tour::getName));
                for (Tour tour : tours) {
                    System.out.printf("%22s >> %-11s total customers= %3d\n", Thread.currentThread().getName(), 
                            tour.getName(), tour.getTotalCustomers());
                }
            
                scan.close();
                input.close();

            } catch(FileNotFoundException e){
                System.err.println(e);
                System.out.println("New file name:");
                fileName=input.next();
            } catch (IOException | InterruptedException e) {
                System.err.println(e.getClass().getName());
            }
        
        }

    }
}
