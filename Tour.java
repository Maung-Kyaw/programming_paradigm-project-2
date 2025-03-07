/*Trinnaya Damrongpatharawat 6581147
Jinjutha Yolsirivat 6581053
Kyaw Zin Thant 6581178
Chartwut Piriyapanyaporn 6481227
Phurilap Kitlertpaisan 6680251*/

package Project2_6581147;

import java.io.*;
import java.util.*;
import java.util.concurrent.CyclicBarrier;

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
                System.out.println(Thread.currentThread().getName() + " >> new arrival = " + arrivals + " remaining customers = " + remainingCustomers);

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
                            System.out.println(Thread.currentThread().getName() + " >> send " + send + " customers to " + tour.getName() + " seats taken = " + tour.getCurrentCustomers());
                        }
                    }
                }

                barrier.await();
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
                
                barrier.await();

                Place place = places.get(random.nextInt(places.size()));
                synchronized(place){
                    int customers = tour.takeCustomers();
                    if (customers > 0) {
                        place.addVisitors(customers);
                        System.out.println(Thread.currentThread().getName() + " >> take " + customers + " customers to " + place.getName() + " visitor count = " + place.getVisitorCount());
                    } else {
                        System.out.println(Thread.currentThread().getName() + " >> no customer");
                    }
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

                System.out.printf("%s >> Days of simulation = %d\n", Thread.currentThread().getName(), days);
                System.out.printf("%s >> Max arrival = %d\n", Thread.currentThread().getName(), maxArrival);
                
                List<Tour> tours = new ArrayList<>();
                for (int i = 0; i < tourCount; i++) {
                    tours.add(new Tour("Tour_" + i, tourCapacity));
                }

                List<Place> places = new ArrayList<>();
                for (int i = 0; i < placeCount; i++) {
                    places.add(new Place("Place_" + i));
                }

                DayCounter dayCounter = new DayCounter();
                CyclicBarrier arrivalBarrier = new CyclicBarrier(agencyCount);
                CyclicBarrier sendBarrier = new CyclicBarrier(agencyCount + tourCount);

                List<AgencyThread> agencies = new ArrayList<>();
                for (int i = 0; i < agencyCount; i++) {
                    AgencyThread agency = new AgencyThread("AgencyThread_" + i, tours, maxArrival, dayCounter, arrivalBarrier, sendBarrier);
                    agencies.add(agency);
                    agency.start();
                }
                System.out.printf("%s >> AgencyThread = [",Thread.currentThread().getName());
                for (AgencyThread agency : agencies) {
                    System.out.printf(" %s ", agency.getName());
                }
                System.out.printf("]\n");
                
                System.out.printf("%s >> Tour capacity = %d\n", Thread.currentThread().getName(), tourCapacity);
                System.out.printf("%s >> OperatorThread = [",Thread.currentThread().getName());

                List<OperatorThread> operators = new ArrayList<>();
                for (Tour tour : tours) {
                    OperatorThread operator = new OperatorThread(tour, places, dayCounter, sendBarrier);
                    operators.add(operator);
                    operator.start();
                    System.out.printf(" %s ", tour.getName());
                }
                System.out.printf("]\n");
                
                System.out.printf("%s >> Places = [",Thread.currentThread().getName());
                for (Place place : places) {
                    System.out.printf(" %s ",place.getName());
                }
                System.out.printf("]\n");

                for (int day = 1; day <= days; day++) {
                    System.out.println(Thread.currentThread().getName() + " >> Day " + day);
                    dayCounter.incrementDay(); 
                    Thread.sleep(100); 
                }

                for (AgencyThread agency : agencies) {
                    agency.join();
                }
                for (OperatorThread operator : operators) {
                    operator.join();
                }

                System.out.println(Thread.currentThread().getName() + " >> Summary");
                tours.sort(Comparator.comparing(Tour::getTotalCustomers).reversed().thenComparing(Tour::getName));
                for (Tour tour : tours) {
                    System.out.println(Thread.currentThread().getName() + " >> " + tour.getName() + "   total customers= " + tour.getTotalCustomers());
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
