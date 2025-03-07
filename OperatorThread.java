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
