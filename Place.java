/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package project2;

/**
 *
 * @author kyawz
 */
public class Place {
    private int placeID;
    private int visitorCount;
    
    public Place(int placeID){
        this.placeID=placeID;
        this.visitorCount=0;
    }
    public synchronized void addVisitors(int num){
        visitorCount += num;
        System.out.println(Thread.currentThread().getName() + "added " + num + " visitors to Place" + placeID);
    }
    
    public int getVisitorCount(){
        return visitorCount;
    }
    @Override
    public String toString(){
        return "Place" + placeID + "has" + visitorCount + "total visitors. " ;
    }
}
