package kSpacePartition;

import skyband.Tuple;

public class IDTuple extends Tuple {
	public int id;

	public IDTuple(Double[] doubles) {
		super(doubles);
	}

	public IDTuple(Double[] doubles, int id) {
		this(doubles);
		this.id = id;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IDTuple other = (IDTuple) obj;
		if (other.id == id) {
			return true;
		}
		return false;
	}

}
