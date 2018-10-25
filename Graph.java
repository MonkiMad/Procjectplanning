import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.PriorityQueue;


class Graph {

  HashMap<Integer, Vertex> graph;
  int projectTime;


  private class Vertex{
    private int id;
    String projectTitle;
    int taskTime, manpower;
    int earliestStart, latestStart, latestFinish, slack;
    LinkedList<Vertex> outEdges, inEdges;
    boolean critical = false; //by default
    boolean visited;

    Vertex(int id){
      this.id = id;
      inEdges = new LinkedList<Vertex>();
      outEdges = new LinkedList<Vertex>();
    }

    @Override
    public String toString(){
      return id + ": " + projectTitle;
    }
  }

  private class Event implements Comparable<Event>{
    int time;
    Vertex v;
    boolean starting;
    boolean finished;

    Event(int time, Vertex v, boolean starting, boolean finished){
      this.time = time;
      this.v = v;
      this.starting = starting;
      this.finished = finished;
    }

    @Override
    public int compareTo(Event event){
      return time-event.time;
    }
  }


  Graph (){
    this.graph = new HashMap<Integer, Vertex>();
  }


  public void optimalTime(){
    ArrayList<Vertex> sykel = hasCycle();
    if(sykel != null){ //prosjektet kan ikke gjennomføres
      System.out.println("Prosjektet kan ikke gjennomføres fordi grafen er syklisk.");
      System.out.println("Sykel: " + sykel);
      return;
    }
    earliestStart(); //setter tidligste starttidspunkt på alle oppgaver
    slack(); //setter slacktid og seneste starttidspunkt på alle oppgaver
    build();

    for(int i = 1; i <= graph.size(); i++){
      Vertex v = graph.get(i);
      System.out.println("\nID: " + v.id + "\nTask: " + v.projectTitle + "\nTime needed to finish the task: " + v.taskTime +
                          "\nManpower needed to complete the task: " + v.manpower + "\nEarliest starting time: " + v.earliestStart +
                          "\nLatest starting time: " + v.latestStart + "\nSlack: " + v.slack + "\n");
                          }
  }

  public void build(){
    PriorityQueue<Event> eventList = new PriorityQueue<Event>();
    Vertex v;
    Event event;
    int countTime = 0, currentStaff = 0;
    for(int i = 1; i<=graph.size(); i++){
      v = graph.get(i);
      eventList.add(new Event(v.earliestStart, v, true, false)); //første runde ting legges inn i heap settes startverdi til å være true, og finish false
    }
    while(eventList.size() != 0){
      System.out.println("Time: " + countTime);
      while(eventList.size() != 0 && countTime == eventList.peek().time){ //ser i toppen av heapen uten å fjerne elementet
        event = eventList.poll(); //fjerner elementet i heapen hvis statementet i while går gjennom

        if(event.starting == true){ //når dette statementet er sant vil vi etter printing opprette nytt event men denne gang som finished!
          currentStaff += event.v.manpower; //legger til manpower i currentStaff siden det nå kan være flere oppgaver som kjører samtidig
          System.out.println("\t\tStarting: " + event.v.id); //printer starttid til oppgave
          eventList.add(new Event(countTime + event.v.taskTime, event.v, false, true)); //andre runde man legger inn i heap settes startverdi til å være false fordi vi nå jobber med sluttiden
        }
        if(event.finished == true){
          currentStaff -= event.v.manpower; //fjerner manpower til prosjektet fra currentStaff siden vi nå er ferdig med dette eventet
          System.out.println("\t\tFinished: " + event.v.id);
        }
      }
      if(eventList.peek() != null){ //sjekker om det fortsatt er eleenter i heapen
      System.out.println("\t\tCurrent staff: " + currentStaff);
      countTime = eventList.peek().time; //oppdaterer countTime til å ha neste event sin tid
      }
      else{
        System.out.println("**** Shortest possible project execution is " + this.projectTime + "****"); //printer siste linje hvis det ikke er fler elementer i heapen
      }
    }
  }

  public void readFromFile(File fil) throws FileNotFoundException {
    Scanner in = new Scanner(fil);
    int size = Integer.parseInt(in.nextLine()); //første linje i fil gir antall tasks, altså str på grafen
    for (int i = 1; i<=size; i++){ //lager grafen med vertexr
      graph.put(i, new Vertex(i));
    }

    while(in.hasNextLine()){
      String [] line = in.nextLine().split("\\s+");
      if(line[0].isEmpty()){ //sjekker om linja er blank
        continue;
      }

      Vertex vertex = graph.get(Integer.parseInt(line[0]));
      vertex.projectTitle = line[1];
      vertex.taskTime = Integer.parseInt(line[2]);
      vertex.manpower = Integer.parseInt(line[3]);
      int i = 4;
        while(!line[i].equals("0")){
          Vertex inEdge = graph.get(Integer.parseInt(line[i++]));
          inEdge.outEdges.add(vertex); //legger til denne vertex som outEdge i vertexe vi fant
          vertex.inEdges.add(inEdge); //legger til inEdges vi fant fra filen
        }
      }
      in.close();
    }

  //inspirert av kode sett på youtube
  public ArrayList<Vertex> hasCycle(){
    ArrayList<Vertex> whiteList = new ArrayList<>(); //liste over alle vertexer som vi ikke har sjekket
    ArrayList<Vertex> greyList = new ArrayList<>(); //liste over vertexer som sjekkes
    ArrayList<Vertex> blackList = new ArrayList<>(); //liste over ferdig-sjekkede vertexer

    //vi legger alle vertexer fra grafen vi har i den hvite listen
    for(int i = 1; i < graph.size(); i++){
      whiteList.add(graph.get(i));
    }

    for(int i = 0; i < whiteList.size(); i++){
      Vertex vertex = whiteList.get(i);
      if(dybdeFørstSøk(vertex, whiteList, greyList, blackList) != null){
        return greyList;
      }
    }
    return null;
  }

  public ArrayList<Vertex> dybdeFørstSøk(Vertex current, ArrayList<Vertex> whiteList, ArrayList<Vertex> greyList, ArrayList<Vertex> blackList){
    //first move vertex from white to grey liste
    moveVertex(current, whiteList, greyList);
    for(Vertex neighbor : current.outEdges){
      if(blackList.contains(neighbor)){ //we have seen it before, and are done with it
        continue;
      }
      if(greyList.contains(neighbor)){ //we have seen it before, and we're still working on it! So it has to be a cycle! Return the list!
        return greyList;
      }
      if(dybdeFørstSøk(neighbor, whiteList, greyList, blackList) != null){ //recursive call, when it's not null we should return the list!
        return greyList;
      }
    }
    moveVertex(current, greyList, blackList); //move the vertex to blackList and are "done" with this one
    return null;
  }


  private void moveVertex(Vertex v, ArrayList<Vertex> source, ArrayList<Vertex> destination){
    source.remove(v);
    destination.add(v);
  }


  public void earliestStart(){
    for(int i = 1; i<graph.size(); i++){
      Vertex v = graph.get(i);
      if(onlyOutedge(v)){ //starter på node(r) som kun har outegdes
        v.earliestStart = 0; //setter node uten inedge til å ha earliest start lik 0.
        earliestStart(v, v.taskTime);
      }
    }
  }

  private int earliestStart(Vertex v, int time){
    int tempTime;
    int ptid = 0;
    if(v.outEdges.size() == 0){
      ptid = v.earliestStart + v.taskTime;
      return ptid;
    }
    for(Vertex out : v.outEdges){
      if(out.earliestStart == 0 || out.earliestStart < time){ //updates only when earliestStart is empty or taskTime is higher because of its dependencies
        out.earliestStart = time;
      }
      tempTime = earliestStart(out, (out.taskTime + time));
      if(tempTime > ptid){
        ptid = tempTime;
      }
    }
    return this.projectTime = ptid;
  }


  public void slack(){
    for(int i = 1; i<=graph.size(); i++){
      graph.get(i).visited = false; //setter alle til aa være false første gang
      if(onlyInedge(graph.get(i))){
        slack(graph.get(i));
      }
    }
  }

  private void slack(Vertex v){ //finn en node som ikke har outedges
    int tempSlack;
     if(onlyInedge(v)){
       v.slack = this.projectTime - (v.earliestStart + v.taskTime);
       v.latestStart = v.earliestStart + v.slack;
       if(v.slack == 0){
         v.critical = true;
       }
     }

    for(Vertex inedge : v.inEdges){
      tempSlack = v.earliestStart-(inedge.earliestStart + inedge.taskTime);
      if(inedge.visited && inedge.slack == 0){
        continue;
      }
      if(inedge.slack == 0 || inedge.slack > tempSlack){
        inedge.visited = true;
        inedge.slack = tempSlack;
        inedge.latestStart = inedge.earliestStart + inedge.slack;
        inedge.latestFinish = inedge.latestStart + inedge.taskTime;
        if(inedge.slack == 0){ //oppdaterer critical
          inedge.critical = true;
        }
      }
      slack(inedge);
      }
  }


  public boolean isNotConnected(Vertex v){ //checkes if there are vertexes without connection
    if(v.inEdges.size() == 0 && v.outEdges.size() == 0){
      return true;
    }
    return false;
  }

  public boolean onlyInedge(Vertex v){ //vurder å putte alle noder som kun har en inedge i egen liste siden vi flere ganger går gjennom hele grafen
    return v.outEdges.isEmpty();
  }

  public boolean onlyOutedge(Vertex v){ //checkes if there are vertexes without inEdges
      return v.inEdges.isEmpty();
    }


}
