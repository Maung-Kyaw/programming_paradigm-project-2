/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package project2;

/**
 *
 * @author kyawz
 */
public class Tour {
    private int tourID;
    private int capacity;
    private int currentCustomers;
    private int totalCustomers;

    public Tour(int tourID, int capacity){
        this.tourID = tourID;
        this.capacity = capacity;
        this.currentCustomers = 0;
        this.totalCustomers = 0;
    }

    public int getTourID(){
        return tourID;
    }

    public int getTotalCustomers(){
        return totalCustomers;
    }

    public synchronized boolean hasSpace(){
        return currentCustomers < capacity;
    }

    public synchronized addCustomers(int customers){
        int availableSeats = capacity - currentCustomers;
        int accepted;

        if(customers < availableSeats){
            accepted = customers;
        } else{
            accepted = availableSeats;
        }

        currentCustomers += accepted;
        totalCustomers += accepted;
        return accepted;
    }

    public synchronized takeCustomers(){
        int leaving = currentCustomers;
        currentCustomers = 0;
        return leaving;
    }

    @Override
    public String toString(){
        return "Tour " + tourID + "has " + totalCustomers + " total customers";
    }
}
