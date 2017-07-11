package skyband;

public class DomCountDiffTuple {
	
	private Tuple tuple;
	private int domCountDiff;
	
	public DomCountDiffTuple(Tuple t, int d)
	{
		tuple = t;
		domCountDiff = d;
	}
	
	public Tuple getTuple()
	{
		return tuple;
	}

	public int getDomCountDiff()
	{
		return domCountDiff;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
        StringBuffer sb = new StringBuffer();
        sb.append("[" + domCountDiff + "]: ");
        sb.append(tuple.toString());
       
        return sb.toString();
	}

	
}
