import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringReader;

import javax.swing.JPanel;
import com.jme3.scene.Spatial;
public class Compone {
	
	Spatial threed;
	String filepath;
	String filename;
	Loc3f displacement;
	double scalex;
	double scaley;
	double scalez;
	double rotateangle;
	int code;
	JPanel controls;
	String fullpic;
	int coloured;
	float tiling;
	String matfile;
	
	public String getpic(){
		return fullpic;
	}
	
	public String path(){
		return filepath+"/"+filename;
	}
	
	public String getmat(){
		return matfile;
	}
	
	public String getdir(){
		return filepath;
	}
	
	Compone(Spatial td, int id, JPanel controller, String material, String path, String fname){
		code = id;
		threed = td;
		fullpic = null;
		filepath = path;
		filename = fname;
		displacement = new Loc3f(0,0,0);
		rotateangle = 0;
		matfile = material;
		coloured = 0;
		tiling = 1.0f;
		scalex = 1.0;
		scaley = 1.0;
		scalez = 1.0;
		controls = controller;
	}
	
	public void print(){
		System.out.println(displacement.x() + " , " + displacement.y() + " , " +displacement.z());
		System.out.println(rotateangle);
		System.out.println(scalex);
	}
	
	public void colour(float red, float green, float blue){
		coloured = 1;
		StringBuffer output = new StringBuffer();
		String[] lines = matfile.split(System.getProperty("line.separator"));
		for(int i=0;i<lines.length;i++){
			if(lines[i].substring(0,2).equals("Kd")){
				output.append("Kd " + red + " " + green + " " + blue).append(System.getProperty("line.separator"));
			}else{
				if(!lines[i].contains("map_Kd")){
					output.append(lines[i]).append(System.getProperty("line.separator"));
				}
			}
		}
		matfile = output.toString();		
	}
	
	public void texture(String path, String fname){
		coloured = 0;
		String full = path + "/" +  fname;
		fullpic = full;
		StringBuffer output = new StringBuffer();
		String[] lines = matfile.split(System.getProperty("line.separator"));
		int found = 0;
		for(int i=0;i<lines.length;i++){
				if(lines[i].contains("map_Kd")){
					found = 1;
					output.append("map_Kd " + fname).append(System.getProperty("line.separator"));
				}else{
					output.append(lines[i]).append(System.getProperty("line.separator"));
				}
		}
		if(found==0){output.append("map_Kd " + fname).append(System.getProperty("line.separator"));}
		matfile = output.toString();
	}
	
	public void appendscalex(double num){
		scalex += num;
		if(scalex<0.0){scalex=0.0;}
		return;
	}
	
	public void appendscaley(double num){
		scaley += num;
		if(scaley<0.0){scaley=0.0;}
		return;
	}
	public void appendscalez(double num){
		scalez += num;
		if(scalez<0.0){scalez=0.0;}
		return;
	}
	
	public int getcol(){
		return coloured;
	}
	
	public void appendtile(float num){
		tiling += num;
		return;
	}
	
	public void appendx(float val){
		displacement = new Loc3f(displacement.x()+val,displacement.y(),displacement.z());
		return;
	}
	public void appendy(float val){
		displacement = new Loc3f(displacement.x(),displacement.y()+val,displacement.z());
		return;
	}
	public void appendz(float val){
		displacement = new Loc3f(displacement.x(),displacement.y(),displacement.z()+val);
		return;
	}
	
	public void appendrot(double val){
		rotateangle += val;
		return;
	}
	
	public double getrot(){
		return rotateangle;
	}
	
	public double getscalex(){
		return scalex;
	}
	public double getscaley(){
		return scaley;
	}
	public double getscalez(){
		return scalez;
	}
	
	public Loc3f getdis(){
		return displacement;
	}
	
	public int getid(){
		return code;
	}
	
	public JPanel getcon(){
		return controls;
	}
	
	public float gettile(){
		return tiling;
	}
	
	public Spatial getsp(){
		return threed;
	}
}