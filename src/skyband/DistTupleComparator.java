package skyband;
import java.util.Comparator;

public class DistTupleComparator implements Comparator<DistTuple> {
	@Override
	public int compare(DistTuple x, DistTuple y) {
		// TODO Auto-generated method stub

		return Double.compare(x.getDist(), y.getDist());
	}
}
