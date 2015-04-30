package ksvm.data;

import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.StringTokenizer;

public class Record {
	public double 				label = -1;
	public PriorityQueue<TData> features = null;
	
	public Record(){features=new PriorityQueue<TData>();}
	public Record(StringTokenizer st)
	{
		this();
		label = Double.valueOf(st.nextToken());
		if(st.countTokens()%2==0)
		{
			int m = st.countTokens()/2;
			for(int i=0; i<m; i++)
			{
				addFeature(st.nextToken(), st.nextToken());
			}
		}
		else
		{
			System.out.printf("\t[Error] Illegal data input!\n");
		}
	}
	
	@Override
	public String toString()
	{
		StringBuffer strBuf = new StringBuffer();
		Iterator<TData> iter = features.iterator();
		while(iter.hasNext()) strBuf.append(String.format("%s ", iter.next()));
		return strBuf.toString();
	}
	
	public void addFeature(String idx, String val){this.addFeature(Integer.valueOf(idx), Double.valueOf(val));}
	public void addFeature(int idx, double val)
	{
		features.add(new TData(idx, val));
	}
	
	public static void main(String args[])
	{
		Record record = new Record();
		record.addFeature(1, -10);
		record.addFeature(2, 48);
		record.label=2;
	}
}
