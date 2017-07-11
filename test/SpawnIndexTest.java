import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;


public class SpawnIndexTest {

	int dim = 4;
	
	@Test
	public void testSpawnIndexes() throws Exception {
		Integer[] indexes = new Integer[dim];
		ArrayList<Integer[]> r = new ArrayList<Integer[]>();
		spawnIndexes(indexes, 0, r);
		
		for (Integer[] integers : r) {
			System.out.print("[");
			for (int i = 0; i < integers.length; i++) {
				System.out.print(integers[i] + ",");
			}
			System.out.println("]");
		}
	}
	
	public void spawnIndexes(Integer[] context, int d, ArrayList<Integer[]> r) {
		if (d < dim) {
			Integer[] newcontext = Arrays.copyOf(context, context.length);
			Integer[] newcontext2 = Arrays.copyOf(context, context.length);
			newcontext[d] = 0;
			newcontext2[d] = 1;
			spawnIndexes(newcontext, d + 1, r);
			spawnIndexes(newcontext2, d + 1, r);
		} else {
			r.add(context);
		}
	}
}
