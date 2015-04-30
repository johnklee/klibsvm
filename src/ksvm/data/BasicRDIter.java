package ksvm.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

public class BasicRDIter implements IRecordIter{
	private BufferedReader 	br = null;
	private String 			nextProcline = null;	

	public BasicRDIter(File trainFile)throws IOException{this(new BufferedReader(new FileReader(trainFile)));}
	public BasicRDIter(BufferedReader br){this.br = br; retriveProcline();}
	
	protected void retriveProcline()
	{
		try
		{
			do
			{
				nextProcline = br.readLine();
				if(nextProcline!=null && 
				   (nextProcline.isEmpty() || nextProcline.startsWith("#"))) continue;
				else break;
			}while(nextProcline!=null);
			if(nextProcline==null) br.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
			nextProcline = null;
		}
	}
	
	@Override
	public boolean hasNext() {
		return (nextProcline!=null);
	}

	@Override
	public Record next() {
		if(nextProcline!=null)
		{
			//System.out.printf("\t[Test] line=%s\n", nextProcline);
			StringTokenizer st = new StringTokenizer(nextProcline," \t\n\r\f:");
			Record rd = new Record(st);
			retriveProcline();
			return rd;
		}
		return null;
	}

	@Override
	public void remove() {
		throw new java.lang.UnsupportedOperationException("Not support");		
	}
}
