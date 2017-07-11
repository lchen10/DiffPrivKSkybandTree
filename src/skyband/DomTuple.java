package skyband;

public class DomTuple {
	
	private Tuple tuple;
	private int dominanceCount;
	
	public DomTuple(Tuple t, int d)
	{
		tuple = t;
		dominanceCount = d;
	}
	
	public Tuple getTuple()
	{
		return tuple;
	}

	public double getDist()
	{
		return dominanceCount;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
        StringBuffer sb = new StringBuffer();
        sb.append("[" + dominanceCount + "]: ");
        sb.append(tuple.toString());
       
        return sb.toString();
	}

	
}
