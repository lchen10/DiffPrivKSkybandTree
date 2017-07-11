import static org.junit.Assert.*;

import java.util.HashSet;

import org.junit.Test;

import skyband.Tuple;

public class TupleTest {
	@Test
	public void testHashSet() throws Exception {
		Tuple t = new Tuple(new int[] {5,6});
		HashSet<Tuple> set = new HashSet<>();
		set.add(t);
		assertTrue(set.contains(new Tuple(new int[]{5,6})));
		assertFalse(set.contains(new Tuple(new int[]{4,6})));
		
		set.add(new Tuple(new int[]{5,6}));
		assertEquals(1, set.size());
		set.add(new Tuple(new int[]{4,6}));
		assertEquals(2, set.size());

	}
}
