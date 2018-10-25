import java.io.File;
import java.io.FileNotFoundException;

class Oblig2{


public static void main(String[] args) {
  Graph test = new Graph();

  String filnavn;
  filnavn = args[0];
  File fil = new File(filnavn);

  try{
    test.readFromFile(fil);
  }
  catch(FileNotFoundException e){
    System.out.println("Finner ikke filen.");
    System.exit(1);
    }
  test.optimalTime();
  }
}
