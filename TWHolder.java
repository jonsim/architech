import java.awt.*;
import javax.swing.*;

public class TWHolder extends JPanel {
  Tweaker preview;
  Main main;
  public TWHolder(Main main) {
	super();
	this.main = main;
    preview = new Tweaker(main);
    add(preview);
  }  
  public void removetw(){
	  remove(preview);
  }
}