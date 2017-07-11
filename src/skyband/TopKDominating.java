package skyband;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


public class TopKDominating {

	public static boolean isDebug = false;
	private static void debug(String text) {
		if (isDebug) {
			System.out.println("DEBUG: " + text);
		}
	}
	
	public static List<Tuple> computeTopKDominating(List<Tuple> group,
			List<Comparison> comparisons, int k) throws Exception {
		List<Tuple> results = new ArrayList<Tuple>();
		List<Tuple> currentGroup = new ArrayList<Tuple>(group);
		
		for (int i = 0; i < k; i++) {
			Tuple[] skylines = BNL.computeSkyline(currentGroup, comparisons);
			debug("skyline tuples: "
					+ Arrays.asList(skylines));
			
			
			for (Tuple skylineTuple : skylines) {
				skylineTuple.dominatingCount = 0;
				for (Tuple currentTuple : currentGroup) {
					if(skylineTuple.dominate(currentTuple, comparisons) == 1)
						skylineTuple.dominatingCount++;
				}
				
			}
			
			Tuple maxTuple = skylines[0];
			for (int j = 1; j < skylines.length; j++) {
				if (skylines[j].dominatingCount > maxTuple.dominatingCount)
				{
					maxTuple = skylines[j];
				}	
			}
			debug("Dominating [" + maxTuple.dominatingCount + "]: " + maxTuple);
			
			if (!currentGroup.remove(maxTuple)){
				throw new Exception("Cannot remove " + maxTuple);
			}
			
			results.add(maxTuple);
		}
		
		return results;		
	}

	
}
