package ksvm.data;

import java.util.PriorityQueue;

public class TData implements Comparable<TData>{
	public int 		index=-1;
	public double 	value=-1;
	
	public TData(){}
	public TData(int index, double val){this.index = index; this.value = val;}

	@Override
	public int compareTo(TData o) {
		if(index>o.index) return 1;
		else if(index<o.index) return -1;
		return 0;
	}
	
	@Override
	public String toString(){return String.format("%d:%.01f", index, value);}

	public static void main(String args[])
	{
		PriorityQueue<TData> pq = new PriorityQueue<TData>();
		pq.add(new TData(1, 2));
		pq.add(new TData(3, 1));
		pq.add(new TData(2, 1));
		while(!pq.isEmpty())
		{
			System.out.printf("\t[Info] Data->%s\n", pq.poll());
		}
	}
}
