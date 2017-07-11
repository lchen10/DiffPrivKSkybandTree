package skyband;

public class DistTuple {
	
	private Tuple tuple;
	private double distance;
	
	public DistTuple(Tuple t, double d)
	{
		tuple = t;
		distance = d;
	}
	
	public Tuple getTuple()
	{
		return tuple;
	}

	public double getDist()
	{
		return distance;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
        StringBuffer sb = new StringBuffer();
        sb.append("[" + distance + "]: ");
        sb.append(tuple.toString());
       
        return sb.toString();
	}

	
}
