import java.util.ArrayList;

public class Domain {
	public ArrayList<Double> dimMIN = new ArrayList<>();
	public ArrayList<Double> dimMAX = new ArrayList<>();
	
	public Domain(int dim){
		for (int i = 0; i < dim; i++) {
			dimMIN.add(0.0);
			dimMAX.add(0.0);
		}
	}

	@Override
	public String toString() {
		return "Domain [dimMIN=" + dimMIN + ", dimMAX=" + dimMAX + "]";
	}
	
	
}
