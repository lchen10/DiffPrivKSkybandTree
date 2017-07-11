package kSpacePartition;

import static org.junit.Assert.*;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import skyband.Comparison;
import skyband.Tuple;

public class NodeTest {
	@Test
	public void testNodeSplit() throws Exception {
		Node n = new Node(0, 0, 10, 10, Comparison.MIN);
		n.split(5, 5);
		Node sw = n.childMap.get("sw");
		assertEquals(0.0, sw.xmin,0.0001);
		assertEquals(0.0, sw.ymin,0.0001);
		assertEquals(5.0, sw.xmax,0.0001);
		assertEquals(5.0, sw.ymax,0.0001);
		
		Node se = n.childMap.get("se");
		assertEquals(5.0, se.xmin,0.0001);
		assertEquals(0.0, se.ymin,0.0001);
		assertEquals(10.0, se.xmax,0.0001);
		assertEquals(5.0, se.ymax,0.0001);
		
		Node nw = n.childMap.get("nw");
		assertEquals(0.0, nw.xmin,0.0001);
		assertEquals(5.0, nw.ymin,0.0001);
		assertEquals(5.0, nw.xmax,0.0001);
		assertEquals(10.0, nw.ymax,0.0001);
		
		Node ne = n.childMap.get("ne");
		assertEquals(5.0, ne.xmin,0.0001);
		assertEquals(5.0, ne.ymin,0.0001);
		assertEquals(10.0, ne.xmax,0.0001);
		assertEquals(10.0, ne.ymax,0.0001);
	}
	
	@Test
	public void testSplit() throws Exception {
		List<String> lines = Files.readAllLines(Paths.get("data/test.dat"), Charset.defaultCharset());
		List<IDTuple> tuples = new ArrayList<IDTuple>();
		for (int i = 0; i < lines.size(); i++) {
			String[] line = lines.get(i).split("\\t");
			double x = Double.parseDouble(line[0]);
			double y = Double.parseDouble(line[1]);
			tuples.add(new IDTuple(new Double[] { x, y }));
		}
		
		Node n = new Node(0, 0, 10, 10, Comparison.MIN);
		n.loadTuples(tuples);
		
		n.split(5, 5);
		Node sw = n.childMap.get("sw");
		System.out.println("SW:");
		System.out.println(sw);
		
		Node se = n.childMap.get("se");
		System.out.println("SE:");
		System.out.println(se);
		
		Node nw = n.childMap.get("nw");
		System.out.println("NW:");
		System.out.println(nw);
		
		Node ne = n.childMap.get("ne");
		System.out.println("NE:");
		System.out.println(ne);
	}
}
